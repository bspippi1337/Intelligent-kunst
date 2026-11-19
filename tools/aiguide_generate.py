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
    if not isinstance(data, dict):
        raise ValueError("Scene must be a JSON object")
    if data.get("version") != 1:
        raise ValueError("Only version 1 supported")
    if not (1 <= len(data.get("title", "")) <= 76):
        raise ValueError("Invalid scene title")
    if not (1 <= len(data.get("intent", "")) <= 240):
        raise ValueError("Invalid scene intention")
    import re
    colour = re.compile(r"^#[0-9a-fA-F]{6}$")
    if not colour.fullmatch(data.get("background", "")):
        raise ValueError("Invalid background colour")
    elements = data.get("elements", [])
    if not isinstance(elements, list) or not 8 <= len(elements) <= 90:
        raise ValueError("8..90 elements required")
    for el in elements:
        if not isinstance(el, dict) or el.get("kind") not in {
            "rect", "ellipse", "line", "curve", "poly"
        }:
            raise ValueError("Unsupported element")
        if not colour.fullmatch(el.get("color", "")):
            raise ValueError("Invalid element colour")
        kind = el["kind"]
        props = {
            "rect": ("x", "y", "w", "h"),
            "ellipse": ("x", "y", "w", "h"),
            "line": ("x", "y", "x2", "y2"),
            "curve": ("x", "y", "cx1", "cy1", "cx2", "cy2", "x2", "y2"),
            "poly": ()
        }[kind]
        alpha = el.get("alpha", 1)
        if not isinstance(alpha, (int, float)) or not 0.04 <= alpha <= 1:
            raise ValueError("Invalid alpha")
        for prop in props:
            n = el[prop]
            if not isinstance(n, (int, float)):
                raise ValueError(f"Invalid number: {prop}")
            if prop in {"w", "h"}:
                if not 0 < n <= 1.5:
                    raise ValueError(f"Invalid extent: {prop}")
            elif not -0.25 <= n <= 1.25:
                raise ValueError(f"Invalid coordinate: {prop}")
        if kind in {"line", "curve"}:
            width = el.get("width", .009)
            if not isinstance(width, (int, float)) or not .001 <= width <= .09:
                raise ValueError("Invalid stroke width")
        if kind == "poly":
            p = el["points"]
            if not isinstance(p, list) or not 3 <= len(p) <= 18:
                raise ValueError("Invalid polygon")
            if any(not isinstance(pair, list) or len(pair) != 2
                   or any(not isinstance(n, (int, float))
                          or not -0.25 <= n <= 1.25 for n in pair)
                   for pair in p):
                raise ValueError("Invalid polygon point")
    result = json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    if len(result) > 18000:
        raise ValueError("Scene exceeds Android storage bounds")
    return result


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
