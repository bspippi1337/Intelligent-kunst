package no.blckswan.intelligentkunst.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.util.Locale;
import java.util.SplittableRandom;

import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.core.StoryPlan;

/**
 * Conceptual studies of everyday objects under impossible physical rules.
 *
 * Seven consequences x eight ordinary subjects x three kinds of space.
 * The painter never uses the user's history, songs, characters or associations.
 * A concept must visibly do something to the subject, not merely title a pattern.
 * Native Android Canvas, seeded, offline and resolution independent.
 */
public final class StoryPainter {
    private static final int[][] PAPER = {
        {0xFFEEE5D3,0xFF1E2A29,0xFFB75E4B,0xFF74958D,0xFFF4CB83},
        {0xFFF1E8DF,0xFF34313C,0xFFAB4E68,0xFF9B8FA6,0xFFDAAC75},
        {0xFFEDEBDE,0xFF2C3939,0xFFBA7952,0xFF91A79C,0xFFECC69B},
        {0xFFF5E9D6,0xFF1D303E,0xFFDE8265,0xFF95B2B9,0xFFF2DCA9},
        {0xFFE6DDD6,0xFF333C35,0xFF986C52,0xFF9EAF9E,0xFFD8B77F},
        {0xFFE8E5DA,0xFF292B37,0xFF9C698E,0xFF99A3AB,0xFFD4BF93},
        {0xFFF3EDE1,0xFF30342E,0xFFB96D4E,0xFFA2AFA1,0xFFE9CA82},
        {0xFFE9E0D2,0xFF303844,0xFFBF715D,0xFF94A8AD,0xFFE6C49E}
    };
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();

    public void draw(Canvas c,int width,int height,Artwork art) {
        if(art==null||width<1||height<1)return;
        float h=height/(float)width;
        StoryPlan.Idea idea=StoryPlan.generate(art.seed);
        int[] pal=PAPER[art.palette%PAPER.length];
        int paper=pal[0], ink=pal[1], accent=pal[2], mist=pal[3], pale=pal[4];
        SplittableRandom rng=new SplittableRandom(art.seed);
        p.reset();p.setAntiAlias(true);
        c.save();
        c.clipRect(0,0,width,height);
        c.scale(width,width);
        paper(c,h,paper,mist,ink,rng);
        room(c,h,idea,ink,paper,mist,pale);
        concept(c,h,idea,ink,paper,accent,mist,pale,rng);
        texture(c,h,paper,ink,rng);
        caption(c,h,art,idea,ink,accent);
        c.restore();
    }

