package no.blckswan.intelligentkunst.core;

import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;

public class ArtDirectorTest {
    private final ArtDirector director=new ArtDirector();
    private final ArtDirector.Taste taste=new ArtDirector.Taste();

    @Test public void sameSeedSameSelection() {
        assertEquals(director.create(42L,Collections.emptyList(),taste).encode(),
            director.create(42L,Collections.emptyList(),taste).encode());
    }
    @Test public void genomeSurvivesRoundTrip() {
        for(long seed=0;seed<100;seed++) {
            Artwork a=director.create(seed,Collections.emptyList(),taste);
            assertEquals(a.encode(),Artwork.decode(a.encode()).encode());
        }
    }
    @Test public void offspringHasNewIdentityAndGeneration() {
        Artwork p=director.create(11,Collections.emptyList(),taste);
        Artwork c=director.evolve(p,22,Collections.singletonList(p),taste);
        assertEquals(p.generation+1,c.generation);
        assertNotEquals(p.seed,c.seed);
    }
    @Test public void evolvedDnaRecordsParent() {
        Artwork parent=director.create(42L,Collections.emptyList(),taste);
        Artwork child=director.evolve(parent,1337L,Collections.singletonList(parent),taste);
        assertTrue(child.hasParent);
        assertEquals(parent.seed,child.parentSeed);
        assertEquals("2",child.encode().split(":")[0]);
        Artwork decoded=Artwork.decode(child.encode());
        assertTrue(decoded.hasParent);
        assertEquals(parent.seed,decoded.parentSeed);
        assertEquals(child.encode(),decoded.encode());
    }
    @Test public void v1ArchivesRemainReadable() {
        Artwork original=director.create(42L,Collections.emptyList(),taste);
        assertEquals("1",original.encode().split(":")[0]);
        assertFalse(Artwork.decode(original.encode()).hasParent);
    }
    @Test public void oneUtcDayOneSharedArtwork() {
        for(long day=20000;day<20100;day++) {
            Artwork piece=DailyEdition.ofUtcDay(day);
            assertEquals(piece.encode(),DailyEdition.ofUtcDay(day).encode());
            assertNotEquals(DailyEdition.seedForUtcDay(day),DailyEdition.seedForUtcDay(day+1));
        }
    }
    @Test public void critiqueMatchesStoredSelectionAndRendersExplanation() {
        Artwork piece=director.create(42L,Collections.emptyList(),taste);
        CritiqueReport report=CritiqueReport.evaluate(piece,Collections.emptyList(),taste);
        assertEquals(piece.score,report.total,0.000001);
        assertTrue(report.explain().contains("RYTME"));
        assertTrue(report.explain().contains("NY VURDERING"));
    }
    @Test public void malformedV2DnaFailsClosed() {
        try {
            Artwork.decode("2:0123:0:0:0:0:0:0:0:1:NOT_HEX");
            fail("invalid parent id accepted");
        } catch (IllegalArgumentException expected) { /* expected */ }
    }
    @Test public void corruptDnaIsRejected() {
        try{Artwork.decode("evil");fail("must reject");}catch(IllegalArgumentException expected){}
    }
    @Test public void noiseIsReproducibleAndBounded() {
        FieldNoise field=new FieldNoise(7);
        for(int i=0;i<100;i++) {
            double value=field.fractal(i*.13,i*.031);
            assertEquals(value,new FieldNoise(7).fractal(i*.13,i*.031),0);
            assertTrue(value>=0&&value<=1);
        }
    }
}
