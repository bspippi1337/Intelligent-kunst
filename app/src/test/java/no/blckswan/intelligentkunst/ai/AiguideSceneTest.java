package no.blckswan.intelligentkunst.ai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/** Real JSON contract tests, independent of API credentials or online calls. */
public final class AiguideSceneTest {
    private static JSONObject base()throws JSONException {
        JSONObject root=new JSONObject()
            .put("version",1).put("title","Unfamiliar structure")
            .put("intent","An invented visual proposition")
            .put("background","#F2E9D8");
        JSONArray items=new JSONArray();
        for(int i=0;i<9;i++){
            items.put(new JSONObject().put("kind","ellipse")
                .put("color","#102030").put("x",.08+i*.07)
                .put("y",.3).put("w",.08).put("h",.16));
        }
        return root.put("elements",items);
    }
    private static JSONObject path()throws JSONException {
        JSONArray commands=new JSONArray()
            .put(new JSONObject().put("op","M").put("x",.1).put("y",.2))
            .put(new JSONObject().put("op","C").put("cx1",.8).put("cy1",.05)
                .put("cx2",.9).put("cy2",.8).put("x",.65).put("y",.92))
            .put(new JSONObject().put("op","Q").put("cx",.25).put("cy",.8)
                .put("x",.1).put("y",.2))
            .put(new JSONObject().put("op","Z"));
        return new JSONObject().put("kind","path").put("color","#304050")
            .put("commands",commands)
            .put("gradient",new JSONObject().put("type","linear")
                .put("start","#120f25").put("end","#d7a071")
                .put("x1",.1).put("y1",.2).put("x2",.8).put("y2",.85));
    }
    @Test public void acceptsAndNormalizesExpressivePath()throws Exception {
        JSONObject input=base();
        input.getJSONArray("elements").put(0,path());
        String result=AiguideScene.validate(input.toString());
        JSONObject normalized=new JSONObject(result);
        JSONObject shape=normalized.getJSONArray("elements").getJSONObject(0);
        assertEquals("path",shape.getString("kind"));
        assertEquals("#120F25",shape.getJSONObject("gradient").getString("start"));
        assertEquals("fill",shape.getString("mode"));
        assertEquals("Z",shape.getJSONArray("commands").getJSONObject(3).getString("op"));
        assertEquals(result,AiguideScene.validate(result));
    }
    @Test public void rejectsOpenFilledPaths()throws Exception {
        JSONObject input=base();JSONObject shape=path();
        JSONArray commands=shape.getJSONArray("commands");
        shape.put("commands",new JSONArray()
            .put(commands.getJSONObject(0)).put(commands.getJSONObject(1))
            .put(commands.getJSONObject(2)));
        input.getJSONArray("elements").put(0,shape);
        try{AiguideScene.validate(input.toString());fail("accepted open path");}
        catch(JSONException expected){}
    }
    @Test public void rejectsExternalCommandsAndOutOfRangeCoordinates()throws Exception {
        JSONObject input=base();JSONObject shape=path();
        shape.getJSONArray("commands").getJSONObject(1).put("cx1",999);
        input.getJSONArray("elements").put(0,shape);
        try{AiguideScene.validate(input.toString());fail("accepted out of range");}
        catch(JSONException expected){}
        shape=path();
        shape.getJSONArray("commands").getJSONObject(2).put("op","eval");
        input.getJSONArray("elements").put(0,shape);
        try{AiguideScene.validate(input.toString());fail("accepted command injection");}
        catch(JSONException expected){}
    }
    @Test public void rejectsDegenerateGradient()throws Exception {
        JSONObject input=base();JSONObject shape=path();
        shape.getJSONObject("gradient").put("x2",.1).put("y2",.2);
        input.getJSONArray("elements").put(0,shape);
        try{AiguideScene.validate(input.toString());fail("accepted zero-length gradient");}
        catch(JSONException expected){}
    }
    @Test public void rejectsHugeSubtrees()throws Exception {
        JSONObject input=base();JSONObject shape=path();
        JSONArray commands=new JSONArray();
        for(int i=0;i<66;i++)
            commands.put(new JSONObject().put("op",i==0?"M":"L").put("x",.5).put("y",.5));
        shape.put("commands",commands);
        input.getJSONArray("elements").put(0,shape);
        try{AiguideScene.validate(input.toString());fail("accepted excessive path");}
        catch(JSONException expected){}
    }
}