    private void paper(Canvas c,float h,int paper,int mist,int ink,SplittableRandom rng){
        fill(paper,255);
        c.drawRect(0,0,1,h,p);
        p.setShader(new LinearGradient(0,0,1,h,
            new int[]{paper,tint(paper,mist,.13f),paper},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,1,h,p);p.setShader(null);
        // Grain never becomes the subject: quiet imperfection of paper/ink.
        fill(ink,11);
        for(int i=0;i<900;i++){
            float x=(float)rng.nextDouble(),y=(float)rng.nextDouble()*h;
            c.drawCircle(x,y,.0004f+(float)rng.nextDouble()*.0007f,p);
        }
    }

    private void room(Canvas c,float h,StoryPlan.Idea idea,int ink,int paper,int mist,int pale){
        float floor=h*.73f;
        switch(idea.space) {
            case WHITE_ROOM:
                fill(mist,37);
                quad(c,0,floor,1,floor-.034f,1,h,0,h);
                stroke(ink,70,.0016f);
                c.drawLine(0,floor,.99f,floor-.03f,p);
                stroke(ink,35,.001f);
                c.drawLine(.16f,h*.18f,.16f,floor,p);
                break;
            case NOCTURNE:
                fill(ink,235);
                c.drawRect(0,h*.17f,1,h*.85f,p);
                glow(c,.49f,h*.47f,.40f,pale,.32f);
                fill(paper,45);
                quad(c,0,floor,1,floor,1,h*.85f,0,h*.85f);
                stroke(pale,85,.0015f);
                c.drawLine(.06f,floor,.94f,floor,p);
                break;
            case ARCHIVE:
                stroke(ink,48,.0019f);
                c.drawRect(.09f,h*.18f,.91f,h*.79f,p);
                c.drawLine(.09f,floor,.91f,floor,p);
                c.drawLine(.10f,h*.79f,.91f,h*.79f,p);
                // Architecture is a datum, not an endless grid.
                fill(mist,50);
                c.drawRect(.12f,h*.2f,.14f,h*.73f,p);
                c.drawRect(.86f,h*.2f,.88f,h*.73f,p);
                break;
        }
    }

    private void concept(Canvas c,float h,StoryPlan.Idea idea,
                         int ink,int paper,int accent,int mist,int pale,SplittableRandom rng){
        final float stageX=.51f+(float)(idea.displacement-.5)*.13f;
        final float stageY=h*.505f+(float)(idea.openness-.5)*h*.028f;
        final float size=(float)idea.scale*.52f;
        // A single subject is large enough to read at thumbnail size.
        // The selected physical impossibility changes its physical rendering.
        c.save();
        c.translate(stageX,stageY);
        c.rotate((float)idea.lean*20f);
        c.scale(size,size);
        StoryPlan.ObjectKind o=idea.object;
        switch(idea.proposition){
            case INDEPENDENT_SHADOW:
                c.save();c.translate(.35f,-.23f);
                drawObject(c,o,ink,paper,accent,69,true);
                c.restore();
                stroke(accent,175,.003f);
                p.setPathEffect(new android.graphics.DashPathEffect(new float[]{.011f,.013f},0));
                c.drawLine(.04f,-.02f,.26f,-.17f,p);
                p.setPathEffect(null);
                drawObject(c,o,ink,paper,accent,255,false);
                break;
            case UNFINISHED:
                drawObject(c,o,ink,paper,accent,255,false);
                // The missing fragment is material negative space, bounded by a clear seam.
                fill(paper,255);
                quad(c,-.16f,-.05f,.16f,-.06f,.25f,.08f,-.18f,.10f);
                stroke(accent,212,.006f);
                c.drawLine(-.18f,.10f,.25f,.08f,p);
                for(int i=0;i<7;i++){
                    float x=-.16f+i*.063f;
                    c.drawLine(x,.11f,x+.012f,.13f,p);
                }
                break;
            case AFTERIMAGE:
                for(int i=3;i>=1;i--){
                    c.save();c.translate(-.15f*i,.09f*i);
                    drawObject(c,o,mist,paper,accent,45+i*13,true);
                    c.restore();
                }
                drawObject(c,o,ink,paper,accent,255,false);
                break;
            case LEVITATION:
                c.save();c.translate(0,-.22f);
                drawObject(c,o,ink,paper,accent,255,false);
                c.restore();
                stroke(accent,185,.003f);
                p.setPathEffect(new android.graphics.DashPathEffect(new float[]{.025f,.021f},0));
                c.drawLine(-.20f,.30f,-.20f,.67f,p);
                c.drawLine(.22f,.30f,.22f,.67f,p);
                p.setPathEffect(null);
                fill(ink,45);
                c.drawOval(new RectF(-.40f,.75f,.42f,.85f),p);
                break;
            case SEPARATION:
                // Two clipped halves physically diverge, joined by repair marks.
                c.save();c.clipRect(-1.25f,-1.25f,1.25f,0);
                c.translate(-.075f,-.09f);
                drawObject(c,o,ink,paper,accent,255,false);c.restore();
                c.save();c.clipRect(-1.25f,0,1.25f,1.25f);
                c.translate(.08f,.10f);
                drawObject(c,o,ink,paper,accent,255,false);c.restore();
                stroke(accent,235,.008f);
                for(int i=0;i<6;i++){
                    float x=-.28f+i*.11f;
                    c.drawLine(x-.016f,-.037f,x+.016f,.042f,p);
                }
                break;
            case OVERFLOW:
                drawObject(c,o,ink,paper,accent,255,false);
                // An opening produces an increasing swarm of original contents.
                for(int i=0;i<22;i++){
                    float t=i/22f;
                    float x=-.15f+(float)rng.nextDouble()*.34f + .21f*t;
                    float y=-.19f-.43f*t-(float)rng.nextDouble()*.11f;
                    float rad=.009f+t*.022f;
                    fill(i%3==0?accent:pale,190-i*4);
                    if(i%2==0)c.drawCircle(x,y,rad,p);
                    else c.drawRect(x-rad,y-rad,x+rad,y+rad,p);
                }
                break;
            case SCALE:
                drawObject(c,o,ink,paper,accent,255,false);
                // A legible measurement system disagrees with the subject.
                stroke(accent,225,.004f);
                c.drawLine(.58f,-.50f,.58f,.49f,p);
                for(int i=0;i<15;i++){
                    float yy=-.50f+i*.071f;
                    c.drawLine(.58f,yy,.58f+(i%3==0?.095f:.04f),yy,p);
                }
                fill(accent,230);
                p.setTypeface(Typeface.create("monospace",Typeface.NORMAL));
                p.setTextSize(.055f);
                c.drawText("1:?",.65f,-.46f,p);
                break;
        }
        c.restore();
        // Register marks are deliberately absent: the work is not a UI diagram.
        stroke(ink,50,.0017f);
        c.drawLine(.08f,h*.85f,.92f,h*.85f,p);
    }

    private void drawObject(Canvas c,StoryPlan.ObjectKind kind,
                            int ink,int paper,int accent,int opacity,boolean outline) {
        c.save();
        switch(kind){
            case CHAIR:
                if(outline){
                    stroke(ink,opacity,.065f);
                    c.drawLine(-.30f,-.36f,-.30f,.21f,p);
                    c.drawLine(.19f,-.36f,.19f,.21f,p);
                    c.drawLine(-.34f,.15f,.27f,.15f,p);
                    c.drawLine(-.23f,.19f,-.30f,.52f,p);
                    c.drawLine(.20f,.19f,.28f,.52f,p);
                }else{
                    fill(ink,opacity);
                    c.drawRoundRect(new RectF(-.33f,-.48f,.25f,-.27f),.045f,.045f,p);
                    c.drawRoundRect(new RectF(-.37f,-.27f,-.28f,.17f),.017f,.017f,p);
                    c.drawRoundRect(new RectF(.20f,-.27f,.28f,.17f),.017f,.017f,p);
                    quad(c,-.40f,.12f,.27f,.12f,.35f,.24f,-.33f,.24f);
                    quad(c,-.32f,.21f,-.23f,.21f,-.27f,.60f,-.37f,.60f);
                    quad(c,.22f,.21f,.31f,.21f,.37f,.60f,.29f,.60f);
                    fill(accent,opacity*.88f);
                    c.drawRect(-.26f,-.45f,-.19f,-.31f,p);
                }
                break;
            case CUP:
                fill(ink,opacity);
                c.drawOval(new RectF(.10f,-.27f,.48f,.21f),p);
                fill(paper,opacity);
                c.drawOval(new RectF(.19f,-.18f,.39f,.12f),p);
                fill(ink,opacity);
                path.reset();path.moveTo(-.33f,-.29f);
                path.lineTo(.24f,-.29f);path.lineTo(.16f,.44f);
                path.quadTo(-.06f,.53f,-.24f,.44f);
                path.close();c.drawPath(path,p);
                fill(accent,opacity*.7f);
                c.drawOval(new RectF(-.34f,-.38f,.25f,-.20f),p);
                stroke(paper,opacity*.8f,.019f);
                c.drawLine(-.23f,-.16f,-.19f,.34f,p);
                break;
            case LADDER:
                stroke(ink,opacity,.070f);
                p.setStrokeCap(Paint.Cap.ROUND);
                c.drawLine(-.26f,-.55f,-.38f,.59f,p);
                c.drawLine(.20f,-.55f,.31f,.59f,p);
                stroke(accent,opacity,.040f);
                for(int i=0;i<7;i++){
                    float yy=-.44f+i*.15f;
                    c.drawLine(-.27f+(yy+.55f)*-.09f,yy,
                        .20f+(yy+.55f)*.08f,yy,p);
                }
                break;
            case SUITCASE:
                fill(ink,opacity);
                c.drawRoundRect(new RectF(-.44f,-.31f,.42f,.52f),.11f,.11f,p);
                stroke(ink,opacity,.055f);
                c.drawRoundRect(new RectF(-.12f,-.48f,.12f,-.26f),.04f,.04f,p);
                fill(accent,opacity*.83f);
                c.drawRoundRect(new RectF(-.34f,-.22f,-.25f,.45f),.028f,.028f,p);
                c.drawRoundRect(new RectF(.23f,-.22f,.32f,.45f),.028f,.028f,p);
                fill(paper,opacity);
                c.drawRect(-.045f,.02f,.045f,.13f,p);
                stroke(paper,opacity*.65f,.011f);
                c.drawRoundRect(new RectF(-.37f,-.25f,.35f,.47f),.05f,.05f,p);
                break;
            case LAMP:
                fill(ink,opacity);
                c.drawOval(new RectF(-.34f,.45f,.32f,.59f),p);
                stroke(ink,opacity,.061f);
                c.drawLine(-.02f,.48f,.11f,-.13f,p);
                c.drawLine(.11f,-.13f,-.01f,-.31f,p);
                fill(ink,opacity);
                quad(c,-.31f,-.40f,.28f,-.40f,.40f,-.12f,-.43f,-.12f);
                fill(accent,opacity);
                c.drawOval(new RectF(-.39f,-.15f,.36f,-.09f),p);
                stroke(paper,opacity*.65f,.012f);
                c.drawLine(-.23f,-.34f,.18f,-.34f,p);
                break;
            case BOOK:
                fill(ink,opacity);
                quad(c,-.42f,-.32f,.28f,-.39f,.46f,.30f,-.24f,.42f);
                fill(paper,opacity);
                quad(c,-.36f,-.23f,.23f,-.30f,.38f,.27f,-.21f,.35f);
                fill(accent,opacity*.88f);
                quad(c,-.42f,-.32f,-.36f,-.23f,-.21f,.35f,-.24f,.42f);
                stroke(ink,opacity*.72f,.012f);
                for(int i=0;i<5;i++){
                    float yy=-.13f+i*.08f;
                    c.drawLine(-.18f,yy,.17f,yy-.04f,p);
                }
                break;
            case UMBRELLA:
                stroke(ink,opacity,.032f);
                c.drawLine(0,-.16f,.05f,.45f,p);
                path.reset();path.moveTo(.05f,.45f);
                path.cubicTo(.07f,.70f,.30f,.63f,.28f,.47f);
                c.drawPath(path,p);
                fill(ink,opacity);
                path.reset();path.moveTo(-.47f,-.14f);
                path.quadTo(-.11f,-.81f,.13f,-.68f);
                path.quadTo(.41f,-.63f,.49f,-.14f);
                path.quadTo(.29f,-.25f,.13f,-.14f);
                path.quadTo(-.04f,-.26f,-.19f,-.14f);
                path.quadTo(-.32f,-.26f,-.47f,-.14f);path.close();
                c.drawPath(path,p);
                stroke(accent,opacity*.9f,.014f);
                c.drawLine(.02f,-.70f,.03f,-.16f,p);
                break;
            case TEAPOT:
                fill(ink,opacity);
                c.drawOval(new RectF(-.37f,-.26f,.32f,.48f),p);
                fill(ink,opacity);
                c.drawOval(new RectF(-.50f,-.13f,-.18f,.27f),p);
                fill(paper,opacity);
                c.drawOval(new RectF(-.42f,-.04f,-.24f,.19f),p);
                fill(ink,opacity);
                path.reset();path.moveTo(.26f,-.14f);
                path.quadTo(.52f,-.28f,.50f,-.43f);
                path.lineTo(.61f,-.43f);
                path.quadTo(.64f,-.14f,.35f,.13f);path.close();
                c.drawPath(path,p);
                fill(accent,opacity);
                c.drawOval(new RectF(-.19f,-.37f,.18f,-.23f),p);
                c.drawCircle(0,-.42f,.047f,p);
                stroke(paper,opacity*.53f,.021f);
                c.drawArc(new RectF(-.24f,-.13f,.14f,.30f),120,95,false,p);
                break;
        }
        c.restore();
    }

    private void caption(Canvas c,float h,Artwork art,StoryPlan.Idea idea,
                         int ink,int accent){
        fill(ink,234);
        p.setTypeface(Typeface.create("serif",Typeface.BOLD));
        String heading=idea.proposition.title;
        p.setTextSize(.046f);
        while(p.measureText(heading)>.88f&&p.getTextSize()>.025f)p.setTextSize(p.getTextSize()*.92f);
        c.drawText(heading,.065f,h*.091f,p);
        fill(accent,230);
        p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(.019f);
        c.drawText(idea.object.title+"   /   "+idea.space.name().replace('_',' '),.065f,h*.116f,p);
        stroke(ink,125,.0015f);
        c.drawLine(.065f,h*.136f,.935f,h*.136f,p);
        c.drawLine(.065f,h*.892f,.935f,h*.892f,p);
        fill(ink,170);
        p.setTextSize(.019f);p.setTypeface(Typeface.create("monospace",Typeface.NORMAL));
        c.drawText("STUDIE  "+art.id().substring(0,8),.065f,h*.920f,p);
        p.setTextSize(.016f);
        c.drawText(String.format(Locale.ROOT,"%02d / %02d",
            idea.object.ordinal()+1,idea.proposition.ordinal()+1),.82f,h*.920f,p);
    }

    private void texture(Canvas c,float h,int paper,int ink,SplittableRandom rng){
        // Independent noisy paper texture, visually subordinate to the subject.
        fill(paper,22);
        for(int i=0;i<460;i++){
            float x=(float)rng.nextDouble(),y=(float)rng.nextDouble()*h;
            c.drawCircle(x,y,.0006f,p);
        }
        stroke(ink,16,.0006f);
        for(int i=0;i<110;i++){
            float x=(float)rng.nextDouble(),y=(float)rng.nextDouble()*h;
            c.drawLine(x,y,x+.005f,y,p);
        }
    }

    private void glow(Canvas c,float x,float y,float radius,int col,float alpha){
        p.setShader(new RadialGradient(x,y,radius,
            new int[]{Color.argb((int)(255*alpha),Color.red(col),Color.green(col),Color.blue(col)),
                Color.TRANSPARENT},null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,radius,p);p.setShader(null);
    }
    private void quad(Canvas c,float x1,float y1,float x2,float y2,
                      float x3,float y3,float x4,float y4){
        path.reset();path.moveTo(x1,y1);path.lineTo(x2,y2);
        path.lineTo(x3,y3);path.lineTo(x4,y4);path.close();c.drawPath(path,p);
    }
    private void fill(int color,float alpha){
        p.setShader(null);p.setPathEffect(null);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(Math.max(0,Math.min(255,(int)alpha)),
            Color.red(color),Color.green(color),Color.blue(color)));
    }
    private void stroke(int color,float alpha,float width){
        fill(color,alpha);p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }
    private static int tint(int a,int b,float t){
        return Color.rgb((int)(Color.red(a)*(1-t)+Color.red(b)*t),
            (int)(Color.green(a)*(1-t)+Color.green(b)*t),
            (int)(Color.blue(a)*(1-t)+Color.blue(b)*t));
    }
}
