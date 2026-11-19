package no.blckswan.intelligentkunst.core;

import java.util.SplittableRandom;

/**
 * Real semantic continuity for the offline Concept Studio.
 *
 * A new seed can change paper, composition, lighting and silhouette while
 * retaining the parent object's identity. Optionally retain the premise;
 * otherwise explore another physical contradiction involving the same object.
 *
 * Works with historical DNA, without changing the deterministic story grammar.
 */
public final class ConceptLineage {
    private ConceptLineage() {}

    public static long childSeed(long requested, StoryPlan.Idea ancestor,
                                 boolean keepProposition) {
        if(ancestor==null) return requested;
        SplittableRandom candidates=new SplittableRandom(requested);
        long sameObjectCandidate=requested;
        boolean foundObject=false;
        for(int attempt=0;attempt<1024;attempt++){
            long seed=candidates.nextLong();
            StoryPlan.Idea idea=StoryPlan.generate(seed);
            if(idea.object!=ancestor.object)continue;
            if(!foundObject){
                sameObjectCandidate=seed;
                foundObject=true;
            }
            if(keepProposition) {
                if(idea.proposition==ancestor.proposition)return seed;
            } else if(idea.proposition!=ancestor.proposition) {
                return seed;
            }
        }
        // Fixed bound on compute. Even when a requested combination is sparse,
        // preserve the character rather than picking an unrelated subject.
        return foundObject?sameObjectCandidate:requested;
    }
}
