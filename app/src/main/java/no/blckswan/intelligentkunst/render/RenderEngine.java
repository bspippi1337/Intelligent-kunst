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

import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.core.FieldNoise;

/** Native Canvas painter. Same genome produces the same output at any resolution. */
public final class RenderEngine {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path path = new Path();

    public void draw(Canvas canvas, int width, int height, Artwork art) {
        if (art == null || width < 1 || height < 1) return;
        if (art.style == Artwork.Style.FABLE) {
            new StoryPainter().draw(canvas, width, height, art);
            return;
        }
        canvas.save();
        canvas.clipRect(0, 0, width, height);
        int[] colors = art.colors();
        final float w = width, h = height, unit = Math.min(w, h);
        final SplittableRandom rng = new SplittableRandom(art.seed);
        final FieldNoise noise = new FieldNoise(art.seed);

        paint.reset(); paint.setAntiAlias(true); paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, w, h,
            new int[] { colors[0], blend(colors[0], colors[3], .12f), colors[0] },
            new float[] { 0f, .55f, 1f }, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint); paint.setShader(null);

        // Atmospheric light is part of the genome, not a stock background.
        for (int i = 0; i < 5; i++) {
            float cx = (float) (.13 + rng.nextDouble() * .74) * w;
            float cy = (float) (.12 + rng.nextDouble() * .8) * h;
            float r = unit * (float) (.45 + rng.nextDouble() * .52);
            int c = colors[1 + (i % 3)];
            paint.setShader(new RadialGradient(cx, cy, r,
                new int[] { withAlpha(c, 35), withAlpha(c, 9), withAlpha(c, 0) },
                new float[] { 0f, .38f, 1f }, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, r, paint);
            paint.setShader(null);
        }

        switch (art.style) {
            case CURRENT: current(canvas, w, h, unit, colors, art, rng, noise); break;
            case BLOOM: bloom(canvas, w, h, unit, colors, art, rng); break;
            case ORBIT: orbit(canvas, w, h, unit, colors, art, rng); break;
            case TERRAIN: terrain(canvas, w, h, unit, colors, art, rng, noise); break;
            case WEAVE: weave(canvas, w, h, unit, colors, art, rng, noise); break;
            case PRISM: prism(canvas, w, h, unit, colors, art, rng); break;
        }

        // Museum-print texture: fine deterministic specks, sparse enough for mobile GPUs.
        paint.setShader(null); paint.setStyle(Paint.Style.FILL);
        int grain = Math.min(1400, Math.max(280, (int) (w * h / 2500)));
        for (int i = 0; i < grain; i++) {
            paint.setColor(withAlpha(colors[4], 6 + rng.nextInt(19)));
            canvas.drawCircle((float)rng.nextDouble() * w, (float)rng.nextDouble() * h,
                Math.max(.4f, unit * .00052f) * (.3f + (float)rng.nextDouble()), paint);
        }
        // Signature mark, visibly part of the work but free of watermark text.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1, unit * .0012f));
        paint.setColor(withAlpha(colors[4], 65));
        float lx = .075f * w, ly = .91f * h, s = .022f * unit;
        path.reset(); path.moveTo(lx, ly); path.cubicTo(lx+s*.5f,ly-s*2,lx+s,ly+s*2,lx+s*1.6f,ly);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void current(Canvas c, float w, float h, float u, int[] p, Artwork a,
                         SplittableRandom r, FieldNoise field) {
        float density = (float) (95 + a.complexity * 155);
        for (int i = 0; i < (int) density; i++) {
            float x = (float) (-.15 + r.nextDouble() * 1.22) * w;
            float y = (float) (.08 + r.nextDouble() * .78) * h;
            float speed = u * (float) (.008 + r.nextDouble() * .008);
            int tint = p[1 + i % 3];
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(u * (float) (.0006 + r.nextDouble() * .0026));
            paint.setColor(withAlpha(tint, 32 + r.nextInt(132)));
            path.reset(); path.moveTo(x, y);
            for (int j = 0; j < 50; j++) {
                double curl = field.fractal(x / u * 3.5, y / u * 3.5);
                double angle = -.75 + curl * 5.8 + a.drift * 1.4 + Math.sin(j * .058 + i * .09) * .33;
                x += Math.cos(angle) * speed;
                y += Math.sin(angle) * speed;
                path.lineTo(x, y);
            }
            c.drawPath(path, paint);
        }
        for (int i = 0; i < 110; i++) {
            float x = (float)r.nextDouble()*w, y = (float)r.nextDouble()*h;
            paint.setStyle(Paint.Style.FILL); paint.setColor(withAlpha(p[4], 80+r.nextInt(110)));
            c.drawCircle(x, y, u * (.0005f + (float)r.nextDouble()*.003f), paint);
        }
    }

    private void bloom(Canvas c, float w, float h, float u, int[] p, Artwork a, SplittableRandom r) {
        int stems = 9 + (int)(a.complexity * 14);
        for (int n = 0; n < stems; n++) {
            float cx = w*(float)(.09+r.nextDouble()*.82);
            float cy = h*(float)(.10+r.nextDouble()*.78);
            float radius = u*(float)(.07 + r.nextDouble()*.21);
            int petals = 6 + r.nextInt(9);
            float spin = (float)r.nextDouble() * 6.283185f;
            c.save(); c.translate(cx, cy); c.rotate((float) Math.toDegrees(spin));
            for (int layer = 0; layer < 3; layer++) {
                float scale = .58f + layer * .23f;
                for (int i = 0; i < petals; i++) {
                    float t = i * 6.283185f / petals;
                    float nx = (float) Math.cos(t), ny = (float) Math.sin(t);
                    float tx = -ny, ty = nx;
                    float tipX = nx*radius*scale, tipY=ny*radius*scale;
                    path.reset(); path.moveTo(0,0);
                    path.cubicTo(tx*radius*.45f,ty*radius*.45f,tipX+tx*radius*.3f,tipY+ty*radius*.3f,tipX,tipY);
                    path.cubicTo(tipX-tx*radius*.3f,tipY-ty*radius*.3f,-tx*radius*.45f,-ty*radius*.45f,0,0);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(withAlpha(p[1+(n+layer)%3], 13 + layer*17)); c.drawPath(path,paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(u*(.0006f+layer*.00025f));
                    paint.setColor(withAlpha(p[1+(n+i)%3], 58 + layer*18)); c.drawPath(path,paint);
                }
            }
            paint.setStyle(Paint.Style.FILL); paint.setColor(withAlpha(p[4], 200));
            c.drawCircle(0,0,u*.0033f,paint); c.restore();
        }
    }

    private void orbit(Canvas c, float w, float h, float u, int[] p, Artwork a, SplittableRandom r) {
        float cx = w*(float)(.28 + a.balance * .42);
        float cy = h*(float)(.26 + a.drift*.46);
        int ringCount = 43 + (int)(a.complexity * 60);
        for (int i = ringCount; i > 0; i--) {
            float radius = u * (.05f + i * .008f);
            float eccentricity = (float)(.50 + a.energy * .65 + Math.sin(i*.13)*.19);
            float angle = i*3.8f+(float)(a.drift*115);
            c.save(); c.rotate(angle,cx,cy);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(u*(.0007f + ((i%13)==0?.0014f:0)));
            paint.setColor(withAlpha(p[1 + i%3], 24 + i%8*9));
            c.drawOval(new RectF(cx-radius,cy-radius*eccentricity,cx+radius,cy+radius*eccentricity),paint);
            c.restore();
        }
        for (int i=0; i<150; i++) {
            float theta = (float)r.nextDouble() * 6.283185f, radius=u*(.10f+(float)r.nextDouble()*.8f);
            float x=cx+(float)Math.cos(theta)*radius, y=cy+(float)Math.sin(theta)*radius*.8f;
            float rr = u*(.0005f + (float)r.nextDouble()*.0035f);
            paint.setStyle(Paint.Style.FILL); paint.setColor(withAlpha(p[1+i%3],75+r.nextInt(155)));
            c.drawCircle(x,y,rr,paint);
        }
    }

    private void terrain(Canvas c, float w, float h, float u, int[] p, Artwork a,
                         SplittableRandom r, FieldNoise noise) {
        int lines = 65 + (int)(a.complexity * 70);
        float gap = h / (lines + 8);
        for (int row = -4; row < lines+5; row++) {
            path.reset();
            float baseY = (row + 4) * gap;
            for (int step = 0; step <= 120; step++) {
                float x = step * w / 120;
                double drift = noise.fractal(x/u*1.6 + a.drift*3, row*.047);
                double slow = Math.sin(x/w*6.5 + row*.08 + a.energy*2.7);
                float y = baseY + (float)((drift-.5)*u*(.23+a.energy*.25)+slow*u*.06);
                if (step==0) path.moveTo(x,y); else path.lineTo(x,y);
            }
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(u*(row%7==0 ? .0018f : .00073f));
            paint.setColor(withAlpha(p[1 + Math.floorMod(row,3)],row%7==0?140:62));
            c.drawPath(path,paint);
        }
        // A displaced luminous fault line adds a focal point.
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(u*.0028f);
        paint.setColor(withAlpha(p[4],140));
        path.reset();
        for (int j=0;j<=100;j++) {
            float x = j*w/100;
            float y = h*.48f + (float)Math.sin(x/w*9.0+a.drift*4)*u*.09f;
            if(j==0)path.moveTo(x,y); else path.lineTo(x,y);
        }
        c.drawPath(path,paint);
    }

    private void weave(Canvas c, float w, float h, float u, int[] p, Artwork a,
                       SplittableRandom r, FieldNoise noise) {
        int count=90+(int)(a.complexity*120);
        for(int i=0;i<count;i++) {
            float t=(float)i/count;
            float y0 = h * (.05f + t*.88f);
            float amplitude=u*(.1f+(float)a.energy*.18f);
            path.reset();
            for(int j=0;j<=100;j++) {
                float x=j*w/100f;
                float warp=(float)Math.sin(x/w*(7+a.drift*9)+t*10)*amplitude;
                float breath=(float)(noise.value(j*.035, i*.025)-.5)*u*.2f;
                float y=y0+warp+breath;
                if(j==0)path.moveTo(x,y);else path.lineTo(x,y);
            }
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(u*(.0007f+(i%19==0?.0011f:0)));
            paint.setColor(withAlpha(p[1+i%3],i%19==0?170:48+r.nextInt(42)));
            c.drawPath(path,paint);
        }
    }

    private void prism(Canvas c, float w, float h, float u, int[] p, Artwork a, SplittableRandom r) {
        int shards=70+(int)(a.complexity*125);
        float anchorX=w*(float)(.26 + a.balance*.44), anchorY=h*(float)(.28+a.drift*.41);
        for(int i=0;i<shards;i++) {
            float theta=(float)r.nextDouble()*6.283185f;
            float distance=u*(float)(r.nextDouble()*.9);
            float cx=anchorX+(float)Math.cos(theta)*distance;
            float cy=anchorY+(float)Math.sin(theta)*distance;
            float size=u*(float)(.025+r.nextDouble()*.19);
            float rotation=(float)r.nextDouble()*6.283185f;
            int sides=3+r.nextInt(3);
            path.reset();
            for(int v=0;v<sides;v++) {
                float angle=rotation+6.283185f*v/sides;
                float radius=size*(v%2==0?1:.58f);
                float x=cx+(float)Math.cos(angle)*radius, y=cy+(float)Math.sin(angle)*radius;
                if(v==0)path.moveTo(x,y);else path.lineTo(x,y);
            }
            path.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(p[1+i%3],9+r.nextInt(40)));
            c.drawPath(path,paint);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(u*(.0006f+(float)r.nextDouble()*.0015f));
            paint.setColor(withAlpha(p[1+i%3],42+r.nextInt(110)));
            c.drawPath(path,paint);
        }
        paint.setStyle(Paint.Style.FILL);paint.setColor(withAlpha(p[4],210));
        c.drawCircle(anchorX,anchorY,u*.007f,paint);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0,Math.min(255,alpha)),Color.red(color),Color.green(color),Color.blue(color));
    }
    private static int blend(int from, int to, float t) {
        return Color.rgb((int)(Color.red(from)*(1-t)+Color.red(to)*t),
            (int)(Color.green(from)*(1-t)+Color.green(to)*t),
            (int)(Color.blue(from)*(1-t)+Color.blue(to)*t));
    }
}
