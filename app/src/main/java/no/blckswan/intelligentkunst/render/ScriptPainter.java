package no.blckswan.intelligentkunst.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Path;
import android.graphics.RectF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.blckswan.intelligentkunst.core.Artwork;

/**
 * Deterministic interpreter for a model-authored, bounded vector composition.
 * No JavaScript, resource loading or arbitrary code execution.
 */
public final class ScriptPainter {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final Path path=new Path();

    public void draw(Canvas canvas,int width,int height,Artwork artwork){
        if(width<1||height<1)return;
        canvas.save();
        try{
            canvas.clipRect(0,0,width,height);
            JSONObject scene=new JSONObject(artwork.sceneJson);
            paint.reset();paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor(scene.getString("background")));
            canvas.drawColor(paint.getColor());
            JSONArray elements=scene.getJSONArray("elements");
            for(int i=0;i<elements.length()&&i<90;i++){
                JSONObject element=elements.getJSONObject(i);
                String kind=element.getString("kind");
                int rgb=Color.parseColor(element.getString("color"));
                int alpha=(int)Math.round(element.optDouble("alpha",1)*255);
                paint.setShader(null);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(alpha,Color.red(rgb),Color.green(rgb),Color.blue(rgb)));
                if(element.has("gradient"))applyGradient(element.getJSONObject("gradient"),width,height,alpha);
                float x=(float)element.optDouble("x",0)*width;
                float y=(float)element.optDouble("y",0)*height;
                switch(kind){
                    case "rect":
                        canvas.drawRect(x,y,
                            x+(float)element.getDouble("w")*width,
                            y+(float)element.getDouble("h")*height,paint);
                        break;
                    case "ellipse":
                        canvas.drawOval(new RectF(x,y,
                            x+(float)element.getDouble("w")*width,
                            y+(float)element.getDouble("h")*height),paint);
                        break;
                    case "line":
                        setupStroke(element,width,height);
                        canvas.drawLine(x,y,(float)element.getDouble("x2")*width,
                            (float)element.getDouble("y2")*height,paint);
                        break;
                    case "curve":
                        setupStroke(element,width,height);
                        path.reset();path.moveTo(x,y);
                        path.cubicTo((float)element.getDouble("cx1")*width,
                            (float)element.getDouble("cy1")*height,
                            (float)element.getDouble("cx2")*width,
                            (float)element.getDouble("cy2")*height,
                            (float)element.getDouble("x2")*width,
                            (float)element.getDouble("y2")*height);
                        canvas.drawPath(path,paint);
                        break;
                    case "path":
                        if("stroke".equals(element.optString("mode","fill")))
                            setupStroke(element,width,height);
                        JSONArray commands=element.getJSONArray("commands");
                        path.reset();
                        for(int j=0;j<commands.length();j++){
                            JSONObject command=commands.getJSONObject(j);
                            String op=command.getString("op");
                            switch(op) {
                                case "M":
                                    path.moveTo((float)command.getDouble("x")*width,
                                        (float)command.getDouble("y")*height);break;
                                case "L":
                                    path.lineTo((float)command.getDouble("x")*width,
                                        (float)command.getDouble("y")*height);break;
                                case "Q":
                                    path.quadTo((float)command.getDouble("cx")*width,
                                        (float)command.getDouble("cy")*height,
                                        (float)command.getDouble("x")*width,
                                        (float)command.getDouble("y")*height);break;
                                case "C":
                                    path.cubicTo((float)command.getDouble("cx1")*width,
                                        (float)command.getDouble("cy1")*height,
                                        (float)command.getDouble("cx2")*width,
                                        (float)command.getDouble("cy2")*height,
                                        (float)command.getDouble("x")*width,
                                        (float)command.getDouble("y")*height);break;
                                case "Z":path.close();break;
                            }
                        }
                        canvas.drawPath(path,paint);
                        break;
                    case "poly":
                        JSONArray points=element.getJSONArray("points");
                        if(points.length()<3)break;
                        path.reset();
                        JSONArray first=points.getJSONArray(0);
                        path.moveTo((float)first.getDouble(0)*width,(float)first.getDouble(1)*height);
                        for(int k=1;k<points.length();k++){
                            JSONArray pt=points.getJSONArray(k);
                            path.lineTo((float)pt.getDouble(0)*width,
                                (float)pt.getDouble(1)*height);
                        }
                        path.close();canvas.drawPath(path,paint);
                        break;
                }
            }
        }catch(JSONException|IllegalArgumentException invalid){
            paint.reset();paint.setAntiAlias(true);
            canvas.drawColor(0xFF1A1D20);
            paint.setColor(0xFFF7EAE3);paint.setTextSize(Math.min(width,height)*.048f);
            canvas.drawText("UGYLDIG AI-BILDEMANUS",width*.07f,height*.52f,paint);
        }finally{canvas.restore();}
    }
    private void applyGradient(JSONObject gradient,int width,int height,int alpha)
        throws JSONException {
        int start=Color.parseColor(gradient.getString("start"));
        int end=Color.parseColor(gradient.getString("end"));
        Shader shader;
        if("linear".equals(gradient.getString("type"))) {
            shader=new LinearGradient(
                (float)gradient.getDouble("x1")*width,
                (float)gradient.getDouble("y1")*height,
                (float)gradient.getDouble("x2")*width,
                (float)gradient.getDouble("y2")*height,
                start,end,Shader.TileMode.CLAMP);
        } else {
            shader=new RadialGradient(
                (float)gradient.getDouble("cx")*width,
                (float)gradient.getDouble("cy")*height,
                (float)gradient.getDouble("r")*Math.min(width,height),
                start,end,Shader.TileMode.CLAMP);
        }
        paint.setShader(shader);
        paint.setColor(Color.WHITE);
        paint.setAlpha(alpha);
    }
    private void setupStroke(JSONObject command,int width,int height){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)command.optDouble("width",.009)*Math.min(width,height));
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }
}
