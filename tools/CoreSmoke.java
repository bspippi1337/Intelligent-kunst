import no.blckswan.intelligentkunst.core.*;
import java.util.*;

public class CoreSmoke {
    public static void main(String[] args) {
        ArtDirector d = new ArtDirector();
        ArtDirector.Taste t = new ArtDirector.Taste();
        Artwork a = d.create(42, List.of(), t);
        Artwork b = d.create(42, List.of(), t);
        check(a.encode().equals(b.encode()), "reproducible selection");
        check(Artwork.decode(a.encode()).encode().equals(a.encode()), "stable archive encoding");
        check(a.score > 0 && a.score <= 1, "critique range");
        t.like(a);
        Artwork c = d.evolve(a, 1337, List.of(a), t);
        check(c.seed != a.seed && c.generation == 1 && c.hasParent && c.parentSeed == a.seed,
            "evolution lineage");
        check(Artwork.decode(c.encode()).parentSeed == a.seed, "parent saved in versioned DNA");
        check(CritiqueReport.evaluate(a, List.of(), new ArtDirector.Taste()).total == a.score,
            "score breakdown matches selection");
        for (int day=20000;day<20365;day++) {
            check(DailyEdition.ofUtcDay(day).encode().equals(DailyEdition.ofUtcDay(day).encode()),
                "daily work is reproducible");
        }
        for (int i = 0; i < 3000; i++) {
            Artwork x = d.create(i * 10007L, List.of(a, c), t);
            check(x.score >= 0 && x.score <= 1, "bounded score");
            check(Artwork.decode(x.encode()).encode().equals(x.encode()), "archive round trip");
        }
        FieldNoise noise = new FieldNoise(1337);
        for (int i = -300; i < 300; i++) {
            double n = noise.fractal(i / 17.0, i / 29.0);
            check(n >= 0 && n <= 1, "bounded smooth noise");
        }
        System.out.println("PASS: reproducibility, 365 daily editions, genealogy, critic, 3000 artworks, codecs, noise");
        System.out.println("Example: " + a.title() + " | " + a.style.label + " | genome=" + a.id());
    }
    private static void check(boolean v, String message) { if (!v) throw new AssertionError(message); }
}
