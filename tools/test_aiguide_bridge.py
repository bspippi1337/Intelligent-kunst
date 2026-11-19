import importlib.util
from pathlib import Path
import unittest

module_path=Path(__file__).with_name("aiguide_generate.py")
spec=importlib.util.spec_from_file_location("aiguide_generate",module_path)
adapter=importlib.util.module_from_spec(spec)
spec.loader.exec_module(adapter)


class AiguideBridgeContractTest(unittest.TestCase):
    def scene(self):
        return {
            "version": 1,
            "title": "A different impossible object",
            "intent": "A composition that visibly contradicts its own construction.",
            "background": "#f0e7d3",
            "elements": [
                {"kind": "ellipse", "color": "#203040", "x": .15+i*.04, "y": .2,
                 "w": .14, "h": .24} for i in range(12)
            ],
        }

    def test_valid_scene_round_trip(self):
        import json
        text=adapter.validate(self.scene())
        self.assertEqual(json.loads(text)["elements"][0]["kind"],"ellipse")

    def test_reject_executable_or_unknown_commands(self):
        invalid=self.scene()
        invalid["elements"][0]["kind"]="script"
        with self.assertRaises(ValueError):
            adapter.validate(invalid)

    def test_reject_oversized_commands(self):
        invalid=self.scene()
        invalid["elements"]=invalid["elements"]*9
        with self.assertRaises(ValueError):
            adapter.validate(invalid)

    def test_validate_without_importing_openai_sdk(self):
        self.assertTrue(adapter.validate(self.scene()).startswith("{"))

    def test_expressive_closed_path_with_gradient(self):
        import json
        scene = self.scene()
        scene["elements"][0] = {
            "kind": "path", "color": "#304050", "alpha": 0.8,
            "gradient": {
                "type": "linear", "start": "#120f25", "end": "#d7a071",
                "x1": .1, "y1": .2, "x2": .8, "y2": .85
            },
            "commands": [
                {"op": "M", "x": .1, "y": .2},
                {"op": "C", "cx1": .8, "cy1": .05, "cx2": .9, "cy2": .8,
                 "x": .65, "y": .92},
                {"op": "Q", "cx": .25, "cy": .8, "x": .1, "y": .2},
                {"op": "Z"},
            ],
        }
        normalized = json.loads(adapter.validate(scene))
        path = normalized["elements"][0]
        self.assertEqual("path", path["kind"])
        self.assertEqual("C", path["commands"][1]["op"])
        self.assertEqual("#120F25", path["gradient"]["start"])
        self.assertEqual("fill", path["mode"])

    def test_reject_open_filled_path_and_unbounded_bezier(self):
        scene = self.scene()
        scene["elements"][0] = {
            "kind": "path", "color": "#222222",
            "commands": [
                {"op": "M", "x": .1, "y": .2},
                {"op": "L", "x": .5, "y": .1},
                {"op": "L", "x": .8, "y": .9},
            ]
        }
        with self.assertRaises(ValueError):
            adapter.validate(scene)
        scene["elements"][0]["commands"].append({"op": "Z"})
        scene["elements"][0]["commands"][1]["x"] = 1000
        with self.assertRaises(ValueError):
            adapter.validate(scene)

    def test_reject_degenerate_gradient(self):
        scene = self.scene()
        scene["elements"][0]["gradient"] = {
            "type": "linear", "start": "#112233", "end": "#eeddcc",
            "x1": .3, "y1": .4, "x2": .3, "y2": .4
        }
        with self.assertRaises(ValueError):
            adapter.validate(scene)

    def test_radial_gradient_and_stroked_paths(self):
        import json
        scene = self.scene()
        scene["elements"][0] = {
            "kind": "path", "mode": "stroke", "color": "#324751",
            "width": .006, "gradient": {
                "type": "radial", "start": "#443322", "end": "#bababa",
                "cx": .5, "cy": .5, "r": .45
            },
            "commands": [
                {"op": "M", "x": .1, "y": .1},
                {"op": "Q", "cx": .5, "cy": .3, "x": .3, "y": .5},
                {"op": "L", "x": .6, "y": .8},
            ],
        }
        normalized = json.loads(adapter.validate(scene))
        self.assertEqual("stroke", normalized["elements"][0]["mode"])
        self.assertEqual(.45, normalized["elements"][0]["gradient"]["r"])


if __name__=="__main__":
    unittest.main()
