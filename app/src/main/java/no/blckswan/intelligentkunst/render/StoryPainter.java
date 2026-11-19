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

import java.util.SplittableRandom;

import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.core.StoryPlan;

/**
 * Deliberate, subject-led illustration in the tradition of limited-palette
 * relief prints, book covers and theatrical set design.
 *
 * Four authored narrative archetypes. The genome controls viewpoint, weather,
 * focus, placement and mark-making; it never merely rearranges screensaver lines.
 * Pure Android Canvas, deterministic, offline, resolution independent.
 */
public final class StoryPainter {
    private static final int[][] INKS={
        {0xFFE7DEC9,0xFF22312C,0xFFC27A50,0xFF82998B,0xFFF4C88C},
        {0xFF101C24,0xFFE9DFCC,0xFFDC9162,0xFF436570,0xFFFFD8A0},
        {0xFFEADDCF,0xFF363833,0xFFB75844,0xFF859281,0xFFF6CA89},
        {0xFFEEE7D7,0xFF29313D,0xFFAB6F63,0xFF819DAA,0xFFF8D49A},
        {0xFF14212B,0xFFE9DBC5,0xFFBD8D64,0xFF485F65,0xFFFFD6A5},
        {0xFFE8D9C6,0xFF46362E,0xFFBB6750,0xFF83918B,0xFFFFD48E}
    };

    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path=new Path();

