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
import android.text.InputType;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import no.blckswan.intelligentkunst.ai.AiguideClient;
import no.blckswan.intelligentkunst.ai.AiguideScene;
import no.blckswan.intelligentkunst.core.ArtDirector;
import no.blckswan.intelligentkunst.core.Artwork;
import no.blckswan.intelligentkunst.core.CritiqueReport;
import no.blckswan.intelligentkunst.core.DailyEdition;
import no.blckswan.intelligentkunst.core.PixelCritic;
import no.blckswan.intelligentkunst.render.RenderEngine;
import no.blckswan.intelligentkunst.render.StudioDirector;
import no.blckswan.intelligentkunst.storage.ArtArchive;
import no.blckswan.intelligentkunst.ui.ArtworkView;

/** KUNST / native procedural studio + explicit opt-in AIGUIDE cloud composition. */
public final class MainActivity extends Activity {
    private static final int INK=0xFFF6F4ED;
    private static final int QUIET=0xFFB1B4B5;
    private static final int ACID=0xFFD9FB81;
    private static final int DARK=0xFF121519;
    private final ArtDirector director=new ArtDirector();
    private final StudioDirector studio=new StudioDirector();
    private final ExecutorService composer=Executors.newSingleThreadExecutor();
    private final AtomicInteger compositionToken=new AtomicInteger();
    private final ExecutorService exports=Executors.newSingleThreadExecutor();
    private final SecureRandom random=new SecureRandom();

    private ArtArchive archive;
    private Artwork artwork;
    private ArtworkView gallery;
    private TextView title, metadata, narration, status, edition;
    private TextView createButton, evolveButton, aiButton, likeButton, exportButton, archiveButton;
    private LinearLayout top, bottom;
    private boolean exporting=false;
    private boolean composing=false;
    private boolean theaterMode=false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        archive=new ArtArchive(this);
        constructUi();
        // Debug-only integration fixture: renders the same bundled scene the user can
        // open from the menu. Never exposed by a production build intent.
        if((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0
            && getIntent().getBooleanExtra("ik_visual_fixture",false)){
            loadDemoScene();return;
        }
        Artwork last=archive.current();
        if(last!=null && (last.style==Artwork.Style.FABLE || last.style==Artwork.Style.AIGUIDE))show(last,false);
        else newArtwork(); // old works remain in the archive; first launch shows the new painter
    }

