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
 * A photograph is a reference, NOT an invented photorealistic AI scene.
 * The photo mode reproduces its actual pixels without texture synthesis.
 * Classical studies are on-device material interpretations of that reference.
 */
public final class PhotoPainter {
    public enum Mode { PHOTO, OIL, CHARCOAL, SEPIA }
    private PhotoPainter() {}

    /** Decode orientation-corrected image with bounded memory usage, API 29+. */
    public static Bitmap decode(Context context,Uri uri,int maxDimension)throws IOException{
        if(maxDimension<128||maxDimension>4096)throw new IOException("Invalid decoding limit");
        ImageDecoder.Source source=ImageDecoder.createSource(context.getContentResolver(),uri);
        return ImageDecoder.decodeBitmap(source,(decoder,info,src)->{
            int width=info.getSize().getWidth(),height=info.getSize().getHeight();
            if(width<1||height<1)throw new IOException("Empty image");
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
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        float scale=Math.min((float)width/image.getWidth(),(float)height/image.getHeight());
        float imgW=image.getWidth()*scale,imgH=image.getHeight()*scale;
        RectF dest=new RectF((width-imgW)*.5f,(height-imgH)*.5f,
            (width+imgW)*.5f,(height+imgH)*.5f);
        if(mode==Mode.SEPIA){
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                .393f,.769f,.189f,0,0,
                .349f,.686f,.168f,0,0,
                .272f,.534f,.131f,0,0,
                0,0,0,1,0
            })));
        }else if(mode==Mode.CHARCOAL){
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                .30f,.59f,.11f,0,0,
                .29f,.58f,.11f,0,3,
                .25f,.52f,.10f,0,12,
                0,0,0,1,0
            })));
        }else if(mode==Mode.OIL){
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                1.12f,-.02f,-.02f,0,-5,
                -.02f,1.08f,-.02f,0,-4,
                -.03f,.01f,1.04f,0,-3,
                0,0,0,1,0
            })));
        }
        canvas.drawBitmap(image,null,dest,paint);
        paint.setColorFilter(null);
        if(mode==Mode.OIL){
            // Coarse, colored brush marks following the actual reference pixels.
            // Drawn only within the photograph; never hallucinate missing details.
            int grid=42;
            SplittableRandom rng=new SplittableRandom(seed);
            Paint brush=new Paint(Paint.ANTI_ALIAS_FLAG);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeWidth(Math.max(2f,Math.min(imgW,imgH)/120f));
            float stepX=imgW/grid;
            float stepY=Math.max(2,stepX*1.16f);
            for(float y=stepY/2;y<imgH;y+=stepY){
                for(float x=stepX/2;x<imgW;x+=stepX){
                    float px=Math.max(0,Math.min(imgW-1,x+(float)(rng.nextDouble()-.5)*stepX*.7f));
                    float py=Math.max(0,Math.min(imgH-1,y+(float)(rng.nextDouble()-.5)*stepY*.7f));
                    int sx=Math.max(0,Math.min(image.getWidth()-1,(int)(px/imgW*image.getWidth())));
                    int sy=Math.max(0,Math.min(image.getHeight()-1,(int)(py/imgH*image.getHeight())));
                    int color=image.getPixel(sx,sy);
                    brush.setColor(Color.argb(105,Color.red(color),Color.green(color),Color.blue(color)));
                    float bx=dest.left+px,by=dest.top+py;
                    float len=stepX*(.3f+(float)rng.nextDouble()*.65f);
                    canvas.drawLine(bx-len*.35f,by-len*.2f,
                        Math.min(dest.right,bx+len*.55f),Math.min(dest.bottom,by+len*.2f),brush);
                }
            }
        }
        if(mode!=Mode.PHOTO){
            // A subtle, opt-in archival vignette; photo mode remains untouched.
            Paint vignette=new Paint(Paint.ANTI_ALIAS_FLAG);
            vignette.setStyle(Paint.Style.STROKE);
            vignette.setStrokeWidth(Math.max(1,width*.014f));
            vignette.setColor(mode==Mode.CHARCOAL?0x5072634E:0x30634732);
            canvas.drawRect(dest,vignette);
            vignette.setStyle(Paint.Style.FILL);
            vignette.setShader(new LinearGradient(0,0,0,height,
                new int[]{0x06E9D5A9,0x00000000,0x11A48555},null,Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,width,height,vignette);
        }
        canvas.restore();
    }
}
