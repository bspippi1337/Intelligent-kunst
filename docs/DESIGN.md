# Design notes: autonomous artist, not a filter pack

The experiment asks whether an autonomous system can have a distinct aesthetic process without simulating human authorship.

## Invariants
1. An artwork is an immutable, versioned genome, not a transient bitmap.
2. Given the same genome, a canvas render is deterministic at a fixed resolution.
3. Exactly 42 genome proposals are evaluated per CREATE or EVOLVE action.
4. Fitness is a transparent heuristic, not objective artistic truth.
5. Taste adaptation is local, bounded and optional.
6. Rendering and PNG export do not run on the main UI thread.
7. No accounts, external image models, permissions for network access, or telemetry.

## Evaluation
The art director scores rhythm, density, asymmetry, implied motion, novelty relative to recent pieces, and bounded learned preference. EVOLVE draws mutated offspring with the chosen parent's style and palette as probabilistic anchors.

## Limitations
The current evaluator scores compositional genes, not rendered pixel contents. As a research follow-up, a low-resolution image critique stage could assess visual diversity, histogram balance and edge information. This is deliberately listed as future work rather than marketed as an implemented feature.

## Test strategy
Pure JVM randomized smoke test of 3,000 pieces, deterministic codec roundtrip, mutation lineage and noise bounds; JUnit tests; Android CI producing a debug-signed test APK. Hardware testing and Play Store signing are separate release gates.
