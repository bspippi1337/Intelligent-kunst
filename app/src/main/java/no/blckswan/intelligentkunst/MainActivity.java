package no.blckswan.intelligentkunst;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import no.blckswan.intelligentkunst.core.ArtDirector;
import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.render.RenderEngine;
import no.blckswan.intelligentkunst.storage.ArtArchive;
import no.blckswan.intelligentkunst.ui.ArtworkView;

/** KUNST / a deliberately offline, native, autonomous generative artist. */
public final class MainActivity extends Activity {
    private static final int INK=0xFFF6F4ED;
    private static final int QUIET=0xFFB1B4B5;
    private static final int ACID=0xFFD9FB81;
    private static final int DARK=0xFF121519;
    private final ArtDirector director=new ArtDirector();
    private final ExecutorService exports=Executors.newSingleThreadExecutor();
    private final SecureRandom random=new SecureRandom();

    private ArtArchive archive;
    private Artwork artwork;
    private ArtworkView gallery;
    private TextView title, metadata, narration, status, edition;
    private TextView createButton, evolveButton, likeButton, exportButton, archiveButton;
    private LinearLayout top, bottom;
    private boolean exporting=false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        archive=new ArtArchive(this);
        constructUi();
        Artwork last=archive.current();
        if(last!=null)show(last,false);
        else newArtwork();
    }

    private void constructUi() {
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(0xFF050609);
        gallery=new ArtworkView(this);
        gallery.setContentDescription("Generativt kunstverk. Berør lerretet for å flytte lyset.");
        root.addView(gallery,new FrameLayout.LayoutParams(-1,-1));

        top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(23),dp(25),dp(23),dp(20));
        top.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xF007090D,0xC507090D,0x0007090D}));
        FrameLayout.LayoutParams header=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);
        root.addView(top,header);

        LinearLayout brandRow=new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(brandRow,new LinearLayout.LayoutParams(-1,-2));
        TextView mark=text("IK.",24,ACID,Typeface.BOLD);
        mark.setLetterSpacing(-.07f);
        brandRow.addView(mark,new LinearLayout.LayoutParams(dp(58),dp(42)));
        LinearLayout brandWords=new LinearLayout(this);brandWords.setOrientation(LinearLayout.VERTICAL);
        brandRow.addView(brandWords,new LinearLayout.LayoutParams(0,-2,1));
        TextView name=text("INTELLIGENT KUNST",13,INK,Typeface.BOLD);
        name.setLetterSpacing(.21f);brandWords.addView(name);
        TextView kind=text("AN AUTONOMOUS ART EXPERIMENT",8,QUIET,Typeface.NORMAL);
        kind.setLetterSpacing(.15f);kind.setPadding(0,dp(5),0,0);brandWords.addView(kind);
        TextView dna=button("DNA / ?",false,true);
        dna.setContentDescription("Vis kunstverkets DNA, importer eller les om eksperimentet");
        dna.setOnClickListener(v->showDnaMenu());
        brandRow.addView(dna,new LinearLayout.LayoutParams(dp(90),dp(40)));

        edition=text("ATELIER 001     /     LOKALT · UTEN SKY",9,ACID,Typeface.NORMAL);
        edition.setLetterSpacing(.13f);edition.setPadding(0,dp(21),0,0);
        top.addView(edition);

        bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(23),dp(90),dp(23),dp(24));
        bottom.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0x00050609,0xCC050609,0xFF050609}));
        root.addView(bottom,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));

        metadata=text("",10,ACID,Typeface.BOLD);
        metadata.setLetterSpacing(.22f);bottom.addView(metadata);
        title=text("",31,INK,Typeface.NORMAL);
        title.setTypeface(Typeface.create("serif",Typeface.NORMAL));
        title.setPadding(0,dp(10),0,dp(3));
        title.setMaxLines(2);
        bottom.addView(title);
        narration=text("",12,QUIET,Typeface.NORMAL);
        bottom.addView(narration);
        status=text("",10,QUIET,Typeface.NORMAL);
        status.setPadding(0,dp(13),0,dp(20));bottom.addView(status);

        LinearLayout primaryRow=new LinearLayout(this);
        bottom.addView(primaryRow,new LinearLayout.LayoutParams(-1,dp(55)));
        createButton=button("✳  SKAP NYTT",true,false);
        evolveButton=button("↗  EVOLVER",false,false);
        primaryRow.addView(createButton,weighted(1.35f,0));
        primaryRow.addView(space(dp(9)));
        primaryRow.addView(evolveButton,weighted(1f,0));
        createButton.setOnClickListener(v->newArtwork());
        evolveButton.setOnClickListener(v->evolveArtwork());

        LinearLayout secondaryRow=new LinearLayout(this);
        LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(-1,dp(45));
        secondParams.topMargin=dp(10);bottom.addView(secondaryRow,secondParams);
        likeButton=button("♡  LÆR",false,true);
        exportButton=button("⇩  PNG",false,true);
        archiveButton=button("▦  SAMLING",false,true);
        secondaryRow.addView(likeButton,weighted(1,0));
        secondaryRow.addView(space(dp(9)));
        secondaryRow.addView(exportButton,weighted(1,0));
        secondaryRow.addView(space(dp(9)));
        secondaryRow.addView(archiveButton,weighted(1.1f,0));
        likeButton.setOnClickListener(v->like());
        exportButton.setOnClickListener(v->export());
        archiveButton.setOnClickListener(v->openArchive());

        root.setOnApplyWindowInsetsListener((v,insets)->{
            int t,b;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets sys=insets.getInsets(WindowInsets.Type.systemBars());
                t=sys.top;b=sys.bottom;
            } else {t=insets.getSystemWindowInsetTop();b=insets.getSystemWindowInsetBottom();}
            top.setPadding(dp(23),dp(25)+t,dp(23),dp(20));
            bottom.setPadding(dp(23),dp(90),dp(23),dp(24)+b);
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
    }

    private View space(int width){ View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(width,1));return v; }
    private LinearLayout.LayoutParams weighted(float weight,int margin){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,weight);lp.leftMargin=margin;return lp;
    }
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private TextView text(String s,int size,int color,int weight){
        TextView v=new TextView(this);
        v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);
        v.setTypeface(Typeface.create("sans-serif",weight));
        v.setIncludeFontPadding(false);
        return v;
    }
    private TextView button(String caption,boolean accent,boolean small){
        TextView v=text(caption,small?10:12,accent?0xFF0B1207:INK,Typeface.BOLD);
        v.setGravity(Gravity.CENTER);v.setLetterSpacing(.055f);
        v.setClickable(true);v.setFocusable(true);
        v.setContentDescription(caption);
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(accent?ACID:0xEB171B20);
        bg.setCornerRadius(dp(small?12:15));
        if(!accent)bg.setStroke(dp(1),0xFF32383E);
        v.setBackground(bg);
        v.setOnTouchListener((view,event)->{
            switch(event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN: view.setAlpha(.68f);break;
                case android.view.MotionEvent.ACTION_CANCEL:
                case android.view.MotionEvent.ACTION_UP: view.setAlpha(1f);break;
            }
            return false;
        });
        return v;
    }

    private long freshSeed(){return random.nextLong() ^ SystemClock.elapsedRealtimeNanos();}
    private void newArtwork(){show(director.create(freshSeed(),archive.recent(),archive.taste()),true);}
    private void evolveArtwork(){
        if(artwork==null){newArtwork();return;}
        show(director.evolve(artwork,freshSeed(),archive.recent(),archive.taste()),true);
    }
    private void show(Artwork next,boolean save){
        artwork=next;
        if(save)archive.remember(next);
        gallery.setArtwork(next);
        metadata.setText(next.style.label + "   /   GEN " + String.format(Locale.ROOT,"%02d",next.generation)
            + "   /   " + next.id().substring(0,6));
        title.setText(next.title());
        narration.setText(next.style.thought);
        status.setText("42 SKISSER VURDERT   ·   ESTETISK HEURISTIKK "
            + Math.round(next.score*100) + "/100");
        edition.setText("ATELIER 001     /     VERK " + archive.recent().size() + " · UTEN SKY");
        likeButton.setText("♡  LÆR");
    }
    private void like(){
        if(artwork==null)return;
        archive.teach(artwork);
        likeButton.setText("♥  LÆRT");
        shortToast("Smaken din er lagret lokalt. Verket er i samlingen.");
    }

    private void showDnaMenu(){
        new AlertDialog.Builder(this).setTitle("KUNSTENS DNA")
            .setItems(new String[]{"Kopier verket som DNA", "Importer et DNA", "Hvordan tenker kunstneren?"},
                (d,which)->{
                    if(which==0&&artwork!=null){
                        ClipboardManager clips=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        clips.setPrimaryClip(ClipData.newPlainText("Kunst-DNA",artwork.encode()));
                        shortToast("DNA kopiert. Dette kan gjenskape verket.");
                    }else if(which==1)importDna();else if(which==2)about();
                }).show();
    }
    private void importDna(){
        EditText input=new EditText(this);input.setSingleLine(false);
        input.setMaxLines(4);input.setTextSize(13);
        input.setHint("1:SEED:STYLE:PALETTE:...");
        FrameLayout wrap=new FrameLayout(this);wrap.setPadding(dp(20),0,dp(20),0);
        wrap.addView(input,new FrameLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("Importer DNA")
            .setMessage("Lim inn DNA fra en annen enhet. Ingen server kreves.")
            .setView(wrap).setNegativeButton("Avbryt",null)
            .setPositiveButton("Åpne",(dialog,which)->{
                try{
                    String source=input.getText().toString().trim();
                    if(source.length()>200)throw new IllegalArgumentException("DNA too long");
                    show(Artwork.decode(source),true);
                }catch(RuntimeException ex){shortToast("Ugyldig DNA. Ingen endring ble gjort.");}
            }).show();
    }
    private void about(){
        new AlertDialog.Builder(this).setTitle("Et kunstig blikk")
            .setMessage("Kan et program utvikle et eget kunstnerisk uttrykk?\n\n"
                + "Hver gang du trykker SKAP, foreslår kunstneren 42 komposisjoner. "
                + "En åpent definert estetisk heuristikk vurderer rytme, tetthet, asymmetri, "
                + "bevegelse og forskjellen fra tidligere verk.\n\n"
                + "LÆR påvirker neste utvalg. EVOLVER muterer DNA-et til det valgte bildet. "
                + "Alle verk kan gjenskapes fra samme DNA.\n\n"
                + "Dette er evolusjonær generativ kunst, ikke en språkmodell eller en objektiv "
                + "dom over kunst. Ingen konto, server, sporing eller skjulte API-er.")
            .setPositiveButton("Tilbake til atelieret",null).show();
    }
    private void openArchive(){
        List<Artwork> collection=archive.saved();
        if(collection.isEmpty()){
            new AlertDialog.Builder(this).setTitle("Samlingen er tom")
                .setMessage("Trykk ♡ LÆR for å samle et verk og lære kunstneren hva du liker.")
                .setPositiveButton("Greit",null).show();return;
        }
        String[] entries=new String[collection.size()];
        for(int i=0;i<entries.length;i++){
            Artwork a=collection.get(i);
            entries[i]=a.title()+"\n"+a.style.label+" · "+a.id().substring(0,8);
        }
        new AlertDialog.Builder(this).setTitle("Din samling · "+collection.size()+" verk")
            .setItems(entries,(d,choice)->show(collection.get(choice),true))
            .setNeutralButton("Slett et verk",(d,w)->deleteFromArchive(collection))
            .setNegativeButton("Lukk",null).show();
    }
    private void deleteFromArchive(List<Artwork> collection){
        String[] names=new String[collection.size()];
        for(int i=0;i<names.length;i++)names[i]=collection.get(i).title();
        new AlertDialog.Builder(this).setTitle("Velg verk å fjerne")
            .setItems(names,(d,index)->{
                Artwork target=collection.get(index);
                new AlertDialog.Builder(this).setTitle("Fjerne fra samlingen?")
                    .setMessage(target.title()+" slettes bare fra den lokale samlingen, ikke fra eventuelle eksporterte PNG-filer.")
                    .setPositiveButton("Slett",(confirm,which)->{archive.delete(target);shortToast("Fjernet fra samlingen");})
                    .setNegativeButton("Avbryt",null).show();
            }).setNegativeButton("Avbryt",null).show();
    }

    private void export(){
        if(artwork==null||exporting)return;
        final Artwork target=artwork;
        exporting=true;exportButton.setText("…  LAGRER");exportButton.setEnabled(false);
        status.setText("RENDERER PNG I 2160 × 3840   ·   HELE VERKET");
        exports.execute(()->{
            Uri uri=null;
            try{
                uri=savePng(target,2160,3840);
                final Uri completed=uri;
                runOnUiThread(()->{
                    exporting=false;exportButton.setEnabled(true);exportButton.setText("⇩  PNG");
                    status.setText("EKSPORTERT TIL BILDER · "+target.id().substring(0,8));
                    new AlertDialog.Builder(this).setTitle("Verket er lagret")
                        .setMessage("PNG i 2160 × 3840 er lagret i Bilder / Intelligent Kunst. Vil du dele verket?")
                        .setPositiveButton("Del",(d,which)->share(completed))
                        .setNegativeButton("Tilbake",null).show();
                });
            }catch(Exception|OutOfMemoryError e){
                runOnUiThread(()->{
                    exporting=false;exportButton.setEnabled(true);exportButton.setText("⇩  PNG");
                    status.setText("EKSPORT FEILET · PRØV IGJEN");
                    new AlertDialog.Builder(this).setTitle("Kunne ikke eksportere")
                        .setMessage("Lagringen ble avbrutt: "+e.getClass().getSimpleName())
                        .setPositiveButton("OK",null).show();
                });
            }
        });
    }
    private Uri savePng(Artwork piece,int w,int h) throws IOException {
        Bitmap bitmap=null;
        Uri uri=null;
        try {
            bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            new RenderEngine().draw(new Canvas(bitmap),w,h,piece);
            ContentValues info=new ContentValues();
            info.put(MediaStore.Images.Media.DISPLAY_NAME,"IntelligentKunst_"+piece.id()+".png");
            info.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
            info.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Intelligent Kunst");
            info.put(MediaStore.Images.Media.IS_PENDING,1);
            uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,info);
            if(uri==null)throw new IOException("MediaStore unavailable");
            try(OutputStream out=getContentResolver().openOutputStream(uri)){
                if(out==null||!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))
                    throw new IOException("PNG writing failed");
            }
            ContentValues ready=new ContentValues();ready.put(MediaStore.Images.Media.IS_PENDING,0);
            getContentResolver().update(uri,ready,null,null);
            return uri;
        }catch(Exception|OutOfMemoryError e){
            if(uri!=null)getContentResolver().delete(uri,null,null);
            if(e instanceof IOException)throw (IOException)e;
            if(e instanceof OutOfMemoryError)throw (OutOfMemoryError)e;
            throw new IOException("Could not render or save image",e);
        }finally{if(bitmap!=null)bitmap.recycle();}
    }
    private void share(Uri image){
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("image/png");send.putExtra(Intent.EXTRA_STREAM,image);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send,"Del kunstverket"));
    }
    private void shortToast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
    @Override protected void onDestroy(){
        if(gallery!=null)gallery.dispose();
        exports.shutdown();super.onDestroy();
    }
}
