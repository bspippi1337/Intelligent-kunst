package no.blckswan.intelligentkunst.render;

/**
 * Small, deterministic image structure field for traditional, reference-based painting.
 * Computes true luminance and Sobel edges from actual pixels, never invents anatomy,
 * faces, or missing photographic detail. No Android dependency, so easily unit tested.
 */
public final class ImageStructure {
    private final int width,height;
    private final int[] pixels;
    private final float[] light,gx,gy;

    public ImageStructure(int[] argb,int width,int height){
        if(argb==null||width<2||height<2||width>512||height>512
                ||argb.length!=width*height)
            throw new IllegalArgumentException("Invalid reference image grid");
        this.width=width;
        this.height=height;
        this.pixels=argb.clone();
        this.light=new float[argb.length];
        this.gx=new float[argb.length];
        this.gy=new float[argb.length];
        for(int i=0;i<argb.length;i++){
            int c=argb[i];
            light[i]=(0.2126f*((c>>16)&255)
                +0.7152f*((c>>8)&255)
                +0.0722f*(c&255))/255f;
        }
        for(int y=1;y<height-1;y++){
            for(int x=1;x<width-1;x++){
                int i=y*width+x;
                float tl=light[i-width-1],tc=light[i-width],tr=light[i-width+1];
                float ml=light[i-1],mr=light[i+1];
                float bl=light[i+width-1],bc=light[i+width],br=light[i+width+1];
                gx[i]=(-tl+tr-2*ml+2*mr-bl+br)/8f;
                gy[i]=(-tl-2*tc-tr+bl+2*bc+br)/8f;
            }
        }
    }

    public int width(){return width;}
    public int height(){return height;}
    private int index(int x,int y){
        return Math.max(0,Math.min(height-1,y))*width
            +Math.max(0,Math.min(width-1,x));
    }
    public int color(int x,int y){return pixels[index(x,y)];}
    public float luminance(int x,int y){return light[index(x,y)];}
    /** Gradient magnitude in normalized image luminance, zero on uniform regions. */
    public float edge(int x,int y){
        int i=index(x,y);
        return Math.min(1f,(float)Math.hypot(gx[i],gy[i])*4f);
    }
    /** Brushstroke angle tangent to luminance contours, in radians. */
    public float contourAngle(int x,int y){
        int i=index(x,y);
        if(Math.abs(gx[i])+Math.abs(gy[i])<.0001f)return 0f;
        return (float)Math.atan2(gx[i],-gy[i]);
    }
}
