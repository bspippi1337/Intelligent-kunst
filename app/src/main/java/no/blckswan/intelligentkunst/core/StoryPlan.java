package no.blckswan.intelligentkunst.core;

import java.util.Locale;

/**
 * Seeded conceptual composition, independent of user history.
 *
 * Each work begins with a familiar, ordinary physical object and a rule that
 * breaks one of its expectations. The rule must have a visible consequence.
 * This is a constrained generative grammar, NOT semantic AI.
 */
public final class StoryPlan {
    private StoryPlan() {}

    public enum ObjectKind {
        CHAIR("STOLEN", "stolen"),
        CUP("KOPPEN", "koppen"),
        LADDER("STIGEN", "stigen"),
        SUITCASE("KOFFERTEN", "kofferten"),
        LAMP("LAMPEN", "lampen"),
        BOOK("BOKEN", "boken"),
        UMBRELLA("PARAPLYEN", "paraplyen"),
        TEAPOT("TEKANNEN", "tekannen");
        public final String title, inflected;
        ObjectKind(String title, String inflected) {
            this.title=title;this.inflected=inflected;
        }
    }
    public enum Proposition {
        INDEPENDENT_SHADOW("SKYGGENS ULYDIGHET",
            "Skyggen har sluttet å adlyde %s. Den peker et annet sted."),
        UNFINISHED("DET SOM MANGLER",
            "Det manglende stykket blir det tydeligste ved %s."),
        AFTERIMAGE("ETTERSLEP",
            "%s er her, men tidligere plasseringer nekter å forsvinne."),
        LEVITATION("TYNGDENS UNNTAK",
            "%s har mistet vekten, men ikke forbindelsen med bakken."),
        SEPARATION("BRUDDFLATEN",
            "%s er delt; delene forsøker fremdeles å høre sammen."),
        OVERFLOW("OVERSKUDDET",
            "%s rommer mer enn sin egen avgrensning."),
        SCALE("MÅLEFEIL",
            "%s er ikke for stor. Rommet har feil målestokk.");
        public final String title, premise;
        Proposition(String title,String premise){this.title=title;this.premise=premise;}
    }
    public enum Space { WHITE_ROOM, NOCTURNE, ARCHIVE }

    public static final class Idea {
        public final ObjectKind object;
        public final Proposition proposition;
        public final Space space;
        public final double displacement, openness, scale, lean;
        public final long seed;
        private Idea(long seed,ObjectKind object,Proposition proposition,Space space,
                     double displacement,double openness,double scale,double lean) {
            this.seed=seed;this.object=object;this.proposition=proposition;
            this.space=space;this.displacement=displacement;this.openness=openness;
            this.scale=scale;this.lean=lean;
        }
        public String title() {
            return proposition.title + " / " + object.title;
        }
        public String premise() {
            return String.format(Locale.ROOT,proposition.premise,object.inflected);
        }
        public String signature() {
            return object.name()+"/"+proposition.name()+"/"+space.name();
        }
    }

    /** Independent mixed bits prevent a simplistic modulo-four picture rotation. */
    public static Idea generate(long seed) {
        long a=mix(seed ^ 0x626F6E657374554CL);
        long b=mix(seed ^ 0x6D6174657269614CL);
        long c=mix(seed ^ 0x4E4F544150524F4DL);
        ObjectKind object=ObjectKind.values()[
            (int)Long.remainderUnsigned(a,ObjectKind.values().length)];
        Proposition proposition=Proposition.values()[
            (int)Long.remainderUnsigned(b,Proposition.values().length)];
        Space space=Space.values()[(int)Long.remainderUnsigned(c,Space.values().length)];
        // Conceptual constraint: a cup, teapot, book or suitcase can spill contents.
        // Other objects are assigned a different physical paradox.
        boolean container=object==ObjectKind.CUP || object==ObjectKind.TEAPOT
            || object==ObjectKind.SUITCASE || object==ObjectKind.BOOK;
        if(proposition==Proposition.OVERFLOW && !container)
            proposition=Proposition.UNFINISHED;
        return new Idea(seed,object,proposition,space,
            ((a>>>11)&1023)/1023.0,
            ((b>>>16)&1023)/1023.0,
            .76+((c>>>25)&1023)/1023.0*.34,
            (((b>>>37)&1023)/1023.0-.5)*.25);
    }
    private static long mix(long z){
        z=(z^(z>>>30))*0xBF58476D1CE4E5B9L;
        z=(z^(z>>>27))*0x94D049BB133111EBL;
        return z^(z>>>31);
    }
    public static int scene(long seed) { return generate(seed).object.ordinal(); }
    public static String title(long seed) { return generate(seed).title(); }
    public static String intention(long seed) { return generate(seed).premise(); }
    public static String motif(long seed) { return generate(seed).proposition.title; }
}
