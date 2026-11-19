# Changelog

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
