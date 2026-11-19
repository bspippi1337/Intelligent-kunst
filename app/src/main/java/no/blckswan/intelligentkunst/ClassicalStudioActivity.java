package no.blckswan.intelligentkunst;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.app.ActivityManager;
import android.widget.EditText;
import android.text.InputType;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.view.WindowInsets;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import no.blckswan.intelligentkunst.render.ClassicalFigurePainter;
import no.blckswan.intelligentkunst.render.PhotoPainter;

/**
 * First-class, keyless Android atelier.
 * Figure studies are original hand-authored parametric painting.
 * Faithful photo rendering and painting studies require the user's own photo.
 * Neither operation uses a neural image generator or an internet service.
 */
public final class ClassicalStudioActivity extends Activity {
    private static final int OPEN_PHOTO=6206;
    private static final int BG=0xFF0E1415,FG=0xFFF4EBDD,ACCENT=0xFFCDA87B;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final AtomicInteger revision=new AtomicInteger();
    private final AtomicInteger paintRevision=new AtomicInteger();
    private final SecureRandom random=new SecureRandom();
    private StudioView stage;
    private TextView message,subjectLabel,sourceButton,figureButton,poseButton,saveButton,neuralButton;
    private TextView[] modes=new TextView[4];
    private Bitmap preview;
    private Uri imageUri;
    private long figureSeed=2712L;
    private boolean figure=true,exporting=false;
    private PhotoPainter.Mode activeMode=PhotoPainter.Mode.CHARCOAL;

