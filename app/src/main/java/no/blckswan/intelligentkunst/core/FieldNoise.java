package no.blckswan.intelligentkunst.core;

/** Smooth deterministic value noise. No platform APIs; can be tested on a regular JVM. */
public final class FieldNoise {
    private final long seed;
    public FieldNoise(long seed) { this.seed = seed; }

    private double lattice(int x, int y) {
        long z = seed + x * 0x9e3779b97f4a7c15L + y * 0xc2b2ae3d27d4eb4fL;
        z ^= z >>> 30; z *= 0xbf58476d1ce4e5b9L;
        z ^= z >>> 27; z *= 0x94d049bb133111ebL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }
    private static double smooth(double a) { return a * a * (3 - 2 * a); }
    private static double mix(double a, double b, double t) { return a + (b - a) * t; }
    public double value(double x, double y) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        double fx = smooth(x - ix), fy = smooth(y - iy);
        return mix(mix(lattice(ix, iy), lattice(ix + 1, iy), fx),
            mix(lattice(ix, iy + 1), lattice(ix + 1, iy + 1), fx), fy);
    }
    public double fractal(double x, double y) {
        return 0.57 * value(x, y) + 0.28 * value(x * 2.13 + 17, y * 2.13 - 12)
            + 0.15 * value(x * 4.17 - 33, y * 4.17 + 21);
    }
}
