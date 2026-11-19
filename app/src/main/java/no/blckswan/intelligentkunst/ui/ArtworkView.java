package no.blckswan.intelligentkunst.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.render.RenderEngine;

/** Responsive, cached gallery view: render work happens off the UI thread. */
public final class ArtworkView extends View {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicInteger version = new AtomicInteger();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap bitmap;
    private Bitmap previous;
    private Artwork artwork;
    private long fadeStart;
    private float pointerX=-1,pointerY=-1;
    private boolean disposed=false;

    public ArtworkView(Context context) { super(context); setLayerType(View.LAYER_TYPE_HARDWARE, null); }

    public void setArtwork(Artwork art) {
        artwork=art;
        renderAsync();
    }

    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        renderAsync();
    }

    private void renderAsync() {
        final Artwork work = artwork;
        final int w=getWidth(), h=getHeight();
        final int token=version.incrementAndGet();
        if(work==null||w==0||h==0||disposed)return;
        worker.execute(()->{
            Bitmap fresh=null;
            try {
                fresh=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                new RenderEngine().draw(new Canvas(fresh),w,h,work);
                final Bitmap ready=fresh;
                post(()->{
                    if(disposed||token!=version.get()) {ready.recycle();return;}
                    if(previous!=null)previous.recycle();
                    previous=bitmap;
                    bitmap=ready;
                    fadeStart=SystemClock.uptimeMillis();
                    invalidate();
                });
            } catch(OutOfMemoryError err) {
                if(fresh!=null)fresh.recycle();
            } catch(RuntimeException err) {
                if(fresh!=null)fresh.recycle();
            }
        });
    }

    @Override protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFF080A10);
        long elapsed=SystemClock.uptimeMillis()-fadeStart;
        float t=Math.min(1f,elapsed/750f);
        if(previous!=null&&t<1) {
            bitmapPaint.setAlpha(255);
            canvas.drawBitmap(previous,0,0,bitmapPaint);
        }
        if(bitmap!=null) {
            bitmapPaint.setAlpha((int)(255*(previous==null?1:t)));
            canvas.drawBitmap(bitmap,0,0,bitmapPaint);
        }
        if(previous!=null&&t>=1){ previous.recycle(); previous=null; }
        if(t<1&&previous!=null)postInvalidateOnAnimation();
        if(pointerX>=0) {
            float radius=Math.min(getWidth(),getHeight())*.17f;
            glowPaint.setShader(new RadialGradient(pointerX,pointerY,radius,
                new int[]{0x30FFFFFF,0x09FFFFFF,0x00FFFFFF},null,Shader.TileMode.CLAMP));
            canvas.drawCircle(pointerX,pointerY,radius,glowPaint);
            glowPaint.setShader(null);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        switch(e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_MOVE:
                pointerX=e.getX();pointerY=e.getY();invalidate();return true;
            case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:
                pointerX=-1;pointerY=-1;invalidate();performClick();return true;
            default:return super.onTouchEvent(e);
        }
    }
    @Override public boolean performClick(){super.performClick();return true;}

    public void dispose() {
        disposed=true;
        version.incrementAndGet();
        worker.shutdownNow();
        if(bitmap!=null){bitmap.recycle();bitmap=null;}
        if(previous!=null){previous.recycle();previous=null;}
    }
}
