package no.blckswan.intelligentkunst.core;

import java.util.List;

/** Verifiable, deterministic evaluation rubric. Not an objective measure of artistic value. */
public final class CritiqueReport {
    public final double rhythm, detail, tension, motion, novelty, surprise, preference, total;

    private CritiqueReport(double rhythm, double detail, double tension, double motion,
                           double novelty, double surprise, double preference) {
        this.rhythm = rhythm;
        this.detail = detail;
        this.tension = tension;
        this.motion = motion;
        this.novelty = novelty;
        this.surprise = surprise;
        this.preference = preference;
        total = Math.max(0, Math.min(1, rhythm*.24 + detail*.20 + tension*.18
            + motion*.12 + novelty*.14 + surprise*.04 + preference));
    }

    public static CritiqueReport evaluate(Artwork a, List<Artwork> recent, ArtDirector.Taste taste) {
        double rhythm = 1 - Math.abs(a.energy - .58);
        double detail = 1 - Math.abs(a.complexity - .62);
        double tension = 1 - Math.abs(a.balance - .68);
        double motion = 1 - Math.abs(a.drift - .53);
        double novelty = 1;
        int seen = 0;
        if (recent != null) {
            for (Artwork r : recent) {
                if (r.seed == a.seed) continue;
                if (seen++ >= 12) break;
                double distance = (r.style == a.style ? 0 : .45)
                    + (r.palette == a.palette ? 0 : .22)
                    + Math.abs(r.energy - a.energy) * .11
                    + Math.abs(r.complexity - a.complexity) * .11
                    + Math.abs(r.balance - a.balance) * .11;
                novelty = Math.min(novelty, distance);
            }
        }
        double surprise = Long.remainderUnsigned(a.seed ^ (a.seed >>> 29), 1000) / 1000.0;
        return new CritiqueReport(rhythm, detail, tension, motion, novelty, surprise,
            taste == null ? 0 : taste.reward(a));
    }

    /** Human-readable breakdown of weighted components; can be checked against source. */
    public String explain() {
        return "RYTME         " + percent(rhythm) + "  ×24%\n"
             + "DETALJ         " + percent(detail) + "  ×20%\n"
             + "SPENNING    " + percent(tension) + "  ×18%\n"
             + "BEVEGELSE " + percent(motion) + "  ×12%\n"
             + "NYHET          " + percent(novelty) + "  ×14%\n"
             + "VARIASJON  " + percent(surprise) + "  ×4%\n"
             + "DIN SMAK      +" + Math.round(preference * 100) + " poeng\n\n"
             + "NY VURDERING   " + percent(total);
    }
    private static String percent(double n) { return Math.round(n * 100) + "%"; }
}
