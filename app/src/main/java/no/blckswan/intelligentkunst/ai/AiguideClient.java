package no.blckswan.intelligentkunst.ai;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Android port of bspippi1337/aiguide-dev's ask(query) + prompt template +
 * JSONL conversation logging. The original Python package cannot be executed
 * by native Java; see tools/aiguide for the direct Python integration.
 *
 * Only explicit user action calls the API. No key is written to files or logs.
 * We log visible prompts and visible model output, never claim hidden reasoning.
 */
public final class AiguideClient {
    private static final String ENDPOINT="https://api.openai.com/v1/chat/completions";
    private static final String SOURCE_COMMIT="c55f658610a0bb27f90c75619510dc8ad5c36a2f";
    private final Context context;

    public AiguideClient(Context context){this.context=context.getApplicationContext();}

    public String ask(String idea,String key,String model)throws IOException,JSONException{
        if(idea==null||idea.trim().length()>700)
            throw new IllegalArgumentException("Prompt too long");
        if(key==null||!key.startsWith("sk-")||key.length()<15||key.length()>300)
            throw new IllegalArgumentException("Provide a valid API key");
        if(model==null||!model.matches("[a-zA-Z0-9._-]{2,64}"))
            throw new IllegalArgumentException("Invalid model ID");
        String task="Create one ORIGINAL visual artwork with a surprising, defensible artistic premise. "
            +"Base the subject ONLY on this instruction: "+(idea.trim().isEmpty()
                ?"Invent a new concept without requesting familiar motifs.":idea.trim())
            +". Do not echo old conversations, fandoms, proprietary icons, user nicknames, logos, "
            +"derivative references or a preselected object list.";
        String system=template().replace("{{OPPGAVE}}",task)
            +"\n\n## ANDROID AI ATELIER ART CONTRACT (overrides earlier creative-context hints)\n"
            +"Produce ONLY a JSON object, with keys: version (integer 1), title (Norwegian <=76 chars), "
            +"intent (Norwegian <=240 chars), background (hex #RRGGBB), elements (array of 12 to 55 objects). "
            +"Each element: kind and color (#RRGGBB), optional alpha number 0.04..1. "
            +"Allowed kinds: rect or ellipse with x,y,w,h; line with x,y,x2,y2,width; "
            +"curve with x,y,cx1,cy1,cx2,cy2,x2,y2,width; "
            +"poly with points [[x,y],[x,y],[x,y],...]; "
            +"PATH with commands array: first {op:M,x,y}, then {op:L,x,y}, "
            +"{op:Q,cx,cy,x,y}, {op:C,cx1,cy1,cx2,cy2,x,y}, closing {op:Z}. "
            +"For path use kind: path, mode: fill or stroke, width only for stroke; "
            +"three to 64 commands and at least two drawing commands. "
            +"Use path with curves to make expressive original silhouettes, rather "
            +"than combining circles and rectangles into generic clip art. " 
            +"An element may optionally have gradient: {type:linear,start:#RRGGBB,end:#RRGGBB,"
            +"x1,y1,x2,y2} or {type:radial,start:#RRGGBB,end:#RRGGBB,cx,cy,r}. "
            +"All coordinates are NORMALIZED floats 0..1 across a portrait canvas (including curved control "
            +"points); positive widths/heights <=1. Use intentionally layered forms, hierarchy, perspective, "
            +"rhythm and negative space. A shape must have visual narrative purpose. The composition must "
            +"read without labels, excessive symmetry, random wallpapers or generic clip art. "
            +"Distinct silhouettes, unusual material language and clear focal tension matter more than decoration. "
            +"Use 4-7 harmonious but surprising colors and 2-8 authored path silhouettes. "
            +"Keep full JSON under 17500 characters. Avoid visual tropes derived from earlier conversations. "
            +"Do not emit Markdown or explanations outside JSON. "
            +"Never include network addresses, executable code or hidden reasoning.";
        JSONArray messages=new JSONArray();
        messages.put(new JSONObject().put("role","system").put("content",system));
        messages.put(new JSONObject().put("role","user").put("content",task+"\nRespond with a JSON composition."));
        JSONObject request=new JSONObject();
        request.put("model",model);
        request.put("messages",messages);
        request.put("response_format",new JSONObject().put("type","json_object"));
        request.put("max_completion_tokens",6500);
        request.put("store",false);
        HttpURLConnection conn=(HttpURLConnection)new URL(ENDPOINT).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(90000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization","Bearer "+key);
        conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
        conn.setRequestProperty("Accept","application/json");
        try{
            byte[] payload=request.toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream out=conn.getOutputStream()){out.write(payload);}
            int status=conn.getResponseCode();
            String body;
            try(InputStream stream=(status>=200&&status<300)
                ?conn.getInputStream():conn.getErrorStream()){
                body=stream==null?"":readLimited(stream,120000);
            }
            if(status!=200){
                String reason="HTTP "+status;
                try{
                    JSONObject e=new JSONObject(body).getJSONObject("error");
                    reason+=": "+e.optString("message","request rejected");
                }catch(JSONException ignored){}
                throw new IOException(reason.length()>280?reason.substring(0,280):reason);
            }
            JSONObject result=new JSONObject(body);
            String raw=result.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content");
            String normalized=AiguideScene.validate(raw);
            log(idea,normalized,model);
            return normalized;
        }finally{
            conn.disconnect();
        }
    }

    private String template() throws IOException{
        try(InputStream input=context.getAssets().open("aiguide_template.md")){
            return readLimited(input,10000);
        }
    }
    private static String readLimited(InputStream input,int maxBytes)throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];
        int count;
        while((count=input.read(buffer))!=-1){
            if(out.size()+count>maxBytes)throw new IOException("Server response too long");
            out.write(buffer,0,count);
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }
    private void log(String prompt,String response,String model){
        try{
            File directory=new File(context.getFilesDir(),"aiguide-logs");
            if(!directory.exists()&&!directory.mkdirs())return;
            SimpleDateFormat day=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT);
            SimpleDateFormat timestamp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ROOT);
            TimeZone utc=TimeZone.getTimeZone("UTC");
            day.setTimeZone(utc);timestamp.setTimeZone(utc);
            Date now=new Date();
            JSONObject entry=new JSONObject();
            entry.put("timestamp",timestamp.format(now));
            entry.put("user",prompt);
            entry.put("assistant",response);
            entry.put("reasoning",JSONObject.NULL);
            entry.put("model",model);
            entry.put("library","bspippi1337/aiguide-dev@"+SOURCE_COMMIT);
            File file=new File(directory,day.format(now)+".jsonl");
            try(FileOutputStream output=new FileOutputStream(file,true)){
                output.write((entry.toString()+"\n").getBytes(StandardCharsets.UTF_8));
            }
        }catch(IOException|JSONException ignored){
            // Logging failures must never discard a validated artwork.
        }
    }
}
