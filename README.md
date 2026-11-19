<div align="center">

# INTELLIGENT KUNST

**AN AUTONOMOUS ART EXPERIMENT**

*Kan en algoritme utvikle et eget kunstnerisk uttrykk, i stedet for bare å gjenta en mal?*

[![Android CI](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml/badge.svg)](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml)
![Android 10+](https://img.shields.io/badge/Android-10%2B-DAFB81?labelColor=111111)
![Fully Offline](https://img.shields.io/badge/network-no%20permission-DAFB81?labelColor=111111)
![Classical Art](https://img.shields.io/badge/classical-art%20%2B%20photo-A3B7FE?labelColor=111111)
![Language](https://img.shields.io/badge/language-Java%2017-A3B7FE?labelColor=111111)

</div>

## Kunstneren bor i telefonen

**Intelligent Kunst** er nå en fullstendig **nøkkelfri og nettverksfri** Android-app. Den har to verksteder: lokal konseptuell generativ kunst og **KLASSISK ATELIER**, der du lager originale akademiske aktstudier eller bruker egne fotografier som referanser for trofast gjengivelse, olje-, kull- og sepiastudier. Ingen pålogging, API-nøkkel, reklame, skylagring eller nettverkstillatelse.

Hvert trykk på **SKAP NYTT** setter nå et lite digitalt atelier i arbeid:

1. En regelbasert idémotor skaper **12 komposisjoner med motiver**. Hver av dem er valgt blant **42 DNA-forslag**.
2. Alle tolv får sin egen ferdig rendrerte **192 × 320** pixelskisse. Ingen skisser vurderes bare ved å lese sine parametere.
3. En åpen, lokal `PixelCritic` måler **lyskontrast, blikkfang, tomrom, tegningsgrad, plassering** og likhet med siste bildene.
   **Et krav overstyrer bildescoren:** en ny, selvstendig komposisjon skal ikke bruke samme hovedobjekt som ett av de fire siste verkene, eller samme visuelle paradoks som de to siste. `EVOLVER` kan beholde motiv for å utforske en valgt idé. Hvis det ikke finnes noen gyldig kandidat etter et begrenset antall forsøk, finnes en eksplisitt reservevei.
4. En kombinert poengsum velger ett bilde. Du kan **EVOLVERE**, trykke **♡ LÆR** for å støtte idéen, eller **holde LÆR inne** for å avvise den og straks få et nytt forsøk. Positive og negative vurderinger gjelder separat motiv, konsept og palett. Du kan se kritikken, eksportere PNG, kopiere DNA eller samle bilder.
5. Eksport gir høyoppløselig **2160 × 3840 PNG**. Alt fungerer frakoblet.

### En fysisk idé, et visuelt utsagn

**FORTELLING** er en generativ *konseptgrammatikk*, ikke fire ferdiglagde bilder eller et bibliotek fra utviklerens tidligere samtaler. Motoren velger blant **åtte hverdagsobjekter**, **syv fysiske paradokser** og **tre romlige oppsett**. En stol kan ha en skygge som har valgt en annen retning; en bok kan ha mistet en bit av seg selv; et objekt kan miste sin tyngde eller vise spor av tidligere posisjoner. Konseptet endrer de tegnede figurene, ikke bare tittelen. Enkelte kombinasjoner sperres når de ikke gir fysisk mening, eksempelvis at en stige «søler innhold».

Ingen av motivene velges på grunnlag av brukerhistorikk eller personlige referanser. Variasjonene er reproduserbare fra DNA og får egne fysiske materialer, fargeflater, skygger og ufullkommen trykktekstur. De gamle seks geometriske stilene er fortsatt lesbare fra eldre DNA, men brukes ikke ved ny generering.

**Ærlig avgrensning:** dette er fremdeles en begrenset *regelbasert* oppfinner med bildepikselanalyse. Det er ikke en trent, åpent assosierende bildemodell, og pixel-statistikk alene kan ikke måle kunstnerisk kvalitet. Hvis målet er et virkelig overraskende kunstnersubjekt, må videre versjoner øke handlingsrom, kritikk og læring utover disse faste byggesteinene.

## Atelier 008.1 · Testene skal avsløre feil, ikke dekke dem til

Ved den første ekte emulatorvisningen av olje- og kullmotorene oppdaget vi at et tomt testparameter utilsiktet valgte olje også ved aktstudium. Dette er rettet. CI sjekker nå at aktstudium, olje og kull faktisk gir **ulike bilder**, og at Android-prosessen overlever renderingen. Versjon 0.8.1 er en korrigert LAB-testversjon.

## Atelier 008 · Referansebasert, anatomisk og strukturbevisst kunst

**Olje** gjør mer enn å legge tilfeldige penselstrøk over et foto. En lokal Sobel-analyse finner faktiske lyskonturer; på tre strøkstørrelser blir maling lagt langs formene med farger hentet fra kildebildet. **Kull** bruker lys- og skyggefeltene til å bygge opp konturer og krysskravering. Behandlingen foregår helt offline, uten bildegenerering fra en server.

For å holde Android-grensesnittet raskt, rendres de tunge materialstrøkene på en egen arbeidstråd, med forhåndsvisning som bitmapcache. Raske stilskifter avbryter foreldede tegneoppdrag. Fotoets egentlige piksler ligger fortsatt under disse materielle tolkningene, og **FOTO**-modus beholder originalpikslene.

Sobel-feltet har strenge minnegrenser og enhetstester for vinkelretning, jevn belysning og feilaktige inndata. Android-emulatoren fotograferer nå også olje- og kullmodus på et **merket syntetisk testmotiv**, ikke et falskt «ekte foto». Reell fotorealistisk bildeoppfinnelse krever fortsatt en lokal diffusjonsmodell gjennom funksjonen LOKAL AI.

## Atelier 007.2 · Renere arbeidsflate

Den første Android-emulatorprøven av nevralknappen viste en altfor stor
dialog som skjøv handlingene fra hverandre. Nå ser du en **kort prompt,
en kort ressursadvarsel og tre enkle valg**. Hold LOKAL AI inne for
detaljer om modellens RAM-krav, lagringstillatelser, lisenser og personvern.

## Atelier 007.1 · Responsivt lerret

På kompakte telefoner og i liggende format ruller verktøypanelet uavhengig
av bildeflaten. Kunstverket skal fortsatt være synlig når du velger
kull, olje, foto eller lokal nevral generering.

## Atelier 007 · Faktisk lokal nevralkunst, uten API-nøkkel

**✦ AKT/FOTO → LOKAL AI** er nå en reell overgang til den åpne,
separate Android-appen [Local Diffusion](https://github.com/rmatif/Local-Diffusion).
Denne kjører [stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp)
lokalt med **nedlastede modellvekter**, uten betalt skykall eller API-nøkkel.
Kunstprompten kopieres til utklippstavlen; du genererer bildet i
Local Diffusion, lagrer det og åpner resultatet i Intelligent Kunst med
**ÅPNE FOTO**.

Dette er **integrasjon mellom to apper**, ikke intern diffusjonsmotor:
vår APK inneholder ingen modellvekter, og ingen bakgrunnsmodell kjører i
Intelligent Kunst. Egen modellinstallasjon kan kreve gigabyte RAM og lagring.
Den andre appen ber om omfattende lagringstilgang. Les
[modellveiledningen og den tekniske avgrensningen](docs/LOCAL_NEURAL.md)
før du installerer. Intelligent Kunst beholder null nettverkstillatelser.

## Atelier 006.1 · Etter visuell Android-kontroll

I Android-emulatoren var den første modellen tydelig for stilisert, med feil hårmateriale, kraftige høylys og et toppfelt som kolliderte med systemets statuslinje. Versjon **0.6.1** retter dette og bruker **akademisk kulltegning** som utgangspunkt, med olje/sepia som valg. Figurene er fremdeles original, prosedyrebasert illustrasjon; realistiske bilder kommer fra brukerens faktiske fotografier, ikke fra et påstått trent nevralt nettverk.

## Atelier 006 · Klassisk akt og fotografisk referanse, helt offline

Trykk **✦ AKT/FOTO** i appen. Dette åpner et eget verksted uten konto og uten noen API-nøkkel.

- **AKTSTUDIE** tegner en ny akademisk voksen aktmodell lokalt med klassiske kroppsproporsjoner, sittende eller stående positur, atelierlys og dempet fargepalett. **NY POSITUR** lager et nytt figurforslag. Dette er original programmatisk anatomistudie, ikke en kopi av et bestemt menneske og ikke fotorealistisk nevralt generert nakenfoto.
- **ÅPNE FOTO** bruker Androids systemvelger. Bildet blir ikke lastet opp. **FOTO** beholder detaljene i det faktiske referansebildet, uten å finne på ansikter eller andre trekk.
- **OLJE** legger lokalt genererte penselstrøk basert på fotografiets farger; **KULL** lager en monokrom studie, og **SEPIA** en klassisk arkivtoning.
- **EKSPORTER PNG** lagrer i Bilder / Intelligent Kunst via Android MediaStore. Fotografieksport beholder originalens sideforhold og bruker en minnebegrenset bildedekoder. Figurstudiens eksport er 2160 × 3840.
- Appen trenger **ingen INTERNET-tillatelse**. Sist valgte foto kan åpnes på nytt via Androids vedvarende dokumenttilgang. Bildene ligger hos deg, ikke hos oss.

**Viktig kvalitetsgrense:** Fotorealistisk *gjengivelse* av et eksisterende fotografi er implementert. Fotorealistisk *nyskapning fra bare tekst* krever en lokal nevralt trent bildegenereringsmodell med betydelige modellfiler og maskinvarekrav. Dette følger **ikke** med i 0.6.0, og vi later ikke som filtre eller vektorfigurer løser det. Appens figurtegner er et klassisk malerieksperiment, ikke maskinlært 3D-anatomi.

## Atelier 005.1 · Idéer får en utviklingshistorie

**EVOLVER** beholder nå objektet i forelderverket. Ny generasjon søker en ny tegning av den samme visuelle idéen, og kan av og til undersøke en annen fysisk motsetning med samme objekt. Slektskap er dermed noe man kan se i bildene, ikke bare et foreldrenummer i DNA. En serie tester bekrefter dette for 180 generasjoner.

Vi har også kontrollert seks forskjellige komposisjoner i en ekte Android-emulator, samt det bundne tegneeksempelet uten API-nøkkel. Brukerens estetiske dom er fortsatt viktigere enn intern poengsum.

## Atelier 005 · Fritt tegnede former, ikke bare clipart

AI-bildemanus kan nå bruke **sammenhengende, fylte Bézier-former** med kubiske og kvadratiske kurver, egne konturer, og **lineære eller radiale fargeforløp**. Hver tegningsinstruksjon valideres før den tegnes. Ingen programkode, eksterne bildekilder eller ubegrensede tegningssekvenser.

En komplett, nøkkelfri prøve ligger i [examples/expressive-vector-scene.json](examples/expressive-vector-scene.json). Importer via **DNA / ? → Importer grafikkmanus (JSON)** uten API-konto. Android og Python har tester for kurver, malingsdata og ugyldige kommandoer.

Dette er bedre *uttrykksrom*, ikke dokumentasjon på at modellen automatisk produserer god kunst.

## Atelier 004 · aiguide: AI lager bildemanus, Android maler

Historiske AI-grafikkmanus kan fortsatt åpnes, lagres og eksporteres offline via DNA v3 eller **DNA / ? → Importer grafikkmanus (JSON)**. Det native nettverkskallet og skjermbildet for API-nøkkel er fjernet fra APK-en. Verken AI eller en ekstern modell kreves for å tegne. Importerte kunstverk blir ikke brukt til å trene en ekstern modell.

Den originale [aiguide-dev](https://github.com/bspippi1337/aiguide-dev)-bibliotekskoden lever fortsatt som et uavhengig **utviklerverktøy** i Termux/Linux. Skriptet `tools/aiguide_generate.py` kan skrive slike bildemanus for import, men dette er ikke en del av Android-appens vanlige arbeidsflyt. Et slikt separat API-skript krever egne modelltilganger og er ikke nødvendig for funksjonene over.

[Les dokumentasjonen for det historiske aiguide-formatet](docs/AIGUIDE.md).

## Atelier 002 · Dagens verk, kritikk og avstamning

- **Dagens original:** velg `DNA / ? → Dagens verk`. Alle enheter får samme verk på samme UTC-dato, helt uten nettverk. Dagens original er uavhengig av lokale smaksdata.
- **Kunstnerens egenkritikk:** åpne `DNA / ? → Kunstnerens egenkritikk` for å se både faktisk bildeanalyse (nytt i 0.3) og den eldre DNA-heuristikken. Lagret utvelgelsesscore bevares. Målingene er strukturelle, ikke semantiske.
- **Spor opphavet:** `EVOLVER` skriver forelderseed inn i DNA v2. `Finn forelderverket` kan gå bakover gjennom opptil 300 lokale verk. Originalt DNA v1 forblir lesbart.
- **Utstillingsmodus:** trykk på selve kunstverket for å gjemme grensesnittet. Trykk igjen for å hente det tilbake.
- **Smak uten juks:** ett verk kan maksimalt gi én lokal stemme. Ingen konto, ingen profil utenfor enheten.

## Seks historiske uttrykk (DNA-kompatibilitet)

| Stil | Visuell idé |
|---|---|
| **STRØM** | Hundrevis av linjer finner en vei gjennom et deterministisk flytfelt |
| **BLOMSTRING** | Organiske, lagdelte petalgeometrier uten fotografiske kilder |
| **BANER** | Bølger av eksentriske, roterte ellipser og stjernelignende punkter |
| **TOPOGRAFI** | Konturlinjer fra et landskap som ikke eksisterer |
| **VEV** | Interferens og ordnet kaos i hundrevis av tråder |
| **PRISME** | Skulpturelle, transparente polygoner og brutt lys |

Fargepaletter, komposisjon, tetthet, asymmetri og tilfeldig frø er med i verkets DNA. Motoren er skrevet fra bunnen av og bruker ikke ferdige bildefiler eller bildegenererings-API-er.

## Last ned og kjør

**Android LAB-installasjon:** Den automatiske debug-APK-en bruker eget pakkenavn `no.blckswan.intelligentkunst.lab` og heter **Intelligent Kunst LAB** i appmenyen. Den installeres ved siden av tidligere Intelligent Kunst-utgaver i stedet for å erstatte dem. Dermed står eksisterende samling urørt. LAB har sin egen isolerte samling og skal ikke forveksles med en produksjonssignert release.

Gå til [Actions → Android CI](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml), åpne en vellykket kjøring og last ned artefakten **intelligent-kunst-debug-apk**. Dette er en debug-signert test-APK, ikke en Play Store-signert produksjonsrelease. Installer APK-en på Android 10 eller nyere.

Bygg lokalt med Android Studio eller Gradle 8.9, JDK 17 og Android SDK Platform 35:

```bash
gradle :app:assembleDebug :app:testDebugUnitTest
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Ren JVM-røykprøve av den kunstneriske beslutningsmotoren (ingen Android SDK nødvendig):

```bash
sh ./tools/test-core.sh
```

## Arkitektur

```text
app/src/main/java/no/blckswan/intelligentkunst/
├── core/
│   ├── Artwork.java        # uforanderlig, versjonert genom og paletter
│   ├── ArtDirector.java    # kandidater, evolusjon og smak
│   ├── CritiqueReport.java # åpen og kontrollerbar utvelgelsesrubrikk
│   ├── StoryPlan.java    # bevisst motiv og fortelling
│   ├── PixelCritic.java  # faktisk bildepikselanalyse
│   ├── DailyEdition.java   # felles UTC-dagsverk uten nett
│   └── FieldNoise.java     # deterministisk, glatt prosedyrestøy
├── render/
│   ├── RenderEngine.java   # DNA-kompatibilitet og routing
│   ├── StoryPainter.java   # typografisk, narrativ trykk-kunst
│   ├── StudioDirector.java # tolv forhåndsvisninger og visuell utvelgelse
│   ├── ClassicalFigurePainter.java # akt og atelierlys
│   └── PhotoPainter.java   # lokal fotoreproduksjon og studiostiler
├── ui/
│   └── ArtworkView.java    # asynkron rendering, caching, crossfade og berøring
├── storage/
│   └── ArtArchive.java     # offline samling, smak og nylig arbeid
├── ClassicalStudioActivity.java # privat akt- og fotoatelier
└── MainActivity.java      # lokal konseptkunst, arkiv, PNG og grafikkmanus
```

**Personvern:** Appen har **ingen INTERNET-tillatelse** og inneholder ingen API-klient. Brukerfoto blir behandlet lokalt via Androids systemvelger. Appen lagrer kun URI-en til siste valgte foto i sin private lagring for gjenåpning, med Androids dokumenttillatelse når leverandøren støtter det. Selve fotoet kopieres ikke til en server. PNG blir lagt i bildegalleriet bare ved eksport. Kildekoden er åpen for inspeksjon.

**Ytelse:** Avansert malearbeid skjer utenfor UI-tråden. Kunstverk blir rendret én gang som skjermcache, og ved eksport i full oppløsning. Verken komposisjon eller innstillinger trenger en server. DNA-valg er reproduserbare, mens opplevelsen rundt dem er interaktiv.

## Videre forskningsspørsmål

Kan en kunstnerisk agent oppdage *nye* estetiske preferanser uten å bli en echo chamber? Hva skjer når utvelgelse får se på selve pikslene, ikke bare genomet? Kan man bygge en enda lengre utstillingshistorikk der hvert verk kjenner sin avstamning? Disse spørsmålene er åpne og gir en retning for videre eksperimenter, ikke funksjoner som feilaktig påstås ferdige.

## Lisens

MIT, se [LICENSE](LICENSE). Genererte verk er dine å bruke.

---

**BLCKSWAN LAB / INTELLIGENT KUNST**  
*Et lite atelier for store spørsmål.*
