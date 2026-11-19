package no.blckswan.intelligentkunst.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.SplittableRandom;

/**
 * Academic figure drawing, from an original, parameterized adult anatomy study.
 * No photograph, generative neural weights or external motif library involved.
 * The deliberately non-sexual life model uses chiaroscuro, anatomy and pose.
 */
public final class ClassicalFigurePainter {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final Path path=new Path();

    public void draw(Canvas canvas,int width,int height,long seed,boolean charcoal){
        if(width<1||height<1)return;
        SplittableRandom random=new SplittableRandom(seed);
        boolean seated=(Math.floorMod(seed,3)==1);
        boolean mirrored=(Math.floorMod(seed,5)==2);
        float figureLean=(float)(random.nextDouble()-.5)*7;
        canvas.save();
        canvas.drawColor(charcoal?0xFFECE4D0:0xFF171E1D);
        float scale=Math.min(width/800f,height/1200f);
        canvas.translate((width-800*scale)*.5f,(height-1200*scale)*.5f);
        canvas.scale(scale,scale);
        background(canvas,charcoal,random);
        if(mirrored){canvas.translate(800,0);canvas.scale(-1,1);}
        canvas.save();
        canvas.rotate(figureLean,400,620);
        if(seated)seatedLegs(canvas,charcoal);
        else standingLegs(canvas,charcoal);
        torso(canvas,charcoal);
        arms(canvas,charcoal,seated);
        head(canvas,charcoal);
        anatomy(canvas,charcoal);
        canvas.restore();
        foreground(canvas,charcoal,random);
        canvas.restore();
    }

