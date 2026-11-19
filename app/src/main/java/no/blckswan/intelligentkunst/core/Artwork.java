package no.blckswan.intelligentkunst.core;

import java.util.Locale;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/** Immutable genome of one piece. Every generation is reproducible by its encoded genome. */
public final class Artwork {
    public enum Style {
        CURRENT("STRØM", "Lys som finner sin egen vei"),
        BLOOM("BLOMSTRING", "Organiske former, uten botanikk"),
        ORBIT("BANER", "Tyngdekraften er kun et forslag"),
        TERRAIN("TOPOGRAFI", "Et landskap som aldri fantes"),
        WEAVE("VEV", "Orden på grensen til kaos"),
        PRISM("PRISME", "Geometrien drømmer i farger"),
        FABLE("FORTELLING", "Et motiv, et spørsmål og en verden utenfor rammen"),
        AIGUIDE("AI-ATELIER", "Et bildemanus skapt av en ekstern språkmodell");

        public final String label;
        public final String thought;
        Style(String label, String thought) { this.label = label; this.thought = thought; }
    }

    public static final int[][] PALETTES = {
        {0xFF090C14, 0xFF9C88FF, 0xFF66E5D4, 0xFFFAAF6A, 0xFFF7EAE3},
        {0xFF0D0B17, 0xFFEFA6D6, 0xFFF7D18B, 0xFF8C96E9, 0xFFFFF4E8},
        {0xFF071413, 0xFFD9FB81, 0xFF58CCC5, 0xFFEAF3C4, 0xFFFCF9EE},
        {0xFF161015, 0xFFFF7F73, 0xFFFFC58B, 0xFFB379BA, 0xFFFFE7DB},
        {0xFF0A0E1D, 0xFF6FC7FF, 0xFFE8F2FA, 0xFF796BCA, 0xFFB5DDEC},
        {0xFF120C11, 0xFFF4B8A2, 0xFFCB6E6D, 0xFFF4DBAA, 0xFFFFF3E1},
        {0xFF100E19, 0xFFB5F0A7, 0xFFE39FC5, 0xFF90A3FC, 0xFFFFF5F2},
        {0xFF090C0C, 0xFFD8DAD5, 0xFF84A398, 0xFFDBAA7B, 0xFFF8F1E7}
    };

    public final long seed;
    public final Style style;
    public final int palette;
    public final double energy;
    public final double complexity;
    public final double balance;
    public final double drift;
    public final double score;
    public final int generation;
    public final boolean hasParent;
    public final long parentSeed;
    /** Validated JSON drawing commands, embedded in DNA v3 for offline reopening. */
    public final String sceneJson;

    public Artwork(long seed, Style style, int palette, double energy, double complexity,
                   double balance, double drift, double score, int generation) {
        this(seed, style, palette, energy, complexity, balance, drift, score, generation, false, 0L, null);
    }

    public Artwork(long seed, Style style, int palette, double energy, double complexity,
                   double balance, double drift, double score, int generation, String sceneJson) {
        this(seed, style, palette, energy, complexity, balance, drift, score, generation, false, 0L, sceneJson);
    }

    /** DNA v2: records which earlier work was deliberately evolved. */
    public Artwork(long seed, Style style, int palette, double energy, double complexity,
                   double balance, double drift, double score, int generation, long parentSeed) {
        this(seed, style, palette, energy, complexity, balance, drift, score, generation, true, parentSeed, null);
    }

    private Artwork(long seed, Style style, int palette, double energy, double complexity,
                    double balance, double drift, double score, int generation,
                    boolean hasParent, long parentSeed, String sceneJson) {
        if (style == null || palette < 0 || palette >= PALETTES.length || generation < 0
                || (hasParent && generation == 0) || (style == Style.AIGUIDE && (sceneJson == null || sceneJson.length() > 18000))
                || (style != Style.AIGUIDE && sceneJson != null)) {
            throw new IllegalArgumentException("Invalid artwork genome");
        }
        this.seed = seed;
        this.style = style;
        this.palette = palette;
        this.energy = clamp(energy);
        this.complexity = clamp(complexity);
        this.balance = clamp(balance);
        this.drift = clamp(drift);
        this.score = clamp(score);
        this.generation = generation;
        this.hasParent = hasParent;
        this.parentSeed = parentSeed;
        this.sceneJson = sceneJson;
    }

