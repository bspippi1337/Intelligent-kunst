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
    @Test public void tasteLearnsIdeasWithoutConfusingEverythingWithPalette() {
        ArtDirector.Taste taste=new ArtDirector.Taste();
        ArtDirector director=new ArtDirector();
        Artwork loved=director.create(456L,java.util.Collections.emptyList(),taste);
        StoryPlan.Idea favorite=StoryPlan.generate(loved.seed);
        Artwork counterexample=null;
        for(long seed=100L;seed<5000;seed++) {
            Artwork alt=director.create(seed,java.util.Collections.emptyList(),new ArtDirector.Taste());
            StoryPlan.Idea other=StoryPlan.generate(alt.seed);
            if(other.object!=favorite.object && other.proposition!=favorite.proposition
                && alt.palette==loved.palette) {
                counterexample=alt;break;
            }
        }
        assertNotNull("need contrasting ideas with same palette",counterexample);
        for(int k=0;k<9;k++)taste.like(loved);
        assertTrue("subject+premise taste must matter",
            taste.reward(loved)>taste.reward(counterexample));
        assertEquals(9,taste.objectVotes()[favorite.object.ordinal()]);
        assertEquals(9,taste.propositionVotes()[favorite.proposition.ordinal()]);
        ArtDirector.Taste restored=new ArtDirector.Taste();
        restored.load(taste.styleVotes(),taste.paletteVotes(),
            taste.objectVotes(),taste.propositionVotes());
        assertEquals(taste.reward(loved),restored.reward(loved),0);
    }

    @Test public void importedAiScenesDoNotPretendToTrainOfflinePainter() {
        ArtDirector.Taste taste=new ArtDirector.Taste();
        Artwork remote=new Artwork(9911L,Artwork.Style.AIGUIDE,0,
            .5,.5,.5,.5,0,0,"{}");
        taste.like(remote);
        taste.dislike(remote);
        int styles=0,palettes=0,objects=0,propositions=0,rejections=0;
        for(int v:taste.styleVotes())styles+=v;
        for(int v:taste.paletteVotes())palettes+=v;
        for(int v:taste.objectVotes())objects+=v;
        for(int v:taste.propositionVotes())propositions+=v;
        for(int v:taste.paletteRejections())rejections+=v;
        for(int v:taste.objectRejections())rejections+=v;
        for(int v:taste.propositionRejections())rejections+=v;
        assertEquals(0,styles+palettes+objects+propositions+rejections);
    }

    @Test public void rejectionMovesScoresAwayFromBoringIdeas() {
        ArtDirector.Taste taste=new ArtDirector.Taste();
        Artwork ignored=new ArtDirector().create(700L,
            java.util.Collections.emptyList(),taste);
        double before=taste.reward(ignored);
        for(int i=0;i<8;i++)taste.dislike(ignored);
        double after=taste.reward(ignored);
        assertTrue("negative feedback must actually alter concept reward", after<before);
        ArtDirector.Taste restored=new ArtDirector.Taste();
        restored.load(taste.styleVotes(),taste.paletteVotes(),
            taste.objectVotes(),taste.propositionVotes(),
            taste.objectRejections(),taste.propositionRejections(),
            taste.paletteRejections());
        assertEquals(after,restored.reward(ignored),0);
    }

    @Test public void offspringPreserveTheirParentsVisualSubject() {
        ArtDirector author=new ArtDirector();
        ArtDirector.Taste taste=new ArtDirector.Taste();
        for(long i=0;i<180;i++){
            Artwork parent=author.create(i*31+5,
                java.util.Collections.emptyList(),taste);
            Artwork child=author.evolve(parent,i*37+213,
                java.util.Collections.singletonList(parent),taste);
            StoryPlan.Idea before=StoryPlan.generate(parent.seed);
            StoryPlan.Idea after=StoryPlan.generate(child.seed);
            assertEquals("A child should not abruptly switch subject",
                before.object,after.object);
            assertNotEquals(parent.seed,child.seed);
            assertEquals(parent.seed,child.parentSeed);
            assertTrue(child.hasParent);
        }
    }

    @Test public void conceptualVariantsCanAlterPremiseWithoutLosingSubject() {
        StoryPlan.Idea ancestor=StoryPlan.generate(12345);
        boolean foundDifferent=false;
        for(int i=0;i<100;i++){
            long seed=ConceptLineage.childSeed(i*137L+3,ancestor,false);
            StoryPlan.Idea child=StoryPlan.generate(seed);
            assertEquals(ancestor.object,child.object);
            if(child.proposition!=ancestor.proposition)foundDifferent=true;
        }
        assertTrue(foundDifferent);
        long close=ConceptLineage.childSeed(42,ancestor,true);
        StoryPlan.Idea sibling=StoryPlan.generate(close);
        assertEquals(ancestor.object,sibling.object);
        assertEquals(ancestor.proposition,sibling.proposition);
    }

    @Test public void independentWorksCannotRepeatRecentSubjectsOrIdeas() {
        ArtDirector author=new ArtDirector();
        ArtDirector.Taste blank=new ArtDirector.Taste();
        Artwork first=author.create(42,java.util.Collections.emptyList(),blank);
        assertFalse(ConceptDiversity.isFresh(first,java.util.Collections.singletonList(first)));
        StoryPlan.Idea original=StoryPlan.generate(first.seed);
        Artwork different=null;
        for(int seed=0;seed<1000;seed++) {
            Artwork candidate=author.create(seed,java.util.Collections.emptyList(),blank);
            StoryPlan.Idea idea=StoryPlan.generate(candidate.seed);
            if(idea.object!=original.object && idea.proposition!=original.proposition){
                different=candidate;break;
            }
        }
        assertNotNull(different);
        assertTrue(ConceptDiversity.isFresh(different,
            java.util.Collections.singletonList(first)));
        assertTrue(ConceptDiversity.isFresh(first,java.util.Collections.emptyList()));
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
