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
            if(el.has("gradient")) {
                JSONObject gradient=el.getJSONObject("gradient");
                String type=gradient.getString("type");
                if(!"linear".equals(type)&&!"radial".equals(type))
                    throw new JSONException("Unsupported gradient");
                JSONObject safeGradient=new JSONObject();
                safeGradient.put("type",type);
                safeGradient.put("start",color(gradient.getString("start")));
                safeGradient.put("end",color(gradient.getString("end")));
                if("linear".equals(type)) {
                    coord(gradient,safeGradient,"x1");coord(gradient,safeGradient,"y1");
                    coord(gradient,safeGradient,"x2");coord(gradient,safeGradient,"y2");
                    double dx=safeGradient.getDouble("x1")-safeGradient.getDouble("x2");
                    double dy=safeGradient.getDouble("y1")-safeGradient.getDouble("y2");
                    if(dx*dx+dy*dy<.0001)throw new JSONException("Degenerate gradient");
                } else {
                    coord(gradient,safeGradient,"cx");coord(gradient,safeGradient,"cy");
                    extent(gradient,safeGradient,"r");
                }
                clean.put("gradient",safeGradient);
            }
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
                case "path":
                    String mode=el.optString("mode","fill");
                    if(!"fill".equals(mode)&&!"stroke".equals(mode))
                        throw new JSONException("Invalid path mode");
                    clean.put("mode",mode);
                    if("stroke".equals(mode))thickness(el,clean);
                    JSONArray commands=el.getJSONArray("commands");
                    if(commands.length()<3||commands.length()>64)
                        throw new JSONException("Invalid path length");
                    JSONArray safeCommands=new JSONArray();
                    int draws=0;
                    for(int j=0;j<commands.length();j++) {
                        JSONObject instruction=commands.getJSONObject(j);
                        String op=instruction.getString("op");
                        if(j==0 && !"M".equals(op))throw new JSONException("Path must start at M");
                        if(j>0 && "M".equals(op))throw new JSONException("Only one contour per path");
                        JSONObject safeCommand=new JSONObject().put("op",op);
                        switch(op){
                            case "M":
                                coord(instruction,safeCommand,"x");
                                coord(instruction,safeCommand,"y");break;
                            case "L":
                                coord(instruction,safeCommand,"x");
                                coord(instruction,safeCommand,"y");draws++;break;
                            case "Q":
                                coord(instruction,safeCommand,"cx");
                                coord(instruction,safeCommand,"cy");
                                coord(instruction,safeCommand,"x");
                                coord(instruction,safeCommand,"y");draws++;break;
                            case "C":
                                coord(instruction,safeCommand,"cx1");
                                coord(instruction,safeCommand,"cy1");
                                coord(instruction,safeCommand,"cx2");
                                coord(instruction,safeCommand,"cy2");
                                coord(instruction,safeCommand,"x");
                                coord(instruction,safeCommand,"y");draws++;break;
                            case "Z":
                                if(j!=commands.length()-1)
                                    throw new JSONException("Z must close at end");
                                break;
                            default:throw new JSONException("Unknown path instruction");
                        }
                        safeCommands.put(safeCommand);
                    }
                    if(draws<2)throw new JSONException("Path too simple");
                    if("fill".equals(mode) && !"Z".equals(
                        commands.getJSONObject(commands.length()-1).getString("op")))
                        throw new JSONException("Filled path must close with Z");
                    clean.put("commands",safeCommands);
                    break;
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