    private static double clamp(double n) { return Double.isFinite(n) ? Math.max(0, Math.min(1, n)) : 0.5; }

    public int[] colors() { return PALETTES[palette].clone(); }
    public String id() { return String.format(Locale.ROOT, "%016X", seed); }
    public String title() {
        if(style == Style.AIGUIDE) return "Et generert visuelt utsagn";
        if(style == Style.FABLE) return StoryPlan.title(seed);
        String[] first = {"Stillheten", "Det uferdige", "Et ekko", "Mellomrom", "Tyngdeløs", "Den siste", "Nattens", "Uten kart", "Lysbrudd", "Uten navn", "Forskyvning", "Under huden"};
        String[] last = {"arkitektur", "av morgendagen", "i bevegelse", "av ingenting", "horisont", "som puster", "paradoks", "av lys", "i et annet univers", "tilfeldighet", "balanse", "etter stormen"};
        int a = (int) Long.remainderUnsigned(seed, first.length);
        int b = (int) Long.remainderUnsigned(Long.rotateRight(seed, 23), last.length);
        return first[a] + " " + last[b];
    }

    /** This is a human-authored visual prompt derived from the artwork DNA. */
    public String intention() {
        return style == Style.FABLE ? StoryPlan.intention(seed) : style.thought;
    }

    /** Compact, versioned record for permanent offline storage. No Java serialization. */
    public String encode() {
        String record = String.format(Locale.ROOT, "1:%s:%d:%d:%.6f:%.6f:%.6f:%.6f:%.6f:%d",
            id(), style.ordinal(), palette, energy, complexity, balance, drift, score, generation);
        if(sceneJson != null) return "3" + record.substring(1) + ":"
            + Base64.getUrlEncoder().withoutPadding().encodeToString(sceneJson.getBytes(StandardCharsets.UTF_8));
        return hasParent ? "2" + record.substring(1) + ":"
            + String.format(Locale.ROOT, "%016X", parentSeed) : record;
    }

    public static Artwork decode(String value) {
        if (value == null) throw new IllegalArgumentException("Missing DNA");
        String[] p = value.split(":", -1);
        boolean v2 = p.length == 11 && "2".equals(p[0]);
        boolean v3 = p.length == 11 && "3".equals(p[0]);
        if (!v2 && !v3 && !(p.length == 10 && "1".equals(p[0])))
            throw new IllegalArgumentException("Unknown artwork format");
        int style = Integer.parseInt(p[2]);
        if (style < 0 || style >= Style.values().length) throw new IllegalArgumentException("Invalid style");
        long seed = Long.parseUnsignedLong(p[1], 16);
        int palette = Integer.parseInt(p[3]);
        double energy = Double.parseDouble(p[4]);
        double complexity = Double.parseDouble(p[5]);
        double balance = Double.parseDouble(p[6]);
        double drift = Double.parseDouble(p[7]);
        double score = Double.parseDouble(p[8]);
        int generation = Integer.parseInt(p[9]);
        if (v3) {
            byte[] bytes=Base64.getUrlDecoder().decode(p[10]);
            if(bytes.length > 18000)throw new IllegalArgumentException("Oversized AI scene");
            return new Artwork(seed,Style.values()[style],palette,energy,complexity,balance,
                drift,score,generation,new String(bytes,StandardCharsets.UTF_8));
        }
        if (v2) return new Artwork(seed, Style.values()[style], palette,
            energy, complexity, balance, drift, score, generation,
            Long.parseUnsignedLong(p[10], 16));
        return new Artwork(seed, Style.values()[style], palette,
            energy, complexity, balance, drift, score, generation);
    }
}
