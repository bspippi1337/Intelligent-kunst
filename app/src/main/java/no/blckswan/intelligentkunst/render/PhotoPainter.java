package no.blckswan.intelligentkunst.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageDecoder;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;

import java.io.IOException;
import java.util.SplittableRandom;

/**
 * Entirely offline reference-image painting, not text-to-image AI.
 * Photo uses the reference's actual pixels. Painterly modes sample real
 * image colors and Sobel contours, including dark-form hatching for charcoal.
 */
public final class PhotoPainter {
    public enum Mode { PHOTO, OIL, CHARCOAL, SEPIA }
    private PhotoPainter() {}

    /** Decode orientation-corrected image with bounded memory usage, API 29+. */
    public static Bitmap decode(Context context,Uri uri,int maxDimension)throws IOException {
        if(maxDimension<128||maxDimension>4096)throw new IOException("Invalid decoding limit");
        ImageDecoder.Source source=ImageDecoder.createSource(context.getContentResolver(),uri);
        return ImageDecoder.decodeBitmap(source,(decoder,info,src)->{
            int width=info.getSize().getWidth(),height=info.getSize().getHeight();
            if(width<1||height<1)throw new IllegalArgumentException("Empty image");
            double factor=Math.min(1.0,(double)maxDimension/Math.max(width,height));
            decoder.setTargetSize(Math.max(1,(int)Math.round(width*factor)),
                Math.max(1,(int)Math.round(height*factor)));
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
    }

    public static void draw(Canvas canvas,int width,int height,Bitmap image,Mode mode,long seed){
        if(image==null||image.isRecycled()||width<1||height<1)return;
        canvas.save();
        canvas.clipRect(0,0,width,height);
        canvas.drawColor(mode==Mode.PHOTO?0xFF101215:0xFFE4D9C2);
        final float scale=Math.min((float)width/image.getWidth(),(float)height/image.getHeight());
        final float imgW=image.getWidth()*scale,imgH=image.getHeight()*scale;
        final RectF dest=new RectF((width-imgW)*.5f,(height-imgH)*.5f,
            (width+imgW)*.5f,(height+imgH)*.5f);
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        if(mode==Mode.PHOTO){
            canvas.drawBitmap(image,null,dest,paint);
            canvas.restore();
            return;
        }

        if(mode==Mode.SEPIA){
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                .393f,.769f,.189f,0,0,
                .349f,.686f,.168f,0,0,
                .272f,.534f,.131f,0,0,
                0,0,0,1,0
            })));
            canvas.drawBitmap(image,null,dest,paint);
            paint.setColorFilter(null);
        }else{
            // Stable capped analysis source, not a 3840x3840 allocation.
            // Preview and export share the exact same normalized stroke field.
            final int sampleMax=232;
            final float reduction=Math.min(1f, sampleMax/(float)Math.max(image.getWidth(),image.getHeight()));
            final int sampleW=Math.max(2,Math.round(image.getWidth()*reduction));
            final int sampleH=Math.max(2,Math.round(image.getHeight()*reduction));
            final Bitmap sample=Bitmap.createScaledBitmap(image,sampleW,sampleH,true);
            final int[] argb=new int[sampleW*sampleH];
            sample.getPixels(argb,0,sampleW,0,0,sampleW,sampleH);
            if(sample!=image)sample.recycle();
            final ImageStructure structure=new ImageStructure(argb,sampleW,sampleH);
            if(mode==Mode.OIL)oil(canvas,image,dest,structure,seed);
            else charcoal(canvas,image,dest,structure,seed);
        }
        archivalFinish(canvas,dest,mode);
        canvas.restore();
    }

    /**
     * Painterly marks run along image contours rather than in arbitrary
     * horizontal rows. Three brush scales preserve the scene's composition.
     */
    private static void oil(Canvas canvas,Bitmap image,RectF dest,ImageStructure ref,long seed){
        Paint under=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        under.setAlpha(189);
        under.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
            1.07f,-.02f,-.02f,0,3,
            -.02f,1.07f,-.02f,0,2,
            -.02f,-.02f,1.05f,0,1,
            0,0,0,1,0
        })));
        canvas.drawBitmap(image,null,dest,under);

        Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeCap(Paint.Cap.ROUND);
        SplittableRandom rand=new SplittableRandom(seed^0x6A09E667F3BCC909L);
        canvas.save();
        canvas.clipRect(dest);
        int[][] passes={{36,48},{62,92},{105,125}};
        for(int pass=0;pass<passes.length;pass++){
            int columns=passes[pass][0];
            int rows=Math.max(2,Math.min(240,Math.round(columns*dest.height()/Math.max(1f,dest.width()))));
            float dx=dest.width()/columns,dy=dest.height()/rows;
            float thick=Math.max(.7f,Math.min(dx,dy)*(pass==0?.55f:pass==1?.38f:.26f));
            brush.setStrokeWidth(thick);
            for(int cy=0;cy<rows;cy++){
                for(int cx=0;cx<columns;cx++){
                    float u=(cx+.5f+(float)(rand.nextDouble()-.5)*.72f)/columns;
                    float v=(cy+.5f+(float)(rand.nextDouble()-.5)*.72f)/rows;
                    u=clamp01(u);v=clamp01(v);
                    int ix=Math.min(ref.width()-1,(int)(u*(ref.width()-1)));
                    int iy=Math.min(ref.height()-1,(int)(v*(ref.height()-1)));
                    int base=ref.color(ix,iy);
                    float edge=ref.edge(ix,iy);
                    float dir=ref.contourAngle(ix,iy);
                    // Change direction in flat pigment fields, not at boundaries.
                    if(edge<.075f)dir=(float)(-.25+.5*rand.nextDouble());
                    else dir+=(float)(rand.nextDouble()-.5)*.18f;
                    float len=Math.min(dx,dy)*(pass==0?2.35f:pass==1?1.75f:1.15f)
                        *(edge>.55f?.45f:1f);
                    float x=dest.left+u*dest.width(),y=dest.top+v*dest.height();
                    int alpha=pass==0?160:pass==1?135:105;
                    int variation=(int)Math.round((rand.nextDouble()-.5)*11);
                    brush.setColor(Color.argb(alpha,
                        clampByte(Color.red(base)+variation),
                        clampByte(Color.green(base)+variation),
                        clampByte(Color.blue(base)+variation)));
                    float vx=(float)Math.cos(dir)*len*.5f;
                    float vy=(float)Math.sin(dir)*len*.5f;
                    canvas.drawLine(x-vx,y-vy,x+vx,y+vy,brush);
                }
            }
        }
        canvas.restore();
    }

    /**
     * Real tonal underdrawing plus fine graphite edge direction and crosshatch.
     * Contours are taken from the supplied photograph's actual light field.
     */
    private static void charcoal(Canvas canvas,Bitmap image,RectF dest,ImageStructure ref,long seed){
        Paint under=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        under.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
            .30f,.59f,.11f,0,34,
            .30f,.59f,.11f,0,30,
            .30f,.59f,.11f,0,22,
            0,0,0,1,0
        })));
        under.setAlpha(180);
        canvas.drawBitmap(image,null,dest,under);
        under.setColorFilter(null);
        Paint line=new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        SplittableRandom rand=new SplittableRandom(seed^0xBB67AE8584CAA73BL);
        canvas.save();
        canvas.clipRect(dest);
        final int cols=150;
        final int rows=Math.max(2,Math.min(240,Math.round(cols*dest.height()/Math.max(1f,dest.width()))));
        float dx=dest.width()/cols,dy=dest.height()/rows;
        for(int y=1;y<rows-1;y++){
            for(int x=1;x<cols-1;x++){
                float u=(x+.42f+(float)(rand.nextDouble()-.5)*.42f)/cols;
                float v=(y+.42f+(float)(rand.nextDouble()-.5)*.42f)/rows;
                int ix=Math.min(ref.width()-1,(int)(u*(ref.width()-1)));
                int iy=Math.min(ref.height()-1,(int)(v*(ref.height()-1)));
                float darkness=1f-ref.luminance(ix,iy);
                float edge=ref.edge(ix,iy);
                if(darkness<.25f&&edge<.17f)continue;
                float px=dest.left+u*dest.width();
                float py=dest.top+v*dest.height();
                if(edge>.12f){
                    float angle=ref.contourAngle(ix,iy);
                    float span=Math.min(dx,dy)*(edge>.58f?1.25f:.72f);
                    line.setStrokeWidth(Math.max(.38f,Math.min(dx,dy)*.23f));
                    line.setColor(Color.argb(Math.min(209,70+(int)(edge*140)),
                        45,39,34));
                    float vx=(float)Math.cos(angle)*span;
                    float vy=(float)Math.sin(angle)*span;
                    canvas.drawLine(px-vx,py-vy,px+vx,py+vy,line);
                }
                if(darkness>.32f){
                    line.setColor(Color.argb(
                        Math.min(132,12+(int)((darkness-.28f)*115)),49,42,37));
                    line.setStrokeWidth(Math.max(.35f,Math.min(dx,dy)*.14f));
                    float span=Math.min(dx,dy)*(.42f+darkness*.6f);
                    canvas.drawLine(px-span,py-span*.3f,px+span,py+span*.3f,line);
                    if(darkness>.69f){
                        canvas.drawLine(px-span*.4f,py+span*.5f,
                            px+span*.4f,py-span*.5f,line);
                    }
                }
            }
        }
        canvas.restore();
    }

    private static void archivalFinish(Canvas canvas,RectF dest,Mode mode){
        Paint border=new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Math.max(1f,Math.min(dest.width(),dest.height())*.005f));
        border.setColor(mode==Mode.CHARCOAL?0x4E5A4C40:0x34634732);
        canvas.drawRect(dest,border);
        border.setStyle(Paint.Style.FILL);
        border.setShader(new LinearGradient(0,0,0,canvas.getHeight(),
            new int[]{0x05FFF7E7,0x00000000,0x0C71563B},null,Shader.TileMode.CLAMP));
        canvas.drawRect(dest,border);
        border.setShader(null);
    }

    private static int clampByte(int n){return Math.max(0,Math.min(255,n));}
    private static float clamp01(float n){return Math.max(0f,Math.min(1f,n));}
}
