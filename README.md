<div align="center">

# INTELLIGENT KUNST

**AN AUTONOMOUS ART EXPERIMENT**

*Kan en algoritme utvikle et eget kunstnerisk uttrykk, i stedet for bare å gjenta en mal?*

[![Android CI](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml/badge.svg)](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml)
![Android 10+](https://img.shields.io/badge/Android-10%2B-DAFB81?labelColor=111111)
![Offline](https://img.shields.io/badge/network-0%20requests-DAFB81?labelColor=111111)
![Language](https://img.shields.io/badge/language-Java%2017-A3B7FE?labelColor=111111)

</div>

## Kunstneren bor i telefonen

**Intelligent Kunst** er en helt native Android-app som skaper originale, reproducerbare generative verk. Den sender aldri data til en sky. Ingen innlogging, API-nøkler, reklame, analyse-SDK-er eller nettverkstillatelser.

Hvert trykk på **SKAP NYTT** starter en liten kunstnerisk utvelgelse:

1. Kunstneren lager **42 forskjellige komposisjonsforslag** med et evolusjonært genom.
2. En transparent, heuristisk *art director* vurderer rytme, kompleksitet, asymmetri, bevegelse og originalitet i forhold til nylige verk.
3. Ett forslag blir valgt og malt på Android Canvas i en av seks distinkte visuelle stiler.
4. Du kan **EVOLVERE** verket, lære kunstneren preferansene dine med **♡ LÆR**, lagre favoritter i en lokal samling og eksportere som **2160 × 3840 PNG**.
5. Alle verk har et **DNA** som kan kopieres, deles og importeres slik at samme verk kan gjenskapes på en annen telefon.

Dette er et **eksperiment i algoritmisk kreativitet**, ikke et forsøk på å selge heuristikker som en trent språkmodell eller som en objektiv estetisk dom. Scoren er en intern utvelgelsesfunksjon og ikke en vurdering av kunstnerisk verdi.

## Seks uttrykk

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

Gå til [Actions → Android CI](https://github.com/bspippi1337/Intelligent-kunst/actions/workflows/android.yml), åpne en vellykket kjøring og last ned artefakten **intelligent-kunst-debug-apk**. Dette er en debug-signert test-APK, ikke en Play Store-signert produksjonsrelease. Installer APK-en på Android 10 eller nyere.

Bygg lokalt med Android Studio eller Gradle 8.9, JDK 17 og Android SDK Platform 35:

```bash
gradle :app:assembleDebug :app:testDebugUnitTest
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Ren JVM-røykprøve av den kunstneriske beslutningsmotoren (ingen Android SDK nødvendig):

```bash
./tools/test-core.sh
```

## Arkitektur

```text
app/src/main/java/no/blckswan/intelligentkunst/
├── core/
│   ├── Artwork.java        # uforanderlig, versjonert genom og paletter
│   ├── ArtDirector.java    # kandidater, heuristisk selvkritikk, evolusjon og smak
│   └── FieldNoise.java     # deterministisk, glatt prosedyrestøy
├── render/
│   └── RenderEngine.java   # seks native Android Canvas-malere
├── ui/
│   └── ArtworkView.java    # asynkron rendering, caching, crossfade og berøring
├── storage/
│   └── ArtArchive.java     # offline samling, smak og nylig arbeid
└── MainActivity.java      # atelier, import, galleri, eksport og deling
```

**Personvern:** Appen har null `INTERNET`-tillatelser, og skriver bare til privat app-lagring og til den offentlige bildegallerien når du selv eksporterer. Androids delingsmeny vises kun på forespørsel. Du kan inspisere hele kildekoden.

**Ytelse:** Avansert malearbeid skjer utenfor UI-tråden. Kunstverk blir rendret én gang som skjermcache, og ved eksport i full oppløsning. Verken komposisjon eller innstillinger trenger en server. DNA-valg er reproduserbare, mens opplevelsen rundt dem er interaktiv.

## Videre forskningsspørsmål

Kan en kunstnerisk agent oppdage *nye* estetiske preferanser uten å bli en echo chamber? Hva skjer når utvelgelse får se på selve pikslene, ikke bare genomet? Kan man bygge en utstillingshistorikk der hvert verk kjenner sin avstamning? Disse spørsmålene er åpne og gir en retning for videre eksperimenter, ikke funksjoner som feilaktig påstås ferdige.

## Lisens

MIT, se [LICENSE](LICENSE). Genererte verk er dine å bruke.

---

**BLCKSWAN LAB / INTELLIGENT KUNST**  
*Et lite atelier for store spørsmål.*
