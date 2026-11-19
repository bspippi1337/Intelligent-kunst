# aiguide-dev → Intelligent Kunst

Source: [bspippi1337/aiguide-dev](https://github.com/bspippi1337/aiguide-dev), pinned at
`c55f658610a0bb27f90c75619510dc8ad5c36a2f`. MIT licensed; attribution
and license: [third_party/aiguide-dev-LICENSE](../third_party/aiguide-dev-LICENSE).

## What is actually reused

- The original `BLCKSWAN_DIREKTE_ARBEIDSMODUS_PROMPT.md` is bundled unchanged
  as Android asset `aiguide_template.md`.
- `AiguideClient.ask(query, key, model)` ports the Python library's
  prompt + model request + date-stamped JSONL log to native Android Java.
- `tools/aiguide_generate.py` **imports and calls the original Python
  `from aiguide import ask`** so Termux/Linux users can use the library
  itself and paste the produced scene into Android.
- No access to or claims of hidden model reasoning. Log entries contain
  observable requests/responses and `reasoning: null`.

The Python package does **not** paint images. We have added a bounded,
documented visual scene contract, validated by `AiguideScene` and rendered
in `ScriptPainter`. The real model invents the arrangement; native Android
draws the result. This is more flexible than a four-motif procedural painter,
but it is not a photographic diffusion system or a guarantee of high-quality art.

## Use it directly on Android

Tap **✦ AI**, enter an original artistic request (optional), enter your own
OpenAI API key and a model ID, and choose **Komponer**. The app requests one
JSON scene and draws it immediately. The result is stored in **DNA v3** so it
can reopen **offline**, be exported to PNG, copied and imported on another
phone. If you have no key, use offline **SKAP NYTT** or import an externally
generated `aiguide` scene.

- Only the AI button uses the Internet. This app now declares the INTERNET
  permission; cleartext HTTP is disabled.
- API keys are used in-memory for that request and are not persisted or
  included in logs, DNA, files or source control.
- Your optional prompt is sent to OpenAI when you tap **Komponer**. API
  usage may cost money separately from any ChatGPT subscription.
- Visible inputs/outputs are kept in app-private UTC JSONL files under
  `files/aiguide-logs/YYYY-MM-DD.jsonl`. The file never includes hidden
  chain-of-thought.
- Input and model output are bounded and validated. Only rect, ellipse, line,
  cubic curve and filled polygon may execute; the app does not evaluate code.
- Failures show an error and keep the existing artwork intact.

## Actual Python library on Termux / Linux

```sh
python -m pip install 'git+https://github.com/bspippi1337/aiguide-dev.git@c55f658610a0bb27f90c75619510dc8ad5c36a2f'
export OPENAI_API_KEY='YOUR_KEY_HERE'
python tools/aiguide_generate.py "A newly invented work about an impossible material" --clipboard
```

`--clipboard` requires Termux:API and the `termux-api` package on Android.
Without it, omit `--clipboard` to print the validated JSON, or redirect
stdout to a file. In the Android app choose **DNA / ? → Importer AI-bildemanus**,
paste JSON, and press **Tegn**.

The direct Python path uses `aiguide.ask` unchanged, including its original
logging. The native Android path is a **port**, not a Python runtime or
subprocess running inside the APK. These are separate, explicitly documented
paths to the same bounded scene format.

## Visual grammar extensions (0.5.0)

The JSON scene keeps version 1 for compatibility. Existing scenes remain valid.

A path element has kind "path", a mode of "fill" or "stroke", and 3 to 64 commands.
Commands use op "M" (move), "L" (line), "Q" (quadratic curve), "C" (cubic curve)
and "Z" (close). A filled path must end in Z. A stroked path can remain open.
Coordinates and control points are bounded, and only one initial M is accepted.

Each vector element may also contain an optional gradient:

- Linear: type, start/end colours, and normalized x1,y1,x2,y2.
- Radial: type, start/end colours, normalized cx,cy, and positive radius r.

Colours must be six-digit hex RGB; no external images, SVG scripts or executable
payloads are accepted. The validator strips unknown keys rather than forwarding
them to the renderer.

A complete real scene, containing several custom closed curves and both gradient
types, is in [examples/expressive-vector-scene.json](../examples/expressive-vector-scene.json).
In Android select DNA / ? and then "Importer AI-bildemanus" to draw it without
an API key. New path scenes require Android 0.5.0 or later.

## Scene contract

```json
{
  "version": 1,
  "title": "Untitled study",
  "intent": "A visual idea, not a stock motif",
  "background": "#E7DDC9",
  "elements": [
    {"kind":"ellipse","x":0.25,"y":0.2,"w":0.5,"h":0.3,"color":"#203040"},
    {"kind":"line","x":0.1,"y":0.65,"x2":0.9,"y2":0.76,"width":0.01,"color":"#D77C62"}
  ]
}
```

The example illustrates syntax, not a complete valid scene: at least 8 and
at most 90 elements are required. Positions are normalized relative to
the image dimensions. `rect` and `ellipse` require x/y/w/h,
`line` requires x/y/x2/y2/width, `curve` requires two Bezier controls,
and `poly` requires 3–18 [x,y] points. Colors are strict `#RRGGBB`,
alpha is optional. All data is bounded and no external images or scripts
are allowed.

Tests: `python3 -m unittest discover -s tools -p 'test_aiguide_bridge.py'`
and the Android CI workflow. Live model output requires API credentials;
the CI pipeline uses no external key and does not claim to test model taste.
