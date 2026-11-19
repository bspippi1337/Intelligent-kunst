package no.blckswan.intelligentkunst.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import no.blckswan.intelligentkunst.core.ArtDirector;
import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.core.PixelCritic;

/**
 * A bounded on-device generate -> actually render -> inspect -> select loop.
 * Unlike the original version, candidates are judged from pixels, not just DNA.
 * No network, no model weights, no pretend semantic understanding.
 */
public final class StudioDirector {
    public static final int PROPOSALS=12;
    private static final int PREVIEW_W=192, PREVIEW_H=320;
    private final ArtDirector director=new ArtDirector();

    public static final class Selection {
        public final Artwork artwork;
        public final PixelCritic.Analysis visualReport;
        public final int proposals;
        public final double visualWeight;
        private Selection(Artwork artwork,PixelCritic.Analysis vision,int proposals) {
            this.artwork=artwork;
            this.visualReport=vision;
            this.proposals=proposals;
            this.visualWeight=.78;
        }
    }

    public Selection create(long seed,List<Artwork> recent,
                            ArtDirector.Taste taste,Artwork parent){
        SplittableRandom random=new SplittableRandom(seed);
        // Novelty is evaluated against recent image fingerprints, not only style IDs.
        List<Long> previous=new ArrayList<>();
        if(recent!=null){
            int limit=Math.min(4,recent.size());
            for(int i=0;i<limit;i++) {
                try {previous.add(inspect(recent.get(i)).signature);}
                catch(OutOfMemoryError oom) {break;}
            }
        }
        Artwork best=null;
        PixelCritic.Analysis report=null;
        double bestScore=-1;
        for(int i=0;i<PROPOSALS;i++){
            Artwork draft=parent==null
                ? director.create(random.nextLong(),recent,taste)
                : director.evolve(parent,random.nextLong(),recent,taste);
            PixelCritic.Analysis visual=inspect(draft);
            double novelty=.5;
            if(!previous.isEmpty()){
                int distance=64;
                for(long fingerprint:previous)
                    distance=Math.min(distance,PixelCritic.difference(fingerprint,visual.signature));
                novelty=Math.min(1,distance/24.0);
            }
            double result=.78*visual.score + .17*draft.score + .05*novelty;
            // Prefer less similar results when visual quality is otherwise comparable.
            if(!previous.isEmpty() && novelty<.20)result*=.86;
            if(result>bestScore){
                bestScore=result; best=draft;report=visual;
            }
        }
        if(best==null)throw new IllegalStateException("No candidate produced");
        return new Selection(rescore(best,bestScore),report,PROPOSALS);
    }

    public static PixelCritic.Analysis inspect(Artwork piece){
        Bitmap image=null;
        try{
            image=Bitmap.createBitmap(PREVIEW_W,PREVIEW_H,Bitmap.Config.ARGB_8888);
            new RenderEngine().draw(new Canvas(image),PREVIEW_W,PREVIEW_H,piece);
            int[] pixels=new int[PREVIEW_W*PREVIEW_H];
            image.getPixels(pixels,0,PREVIEW_W,0,0,PREVIEW_W,PREVIEW_H);
            return PixelCritic.analyze(pixels,PREVIEW_W,PREVIEW_H);
        }finally{
            if(image!=null)image.recycle();
        }
    }

    private static Artwork rescore(Artwork a,double value){
        double score=Math.max(0,Math.min(1,value));
        return a.hasParent
            ? new Artwork(a.seed,a.style,a.palette,a.energy,a.complexity,
                a.balance,a.drift,score,a.generation,a.parentSeed)
            : new Artwork(a.seed,a.style,a.palette,a.energy,a.complexity,
                a.balance,a.drift,score,a.generation);
    }
}
