package no.blckswan.intelligentkunst.core;

/**
 * Transparent, dependency-free analysis of FINISHED pixels.
 * This is image statistics, not semantic computer vision or an art-trained model.
 * Input is ARGB pixels in scanline order. Never allocates a copy of the bitmap.
 */
public final class PixelCritic {
    private PixelCritic() {}

    public static final class Analysis {
        public final double tone, focus, quiet, edges, placement, score;
        public final long signature;
        public final int sampled;
        private Analysis(double tone,double focus,double quiet,double edges,
                         double placement,double score,long signature,int sampled) {
            this.tone=tone; this.focus=focus; this.quiet=quiet; this.edges=edges;
            this.placement=placement; this.score=score; this.signature=signature;
            this.sampled=sampled;
        }
        public String explain() {
            return "LYS / SKYGGE       " + percent(tone) + "\n"
                + "TYDELIG FOKUS    " + percent(focus) + "\n"
                + "PAUSEROM          " + percent(quiet) + "\n"
                + "TEGNING / DETALJ " + percent(edges) + "\n"
                + "PLASSERING        " + percent(placement) + "\n"
                + "BILDEANALYSE     " + percent(score) + "\n"
                + "Analysert fra " + sampled + " ekte bildepiksler.\n"
                + "Ingen gjenkjenning av symboler eller motiv.";
        }
    }

    public static Analysis analyze(int[] pixels,int width,int height) {
        if (pixels == null || width < 12 || height < 12 || pixels.length < (long)width * height)
            throw new IllegalArgumentException("Invalid image buffer");
        int step=Math.max(1,Math.min(width,height)/110);
        int samples=0, lowGrad=0, strongGrad=0;
        double mean=0,m2=0,totalEnergy=0, cx=0,cy=0;
        double[] regions=new double[9];
        for(int y=step;y<height-step;y+=step) {
            int row=y*width;
            for(int x=step;x<width-step;x+=step) {
                double l=luma(pixels[row+x]);
                double dx=Math.abs(l-luma(pixels[row+x+step]));
                double dy=Math.abs(l-luma(pixels[(y+step)*width+x]));
                double grad=Math.min(1,dx+dy);
                if(grad<.045)lowGrad++;
                if(grad>.11)strongGrad++;
                ++samples;
                double delta=l-mean;
                mean+=delta/samples;
                m2+=delta*(l-mean);
                double saliency=grad * (1+Math.min(1,colourfulness(pixels[row+x]))*.2);
                totalEnergy+=saliency;
                double px=x/(double)width,py=y/(double)height;
                cx+=saliency*px;cy+=saliency*py;
                int ix=Math.min(2,(int)(px*3)),iy=Math.min(2,(int)(py*3));
                regions[iy*3+ix]+=saliency;
            }
        }
        double std=Math.sqrt(m2/Math.max(1,samples));
        double contrast=bell(std,.24,.22);
        double space=lowGrad/(double)Math.max(1,samples);
        double breathing=bell(space,.67,.33);
        double markFraction=strongGrad/(double)Math.max(1,samples);
        double detail=bell(markFraction,.075,.075);
        double maxRegion=0;
        for(double v:regions)maxRegion=Math.max(maxRegion,v);
        double focus=totalEnergy>.00001 ? bell(maxRegion/totalEnergy,.31,.24) : 0;
        double x=totalEnergy>0?cx/totalEnergy:.5,y=totalEnergy>0?cy/totalEnergy:.5;
        // Avoid a clinically centred or edge-clipped focal mass; allow multiple thirds.
        double placement=totalEnergy>.00001
            ? clamp(1 - Math.max(0, Math.abs(x-.5)-.31)*2.2
                      - Math.max(0, Math.abs(y-.5)-.32)*2.2) : 0;
        double result=contrast*.29+focus*.25+breathing*.17+detail*.18+placement*.11;
        if(std<.045 || totalEnergy<.1)result*=.2; // blank canvases must not win.
        return new Analysis(contrast,focus,breathing,detail,placement,clamp(result),
            hash(pixels,width,height),samples);
    }
    public static int difference(long a,long b) {return Long.bitCount(a^b);}
    private static double luma(int c) {
        return (((c>>>16)&255)*.2126+((c>>>8)&255)*.7152+(c&255)*.0722)/255.;
    }
    private static double colourfulness(int c) {
        int r=(c>>>16)&255,g=(c>>>8)&255,b=c&255;
        return (Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b)))/255.;
    }
    private static double bell(double value,double target,double spread) {
        return clamp(1-Math.abs(value-target)/spread);
    }
    private static double clamp(double value){return Math.max(0,Math.min(1,value));}
    private static String percent(double n){return Math.round(n*100)+"%";}

    /** 64-bit average hash of small luminance tiles, stable across resolutions. */
    private static long hash(int[] pixels,int w,int h) {
        double[] values=new double[64];double sum=0;
        for(int yy=0;yy<8;yy++)for(int xx=0;xx<8;xx++) {
            int x0=xx*w/8,x1=(xx+1)*w/8;
            int y0=yy*h/8,y1=(yy+1)*h/8;
            double accum=0;int count=0;
            for(int sy=y0;sy<y1;sy+=Math.max(1,(y1-y0)/4)) {
                for(int sx=x0;sx<x1;sx+=Math.max(1,(x1-x0)/4)) {
                    accum+=luma(pixels[sy*w+sx]);count++;
                }
            }
            double v=accum/Math.max(1,count);
            values[yy*8+xx]=v;sum+=v;
        }
        double average=sum/64.;
        long bits=0;
        for(int i=0;i<64;i++)if(values[i]>=average)bits|=(1L<<i);
        return bits;
    }
}