    private void background(Canvas c,boolean charcoal,SplittableRandom r){
        paint.reset();paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0,0,800,1200,
            charcoal?new int[]{0xFFECE4D0,0xFFD3C6AC,0xFFE9DFC9}
                :new int[]{0xFF273232,0xFF363C36,0xFF171C20},
            null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,800,1200,paint);
        paint.setShader(new RadialGradient(340,380,630,
            charcoal?new int[]{0x44FFFFFF,0x00FFFFFF}
                :new int[]{0x77756D55,0x00312D27},
            null,Shader.TileMode.CLAMP));
        c.drawCircle(340,380,630,paint);paint.setShader(null);
        paint.setColor(charcoal?0x666D6252:0x663C3530);
        c.drawRect(0,1000,800,1006,paint);
        if(!charcoal){
            paint.setShader(new RadialGradient(420,1090,380,
                new int[]{0x88736450,0x002E2F28},null,Shader.TileMode.CLAMP));
            c.drawOval(new RectF(80,960,720,1170),paint);paint.setShader(null);
        }
    }
    private void standingLegs(Canvas c,boolean charcoal){
        fill(charcoal,0xFFC69D80);
        path.reset();path.moveTo(314,749);
        path.cubicTo(270,808,298,919,275,992);
        path.cubicTo(263,1054,252,1108,254,1136);
        path.cubicTo(278,1159,328,1146,333,1129);
        path.cubicTo(335,1108,322,1073,343,1017);
        path.cubicTo(358,959,378,869,401,806);
        path.close();c.drawPath(path,paint);

        fill(charcoal,0xFFB98770);
        path.reset();path.moveTo(416,759);
        path.cubicTo(445,812,492,851,501,911);
        path.cubicTo(506,991,546,1065,551,1131);
        path.cubicTo(559,1156,599,1153,621,1145);
        path.cubicTo(625,1114,594,1081,585,1013);
        path.cubicTo(577,950,588,861,526,773);
        path.close();c.drawPath(path,paint);

        highlight(c,charcoal,318,817,365,1080,0x88FFE4B7);
        highlight(c,charcoal,490,854,565,1057,0x44F2BD95);
        detail(c,charcoal,284,979,272,1051,279,1100);
        detail(c,charcoal,537,948,549,1057,567,1100);
    }
    private void seatedLegs(Canvas c,boolean charcoal){
        // Studio plinth stays under the figure, giving the seated pose weight.
        fill(charcoal,0xFF685F51);c.drawRect(237,816,584,849,paint);
        fill(charcoal,0xFF4C453C);c.drawRect(266,849,304,1138,paint);
        c.drawRect(515,849,549,1128,paint);
        fill(charcoal,0xFFC69D80);
        path.reset();path.moveTo(331,744);
        path.cubicTo(284,782,220,821,182,918);
        path.cubicTo(151,980,138,1049,119,1094);
        path.cubicTo(120,1115,175,1139,203,1126);
        path.cubicTo(239,1043,270,1016,315,950);
        path.cubicTo(361,908,394,863,419,807);
        path.close();c.drawPath(path,paint);
        fill(charcoal,0xFFAF8068);
        path.reset();path.moveTo(422,754);
        path.cubicTo(488,786,543,827,598,893);
        path.cubicTo(633,952,646,1016,636,1109);
        path.cubicTo(644,1136,689,1134,709,1118);
        path.cubicTo(726,1041,713,932,688,885);
        path.cubicTo(668,811,589,749,513,739);
        path.close();c.drawPath(path,paint);
        highlight(c,charcoal,255,820,360,930,0x77F5D5AC);
        highlight(c,charcoal,532,829,630,1035,0x55ECD0AE);
        detail(c,charcoal,198,930,159,1027,151,1084);
        detail(c,charcoal,656,921,685,1020,679,1102);
    }
    private void torso(Canvas c,boolean charcoal){
        fill(charcoal,0xFFD0A889);
        path.reset();path.moveTo(365,329);
        path.cubicTo(312,324,275,347,270,390);
        path.cubicTo(266,436,291,467,308,513);
        path.cubicTo(329,567,334,603,311,658);
        path.cubicTo(284,718,280,764,317,793);
        path.cubicTo(352,820,442,826,493,800);
        path.cubicTo(537,778,539,731,508,667);
        path.cubicTo(483,607,492,558,514,506);
        path.cubicTo(541,448,549,393,527,362);
        path.cubicTo(505,334,473,326,444,335);
        path.close();c.drawPath(path,paint);
        // Main reflective plane: forehead / clavicle / sternum / abdomen.
        paint.setShader(new LinearGradient(290,420,508,770,
            charcoal?new int[]{0x0AFFFFFF,0x66FFF7E7,0x006C5B47}
                :new int[]{0x44FFF4D4,0x6BDDB68D,0x007A4D39},
            null,Shader.TileMode.CLAMP));
        c.drawOval(new RectF(320,365,480,780),paint);
        paint.setShader(null);
        // Shoulder/clavicle illumination and atmospheric breast/pectoral planes,
        // studied as anatomy rather than decoration.
        shade(c,charcoal,new RectF(289,370,405,540),0x70F9D6AD);
        shade(c,charcoal,new RectF(397,380,518,535),0x489B634C);
        shade(c,charcoal,new RectF(345,515,461,704),0x44F8D1A8);
    }
    private void arms(Canvas c,boolean charcoal,boolean seated){
        fill(charcoal,0xFFC69B7D);
        path.reset();path.moveTo(287,360);
        path.cubicTo(245,380,226,447,215,513);
        path.cubicTo(204,575,195,628,213,675);
        path.cubicTo(228,695,253,700,272,689);
        path.cubicTo(274,651,263,622,273,579);
        path.cubicTo(281,538,301,480,309,429);
        path.close();c.drawPath(path,paint);
        // Fingers are subtle because anatomy, not sexual attention, is the subject.
        fill(charcoal,0xFFCCAB8D);
        path.reset();path.moveTo(213,662);
        path.cubicTo(202,704,204,751,220,785);
        path.cubicTo(230,798,236,782,235,761);
        path.cubicTo(246,788,256,786,257,771);
        path.lineTo(261,710);path.lineTo(271,681);
        path.close();c.drawPath(path,paint);
        fill(charcoal,0xFFB28770);
        path.reset();path.moveTo(515,350);
        path.cubicTo(556,367,568,429,580,488);
        path.cubicTo(604,561,603,611,617,660);
        path.cubicTo(623,694,599,719,575,709);
        path.cubicTo(554,673,553,621,537,580);
        path.cubicTo(517,538,512,487,496,434);
        path.close();c.drawPath(path,paint);
        fill(charcoal,0xFFB88D73);
        path.reset();path.moveTo(573,696);
        path.cubicTo(555,734,563,770,572,797);
        path.cubicTo(579,812,588,809,591,794);
        path.cubicTo(598,819,607,815,609,798);
        path.cubicTo(623,783,622,742,620,710);
        path.close();c.drawPath(path,paint);
        highlight(c,charcoal,234,415,240,640,0x66FFE3BB);
        highlight(c,charcoal,561,445,585,642,0x42E1B08D);
        detail(c,charcoal,247,551,218,626,225,672);
    }
    private void head(Canvas c,boolean charcoal){
        // A head is roughly a sixth of the complete study, not a giant icon.
        // Draw the neck first so the chin can overlap naturally.
        c.save();
        c.scale(.75f,.75f,400,350);
        fill(charcoal,0xFFB8896D);
        c.drawOval(new RectF(370,319,434,401),paint);
        fill(charcoal,0xFFCDA184);
        path.reset();path.moveTo(394,172);
        path.cubicTo(343,165,324,203,328,254);
        path.cubicTo(327,309,348,340,381,366);
        path.cubicTo(392,377,420,376,431,362);
        path.cubicTo(461,332,480,295,476,247);
        path.cubicTo(473,198,445,165,394,172);
        path.close();c.drawPath(path,paint);
        shade(c,charcoal,new RectF(341,202,429,330),0x4FFFE6C5);

        // Hair has its own material shader. It must not inherit the skin shader.
        paint.reset();paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(335,150,480,260,
            charcoal?new int[]{0xFF50483E,0xFF8A8174}
                :new int[]{0xFF292927,0xFF5A4541},
            null,Shader.TileMode.CLAMP));
        path.reset();path.moveTo(325,243);
        path.cubicTo(319,178,354,131,402,130);
        path.cubicTo(467,133,483,180,483,226);
        path.cubicTo(471,205,465,185,446,180);
        path.cubicTo(420,162,390,182,371,197);
        path.cubicTo(344,199,344,232,325,243);
        path.close();c.drawPath(path,paint);paint.setShader(null);

        detail(c,charcoal,357,262,379,257,389,263);
        detail(c,charcoal,418,262,443,254,452,260);
        detail(c,charcoal,399,273,390,302,406,308);
        detail(c,charcoal,385,328,399,337,416,330);
        c.restore();
    }
    private void anatomy(Canvas c,boolean charcoal){
        // Structural lines: volume, navel, hip shift and overlapping soft planes.
        detail(c,charcoal,333,387,368,369,388,374);
        detail(c,charcoal,413,375,450,368,485,384);
        detail(c,charcoal,318,510,349,537,388,523);
        detail(c,charcoal,418,524,469,527,499,501);
        detail(c,charcoal,399,556,385,630,398,664);
        detail(c,charcoal,322,685,350,700,370,696);
        detail(c,charcoal,442,701,480,693,506,674);
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);
        paint.setColor(charcoal?0x775A4B3D:0xAA895841);
        c.drawOval(new RectF(390,661,400,669),paint);
        paint.setColor(charcoal?0x125C4A37:0x188E5B45);
        c.drawOval(new RectF(341,483,351,488),paint);
        c.drawOval(new RectF(455,481,465,487),paint);
    }
    private void foreground(Canvas c,boolean charcoal,SplittableRandom r){
        paint.setShader(null);paint.setStyle(Paint.Style.FILL);
        int count=2400;
        for(int i=0;i<count;i++){
            int shade=charcoal?0x18664F36:0x20D7B88F;
            paint.setColor(shade);
            c.drawCircle((float)r.nextDouble()*800,(float)r.nextDouble()*1200,
                .15f+(float)r.nextDouble()*.9f,paint);
        }
        // A restrained floor-plane line anchors the study to space.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        paint.setColor(charcoal?0x706C5842:0x447F7361);
        c.drawLine(74,1149,726,1149,paint);
    }
    private void fill(boolean charcoal,int color){
        paint.reset();paint.setAntiAlias(true);paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        if(charcoal){
            paint.setColor(0xFFBCB4A1);
            paint.setShader(new LinearGradient(260,220,530,850,
                new int[]{0xFFC7C0B1,0xFFE8DFCD,0xFF796D5F},
                null,Shader.TileMode.CLAMP));
        }else{
            paint.setColor(color);
            paint.setShader(new LinearGradient(230,270,570,860,
                new int[]{0xFFE8C5A1, color, 0xFF785447},
                null,Shader.TileMode.CLAMP));
        }
    }
    private void shade(Canvas c,boolean charcoal,RectF area,int color){
        paint.reset();paint.setAntiAlias(true);
        int tint=charcoal?0x66F7F0DF:color;
        paint.setShader(new RadialGradient(area.centerX(),area.centerY(),
            Math.max(area.width(),area.height())*.66f,
            new int[]{tint,0x00000000},null,Shader.TileMode.CLAMP));
        c.drawOval(area,paint);paint.setShader(null);
    }
    private void highlight(Canvas c,boolean charcoal,float x1,float y1,float x2,float y2,int color){
        paint.reset();paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(9);
        int pigment=charcoal?0x30FFF5DE:Color.argb(
            Math.min(38,Color.alpha(color)),Color.red(color),Color.green(color),Color.blue(color));
        paint.setColor(pigment);
        Path p=new Path();p.moveTo(x1,y1);p.quadTo((x1+x2)*.5f-13,(y1+y2)*.5f,x2,y2);
        c.drawPath(p,paint);
    }
    private void detail(Canvas c,boolean charcoal,float x1,float y1,float cx,float cy,float x2,float y2){
        paint.reset();paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(charcoal?1.8f:1.25f);
        paint.setColor(charcoal?0xA16E5F50:0x86744738);
        path.reset();path.moveTo(x1,y1);path.quadTo(cx,cy,x2,y2);
        c.drawPath(path,paint);
    }
}
