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
        int[] flat = new int[192*320];
        int[] composed = new int[192*320];
        java.util.Arrays.fill(flat, 0xFFEFE3CD);
        java.util.Arrays.fill(composed, 0xFFEFE3CD);
        for(int y=80;y<260;y++)for(int x=40;x<108;x++)composed[y*192+x]=0xFF20382C;
        PixelCritic.Analysis plain = PixelCritic.analyze(flat,192,320);
        PixelCritic.Analysis interesting = PixelCritic.analyze(composed,192,320);
        check(interesting.score > plain.score, "actual pixels influence selection");
        check(PixelCritic.difference(interesting.signature,plain.signature) > 0,
            "image fingerprints respond to subject");
        for(int k=0;k<128;k++){
            Artwork story=d.create(k, List.of(),t);
            check(story.style == Artwork.Style.FABLE, "new art tells stories");
            check(!story.intention().isEmpty(), "story has intentional subject");
        }
        String scene = "{\"version\":1,\"title\":\"Unexpected form\",\"elements\":[]}";
        Artwork modelWork = new Artwork(10022L, Artwork.Style.AIGUIDE, 0,
            .5,.5,.5,.5,.9,0,scene);
        check(modelWork.encode().startsWith("3:"), "AI artwork uses DNA v3");
        check(Artwork.decode(modelWork.encode()).sceneJson.equals(scene),
            "AI script survives offline DNA round-trip");
        check(Artwork.decode(modelWork.encode()).encode().equals(modelWork.encode()),
            "AI script reproduces deterministically");
        FieldNoise noise = new FieldNoise(1337);
        for (int i = -300; i < 300; i++) {
            double n = noise.fractal(i / 17.0, i / 29.0);
            check(n >= 0 && n <= 1, "bounded smooth noise");
        }
        System.out.println("PASS: pixel vision, art subject selection, 365 daily editions, genealogy, 3000 artworks, codecs, noise");
        System.out.println("Example: " + a.title() + " | " + a.style.label + " | genome=" + a.id());
    }
    private static void check(boolean v, String message) { if (!v) throw new AssertionError(message); }
}
