# Atelier 007: lokal nevral bildegenerering uten API-nøkkel

**Status: ekstern, ekte lokal inferens.** Intelligent Kunst har ingen nevral
diffusjonsmotor innebygd ennå. Den åpner den egne Android-appen
[Local Diffusion](https://github.com/rmatif/Local-Diffusion), basert på
[stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp).
Det er en fungerende lokal bildemotor, men den må installeres **separat**.
Appen og modellene er ikke del av vår APK.

Dette er et bevisst skille: en lokal bildegenerator er ekte når modellvekter
er lastet inn, inferens faktisk skjer på telefonen og et ferdig PNG-bilde
produseres. En knapp som bare åpner en annen app er **integrasjon**, ikke
egen generativ AI i Intelligent Kunst.

## Bruk uten konto og API-nøkkel

1. I Intelligent Kunst: **AKT/FOTO → LOKAL AI**.
2. Hvis Local Diffusion mangler, velg **HENT LOKAL DIFFUSJON**. Dette åpner
   GitHub Releases i telefonens nettleser. Kontroller utgiver før du
   installerer. Appen må selv bruke nett for å laste ned modellvekter
   (eventuelt overføre dem fra en annen enhet). Intelligent Kunst trenger
   ingen INTERNET-tillatelse.
3. Installer en modell som passer RAM-kapasiteten til telefonen. Start
   gjerne med en **kvantisert SD 1.5-modell**, CPU-backend,
   **256×256 / 384×384** og få samplingsteg.
4. Trykk **ÅPNE LOKAL AI**. Beskrivelsen kopieres til Android-utklippstavlen.
   Lim den inn i Local Diffusion, last modellen og kjør generering der.
5. Lagre det ferdige bildet i Local Diffusion og gå tilbake til
   Intelligent Kunst. Velg **ÅPNE FOTO**. Bildet kan nå vises i opprinnelig
   fotografisk kvalitet, fargetones, viderefortolkes og eksporteres.

**Ikke garantert ytelse:** Upstream Local Diffusion oppgir omtrent **1,9 GB
peak RAM** for SD 1.5, Q4_0, 512×512 med deres benchmarkoppsett.
Android, appen og andre prosesser bruker også RAM. En telefon med 4 GB RAM
kan derfor fortsatt få minnefeil eller svært lang kjøretid. Ingen spesifikk
enhet er benchmarktestet av oss.

**Tillatelser og personvern:** Local Diffusion er et separat program og
ber etter sin offentlige Android-manifest om omfattende lagringstilgang
(MANAGE_EXTERNAL_STORAGE). Du velger selv om dette er akseptabelt.
Intelligent Kunst får ikke slik tilgang: det bruker kun den
systemmedierte dokumentvelgeren til bildene du eksplisitt velger.
Prompten kopieres til den vanlige systemutklippstavlen, som andre apper
med gjeldende Android-tilgang potensielt kan lese. Ikke kopier
hemmeligheter i prompten.

**Modell- og bilderettigheter:** Sjekk lisensen til valgt checkpoint.
Akademiske aktstudier av voksne er et legitimt kunstmotiv, men modellen
kan ha tekniske eller lisensmessige begrensninger. Ikke anta at den gir
god anatomi eller fotografisk kvalitet bare fordi den er en diffusjonsmodell.

## Hva Android-APK-en faktisk garanterer

- Ingen OpenAI API-nøkkel eller nettverkstillatelse.
- Knappen forsøker å starte `com.example.local_diffusion/.MainActivity`,
  som samsvarer med oppstrøms kildekode (november 2026).
- Hvis appen ikke er installert, får du en tydelig installasjonslenke.
- Kun valgt brukerprompt kopieres; appen sender ikke en nettverksforespørsel.
- Resultatimport skjer gjennom eksisterende Android dokumentvelger.
- Ingen påstand om interne AI-modellvekter, innebygd FFI-inferens,
  automatisk modellnedlasting eller automatisk bildeoverføring.

## Neste milepæl

En valgfri intern modellkjører i vår egen APK må levere reell
`prompt → lokal inferens → PNG` med målbar minneprofil, riktig
modelllisens, Android-native ABI-pakker, korrekte signaturer og tester på
fysisk telefon. Ikke før det skal vi kalle Intelligent Kunst en selvstendig
nevral bildegenerator.

Kilder:
- https://github.com/rmatif/Local-Diffusion
- https://github.com/leejet/stable-diffusion.cpp