    @Override protected void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(18),dp(16),dp(14));
        root.setBackgroundColor(BG);
        TextView back=button("‹  TILBAKE",false);
        back.setOnClickListener(v->finish());
        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(back,new LinearLayout.LayoutParams(dp(118),dp(42)));
        LinearLayout headline=new LinearLayout(this);
        headline.setOrientation(LinearLayout.VERTICAL);
        TextView title=label("KLASSISK ATELIER",17,FG,true);
        TextView kicker=label("UTEN KONTO · UTEN NETT · UTEN API-NØKKEL",8,ACCENT,false);
        kicker.setPadding(0,dp(6),0,0);
        headline.addView(title);
        headline.addView(kicker);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,-2,1);
        hp.leftMargin=dp(10);
        header.addView(headline,hp);
        root.addView(header);
        subjectLabel=label("AKADEMISK AKT / STUDIO",11,ACCENT,true);
        subjectLabel.setPadding(0,dp(13),0,dp(10));
        root.addView(subjectLabel);

        stage=new StudioView();
        LinearLayout.LayoutParams stageParams=new LinearLayout.LayoutParams(-1,0,1f);
        root.addView(stage,stageParams);
        // One responsive panel for small and landscape screens. The painting
        // stays visible while tools scroll independently.
        ScrollView controlScroll=new ScrollView(this);
        controlScroll.setFillViewport(false);
        controlScroll.setVerticalScrollBarEnabled(true);
        LinearLayout controls=new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(0,0,0,dp(8));
        controlScroll.addView(controls);
        root.addView(controlScroll,new LinearLayout.LayoutParams(-1,0,.74f));
        message=label("Originalt figurstudium. Bytt positur eller velg ditt eget foto.",11,FG,false);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(4),dp(12),dp(4),dp(12));
        controls.addView(message);

        LinearLayout modeRow=new LinearLayout(this);
        String[] captions={"FOTO", "OLJE", "KULL", "SEPIA"};
        PhotoPainter.Mode[] choices=PhotoPainter.Mode.values();
        for(int i=0;i<captions.length;i++){
            final PhotoPainter.Mode choice=choices[i];
            modes[i]=button(captions[i],false);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(44),1f);
            if(i>0)lp.leftMargin=dp(6);
            modeRow.addView(modes[i],lp);
            modes[i].setOnClickListener(v->{activeMode=choice;showSelection();});
        }
        controls.addView(modeRow);

        LinearLayout primary=new LinearLayout(this);
        primary.setPadding(0,dp(9),0,0);
        sourceButton=button("ÅPNE FOTO",true);
        figureButton=button("AKTSTUDIE",false);
        poseButton=button("NY POSITUR",false);
        primary.addView(sourceButton,weighted(1));
        primary.addView(figureButton,weighted(1));
        primary.addView(poseButton,weighted(1));
        controls.addView(primary);
        sourceButton.setOnClickListener(v->openPhoto());
        figureButton.setOnClickListener(v->{
            revision.incrementAndGet();
            figure=true;activeMode=PhotoPainter.Mode.CHARCOAL;
            subjectLabel.setText("AKADEMISK AKT / STUDIO");
            showSelection();
        });
        poseButton.setOnClickListener(v->{
            revision.incrementAndGet();
            figure=true;
            figureSeed=random.nextLong();
            subjectLabel.setText("AKADEMISK AKT / NY POSITUR");
            showSelection();
        });

        neuralButton=button("✦  LOKAL AI · GENERER MED EGEN MODELL",false);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,dp(46));
        np.topMargin=dp(9);
        controls.addView(neuralButton,np);
        neuralButton.setOnClickListener(v->openLocalNeuralDialog());
        neuralButton.setOnLongClickListener(v->{showLocalNeuralHelp();return true;});
        neuralButton.setContentDescription("Åpne lokal bildegenerering. Hold lenge for RAM- og modellinformasjon.");

        saveButton=button("↓  EKSPORTER KUNSTVERK SOM PNG",true);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(50));
        sp.topMargin=dp(9);
        controls.addView(saveButton,sp);
        saveButton.setOnClickListener(v->exportPng());

        TextView notice=label("Fotorealisme: trofast gjengivelse av ditt foto, ikke syntetisk AI-generering.",9,0xFFB5ADA0,false);
        notice.setGravity(Gravity.CENTER);
        notice.setPadding(dp(5),dp(10),dp(5),0);
        controls.addView(notice);
        // Android 15+ draws behind system bars by default. Keep all controls
        // and the bottom privacy explanation safely within the visible area.
        root.setOnApplyWindowInsetsListener((view,insets)->{
            int left=0,top=0,right=0,bottom=0;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets system=insets.getInsets(WindowInsets.Type.systemBars());
                left=system.left;top=system.top;
                right=system.right;bottom=system.bottom;
            }else{
                left=insets.getSystemWindowInsetLeft();
                top=insets.getSystemWindowInsetTop();
                right=insets.getSystemWindowInsetRight();
                bottom=insets.getSystemWindowInsetBottom();
            }
            root.setPadding(dp(16)+left,dp(18)+top,dp(16)+right,dp(14)+bottom);
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
        if(state!=null){
            figureSeed=state.getLong("ik_figure_seed",figureSeed);
            figure=state.getBoolean("ik_figure",true);
            int modeIndex=state.getInt("ik_mode",PhotoPainter.Mode.CHARCOAL.ordinal());
            if(modeIndex>=0&&modeIndex<PhotoPainter.Mode.values().length)
                activeMode=PhotoPainter.Mode.values()[modeIndex];
            String uriText=state.getString("ik_photo_uri","");
            if(!figure&&!uriText.isEmpty()){
                try{imageUri=Uri.parse(uriText);loadPhoto(imageUri,true);}
                catch(Exception error){figure=true;}
            }
        }else if(getIntent().getBooleanExtra("ik_classical_fixture",false)){
            figureSeed=249833L;figure=true;
        }else{
            String last=getPreferences(MODE_PRIVATE).getString("photo_uri","");
            if(!last.isEmpty()){
                try{imageUri=Uri.parse(last);loadPhoto(imageUri);}
                catch(Exception ignored){figure=true;}
            }
        }
        showSelection();
        if(getIntent().getBooleanExtra("ik_neural_fixture",false)) {
            root.post(this::openLocalNeuralDialog);
        }
    }

    /**
     * Explicitly delegates to a separate, locally-running neural inference app.
     * This APK itself contains no model weights and does no inference. The
     * external app is responsible for model licensing, installation, inference
     * and any permission requests; users reimport a result through ACTION_OPEN_DOCUMENT.
     */
    private void openLocalNeuralDialog(){
        EditText prompt=new EditText(this);
        prompt.setTextSize(13);
        prompt.setMinLines(2);
        prompt.setMaxLines(4);
        prompt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        prompt.setText("Photorealistic fine art portrait, adult life model, "
            +"natural human anatomy, soft atelier window light, "
            +"35mm lens, lifelike skin texture, classical composition");
        LinearLayout wrap=new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18),0,dp(18),0);
        wrap.addView(prompt);
        ActivityManager am=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memory);
        long ramGiB=Math.round(memory.totalMem/1073741824.0);
        String memoryNote="Telefon: ca. "+ramGiB+" GiB RAM. "
            +"Start ved 256–384 px; ekte diffusjon krever ekstra modellfiler.";
        new AlertDialog.Builder(this)
            .setTitle("NEVRAL KUNST · LOKALT")
            .setMessage("Ekte AI-bilder lages i den separate appen "
                +"Local Diffusion, ikke inne i Intelligent Kunst. "
                +"Installer først en modell.\n"+memoryNote
                +"\n\nBildet hentes tilbake med ÅPNE FOTO. "
                +"Ekstern app ber om bred filtilgang. "
                +"Hold LOKAL AI inne for detaljer.")
            .setView(wrap)
            .setNegativeButton("Avbryt",null)
            .setNeutralButton("INSTALLER",(dialog,which)->{
                openLocalDiffusionReleases();
            })
            .setPositiveButton("ÅPNE",(dialog,which)->{
                ClipboardManager cb=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cb.setPrimaryClip(ClipData.newPlainText("Lokal kunstprompt",
                    prompt.getText().toString().trim()));
                Intent local=new Intent(Intent.ACTION_MAIN);
                local.addCategory(Intent.CATEGORY_LAUNCHER);
                local.setClassName("com.example.local_diffusion",
                    "com.example.local_diffusion.MainActivity");
                try{
                    startActivity(local);
                    message.setText("PROMPT KOPIERT · LIM INN I LOKAL DIFFUSJON · RETURNER MED ÅPNE FOTO");
                }catch(ActivityNotFoundException error){
                    new AlertDialog.Builder(this)
                        .setTitle("Lokal modellapp er ikke installert")
                        .setMessage("Prompten er kopiert. Installer Local Diffusion "
                            +"fra det offisielle prosjektet, last inn en modell "
                            +"som passer RAM-en på enheten, og prøv igjen. "
                            +"Ingen API-nøkkel er nødvendig.")
                        .setNegativeButton("Tilbake",null)
                        .setPositiveButton("GitHub-utgivelser",(d,w)->openLocalDiffusionReleases())
                        .show();
                }
            }).show();
    }

    private void showLocalNeuralHelp(){
        new AlertDialog.Builder(this)
            .setTitle("Om lokal AI og RAM")
            .setMessage("Local Diffusion er en separat Android-app basert på "
                +"stable-diffusion.cpp. Intelligent Kunst inneholder ikke "
                +"modellvekter eller egen nevral inferens. "
                +"SD 1.5 ved 512×512 kan bruke rundt 1,9 GB RAM med Q4, "
                +"i tillegg til Android og andre apper. Ytelse varierer.\n\n"
                +"Installer en lovlig lisensiert modell, gjerne "
                +"kvantisert. Den eksterne appen kan be om tilgang til alle "
                +"filer. Gjennomgå dette før installasjon. "
                +"Nedlasting av app/modell kan kreve nett i en ekstern app, "
                +"men Intelligent Kunst har ingen INTERNET-tillatelse.\n\n"
                +"Prompten kopieres til Androids systemutklippstavle. "
                +"Ikke legg inn hemmeligheter. "
                +"Importer bare et ferdig bilde du selv velger.")
            .setNegativeButton("Lukk",null)
            .setPositiveButton("Kilde / installasjon",(d,w)->openLocalDiffusionReleases())
            .show();
    }

    private void openLocalDiffusionReleases(){
        Intent link=new Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/rmatif/Local-Diffusion/releases/latest"));
        try{startActivity(link);}
        catch(ActivityNotFoundException error){
            message.setText("ÅPNE GITHUB.COM/RMATIF/LOCAL-DIFFUSION/RELEASES I NETTLESER");
        }
    }

    @Override protected void onSaveInstanceState(Bundle out){
        out.putLong("ik_figure_seed",figureSeed);
        out.putBoolean("ik_figure",figure);
        out.putInt("ik_mode",activeMode.ordinal());
        out.putString("ik_photo_uri",imageUri==null?"":imageUri.toString());
        super.onSaveInstanceState(out);
    }

    private int dp(float v){return (int)(getResources().getDisplayMetrics().density*v+.5f);}
    private LinearLayout.LayoutParams weighted(float n){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(47),n);
        p.rightMargin=dp(5);return p;
    }
    private TextView label(String text,int size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(text);t.setTextSize(size);t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
        t.setGravity(Gravity.CENTER_VERTICAL);t.setIncludeFontPadding(false);
        return t;
    }
    private TextView button(String title,boolean active){
        TextView b=label(title,title.length()>15?10:11,active?0xFF141312:FG,true);
        b.setGravity(Gravity.CENTER);
        b.setClickable(true);b.setFocusable(true);
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(active?ACCENT:0xFF252B2B);
        shape.setCornerRadius(dp(11));
        if(!active)shape.setStroke(dp(1),0xFF434A48);
        b.setBackground(shape);
        return b;
    }
    private void showSelection(){
        for(int i=0;i<modes.length;i++){
            boolean selected=activeMode.ordinal()==i;
            modes[i].setTextColor(selected?0xFF151515:FG);
            GradientDrawable bg=new GradientDrawable();
            bg.setColor(selected?ACCENT:0xFF252B2B);
            bg.setCornerRadius(dp(10));
            modes[i].setBackground(bg);
        }
        poseButton.setAlpha(figure?1f:.50f);
        if(figure){
            String[] styles={"FIGUR, NATURLIG STUDIE","OLJEMALERI / CHIAROSCURO",
                "KULLSTUDIE / ANATOMI","KLASSISK TONALSTUDIE"};
            message.setText(styles[activeMode.ordinal()]+" · ORIGINALT, LOKALT TEGNET");
            subjectLabel.setText("AKADEMISK AKT / MODELLSTUDIE");
        }else{
            String[] styles={"FOTOGRAFISK GJENGIVELSE · FAKTISKE PIKSLER",
                "OLJE / REFERANSEBASERTE STRØK",
                "KULL / MONOKROM REFERANSE",
                "SEPIA / ARKIVTONER"};
            message.setText(styles[activeMode.ordinal()]);
            subjectLabel.setText("DITT FOTO / LOKAL REFERANSE");
        }
        requestPreviewPaint();
    }
    /**
     * Figure rendering and reference-image brushwork run off the main thread.
     * Only the last selected style is displayed. This also prevents long
     * charcoal stroke sweeps from blocking touch events or screen rotation.
     */
    private void requestPreviewPaint(){
        final int w=stage.getWidth(),h=stage.getHeight();
        final int token=paintRevision.incrementAndGet();
        if(w<1||h<1)return;
        final Bitmap photo=figure?null:preview;
        final boolean figureStudy=figure;
        final long seed=figureSeed;
        final PhotoPainter.Mode style=activeMode;
        if(!figureStudy && (photo==null||photo.isRecycled())){
            stage.setRendered(null);
            return;
        }
        worker.execute(()->{
            if(token!=paintRevision.get())return;
            Bitmap rendered=null;
            try{
                // Render at viewport dimensions. PNG export independently
                // decodes the full available source resolution off-thread.
                rendered=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                Canvas target=new Canvas(rendered);
                if(figureStudy)drawFigure(target,w,h,seed,style);
                else PhotoPainter.draw(target,w,h,photo,style,seed);
                final Bitmap complete=rendered;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||token!=paintRevision.get()){
                        complete.recycle();return;
                    }
                    stage.setRendered(complete);
                });
            }catch(OutOfMemoryError|RuntimeException error){
                if(rendered!=null&&!rendered.isRecycled())rendered.recycle();
                // Avoid a stale/wrong-style preview, but don't crash or
                // interfere with independently queued PNG exports.
                runOnUiThread(()->{
                    if(!isFinishing()&&!isDestroyed()&&token==paintRevision.get())
                        stage.setRendered(null);
                });
            }
        });
    }

    private void openPhoto(){
        Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("image/*");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(picker,OPEN_PHOTO);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=OPEN_PHOTO||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{
            getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }catch(SecurityException ignored){
            // Some providers offer session access only.
        }
        imageUri=uri;
        getPreferences(MODE_PRIVATE).edit().putString("photo_uri",uri.toString()).apply();
        loadPhoto(uri);
    }
    private void loadPhoto(Uri uri){
        loadPhoto(uri,false);
    }
    private void loadPhoto(Uri uri,boolean preserveMode){
        final PhotoPainter.Mode requestedMode=activeMode;
        final int token=revision.incrementAndGet();
        message.setText("LESER FOTO LOKALT · INGEN OPPLASTING");
        worker.execute(()->{
            Bitmap loaded=null;
            try{
                loaded=PhotoPainter.decode(getApplicationContext(),uri,1600);
                final Bitmap ready=loaded;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed()||revision.get()!=token){
                        ready.recycle();return;
                    }
                    Bitmap replaced=preview;
                    preview=ready;
                    // Rendering is serialized on worker. Queue recycling
                    // after existing paints so they never access freed pixels.
                    if(replaced!=null&&replaced!=ready)
                        worker.execute(()->{if(!replaced.isRecycled())replaced.recycle();});
                    figure=false;
                    activeMode=preserveMode?requestedMode:PhotoPainter.Mode.PHOTO;
                    showSelection();
                });
            }catch(Exception|OutOfMemoryError error){
                if(loaded!=null&&!loaded.isRecycled())loaded.recycle();
                runOnUiThread(()->{
                    if(!isFinishing()&&!isDestroyed()&&token==revision.get()){
                        message.setText("KUNNE IKKE ÅPNE FOTO. PRØV EN ANNEN FIL.");
                        new AlertDialog.Builder(this)
                            .setMessage("Bildet kunne ikke leses. Prøv JPEG, PNG eller en annen bildeleverandør.")
                            .setPositiveButton("OK",null).show();
                    }
                });
            }
        });
    }
    private void exportPng(){
        if(exporting)return;
        if(!figure&&imageUri==null){
            message.setText("Velg et foto først.");return;
        }
        exporting=true;saveButton.setEnabled(false);
        saveButton.setText("RENDERER PNG…");
        final boolean figureCopy=figure;
        final long seed=figureSeed;
        final Uri source=imageUri;
        final PhotoPainter.Mode mode=activeMode;
        worker.execute(()->{
            Bitmap decoded=null,export=null;
            Uri output=null;
            try{
                int width=2160,height=3840;
                if(!figureCopy){
                    decoded=PhotoPainter.decode(getApplicationContext(),source,3840);
                    int dw=decoded.getWidth(),dh=decoded.getHeight();
                    // Respect source aspect ratio: do not crop a portrait or invent pixels.
                    double factor=Math.min(1.0,3840.0/Math.max(dw,dh));
                    width=Math.max(1,(int)Math.round(dw*factor));
                    height=Math.max(1,(int)Math.round(dh*factor));
                }
                export=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(export);
                if(figureCopy)drawFigure(canvas,width,height,seed,mode);
                else PhotoPainter.draw(canvas,width,height,decoded,mode,seed);
                ContentValues values=new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "IntelligentKunst_Classical_"+System.currentTimeMillis()+".png");
                values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Intelligent Kunst");
                values.put(MediaStore.Images.Media.IS_PENDING,1);
                output=getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                if(output==null)throw new IOException("Could not create image");
                try(OutputStream stream=getContentResolver().openOutputStream(output)){
                    if(stream==null||!export.compress(Bitmap.CompressFormat.PNG,100,stream))
                        throw new IOException("PNG write failed");
                }
                values.clear();values.put(MediaStore.Images.Media.IS_PENDING,0);
                getContentResolver().update(output,values,null,null);
                final int outWidth=width,outHeight=height;
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    exporting=false;saveButton.setEnabled(true);
                    saveButton.setText("↓  EKSPORTER KUNSTVERK SOM PNG");
                    message.setText("LAGRET I BILDER · "+outWidth+" × "+outHeight);
                });
            }catch(Exception|OutOfMemoryError error){
                if(output!=null)getContentResolver().delete(output,null,null);
                runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    exporting=false;saveButton.setEnabled(true);
                    saveButton.setText("↓  EKSPORTER KUNSTVERK SOM PNG");
                    message.setText("EKSPORT FEILET · PRØV IGJEN");
                });
            }finally{
                if(decoded!=null&&!decoded.isRecycled())decoded.recycle();
                if(export!=null&&!export.isRecycled())export.recycle();
            }
        });
    }
    private static void drawFigure(Canvas c,int w,int h,long seed,PhotoPainter.Mode mode){
        if(mode!=PhotoPainter.Mode.SEPIA){
            new ClassicalFigurePainter().draw(c,w,h,seed,mode==PhotoPainter.Mode.CHARCOAL);
            return;
        }
        Paint sepia=new Paint(Paint.FILTER_BITMAP_FLAG);
        sepia.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
            .393f,.769f,.189f,0,0,
            .349f,.686f,.168f,0,0,
            .272f,.534f,.131f,0,0,
            0,0,0,1,0
        })));
        int layer=c.saveLayer(0,0,w,h,sepia);
        new ClassicalFigurePainter().draw(c,w,h,seed,false);
        c.restoreToCount(layer);
    }

    private final class StudioView extends View {
        private Bitmap rendered;
        private final Paint blit=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG);
        private StudioView(){super(ClassicalStudioActivity.this);}
        private void setRendered(Bitmap ready){
            Bitmap old=rendered;
            rendered=ready;
            if(old!=null&&old!=ready&&!old.isRecycled())old.recycle();
            invalidate();
        }
        @Override protected void onSizeChanged(int w,int h,int oldW,int oldH){
            super.onSizeChanged(w,h,oldW,oldH);
            requestPreviewPaint();
        }
        @Override protected void onDraw(Canvas canvas){
            canvas.drawColor(BG);
            if(rendered!=null&&!rendered.isRecycled())
                canvas.drawBitmap(rendered,null,
                    new android.graphics.Rect(0,0,getWidth(),getHeight()),blit);
        }
    }
    @Override protected void onDestroy(){
        revision.incrementAndGet();
        paintRevision.incrementAndGet();
        // Queue the reference bitmap's cleanup AFTER every in-flight paint.
        // shutdownNow/recycle-on-UI could otherwise use freed Android pixels.
        final Bitmap photo=preview;
        preview=null;
        if(photo!=null)worker.execute(()->{
            if(!photo.isRecycled())photo.recycle();
        });
        worker.shutdown();
        stage.setRendered(null);
        super.onDestroy();
    }
}
