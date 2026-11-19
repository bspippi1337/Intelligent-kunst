package no.blckswan.intelligentkunst.storage;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

import no.blckswan.intelligentkunst.core.ArtDirector;
import no.blckswan.intelligentkunst.core.Artwork;

/** Small, private archive. Preferences never leave the handset. */
public final class ArtArchive {
    private static final int LIMIT=100;
    private static final int LINEAGE_LIMIT=300;
    private final SharedPreferences store;
    public ArtArchive(Context context) {
        store=context.getSharedPreferences("artist_archive_v1",Context.MODE_PRIVATE);
    }
    public List<Artwork> saved(){return read("saved");}
    public List<Artwork> recent(){return read("recent");}
    private List<Artwork> read(String key) {
        ArrayList<Artwork> result=new ArrayList<>();
        try {
            JSONArray items=new JSONArray(store.getString(key,"[]"));
            for(int i=0;i<items.length();i++) {
                try {result.add(Artwork.decode(items.getString(i)));}
                catch(IllegalArgumentException ignored) { /* Broken individual records are skipped. */ }
            }
        } catch(JSONException ignored) { /* Corrupt archive starts empty. */ }
        return result;
    }
    private void write(String key,List<Artwork> items) {
        JSONArray out=new JSONArray();
        int cap="lineage".equals(key)?LINEAGE_LIMIT:LIMIT;
        for(int i=0;i<items.size()&&i<cap;i++)out.put(items.get(i).encode());
        store.edit().putString(key,out.toString()).apply();
    }
    public void remember(Artwork artwork) {
        List<Artwork> items=recent();
        items.removeIf(a->a.id().equals(artwork.id()));
        items.add(0,artwork); write("recent",items);
        List<Artwork> lineage=read("lineage");
        lineage.removeIf(a->a.id().equals(artwork.id()));
        lineage.add(0,artwork);
        write("lineage",lineage);
        store.edit().putString("current",artwork.encode()).apply();
    }
    public Artwork findBySeed(long seed) {
        for(Artwork art:read("lineage"))if(art.seed==seed)return art;
        for(Artwork art:read("recent"))if(art.seed==seed)return art;
        for(Artwork art:saved())if(art.seed==seed)return art;
        return null;
    }
    public Artwork current() {
        try {return Artwork.decode(store.getString("current",""));}
        catch(RuntimeException ignored){return null;}
    }
    public boolean isSaved(Artwork artwork) {
        for(Artwork item:saved())if(item.id().equals(artwork.id()))return true;
        return false;
    }
    public boolean save(Artwork artwork) {
        List<Artwork> items=saved();
        for(Artwork item:items)if(item.id().equals(artwork.id()))return false;
        items.add(0,artwork);write("saved",items);return true;
    }
    public void delete(Artwork artwork) {
        List<Artwork> items=saved();
        items.removeIf(a->a.id().equals(artwork.id()));write("saved",items);
    }
    public ArtDirector.Taste taste() {
        ArtDirector.Taste t=new ArtDirector.Taste();
        t.load(readVotes("style_votes",Artwork.Style.values().length),
            readVotes("palette_votes",Artwork.PALETTES.length),
            readVotes("object_votes",no.blckswan.intelligentkunst.core.StoryPlan.ObjectKind.values().length),
            readVotes("proposition_votes",no.blckswan.intelligentkunst.core.StoryPlan.Proposition.values().length));
        return t;
    }
    private int[] readVotes(String key,int size){
        int[] values=new int[size];String[] parts=store.getString(key,"").split(",");
        for(int i=0;i<size&&i<parts.length;i++){
            try {values[i]=Math.max(0,Integer.parseInt(parts[i]));}catch(NumberFormatException ignored){}
        }
        return values;
    }
    private static String join(int[] values) {
        StringBuilder s=new StringBuilder();
        for(int i=0;i<values.length;i++){if(i>0)s.append(',');s.append(values[i]);}
        return s.toString();
    }
    public boolean teach(Artwork artwork) {
        // One vote per artwork prevents endlessly inflating preferences by tapping LÆR.
        if(!save(artwork))return false;
        ArtDirector.Taste taste=taste();taste.like(artwork);
        store.edit().putString("style_votes",join(taste.styleVotes()))
            .putString("palette_votes",join(taste.paletteVotes()))
            .putString("object_votes",join(taste.objectVotes()))
            .putString("proposition_votes",join(taste.propositionVotes())).apply();
        return true;
    }
}
