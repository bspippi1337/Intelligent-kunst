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
            Artwork.Style style = Artwork.Style.values()[random.nextInt(Artwork.Style.values().length)];
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
            Artwork.Style style = random.nextDouble() < 0.82 ? parent.style :
                Artwork.Style.values()[random.nextInt(Artwork.Style.values().length)];
            int palette = random.nextDouble() < 0.76 ? parent.palette : random.nextInt(Artwork.PALETTES.length);
            Artwork child = new Artwork(random.nextLong(), style, palette,
                mutate(random, parent.energy), mutate(random, parent.complexity),
                mutate(random, parent.balance), mutate(random, parent.drift), 0, parent.generation + 1);
            double score = critique(child, recent, taste);
            if (score > bestScore) { bestScore = score; best = child; }
        }
        return withScore(best, bestScore);
    }

    private static double mutate(SplittableRandom random, double value) {
        return Math.max(0, Math.min(1, value + (random.nextDouble() + random.nextDouble() - 1) * 0.33));
    }

    private static Artwork withScore(Artwork a, double score) {
        return new Artwork(a.seed, a.style, a.palette, a.energy, a.complexity,
            a.balance, a.drift, Math.max(0, Math.min(1, score)), a.generation);
    }

    /** Heuristic, legible rules: rhythm, spatial tension, complexity and novelty. */
    public double critique(Artwork a, List<Artwork> recent, Taste taste) {
        double rhythm = 1 - Math.abs(a.energy - 0.58);
        double detail = 1 - Math.abs(a.complexity - 0.62);
        double asymmetry = 1 - Math.abs(a.balance - 0.68);
        double motion = 1 - Math.abs(a.drift - 0.53);
        double novelty = 1;
        int seen = 0;
        if (recent != null) {
            for (Artwork r : recent) {
                if (seen++ >= 12) break;
                double d = (r.style == a.style ? 0 : 0.45)
                    + (r.palette == a.palette ? 0 : 0.22)
                    + Math.abs(r.energy - a.energy) * 0.11
                    + Math.abs(r.complexity - a.complexity) * 0.11
                    + Math.abs(r.balance - a.balance) * 0.11;
                novelty = Math.min(novelty, d);
            }
        }
        double noise = (Long.remainderUnsigned(a.seed ^ (a.seed >>> 29), 1000)) / 1000.0;
        double fitness = rhythm * 0.24 + detail * 0.20 + asymmetry * 0.18
            + motion * 0.12 + novelty * 0.14 + noise * 0.04
            + (taste == null ? 0 : taste.reward(a));
        return Math.max(0, Math.min(1, fitness));
    }
}