    private void constructUi() {
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(0xFF050609);
        gallery=new ArtworkView(this);
        gallery.setContentDescription("Generativt kunstverk. Berør for lys. Trykk for fullskjerm.");
        gallery.setOnClickListener(v->toggleTheater());
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

        edition=text("ATELIER 005     /     LOKALT + VALGFRI AI",9,ACID,Typeface.NORMAL);
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
        aiButton=button("✦  AI",false,false);
        primaryRow.addView(createButton,weighted(1.16f,0));
        primaryRow.addView(space(dp(7)));
        primaryRow.addView(evolveButton,weighted(.95f,0));
        primaryRow.addView(space(dp(7)));
        primaryRow.addView(aiButton,weighted(.70f,0));
        createButton.setOnClickListener(v->newArtwork());
        evolveButton.setOnClickListener(v->evolveArtwork());
        aiButton.setOnClickListener(v->openAiAtelier(null));

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
        likeButton.setOnLongClickListener(v->{reject();return true;});
        likeButton.setContentDescription("Trykk for å lære kunstneren et positivt eksempel. Hold inne for å avvise og skape et nytt verk.");
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
    private void newArtwork(){compose(null);}
    private void evolveArtwork(){
        if(artwork!=null && artwork.style==Artwork.Style.AIGUIDE){
            openAiAtelier(AiguideScene.intent(artwork.sceneJson));return;
        }
        compose(artwork);
    }

    /** One in-flight job; rendering and evaluation never block the UI thread. */
    private void compose(Artwork parent){
        if(composing)return;
        composing=true;
        createButton.setEnabled(false);evolveButton.setEnabled(false);aiButton.setEnabled(false);
        createButton.setText("…  TENKER");
        status.setText("MALER 12 ULIKE KANDIDATER  ·  SER PÅ FERDIGE BILDER");
        final long seed=freshSeed();
        final List<Artwork> history=archive.recent();
        final ArtDirector.Taste taste=archive.taste();
        final int request=compositionToken.incrementAndGet();
        composer.execute(()->{
            try{
                StudioDirector.Selection result=studio.create(seed,history,taste,parent);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;
                    createButton.setEnabled(true);evolveButton.setEnabled(true);aiButton.setEnabled(true);
                    createButton.setText("✳  SKAP NYTT");
                    show(result.artwork,true);
                    status.setText(result.proposals + " BILDER SETT  ·  BILDE + KONSEPT-KRITIKK  "
                        + Math.round(result.artwork.score*100) + "/100");
                });
            }catch(RuntimeException|OutOfMemoryError error){
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;
                    createButton.setEnabled(true);evolveButton.setEnabled(true);aiButton.setEnabled(true);
                    createButton.setText("✳  SKAP NYTT");
                    status.setText("KUNSTNEREN MÅTTE AVBRYTE  ·  TRYKK IGJEN");
                });
            }
        });
    }
    /** Optional: API access is explicit; no credentials are saved on the phone. */
    private void openAiAtelier(String revisionHint){
        if(composing){shortToast("Kunstneren arbeider allerede.");return;}
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20),dp(8),dp(20),dp(4));
        EditText idea=new EditText(this);
        idea.setTextSize(14);
        idea.setMinLines(2);idea.setMaxLines(4);
        idea.setHint("Gi et tema, eller la modellen finne på noe nytt");
        if(revisionHint!=null)idea.setText("Lag en radikalt annerledes visuell tolkning av: "
            +revisionHint);
        form.addView(idea,new LinearLayout.LayoutParams(-1,-2));
        EditText key=new EditText(this);
        key.setTextSize(13);
        key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        key.setHint("OpenAI API-nøkkel (bare denne økten)");
        form.addView(key,new LinearLayout.LayoutParams(-1,-2));
        EditText model=new EditText(this);
        model.setTextSize(13);
        model.setSingleLine(true);
        model.setText("gpt-6.1-terra");
        model.setHint("Modell-ID");
        form.addView(model,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this)
            .setTitle("AI ATELIER · aiguide")
            .setMessage("Valgfri nettmodus. Din tekst sendes til modellleverandøren over HTTPS. "
                +"Nøkkelen lagres aldri; bare forespørsel og ferdig bildemanus logges "
                +"privat på telefonen. API-bruk kan koste penger.")
            .setView(form)
            .setNegativeButton("Avbryt",null)
            .setPositiveButton("Komponer",(d,w)->{
                String prompt=idea.getText().toString().trim();
                String apiKey=key.getText().toString().trim();
                String modelId=model.getText().toString().trim();
                key.getText().clear();
                generateAi(prompt,apiKey,modelId);
            }).show();
    }

    private void generateAi(String prompt,String apiKey,String modelId){
        if(composing)return;
        if(apiKey.isEmpty()) {shortToast("API-nøkkel er påkrevd i nettmodus.");return;}
        composing=true;
        createButton.setEnabled(false);evolveButton.setEnabled(false);aiButton.setEnabled(false);
        aiButton.setText("…  AI");
        status.setText("AIGUIDE FORFATTER ET ORIGINALT BILDEMANUS…");
        final int request=compositionToken.incrementAndGet();
        final long seed=freshSeed();
        composer.execute(()->{
            try{
                String scene=new AiguideClient(getApplicationContext()).ask(prompt,apiKey,modelId);
                Artwork draft=new Artwork(seed,Artwork.Style.AIGUIDE,0,.5,.5,.5,.5,0,0,scene);
                PixelCritic.Analysis image=StudioDirector.inspect(draft);
                Artwork completed=new Artwork(seed,Artwork.Style.AIGUIDE,0,.5,.5,.5,.5,
                    image.score,0,scene);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;
                    createButton.setEnabled(true);evolveButton.setEnabled(true);aiButton.setEnabled(true);
                    aiButton.setText("✦  AI");
                    show(completed,true);
                    status.setText("AI-MANUS TEGNET I APPEN  ·  BILDEANALYSE "
                        + Math.round(image.score*100) + "/100");
                });
            }catch(Exception|OutOfMemoryError ex){
                final String reason=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;
                    createButton.setEnabled(true);evolveButton.setEnabled(true);aiButton.setEnabled(true);
                    aiButton.setText("✦  AI");
                    status.setText("AI-ATELIER STOPPET · OFFLINE-MODUS FUNGERER FORTSATT");
                    new AlertDialog.Builder(this).setTitle("AI-bilde ble ikke ferdig")
                        .setMessage(reason.length()>350?reason.substring(0,350):reason)
                        .setPositiveButton("OK",null).show();
                });
            }
        });
    }

    private String displayTitle(Artwork piece){
        return piece.style==Artwork.Style.AIGUIDE
            ? AiguideScene.title(piece.sceneJson) : piece.title();
    }

    /** A deliberately hand-authored renderer demo. Never presented as AI output. */
    private void loadDemoScene(){
        if(composing){shortToast("Kunstneren arbeider allerede.");return;}
        composing=true;
        status.setText("ÅPNER ET EKSEMPEL UTEN NETT ELLER API-NØKKEL…");
        final int request=compositionToken.incrementAndGet();
        composer.execute(()->{
            try(InputStream input=getAssets().open("demo_scene.json");
                ByteArrayOutputStream bytes=new ByteArrayOutputStream()){
                byte[] buffer=new byte[4096];int n;
                while((n=input.read(buffer))!=-1){
                    if(bytes.size()+n>18000)throw new IOException("Demo is too large");
                    bytes.write(buffer,0,n);
                }
                String json=AiguideScene.validate(new String(bytes.toByteArray(),StandardCharsets.UTF_8));
                long seed=freshSeed();
                Artwork draft=new Artwork(seed,Artwork.Style.AIGUIDE,0,.5,.5,.5,.5,0,0,json);
                PixelCritic.Analysis visual=StudioDirector.inspect(draft);
                Artwork ready=new Artwork(seed,Artwork.Style.AIGUIDE,0,.5,.5,.5,.5,visual.score,0,json);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;show(ready,true);
                    status.setText("DEMO · MANUELT KOMPONERT · IKKE AI-GENERERT");
                });
            }catch(Exception|OutOfMemoryError error){
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||request!=compositionToken.get())return;
                    composing=false;status.setText("DEMO KUNNE IKKE ÅPNES");
                    shortToast("Feil i lokal demo. Rapportér dette som en bug.");
                });
            }
        });
    }

    private void importAiScene(){
        EditText input=new EditText(this);
        input.setMinLines(4);input.setMaxLines(9);input.setTextSize(12);
        input.setHint("{\"title\":\"...\",\"background\":\"#...\",\"elements\":[...]}");
        FrameLayout wrap=new FrameLayout(this);
        wrap.setPadding(dp(18),0,dp(18),0);
        wrap.addView(input,new FrameLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("Importer aiguide JSON-manus")
            .setMessage("Samme format som Python-bibliotekets CLI-bro. Import gjøres offline.")
            .setView(wrap)
            .setNegativeButton("Avbryt",null)
            .setPositiveButton("Tegn",(d,w)->{
                try{
                    String normalized=AiguideScene.validate(input.getText().toString().trim());
                    Artwork next=new Artwork(freshSeed(),Artwork.Style.AIGUIDE,0,
                        .5,.5,.5,.5,0,0,normalized);
                    show(next,true);
                }catch(Exception ex){shortToast("Ugyldig bildemanus: se formatkravene.");}
            }).show();
    }

    private void show(Artwork next,boolean save){
        artwork=next;
        if(save)archive.remember(next);
        gallery.setArtwork(next);
        metadata.setText(next.style.label + "   /   GEN " + String.format(Locale.ROOT,"%02d",next.generation)
            + "   /   " + next.id().substring(0,6));
        title.setText(displayTitle(next));
        narration.setText(next.style==Artwork.Style.AIGUIDE
            ? AiguideScene.intent(next.sceneJson) : next.intention());
        status.setText((next.style==Artwork.Style.AIGUIDE ? "BILDEANALYSE   " : "VERKETS SELEKSJONSSCORE   ")
            + Math.round(next.score*100) + "/100"
            + (next.hasParent ? "   ·   FRA " + String.format(Locale.ROOT,"%016X",next.parentSeed).substring(0,6) : ""));
        edition.setText("ATELIER 005     /     VERK " + archive.recent().size()
            + (next.style==Artwork.Style.AIGUIDE ? " · AIGUIDE" : " · LOKALT"));
        likeButton.setText(archive.isSaved(next) ? "♥  LÆRT" :
            archive.isRejected(next) ? "✕  AVVIST" : "♡  LÆR");
    }
    private void like(){
        if(artwork==null)return;
        boolean learned=archive.teach(artwork);
        likeButton.setText("♥  LÆRT");
        shortToast(learned ? "Smaken din er lagret lokalt. Verket er i samlingen."
                           : "Dette verket har allerede lært kunstneren noe.");
    }

    private void reject(){
        if(artwork==null||composing)return;
        if(!archive.reject(artwork)){
            shortToast("Dette verket har allerede fått en tilbakemelding.");return;
        }
        shortToast("Registrert: denne ideen skal veie mindre. Skaper et nytt verk.");
        newArtwork();
    }

    private void showDnaMenu(){
        new AlertDialog.Builder(this).setTitle("KUNSTENS DNA")
            .setItems(new String[]{"Dagens verk · samme for alle (UTC)", "Kunstnerens egenkritikk",
                "Finn forelderverket", "Kopier verket som DNA", "Importer et DNA",
                "Importer AI-bildemanus (JSON)", "Se tegnemotoren · ingen API-nøkkel",
                "Om eksperimentet"},
                (d,which)->{
                    if(which==0)dailyEdition();
                    else if(which==1)showCritique();
                    else if(which==2)openParent();
                    else if(which==3&&artwork!=null){
                        ClipboardManager clips=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        clips.setPrimaryClip(ClipData.newPlainText("Kunst-DNA",artwork.encode()));
                        shortToast("DNA kopiert. Dette kan gjenskape verket.");
                    }else if(which==4)importDna();
                    else if(which==5)importAiScene();
                    else if(which==6)loadDemoScene();
                    else if(which==7)about();
                }).show();
    }

    private void openParent(){
        if(artwork==null||!artwork.hasParent){shortToast("Dette verket har ingen registrert forelder.");return;}
        Artwork parent=archive.findBySeed(artwork.parentSeed);
        if(parent!=null)show(parent,true);
        else new AlertDialog.Builder(this).setTitle("Et spor i historien")
            .setMessage("Forelderens DNA-ID er " + String.format(Locale.ROOT,"%016X",artwork.parentSeed)
                + ". Det opprinnelige verket ligger ikke lenger i det lokale arkivet.")
            .setPositiveButton("OK",null).show();
    }

    private void dailyEdition(){
        long utcDay=Math.floorDiv(System.currentTimeMillis(),86_400_000L);
        show(DailyEdition.ofUtcDay(utcDay),true);
        shortToast("Dagens original: samme DNA på alle enheter, uten internett.");
    }

    private void showCritique(){
        if(artwork==null)return;
        final Artwork subject=artwork;
        final CritiqueReport dna=CritiqueReport.evaluate(subject,archive.recent(),archive.taste());
        status.setText("UNDERSØKER FAKTISKE BILDEPIKSLER…");
        composer.execute(()->{
            try{
                final PixelCritic.Analysis pixels=StudioDirector.inspect(subject);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    if(artwork==subject)status.setText("BILDEANALYSE FULLFØRT");
                    new AlertDialog.Builder(this).setTitle("Første ekte visuelle kritikk")
                        .setMessage("UTVALGSSCORE: " + Math.round(subject.score*100) + "/100\n\n"
                            + pixels.explain() + "\n\n"
                            + "DNA-RUBRIKK (UTEN BILDE): " + Math.round(dna.total*100) + "/100\n\n"
                            + (subject.hasParent ? "OPPHAV: " + String.format(Locale.ROOT,"%016X",subject.parentSeed) + "\n\n" : "")
                            + "Appen måler bildestruktur, ikke skjønnhet eller mening. "
                            + "De narrative motivene er komponert fra et åpent regelsett.")
                        .setPositiveButton("Tilbake",null).show();
                });
            }catch(RuntimeException|OutOfMemoryError error){
                runOnUiThread(()->shortToast("Kunne ikke analysere dette verket."));
            }
        });
    }

    private void toggleTheater(){
        theaterMode=!theaterMode;
        top.setVisibility(theaterMode?View.GONE:View.VISIBLE);
        bottom.setVisibility(theaterMode?View.GONE:View.VISIBLE);
        if(theaterMode)shortToast("UTSTILLINGSMODUS · trykk på bildet for å gå tilbake");
    }
    private void importDna(){
        EditText input=new EditText(this);input.setSingleLine(false);
        input.setMaxLines(4);input.setTextSize(13);
        input.setHint("DNA v1, v2 eller v3 (inkludert AI-tegning)");
        FrameLayout wrap=new FrameLayout(this);wrap.setPadding(dp(20),0,dp(20),0);
        wrap.addView(input,new FrameLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("Importer DNA")
            .setMessage("Lim inn DNA fra en annen enhet. Ingen server kreves.")
            .setView(wrap).setNegativeButton("Avbryt",null)
            .setPositiveButton("Åpne",(dialog,which)->{
                try{
                    String source=input.getText().toString().trim();
                    if(source.length()>30000)throw new IllegalArgumentException("DNA too long");
                    Artwork imported=Artwork.decode(source);
                    if(imported.style==Artwork.Style.AIGUIDE)
                        AiguideScene.validate(imported.sceneJson);
                    show(imported,true);
                }catch(Exception ex){shortToast("Ugyldig DNA. Ingen endring ble gjort.");}
            }).show();
    }
    private void about(){
        new AlertDialog.Builder(this).setTitle("Et kunstig blikk")
            .setMessage("Kan et program utvikle et eget kunstnerisk uttrykk?\n\n"
                + "ATELIER 003 skisserer 12 bilder, hvert fra 42 interne DNA-forslag. "
                + "Bildene males i miniatyr, undersøkes og sammenliknes. Kontrast, fokus, "
                + "negative rom, markeringer og visuell ulikhet påvirker hvilket bilde som velges.\n\n"
                + "Verket begynner med en fysisk idé: et vanlig objekt bryter én forståelig "
                + "regel om tyngde, skygge, helhet, størrelse eller hukommelse. "
                + "Den valgte regelen endrer selve illustrasjonen.\n\n"
                + "Det er en kombinatorisk kunstgrammatikk, ikke fri idétenkning. "
                + "Den bruker ingen av dine tidligere prosjekter eller motiver.\n\n"
                + "LÆR gir én positiv tilbakemelding. Hold LÆR inne for å AVVISE "
                + "motivet og ideen, og skape et nytt forsøk.\n\n"
                + "Offline-modusen er regelbasert og fungerer uten nett. "
                + "AI-knappen bruker aiguide-mal og en ekstern tekstmodell som lager et konkret "
                + "tegnbart bildemanus. Bruk av API krever egen nøkkel og nettverk. "
                + "Du kan importere et bildemanus fra Python-aiguide uten API-kall i appen. "
                + "Nøkler blir ikke lagret; synlig AI-forespørsel og resultat får lokal logg. "
                + "Bildescoren er bare strukturell analyse, ikke kunstnerisk forståelse.")
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
            entries[i]=displayTitle(a)+"\n"+a.style.label+" · "+a.id().substring(0,8);
        }
        new AlertDialog.Builder(this).setTitle("Din samling · "+collection.size()+" verk")
            .setItems(entries,(d,choice)->show(collection.get(choice),true))
            .setNeutralButton("Slett et verk",(d,w)->deleteFromArchive(collection))
            .setNegativeButton("Lukk",null).show();
    }
    private void deleteFromArchive(List<Artwork> collection){
        String[] names=new String[collection.size()];
        for(int i=0;i<names.length;i++)names[i]=displayTitle(collection.get(i));
        new AlertDialog.Builder(this).setTitle("Velg verk å fjerne")
            .setItems(names,(d,index)->{
                Artwork target=collection.get(index);
                new AlertDialog.Builder(this).setTitle("Fjerne fra samlingen?")
                    .setMessage(displayTitle(target)+" slettes bare fra den lokale samlingen, ikke fra eventuelle eksporterte PNG-filer.")
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
        compositionToken.incrementAndGet();
        composer.shutdownNow();
        exports.shutdown();super.onDestroy();
    }
}
