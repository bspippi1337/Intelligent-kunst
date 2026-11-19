#!/usr/bin/env python3
"""Direct adapter for the ORIGINAL bspippi1337/aiguide-dev Python package.

Output is the exact JSON scene format imported by the native Android app.
The package itself handles model calls, its original prompt, and JSONL logging.
"""
import argparse
import json
import subprocess
import sys


def validate(data):
    """Normalize to exactly the native Android scene contract, no executable content."""
    import math
    import re

    def number(value, low=-.25, high=1.25):
        if isinstance(value, bool) or not isinstance(value, (int, float)):
            raise ValueError("Coordinate is not a finite number")
        value = float(value)
        if not math.isfinite(value) or not low <= value <= high:
            raise ValueError("Coordinate outside allowed canvas")
        return value

    def hex_color(value):
        if not isinstance(value, str) or re.fullmatch(r"#[0-9a-fA-F]{6}", value) is None:
            raise ValueError("Invalid #RRGGBB color")
        return value.upper()

    def point(source, keys):
        return {key: number(source[key]) for key in keys}

    def size(source, keys):
        return {key: number(source[key], 1e-12, 1.5) for key in keys}

    def stroke(source):
        return {"width": number(source.get("width", .009), .001, .09)}

    if not isinstance(data, dict) or type(data.get("version")) is not int or data["version"] != 1:
        raise ValueError("Only scene version 1 supported")
    title = data.get("title")
    intent = data.get("intent")
    if not isinstance(title, str) or not 1 <= len(title.strip()) <= 76:
        raise ValueError("Invalid title")
    if not isinstance(intent, str) or not 1 <= len(intent.strip()) <= 240:
        raise ValueError("Invalid intent")
    source = data.get("elements")
    if not isinstance(source, list) or not 8 <= len(source) <= 90:
        raise ValueError("8..90 elements required")
    result = {
        "version": 1,
        "title": " ".join(title.split()),
        "intent": " ".join(intent.split()),
        "background": hex_color(data.get("background")),
        "elements": [],
    }
    for el in source:
        if not isinstance(el, dict):
            raise ValueError("Invalid drawing element")
        kind = el.get("kind")
        if kind not in {"rect", "ellipse", "line", "curve", "poly", "path"}:
            raise ValueError("Unsupported drawing command")
        item = {
            "kind": kind,
            "color": hex_color(el.get("color")),
            "alpha": number(el.get("alpha", 1), .04, 1),
        }
        if "gradient" in el:
            g = el["gradient"]
            if not isinstance(g, dict) or g.get("type") not in {"linear", "radial"}:
                raise ValueError("Invalid gradient")
            safe_g = {
                "type": g["type"],
                "start": hex_color(g.get("start")),
                "end": hex_color(g.get("end")),
            }
            if g["type"] == "linear":
                safe_g.update(point(g, ("x1", "y1", "x2", "y2")))
                if (safe_g["x1"] - safe_g["x2"]) ** 2 + (
                    safe_g["y1"] - safe_g["y2"]
                ) ** 2 < .0001:
                    raise ValueError("Degenerate gradient")
            else:
                safe_g.update(point(g, ("cx", "cy")))
                safe_g.update(size(g, ("r",)))
            item["gradient"] = safe_g
        if kind in {"rect", "ellipse"}:
            item.update(point(el, ("x", "y")))
            item.update(size(el, ("w", "h")))
        elif kind in {"line", "curve"}:
            coords = ("x", "y", "x2", "y2")
            if kind == "curve":
                coords += ("cx1", "cy1", "cx2", "cy2")
            item.update(point(el, coords))
            item.update(stroke(el))
        elif kind == "poly":
            pts = el.get("points")
            if not isinstance(pts, list) or not 3 <= len(pts) <= 18:
                raise ValueError("Invalid polygon")
            item["points"] = [
                [number(pair[0]), number(pair[1])]
                for pair in pts
                if isinstance(pair, list) and len(pair) == 2
            ]
            if len(item["points"]) != len(pts):
                raise ValueError("Invalid polygon point")
        else:
            mode = el.get("mode", "fill")
            if mode not in {"fill", "stroke"}:
                raise ValueError("Invalid path mode")
            item["mode"] = mode
            if mode == "stroke":
                item.update(stroke(el))
            commands = el.get("commands")
            if not isinstance(commands, list) or not 3 <= len(commands) <= 64:
                raise ValueError("Invalid path command count")
            fixed = []
            draw_count = 0
            for index, cmd in enumerate(commands):
                if not isinstance(cmd, dict):
                    raise ValueError("Invalid path instruction")
                op = cmd.get("op")
                if index == 0 and op != "M" or index > 0 and op == "M":
                    raise ValueError("Exactly one initial M is allowed")
                if op in {"M", "L"}:
                    fixed.append({"op": op, **point(cmd, ("x", "y"))})
                elif op == "Q":
                    fixed.append({"op": op, **point(cmd, ("cx", "cy", "x", "y"))})
                elif op == "C":
                    fixed.append({"op": op, **point(cmd,
                        ("cx1", "cy1", "cx2", "cy2", "x", "y"))})
                elif op == "Z" and index == len(commands) - 1:
                    fixed.append({"op": op})
                else:
                    raise ValueError("Unsupported path instruction")
                if op in {"L", "Q", "C"}:
                    draw_count += 1
            if draw_count < 2 or mode == "fill" and commands[-1]["op"] != "Z":
                raise ValueError("Invalid path closure")
            item["commands"] = fixed
        result["elements"].append(item)

    text = json.dumps(result, ensure_ascii=False, separators=(",", ":"))
    if len(text) > 18000:
        raise ValueError("Scene exceeds Android storage bounds")
    return text


