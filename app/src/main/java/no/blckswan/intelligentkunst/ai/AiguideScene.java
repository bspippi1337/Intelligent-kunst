package no.blckswan.intelligentkunst.ai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Small, reviewable drawing bytecode. No scripts, URLs, imports or external resources. */
public final class AiguideScene {
    public static final int MAX_ELEMENTS=90;
    private AiguideScene(){}

    public static String validate(String raw) throws JSONException {
        if(raw==null||raw.length()>28000)throw new JSONException("Scene too large");
        JSONObject input=new JSONObject(raw);
        String title=text(input,"title",1,76);
        String intent=text(input,"intent",1,240);
        String background=color(input.getString("background"));
        JSONArray source=input.getJSONArray("elements");
        if(source.length()<8||source.length()>MAX_ELEMENTS)
            throw new JSONException("Expected 8..90 drawing elements");

        JSONObject safe=new JSONObject();
        safe.put("version",1);
        safe.put("title",title);
        safe.put("intent",intent);
        safe.put("background",background);
        JSONArray elements=new JSONArray();
        for(int i=0;i<source.length();i++){
            JSONObject el=source.getJSONObject(i);
            String kind=el.getString("kind");
            JSONObject clean=new JSONObject();
            clean.put("kind",kind);
            clean.put("color",color(el.getString("color")));
            double alpha=el.optDouble("alpha",1);
            if(!Double.isFinite(alpha)||alpha<0.04||alpha>1)
                throw new JSONException("Invalid opacity");
            clean.put("alpha",alpha);
            switch(kind){
                case "rect":
                case "ellipse":
                    coord(el,clean,"x");coord(el,clean,"y");
                    extent(el,clean,"w");extent(el,clean,"h");
                    break;
                case "line":
                    coord(el,clean,"x");coord(el,clean,"y");
                    coord(el,clean,"x2");coord(el,clean,"y2");
                    thickness(el,clean);break;
                case "curve":
                    coord(el,clean,"x");coord(el,clean,"y");
                    coord(el,clean,"cx1");coord(el,clean,"cy1");
                    coord(el,clean,"cx2");coord(el,clean,"cy2");
                    coord(el,clean,"x2");coord(el,clean,"y2");
                    thickness(el,clean);break;
                case "poly":
                    JSONArray pts=el.getJSONArray("points");
                    if(pts.length()<3||pts.length()>18)throw new JSONException("Invalid polygon");
                    JSONArray safePoints=new JSONArray();
                    for(int k=0;k<pts.length();k++){
                        JSONArray point=pts.getJSONArray(k);
                        if(point.length()!=2)throw new JSONException("Invalid coordinate pair");
                        safePoints.put(new JSONArray().put(finite(point.getDouble(0)))
                            .put(finite(point.getDouble(1))));
                    }
                    clean.put("points",safePoints);break;
                default: throw new JSONException("Unsupported drawing command: "+kind);
            }
            elements.put(clean);
        }
        safe.put("elements",elements);
        String normalized=safe.toString();
        if(normalized.length()>18000)throw new JSONException("Normalized scene too large");
        return normalized;
    }
    public static String title(String json) {
        try{return new JSONObject(json).optString("title","AI-STUDIE");}
        catch(JSONException e){return "AI-STUDIE";}
    }
    public static String intent(String json) {
        try{return new JSONObject(json).optString("intent","En generert komposisjon.");}
        catch(JSONException e){return "En generert komposisjon.";}
    }
    private static String text(JSONObject source,String key,int min,int max)throws JSONException{
        String s=source.getString(key).trim().replaceAll("[\\r\\n\\t]"," ");
        if(s.length()<min||s.length()>max)throw new JSONException("Invalid text size");
        return s;
    }
    private static String color(String value)throws JSONException{
        if(!value.matches("#[0-9a-fA-F]{6}"))throw new JSONException("Use #RRGGBB colors");
        return value.toUpperCase(java.util.Locale.ROOT);
    }
    private static double finite(double value)throws JSONException{
        if(!Double.isFinite(value)||value<-.25||value>1.25)
            throw new JSONException("Coordinates outside bounded canvas");
        return value;
    }
    private static void coord(JSONObject from,JSONObject to,String key)throws JSONException{
        to.put(key,finite(from.getDouble(key)));
    }
    private static void extent(JSONObject from,JSONObject to,String key)throws JSONException{
        double x=from.getDouble(key);
        if(!Double.isFinite(x)||x<=0||x>1.5)throw new JSONException("Bad extent");
        to.put(key,x);
    }
    private static void thickness(JSONObject from,JSONObject to)throws JSONException{
        double v=from.optDouble("width",.009);
        if(!Double.isFinite(v)||v<.001||v>.09)throw new JSONException("Bad line width");
        to.put("width",v);
    }
}
