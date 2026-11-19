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
