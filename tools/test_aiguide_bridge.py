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


if __name__=="__main__":
    unittest.main()
