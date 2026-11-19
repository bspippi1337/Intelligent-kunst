package no.blckswan.intelligentkunst.core;

/**
 * A small, authored visual vocabulary: motifs have subjects, stakes and
 * composition rather than random ornaments. Not a language model.
 */
public final class StoryPlan {
    private StoryPlan() {}

    private static final String[] SUBJECTS = {
        "FYRVOKTEREN", "DØREN", "DET SISTE TREET", "HVALEN"
    };
    private static final String[] STORIES = {
        "Et lys som insisterer på å være igjen når alle andre har dratt.",
        "Et umulig sted, og et menneske som velger å gå inn.",
        "Det som overlever, gjør ikke alltid mest lyd.",
        "En kjempe bærer havet som om det var et minne."
    };
    private static final String[] COUNTERPOINTS = {
        "før stormen", "etter stillheten", "uten kart", "mot strømmen",
        "mellom to verdener", "under en fremmed sol", "ved verdens ende",
        "der ingen venter"
    };
    public static int scene(long seed) {
        return (int) Long.remainderUnsigned(seed, SUBJECTS.length);
    }
    public static String title(long seed) {
        int scene=scene(seed);
        int suffix=(int)Long.remainderUnsigned(Long.rotateLeft(seed, 19), COUNTERPOINTS.length);
        return SUBJECTS[scene] + " / " + COUNTERPOINTS[suffix];
    }
    public static String intention(long seed) { return STORIES[scene(seed)]; }
    public static String motif(long seed) { return SUBJECTS[scene(seed)]; }
}
