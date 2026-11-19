package no.blckswan.intelligentkunst.core;

import java.util.List;
import java.util.SplittableRandom;

/** Transparent evolutionary creative selection, not an LLM or a claim of human aesthetic judgment. */
public final class ArtDirector {
    public static final int CANDIDATES = 42;

    public static final class Taste {
        private final int[] styleVotes = new int[Artwork.Style.values().length];
        private final int[] paletteVotes = new int[Artwork.PALETTES.length];
        public void like(Artwork art) {
            if (styleVotes[art.style.ordinal()] < 1000) styleVotes[art.style.ordinal()]++;
            if (paletteVotes[art.palette] < 1000) paletteVotes[art.palette]++;
        }
        public int[] styleVotes() { return styleVotes.clone(); }
        public int[] paletteVotes() { return paletteVotes.clone(); }
        public void load(int[] styles, int[] palettes) {
            for (int i = 0; i < styleVotes.length && i < styles.length; i++) styleVotes[i] = Math.max(0, Math.min(1000, styles[i]));
            for (int i = 0; i < paletteVotes.length && i < palettes.length; i++) paletteVotes[i] = Math.max(0, Math.min(1000, palettes[i]));
        }
        double reward(Artwork candidate) {
            double style = Math.min(8, styleVotes[candidate.style.ordinal()]) / 8.0;
            double palette = Math.min(8, paletteVotes[candidate.palette]) / 8.0;
            return (style + palette) * 0.04;
        }
    }

    /** Create candidates, self-critique and choose. Same input yields the same piece. */
    public Artwork create(long seed, List<Artwork> recent, Taste taste) {
        SplittableRandom random = new SplittableRandom(seed);
        Artwork best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int i = 0; i < CANDIDATES; i++) {
            // New works have subjects and visual stakes; legacy geometry remains
            // accessible from existing DNA and archived artworks.
            Artwork.Style style = Artwork.Style.FABLE;
            int palette = random.nextInt(Artwork.PALETTES.length);
            Artwork candidate = new Artwork(random.nextLong(), style, palette,
                random.nextDouble(), random.nextDouble(), random.nextDouble(),
                random.nextDouble(), 0, 0);
            double score = critique(candidate, recent, taste);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return withScore(best, bestScore);
    }

    /** Genetic mutation: the offspring always has its own seed and a meaningful relationship to its parent. */
    public Artwork evolve(Artwork parent, long seed, List<Artwork> recent, Taste taste) {
        SplittableRandom random = new SplittableRandom(seed ^ parent.seed);
        Artwork best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int i = 0; i < CANDIDATES; i++) {
            Artwork.Style style = Artwork.Style.FABLE; // even legacy parents can become stories
            int palette = random.nextDouble() < 0.76 ? parent.palette : random.nextInt(Artwork.PALETTES.length);
            Artwork child = new Artwork(random.nextLong(), style, palette,
                mutate(random, parent.energy), mutate(random, parent.complexity),
                mutate(random, parent.balance), mutate(random, parent.drift), 0, parent.generation + 1, parent.seed);
            double score = critique(child, recent, taste);
            if (score > bestScore) { bestScore = score; best = child; }
        }
        return withScore(best, bestScore);
    }

    private static double mutate(SplittableRandom random, double value) {
        return Math.max(0, Math.min(1, value + (random.nextDouble() + random.nextDouble() - 1) * 0.33));
    }

    private static Artwork withScore(Artwork a, double score) {
        double bounded = Math.max(0, Math.min(1, score));
        return a.hasParent
            ? new Artwork(a.seed, a.style, a.palette, a.energy, a.complexity,
                a.balance, a.drift, bounded, a.generation, a.parentSeed)
            : new Artwork(a.seed, a.style, a.palette, a.energy, a.complexity,
                a.balance, a.drift, bounded, a.generation);
    }

    /** All critical dimensions and their weights live in CritiqueReport. */
    public double critique(Artwork a, List<Artwork> recent, Taste taste) {
        return CritiqueReport.evaluate(a, recent, taste).total;
    }
}
