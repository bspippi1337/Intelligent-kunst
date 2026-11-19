package no.blckswan.intelligentkunst.core;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelCriticTest {
    private static final int W=192,H=320;
    private static int[] solid(int color) {
        int[] pixels=new int[W*H];
        java.util.Arrays.fill(pixels,color);
        return pixels;
    }
    @Test public void recognizesBlankVersusComposedImage() {
        int[] blank=solid(0xFFEAE1D3);
        int[] composed=solid(0xFFEAE1D3);
        for(int y=95;y<260;y++)for(int x=52;x<104;x++)composed[y*W+x]=0xFF162827;
        for(int y=125;y<188;y++)for(int x=125;x<174;x++)composed[y*W+x]=0xFFBD7855;
        PixelCritic.Analysis b=PixelCritic.analyze(blank,W,H);
        PixelCritic.Analysis a=PixelCritic.analyze(composed,W,H);
        assertTrue("solid colour must not win",a.score>b.score);
        assertTrue(a.sampled>2000);
        assertTrue(a.explain().contains("BILDEANALYSE"));
        assertEquals(a.signature,PixelCritic.analyze(composed,W,H).signature);
    }
    @Test public void visualHashTracksDifferentLayouts() {
        int[] left=solid(0xFFFFFFFF),right=solid(0xFFFFFFFF);
        for(int y=0;y<H;y++)for(int x=0;x<W/3;x++)left[y*W+x]=0xFF090909;
        for(int y=0;y<H;y++)for(int x=W*2/3;x<W;x++)right[y*W+x]=0xFF090909;
        long a=PixelCritic.analyze(left,W,H).signature;
        long b=PixelCritic.analyze(right,W,H).signature;
        assertTrue(PixelCritic.difference(a,b)>=12);
        assertEquals(0,PixelCritic.difference(a,a));
    }
    @Test public void backwardsCompatibleFamilyHasNarrativeIntent() {
        for(int i=0;i<500;i++) {
            Artwork art=new ArtDirector().create(i,java.util.Collections.emptyList(),new ArtDirector.Taste());
            assertEquals(Artwork.Style.FABLE,art.style);
            assertFalse(art.title().isEmpty());
            assertFalse(art.intention().isEmpty());
            assertEquals(art.encode(),Artwork.decode(art.encode()).encode());
        }
        java.util.Set<String> combinations=new java.util.HashSet<>();
        java.util.Set<String> concepts=new java.util.HashSet<>();
        for(long seed=0;seed<3000;seed++) {
            StoryPlan.Idea idea=StoryPlan.generate(seed);
            combinations.add(idea.signature());
            concepts.add(idea.proposition.name());
            assertEquals(idea.signature(),StoryPlan.generate(seed).signature());
            assertFalse(idea.premise().isEmpty());
            assertFalse(idea.title().isEmpty());
            if (idea.proposition==StoryPlan.Proposition.OVERFLOW)
                assertTrue(idea.object==StoryPlan.ObjectKind.CUP
                    || idea.object==StoryPlan.ObjectKind.TEAPOT
                    || idea.object==StoryPlan.ObjectKind.SUITCASE
                    || idea.object==StoryPlan.ObjectKind.BOOK);
        }
        assertTrue("not just four repeating images",combinations.size()>90);
        assertEquals(7,concepts.size());
    }
}
