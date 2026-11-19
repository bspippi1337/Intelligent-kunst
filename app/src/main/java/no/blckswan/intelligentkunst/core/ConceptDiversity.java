package no.blckswan.intelligentkunst.core;

import java.util.List;

/**
 * An explicit exploration constraint that the pixel scorer cannot overrule.
 * A well-lit suitcase is not fresh if the last several works were suitcases.
 * Only new independent proposals must satisfy this: EVOLVER may retain subject.
 */
public final class ConceptDiversity {
    public static final int OBJECT_LOOKBACK=4;
    public static final int PROPOSITION_LOOKBACK=2;
    private ConceptDiversity(){}

    public static boolean isFresh(Artwork candidate,List<Artwork> recent) {
        if(candidate==null)throw new IllegalArgumentException("Missing candidate");
        if(candidate.style!=Artwork.Style.FABLE || recent==null)return true;
        StoryPlan.Idea proposed=StoryPlan.generate(candidate.seed);
        int compared=0;
        for(Artwork old:recent){
            if(old.style!=Artwork.Style.FABLE)continue;
            StoryPlan.Idea previous=StoryPlan.generate(old.seed);
            if(compared<OBJECT_LOOKBACK && previous.object==proposed.object)return false;
            if(compared<PROPOSITION_LOOKBACK &&
               previous.proposition==proposed.proposition)return false;
            if(++compared>=Math.max(OBJECT_LOOKBACK,PROPOSITION_LOOKBACK))break;
        }
        return true;
    }
}