def generate(prompt, model):
    try:
        from aiguide import ask  # the actual external library, not a reimplementation
    except ImportError as exc:
        raise RuntimeError(
            "Install original package first: pip install git+https://github.com/"
            "bspippi1337/aiguide-dev.git@c55f658610a0bb27f90c75619510dc8ad5c36a2f"
        ) from exc
    instructions = (
        "Compose a visually meaningful ORIGINAL artwork, no repeated icons, "
        "fandom or any prior ChatGPT history. Create a concrete drawable concept. "
        "Return ONLY JSON with version:1,title,intent,background and 20-75 elements. "
        "Each element has kind,color,optional alpha; kind rect/ellipse has x,y,w,h; "
        "kind line has x,y,x2,y2,width; curve has x,y,cx1,cy1,cx2,cy2,x2,y2,width; "
        "poly has points as coordinate pairs. All coordinates normalized 0..1. "
        "Use a strong focal subject, authored material language and negative space. "
        f"Artistic request: {prompt}"
    )
    output, _ = ask(instructions, model=model, capture_reasoning=False)
    return validate(json.loads(output))


def main(argv=None):
    parser = argparse.ArgumentParser(description="aiguide artwork -> Android script")
    parser.add_argument("idea", nargs="?", default="Find an unfamiliar visual premise")
    parser.add_argument("--model", default="gpt-6.1-terra")
    parser.add_argument("--clipboard", action="store_true",
                        help="Copy JSON to Termux clipboard if Termux:API is installed")
    parser.add_argument("--check", metavar="FILE",
                        help="Validate existing scene without calling a model")
    args = parser.parse_args(argv)
    try:
        if args.check:
            with open(args.check, encoding="utf-8") as file:
                text = validate(json.load(file))
        else:
            text = generate(args.idea, args.model)
        if args.clipboard:
            subprocess.run(["termux-clipboard-set"], input=text, text=True, check=True)
            print("JSON copied. Android → DNA/? → Importer AI-bildemanus.", file=sys.stderr)
        else:
            print(text)
    except (ValueError, KeyError, RuntimeError, OSError,
            subprocess.CalledProcessError, json.JSONDecodeError) as error:
        print(f"aiguide bridge: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