    public void draw(Canvas canvas,int width,int height,Artwork art) {
        if(art==null||width<=0||height<=0)return;
        canvas.save();
        canvas.clipRect(0,0,width,height);
        canvas.scale(width,width);  // uniform scaling keeps fonts, suns and bodies undistorted
        float h=(float)height/width;
        int[] pal=INKS[Math.floorMod(art.palette,INKS.length)];
        int paper=pal[0],ink=pal[1],rust=pal[2],mist=pal[3],light=pal[4];
        SplittableRandom rng=new SplittableRandom(art.seed);
        reset();
        p.setShader(new LinearGradient(0,0,.12f,h,
            new int[]{paper,mix(paper,mist,.16f),paper},null,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,1,h,p);p.setShader(null);
        // The same composition can be daylight or nocturne, not neon gradients.
        wash(canvas,.58f,.35f*h,.65f,light,.11f);
        wash(canvas,.17f,.75f*h,.50f,rust,.055f);
        switch(StoryPlan.scene(art.seed)){
            case 0: lighthouse(canvas,h,ink,rust,mist,light,art,rng);break;
            case 1: doorway(canvas,h,ink,rust,mist,light,art,rng);break;
            case 2: tree(canvas,h,ink,rust,mist,light,art,rng);break;
            default: whale(canvas,h,ink,rust,mist,light,art,rng);break;
        }
        etchedPaper(canvas,h,ink,paper,rng);
        typography(canvas,h,art,ink,rust);
        canvas.restore();
    }

    private void lighthouse(Canvas c,float h,int ink,int rust,int mist,int light,
                            Artwork a,SplittableRandom r) {
        float horizon=h*(.60f+(float)(a.drift-.5)*.065f);
        float cx=.73f-(float)a.balance*.13f,cy=h*.29f;
        sun(c,cx,cy,.126f,light,rust);
        // Diagonal island mass and quiet open sea keep the focal tower readable.
        paint(mist,175,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,horizon*.95f);
        path.cubicTo(.21f,horizon*.78f,.35f,horizon*.91f,.49f,horizon*.98f);
        path.cubicTo(.64f,horizon*1.04f,.89f,horizon*.89f,1,horizon*.99f);
        path.lineTo(1,horizon+.06f);path.lineTo(0,horizon+.06f);path.close();c.drawPath(path,p);
        // Sea as a few solid planes, not an all-over waveform.
        paint(ink,220,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,horizon+.018f);
        path.cubicTo(.30f,horizon+.005f,.55f,horizon+.028f,1,horizon+.005f);
        path.lineTo(1,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        paint(mist,150,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,horizon+.025f);
        path.cubicTo(.25f,horizon-.015f,.47f,horizon+.03f,.58f,horizon+.03f);
        path.lineTo(.52f,horizon+.11f);path.lineTo(0,horizon+.14f);path.close();c.drawPath(path,p);
        float tx=.30f+(float)a.balance*.18f,foot=horizon+.018f,top=h*.30f;
        // Beacon beam is an act of narrative attention.
        paint(light,32,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(tx,top+.008f);
        path.lineTo(.96f,h*.19f);path.lineTo(.96f,h*.39f);path.close();c.drawPath(path,p);
        // Actual tower has a silhouette, light room, cap and a careful vertical rhythm.
        paint(ink,255,Paint.Style.FILL,.001f);
        polygon(c,new float[]{tx-.060f,foot,tx-.044f,top+.052f,tx+.037f,top+.052f,
            tx+.058f,foot});
        paint(rust,255,Paint.Style.FILL,.001f);
        c.drawRect(tx-.051f,top+.055f,tx+.045f,top+.078f,p);
        paint(ink,255,Paint.Style.FILL,.001f);
        polygon(c,new float[]{tx-.075f,top+.05f,tx,top+.008f,tx+.072f,top+.05f});
        c.drawRect(tx-.063f,top+.079f,tx+.059f,top+.091f,p);
        paint(light,255,Paint.Style.FILL,.001f);
        c.drawRect(tx-.034f,top+.053f,tx+.029f,top+.079f,p);
        for(int i=0;i<3;i++){
            float wy=top+.143f+i*.083f;
            paint(light,205,Paint.Style.FILL,.001f);
            c.drawRoundRect(new RectF(tx-.013f,wy,tx+.013f,wy+.025f),.004f,.004f,p);
        }
        paint(ink,255,Paint.Style.FILL,.001f); c.drawOval(new RectF(tx-.083f,foot-.019f,tx+.093f,foot+.026f),p);
        // Engraved water is reserved for the foreground and reflected beacon.
        for(int i=0;i<72;i++){
            float y=horizon+.10f+(float)r.nextDouble()*(h-horizon-.18f);
            float x=(float)r.nextDouble();
            float len=.014f+(float)r.nextDouble()*.105f;
            boolean reflection=Math.abs(x-tx)<.085f;
            paint(reflection?light:mist,reflection?120:58,Paint.Style.STROKE,
                reflection?.003f:.0018f);
            c.drawLine(x,y,Math.min(1,x+len),y-(float)r.nextDouble()*.005f,p);
        }
        birds(c,.73f,h*.44f,ink,r,5);
    }

    private void doorway(Canvas c,float h,int ink,int rust,int mist,int light,
                         Artwork a,SplittableRandom r) {
        float base=h*.67f,doorX=.56f+(float)(a.balance-.5)*.17f;
        sun(c,.19f,h*.31f,.082f,light,rust);
        // Rocky masses lean inwards, framing the impossible opening.
        paint(mist,155,Paint.Style.FILL,.001f);
        polygon(c,new float[]{0,h*.43f,.19f,h*.36f,.36f,h*.47f,.34f,base,0,base});
        paint(ink,245,Paint.Style.FILL,.001f);
        polygon(c,new float[]{.85f,h*.30f,1,h*.38f,1,base,.81f,base,.78f,h*.48f});
        polygon(c,new float[]{0,base,.36f,base,.23f,h*.89f,0,h});
        polygon(c,new float[]{.75f,base,1,base,1,h,.80f,h*.85f});
        // A painted glow creates negative space within a monumental arch.
        wash(c,doorX,base-.19f,.28f,light,.42f);
        float left=doorX-.105f,right=doorX+.105f,top=base-.39f;
        paint(ink,255,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(left-.027f,base);
        path.lineTo(left-.027f,top+.090f);
        path.cubicTo(left-.03f,top-.055f,right+.032f,top-.055f,right+.027f,top+.09f);
        path.lineTo(right+.027f,base);path.close();c.drawPath(path,p);
        paint(light,255,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(left,base);
        path.lineTo(left,top+.085f);
        path.cubicTo(left,top-.013f,right,top-.013f,right,top+.085f);
        path.lineTo(right,base);path.close();c.drawPath(path,p);
        paint(rust,100,Paint.Style.FILL,.001f);
        polygon(c,new float[]{right-.029f,base,right-.025f,top+.078f,right,top+.078f,right,base});
        paint(ink,110,Paint.Style.STROKE,.003f);
        c.drawLine(doorX,top+.017f,doorX,base,p);
        // Perspective stairs: the human figure has somewhere to move.
        for(int i=0;i<10;i++){
            float t=i/10f,y=base+t*t*h*.235f;
            float extent=.11f+.45f*t;
            paint(ink,110-i*6,Paint.Style.STROKE,.0022f+i*.0002f);
            c.drawLine(doorX-extent,y,doorX+extent,y,p);
        }
        // Tiny solitary human witness.
        person(c,doorX-.20f,base+h*.105f,.033f,ink);
        for(int i=0;i<24;i++){
            float x=(float)r.nextDouble();
            float y=h*.42f+(float)r.nextDouble()*.14f;
            paint(mist,80,Paint.Style.STROKE,.0014f);
            c.drawLine(x,y,x+.012f,y-.01f,p);
        }
        birds(c,.25f,h*.39f,ink,r,3);
    }

    private void tree(Canvas c,float h,int ink,int rust,int mist,int light,
                      Artwork a,SplittableRandom r) {
        float ground=h*.70f,tx=.61f+(float)(a.balance-.5)*.20f;
        sun(c,.26f,h*.34f,.16f,light,rust);
        paint(mist,154,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,ground-.095f);
        path.cubicTo(.21f,ground-.16f,.37f,ground-.06f,.50f,ground-.105f);
        path.cubicTo(.76f,ground-.20f,.87f,ground-.095f,1,ground-.11f);
        path.lineTo(1,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        paint(ink,252,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,ground+.14f);
        path.cubicTo(.36f,ground-.01f,.65f,ground+.07f,1,ground-.02f);
        path.lineTo(1,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        // Woodcut trunk: taper + independent branch structure, not a radial flower.
        float trunkTop=ground-h*.35f;
        paint(ink,255,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(tx-.045f,ground+.04f);
        path.cubicTo(tx-.060f,ground-h*.16f,tx-.005f,ground-h*.22f,tx-.018f,trunkTop);
        path.lineTo(tx+.012f,trunkTop);
        path.cubicTo(tx+.036f,ground-h*.21f,tx+.061f,ground-h*.12f,tx+.045f,ground+.04f);
        path.close();c.drawPath(path,p);
        // Curving branches have coherent growth direction and changing scale.
        for(int b=0;b<22;b++){
            float side=b%2==0?1:-1;
            float origin=ground-h*(.12f+(b/22f)*.24f);
            float spread=(.13f+(float)r.nextDouble()*.18f)*(1-b/45f);
            float endX=tx+side*spread,endY=origin-h*(.08f+(float)r.nextDouble()*.13f);
            paint(ink,240,Paint.Style.STROKE,.012f*(1-b/26f));
            p.setStrokeCap(Paint.Cap.ROUND);
            path.reset();path.moveTo(tx,origin);
            path.cubicTo(tx+side*spread*.25f,origin-.065f,tx+side*spread*.6f,endY+.025f,endX,endY);
            c.drawPath(path,p);
            paint(rust,110,Paint.Style.STROKE,.0025f);
            c.drawLine(endX,endY,endX+side*.045f,endY-.032f,p);
        }
        // Leaves as sparse clustered silhouettes with a deliberate empty halo.
        for(int i=0;i<245;i++){
            float angle=(float)r.nextDouble()*6.2831853f;
            float radial=(float)Math.sqrt(r.nextDouble());
            float x=tx+(float)Math.cos(angle)*radial*(.22f+(float)a.energy*.035f);
            float y=trunkTop-.09f+(float)Math.sin(angle)*radial*h*.105f;
            if(y>ground-h*.12f)continue;
            float size=.0015f+(float)r.nextDouble()*.009f;
            paint(i%5==0?rust:(i%3==0?light:ink),i%5==0?180:120+r.nextInt(135),
                Paint.Style.FILL,.001f);
            c.drawOval(new RectF(x-size,y-size*.5f,x+size,y+size*.5f),p);
        }
        person(c,.22f,ground+.075f,.027f,light);
        paint(mist,80,Paint.Style.STROKE,.0015f);
        for(int i=0;i<30;i++){
            float x=(float)r.nextDouble();
            float y=ground+.1f+(float)r.nextDouble()*(h-ground-.12f);
            c.drawLine(x,y,x+.01f,y,p);
        }
    }

    private void whale(Canvas c,float h,int ink,int rust,int mist,int light,
                       Artwork a,SplittableRandom r) {
        float sea=h*.69f;
        sun(c,.73f,h*.29f,.145f,light,rust);
        paint(mist,135,Paint.Style.FILL,.001f);
        polygon(c,new float[]{0,sea-.10f,.26f,sea-.13f,.51f,sea-.095f,1,sea-.13f,1,sea+.015f,0,sea+.03f});
        paint(ink,238,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(0,sea+.03f);
        path.cubicTo(.26f,sea-.015f,.60f,sea+.03f,1,sea);
        path.lineTo(1,h);path.lineTo(0,h);path.close();c.drawPath(path,p);
        // Whale anatomy reads clearly at thumbnail scale: domed back, rostrum,
        // narrowing tail stock, forked fluke and a ventral fin.
        float wy=h*.43f+(float)(a.drift-.5)*h*.07f;
        c.save();c.translate(.48f,wy);c.rotate((float)(a.balance-.5)*-13f);
        paint(ink,255,Paint.Style.FILL,.001f);
        path.reset();path.moveTo(-.35f,.005f);
        path.cubicTo(-.28f,-.12f,-.02f,-.14f,.22f,-.065f);
        path.cubicTo(.31f,-.035f,.32f,-.025f,.33f,-.025f);
        path.lineTo(.43f,-.06f);
        path.cubicTo(.48f,-.09f,.48f,-.035f,.42f,.005f);
        path.cubicTo(.49f,.05f,.49f,.105f,.42f,.065f);
        path.lineTo(.32f,.02f);
        path.cubicTo(.18f,.11f,-.10f,.12f,-.28f,.067f);
        path.cubicTo(-.35f,.057f,-.37f,.025f,-.35f,.005f);path.close();
        c.drawPath(path,p);
        // Attached flipper has an actual articulated shape.
        path.reset();path.moveTo(.008f,.062f);
        path.cubicTo(-.005f,.11f,-.11f,.245f,-.17f,.235f);
        path.cubicTo(-.19f,.227f,-.065f,.085f,-.057f,.045f);
        path.close();c.drawPath(path,p);
        paint(light,240,Paint.Style.FILL,.001f);
        c.drawCircle(-.247f,.005f,.007f,p);
        paint(rust,160,Paint.Style.STROKE,.0015f);
        path.reset();path.moveTo(-.27f,.038f);path.cubicTo(-.13f,.10f,.09f,.09f,.23f,.008f);c.drawPath(path,p);
        c.restore();
        // A small boat down below is the scale cue and narrative counterpoint.
        float bx=.20f,by=sea-.028f;
        paint(rust,255,Paint.Style.FILL,.001f);
        polygon(c,new float[]{bx-.056f,by,bx+.067f,by,bx+.037f,by+.025f,bx-.035f,by+.025f});
        paint(light,230,Paint.Style.STROKE,.002f);
        c.drawLine(bx,by,bx,by-.10f,p);
        paint(light,190,Paint.Style.FILL,.001f);
        polygon(c,new float[]{bx+.006f,by-.095f,bx+.062f,by-.013f,bx+.006f,by-.013f});
        // A few precisely located wave marks, not a filling pattern.
        for(int i=0;i<65;i++){
            float x=(float)r.nextDouble();
            float y=sea+.06f+(float)r.nextDouble()*(h-sea-.14f);
            float len=.018f+(float)r.nextDouble()*.11f;
            paint(i%6==0?rust:mist,65+i%4*15,Paint.Style.STROKE,.0017f);
            c.drawLine(x,y,Math.min(1,x+len),y,p);
        }
        birds(c,.29f,h*.24f,ink,r,3);
    }

    private void typography(Canvas c,float h,Artwork a,int ink,int rust) {
        // A poster is an authored artifact. Top line and colophon act as framing,
        // not a giant decorative brand overlay.
        paint(ink,235,Paint.Style.FILL,.001f);
        p.setTypeface(Typeface.create("serif",Typeface.BOLD));
        p.setTextSize(.052f);
        c.drawText(StoryPlan.motif(a.seed),.075f,h*.102f,p);
        paint(rust,230,Paint.Style.FILL,.001f);
        p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(.017f);
        c.drawText("INTELLIGENT KUNST   /   STUDIES IN SMALL MYTHS",.075f,h*.124f,p);
        paint(ink,135,Paint.Style.STROKE,.001f);
        c.drawLine(.075f,h*.14f,.925f,h*.14f,p);
        c.drawLine(.075f,h*.88f,.925f,h*.88f,p);
        paint(ink,195,Paint.Style.FILL,.001f);
        p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(.019f);
        c.drawText("SERIE III     /     "+String.format(java.util.Locale.ROOT,"%02d",StoryPlan.scene(a.seed)+1),
            .075f,h*.915f,p);
        paint(rust,175,Paint.Style.FILL,.001f);
        p.setTextSize(.018f);
        c.drawText(a.id().substring(0,8)+"    •    UNIKT AVTRYKK",.51f,h*.915f,p);
    }

    private void sun(Canvas c,float x,float y,float rad,int light,int outline) {
        wash(c,x,y,rad*2.7f,light,.24f);
        paint(light,250,Paint.Style.FILL,.001f);
        c.drawCircle(x,y,rad,p);
        paint(outline,115,Paint.Style.STROKE,.0023f);
        c.drawCircle(x,y,rad,p);
        // A precise broken arc adds an imperfect, hand-pulled print edge.
        paint(outline,110,Paint.Style.STROKE,.0013f);
        c.drawArc(new RectF(x-rad-.014f,y-rad-.014f,x+rad+.014f,y+rad+.014f),
            -35,180,false,p);
    }
    private void birds(Canvas c,float x,float y,int ink,SplittableRandom rng,int count){
        for(int i=0;i<count;i++){
            float xx=x+(float)(rng.nextDouble()-.5)*.23f;
            float yy=y+(float)(rng.nextDouble()-.5)*.11f;
            float s=.012f+(float)rng.nextDouble()*.015f;
            paint(ink,165,Paint.Style.STROKE,.0025f);
            p.setStrokeCap(Paint.Cap.ROUND);
            path.reset();path.moveTo(xx-s,yy-s*.25f);
            path.quadTo(xx-s*.33f,yy-s*.85f,xx,yy);
            path.quadTo(xx+s*.4f,yy-s*.8f,xx+s,yy-s*.2f);
            c.drawPath(path,p);
        }
    }
    private void person(Canvas c,float x,float feet,float scale,int ink){
        paint(ink,255,Paint.Style.FILL,.001f);
        c.drawCircle(x,feet-scale*.9f,scale*.14f,p);
        paint(ink,255,Paint.Style.STROKE,scale*.18f);
        p.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,feet-scale*.72f,x,feet-scale*.31f,p);
        c.drawLine(x,feet-scale*.33f,x-scale*.20f,feet,p);
        c.drawLine(x,feet-scale*.33f,x+scale*.20f,feet,p);
        c.drawLine(x,feet-scale*.64f,x+scale*.24f,feet-scale*.51f,p);
    }
    private void etchedPaper(Canvas c,float h,int ink,int paper,SplittableRandom r){
        paint(ink,15,Paint.Style.FILL,.001f);
        for(int i=0;i<1800;i++){
            float x=(float)r.nextDouble(), y=(float)r.nextDouble()*h;
            float rad=.00035f+(float)r.nextDouble()*.00075f;
            c.drawCircle(x,y,rad,p);
        }
        paint(paper,18,Paint.Style.STROKE,.001f);
        for(int i=0;i<150;i++){
            float x=(float)r.nextDouble(),y=(float)r.nextDouble()*h;
            c.drawLine(x,y,x+(float)r.nextDouble()*.013f,y,p);
        }
    }

    private void wash(Canvas c,float x,float y,float rad,int color,float opacity){
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(x,y,rad,
            new int[]{alpha(color,(int)(opacity*255)),alpha(color,0)},
            null,Shader.TileMode.CLAMP));
        c.drawCircle(x,y,rad,p);p.setShader(null);
    }
    private void polygon(Canvas c,float[] points){
        path.reset();path.moveTo(points[0],points[1]);
        for(int i=2;i<points.length;i+=2)path.lineTo(points[i],points[i+1]);
        path.close();c.drawPath(path,p);
    }
    private void paint(int color,int opacity,Paint.Style style,float stroke){
        p.setShader(null);p.setStyle(style);p.setStrokeWidth(stroke);
        p.setStrokeCap(Paint.Cap.BUTT);p.setStrokeJoin(Paint.Join.ROUND);
        p.setColor(alpha(color,opacity));
    }
    private void reset(){p.reset();p.setAntiAlias(true);p.setFilterBitmap(true);}
    private static int alpha(int c,int opacity){
        return Color.argb(Math.max(0,Math.min(255,opacity)),
            Color.red(c),Color.green(c),Color.blue(c));
    }
    private static int mix(int a,int b,float x){
        return Color.rgb((int)(Color.red(a)*(1-x)+Color.red(b)*x),
            (int)(Color.green(a)*(1-x)+Color.green(b)*x),
            (int)(Color.blue(a)*(1-x)+Color.blue(b)*x));
    }
}
