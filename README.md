<div align="center">

# INTELLIGENT KUNST

**AN AUTONOMOUS ART EXPERIMENT**

*Kan en algoritme utvikle et eget kunstnerisk uttrykk, i stedet for bare å gjenta en mal?*

[![Android CI](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml/badge.svg)](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml)
![Android 10+](https://img.shields.io/badge/Android-10%2B-DAFB81?labelColor=111111)
![Offline core](https://img.shields.io/badge/offline-core-DAFB81?labelColor=111111)
![Optional AI](https://img.shields.io/badge/AI-opt--in-A3B7FE?labelColor=111111)
![Language](https://img.shields.io/badge/language-Java%2017-A3B7FE?labelColor=111111)

</div>

## Kunstneren bor i telefonen

**Intelligent Kunst** er en native Android-app med to adskilte verksteder: en lokal, regelbasert kunstmotor som fungerer helt uten nett, og en **frivillig AI-modus** bygget på [aiguide-dev](https://github.com/bspippi1337/aiguide-dev). AI-modusen sender kun brukervalgt prompt til en ekstern modell med en API-nøkkel du selv oppgir. Ingen innlogging, reklame eller analyse-SDK-er.

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

## Atelier 005 · Fritt tegnede former, ikke bare clipart

AI-bildemanus kan nå bruke **sammenhengende, fylte Bézier-former** med kubiske og kvadratiske kurver, egne konturer, og **lineære eller radiale fargeforløp**. Hver tegningsinstruksjon valideres før den tegnes. Ingen programkode, eksterne bildekilder eller ubegrensede tegningssekvenser.

En komplett, nøkkelfri prøve ligger i [examples/expressive-vector-scene.json](examples/expressive-vector-scene.json). Importer via **DNA / ? → Importer AI-bildemanus** uten API-konto. Android og Python har tester for kurver, malingsdata og ugyldige kommandoer.

Dette er bedre *uttrykksrom*, ikke dokumentasjon på at modellen automatisk produserer god kunst.

## Atelier 004 · aiguide: AI lager bildemanus, Android maler

**✦ AI** åpner et eget atelier som bruker den originale `aiguide-dev`-promptmalen og den samme `ask(query)`- og JSONL-loggflyten i en native Android-port. En ekstern språkmodell lager en **ny, konkret bildekomposisjon med opptil 90 tegnbare elementer**, og Android-maleren gjengir den som et vektorbasert verk. AI-kunstverk lagres som DNA v3 med hele bildemanuset, slik at bildene kan åpnes og eksporteres senere **uten nett**.

Du kan også bruke det **faktiske Python-biblioteket** med `tools/aiguide_generate.py` i Termux/Linux og importere JSON i appen via `DNA / ? → Importer AI-bildemanus`. Originalbiblioteket og den native Android-porten er forskjellige implementasjoner av samme arbeidsflyt.

- AI-knappen krever internett og din egen OpenAI API-nøkkel. Nøkkelen lagres ikke, og modellen kan påføre API-kostnader.
- Resultatets tegnekommandoer blir validert og begrenset til et lite sett med former, kurver, linjer og polygoner. Ingen skript eller eksterne ressurs-URL-er blir utført.
- Logg med synlig forespørsel og svar lagres privat i appens filer, i JSONL-format. Ingen påstand om tilgang til skjult resonnering.
- **Dette er KI-styrt illustrasjon, ikke en diffusjonsmodell.** Kunstnerisk kvalitet kan ikke garanteres av språkmodellen eller den eksisterende pikselpoengsummen.

[Les integrasjonsguiden for aiguide](docs/AIGUIDE.md).

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
│   └── StudioDirector.java # tolv forhåndsvisninger og visuell utvelgelse
├── ui/
│   └── ArtworkView.java    # asynkron rendering, caching, crossfade og berøring
├── storage/
│   └── ArtArchive.java     # offline samling, smak og nylig arbeid
└── MainActivity.java      # atelier, import, galleri, eksport og deling
```

**Personvern:** Appen har `INTERNET`-tillatelse for den valgfrie AI-knappen, men offline-motoren sender ingen forespørsler. AI-modusen sender prompt og en midlertidig API-nøkkel over HTTPS til leverandøren, og loggfører kun synlig prompt og svar privat på enheten. Nøkkelen lagres ikke. PNG havner i bildegalleriet bare når du eksporterer; delingsmenyen vises på forespørsel. Kildekoden kan inspiseres.

**Ytelse:** Avansert malearbeid skjer utenfor UI-tråden. Kunstverk blir rendret én gang som skjermcache, og ved eksport i full oppløsning. Verken komposisjon eller innstillinger trenger en server. DNA-valg er reproduserbare, mens opplevelsen rundt dem er interaktiv.

## Videre forskningsspørsmål

Kan en kunstnerisk agent oppdage *nye* estetiske preferanser uten å bli en echo chamber? Hva skjer når utvelgelse får se på selve pikslene, ikke bare genomet? Kan man bygge en enda lengre utstillingshistorikk der hvert verk kjenner sin avstamning? Disse spørsmålene er åpne og gir en retning for videre eksperimenter, ikke funksjoner som feilaktig påstås ferdige.

## Lisens

MIT, se [LICENSE](LICENSE). Genererte verk er dine å bruke.

---

**BLCKSWAN LAB / INTELLIGENT KUNST**  
*Et lite atelier for store spørsmål.*
