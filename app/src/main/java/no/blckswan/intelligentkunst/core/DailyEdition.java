package no.blckswan.intelligentkunst.core;

import java.util.Collections;

/** The same UTC calendar day produces identical art on every handset. No network or user ID. */
public final class DailyEdition {
    private DailyEdition() {}

    public static long seedForUtcDay(long epochDay) {
        // SplitMix64 avalanche: adjacent days do not produce visually adjacent seeds.
        long z = epochDay * 0x9E3779B97F4A7C15L + 0x494E54454C4C4947L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public static Artwork ofUtcDay(long epochDay) {
        return new ArtDirector().create(seedForUtcDay(epochDay),
            Collections.emptyList(), new ArtDirector.Taste());
    }
}
