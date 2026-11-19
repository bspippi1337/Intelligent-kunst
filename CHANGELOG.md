# Changelog

## 0.6.0 · No keys, Classical Atelier & Photo Studies · 2026-11-19

- Removed Android API-key form and OpenAI HTTP client entirely. The APK no longer declares INTERNET permission; fully offline by default and by Android sandbox enforcement.
- Add a first-class **KLASSISK ATELIER** for independently created academic adult nude figure studies. Two pose families, seeded variations, anatomy, volume, chiaroscuro and optional charcoal style. This is procedural classical drawing, not AI photorealistic figure synthesis.
- Import user-chosen photographs via the Android system document picker and persisted read permission; no upload, credentials or invasive media permissions.
- Faithful photo mode displays real source pixels without fabricating missing details. Oil, charcoal and sepia modes offer local material interpretation of the selected source.
- Native PNG export to MediaStore, respecting source photo aspect ratio with bounded decoding size, and 2160 x 3840 figure export.
- Keep local conceptual generation, 12-candidate pixel selection, evolution, gallery, legacy DNA and offline JSON graphics import intact.
- Historical Python aiguide bridge remains independently available for developers, not required, advertised as separate key-requiring workflow.
- Emulator smoke now includes local figure and photography modes in addition to prior generation and evolution checks.

**Scope and honesty:** A new photorealistic person from pure text needs a real local neural generative model, not a fake illustration filter. 0.6.0 supplies faithful photorealistic *reproduction of supplied photos* and classical on-device nude figure paintings, not synthetic text-to-image realism.

## 0.5.1 · Conceptual continuity · 2026-11-19

- EVOLVER now preserves the parent's visual subject, rather than randomly picking a new object while only inheriting cosmetic DNA.
- Most evolutions keep the same physical proposition while exploring viewpoint, palette and composition. Some change the proposition but preserve the object.
- Added tests covering 180 parent/offspring pairs and deterministic selection of inherited concepts.
- The original aiguide-dev Python library is updated and its API compatibility correction landed in main. Android/Termux documentation now points at the verified commit.
- Genuine emulator smoke captures six distinct offline illustrations and the keyless, expressive vector-rendering fixture.

**Note:** Automated image quality metrics do not prove artistic intelligence. The offline grammar remains a deliberately constrained experiment.


## 0.5.0 · Expressive Canvas · 2026-11-19

- Expand AI scene grammar beyond rectangles, ellipses and polygons to filled/stroked Bezier paths with M/L/Q/C/Z instructions.
- Enable bounded linear and radial gradients on vector drawing elements. Retain all v1/v2 archives and reproducible v3 AI DNA.
- Enforce per-path maximum 64 commands, one starting M, closed filled paths and finite bounded geometry.
- Native Java and the actual Python aiguide bridge normalize and validate the expanded format. Offline tests cover both accepted and hostile fixtures.
- Include an independently imported example that needs no API key.

**Research honesty:** The improved renderer broadens what a model can express. It is not a diffusion model, and no live model output or art quality is verified by tests.


## 0.4.0 · aiguide integration · 2026-11-19

- Integrate original [aiguide-dev](https://github.com/bspippi1337/aiguide-dev) MIT prompt asset and `ask(query)` / private JSONL logging contract into an opt-in native Android AI atelier.
- Provide a direct Python companion `tools/aiguide_generate.py` which imports and calls `from aiguide import ask` and produces Android-importable scene JSON.
- Model output is an actual bounded visual script, not merely a theme or a piece title. Android paints vector rectangles, ellipses, polygonal silhouettes, lines and cubic Bezier curves from it.
- AI works get versioned, portable DNA v3 with the complete validated scene embedded, so previously generated art opens and exports offline. Existing v1/v2 artwork remains readable.
- Only explicit AI user actions call the OpenAI API over HTTPS with an in-memory key that is never persisted; prompts and responses are recorded as private JSONL. No hidden model reasoning is extracted.
- Offline procedural art engine remains fully functional. The application now declares INTERNET permission **solely for opt-in AI calls**, so earlier privacy claims of no network permission no longer apply.
- Add Python bridge contract tests and pure JVM DNA compatibility tests. No live AI or API-key testing in CI.

**Status:** This is a language-model-authored vector composition, not neural image diffusion or proof of human-level creative intelligence.


## 0.3.0 · From wallpaper to a visual argument · 2026-11-19

- Replace screensaver-style geometric defaults with a generative grammar: eight ordinary objects x seven visual contradictions x three space types. Each rule creates a visible physical consequence (displaced shadow, void, afterimage, levitation, separation, overflow, wrong scale). Disallow conceptually incoherent combinations. Existing six legacy styles and their DNA remain readable and exportable.
- A two-stage studio: 12 independently rendered candidate images, each from 42 genetic proposals, are evaluated from actual preview pixels before selection. All CPU work runs off the UI thread.
- New `PixelCritic` measures tonal range, image-space focus, quiet space, mark density, composition placement and 64-bit visual hash similarity against recent work.
- Open `Kunstnerens egenkritikk` to inspect the actual rendered bitmap's measurable image features and compare them to the old genome-only heuristic.
- Compose titles and visual premises from independently mixed seed values, without using user history or earlier projects. Render recognizable object forms with high-contrast ink, spatial tension, varied palettes and deliberate negative space.
- Learn positive AND negative feedback separately for objects, visual propositions and palette. Hold LÆR to reject a work, once per artwork, while maintaining novelty in the next proposals.
- Visual smoke CI opens the Android application in an emulator and captures six real generated works for inspection.
- Preserve deterministic DNA, offline rendering, gallery, PNG exports and backwards compatibility with earlier versions.
- New pixel fixture tests and deterministic story-generation checks.

**Research honesty:** Pixel statistics are not semantic understanding or an art-trained neural model. This is an authored concept grammar, not unlimited invention. Image inspection only approximates contrast, focus, space and novelty. Independent human evaluation remains essential.


## 0.2.0 · Atelier 002 · 2026-11-19

- One shared generative artwork per UTC calendar day, offline and reproducible.
- Auditable aesthetic critique, matching the core selection objective.
- DNA v2 records parent seed; v1 archive and import compatibility retained.
- Locally browsable ancestry, up to 300 recently encountered works.
- Full-canvas exhibition mode: tap artwork to hide or restore interface.
- Limit taste training to one vote per saved artwork.
- Added JVM/JUnit regression tests for lineage, 365 daily editions, critic and codecs.

**Verification:** JVM tests and Android CI are tracked separately. Do not label a build as device-tested without installing it.


## 0.1.0 · First light · 2026-11-19

- Original autonomous genetic art director with 42 candidates and transparent critique.
- Six original Android Canvas art styles, eight palettes and atmospheric materials.
- Offline taste feedback, evolutionary mutations, DNA copy/import and a persistent gallery.
- Asynchronous cached rendering, touch interaction and 2160 × 3840 MediaStore PNG export.
- Accessibility labels, native Android interface and zero network permissions.
- Plain-JVM smoke tests, JUnit tests and automated debug-APK build artifacts.

**Quality note:** Source and pure JVM core tested locally. Android compilation and device operation require CI/Android SDK; do not confuse these with a tested device release.
