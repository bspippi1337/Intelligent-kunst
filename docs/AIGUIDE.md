# aiguide-dev og Intelligent Kunst · historisk format

Fra **Atelier 006** kjører Android-appen fullstendig offline og trenger **ingen API-nøkkel**. Den har ikke INTERNET-tillatelse, ingen embedded klient for OpenAI, og ingen dialog som ber om nøkkel. Den bruker lokale tegnemotorer og Androids dokumentvelger for egne bilder.

Kilde: [bspippi1337/aiguide-dev](https://github.com/bspippi1337/aiguide-dev), MIT, fastlåst til
`92609d71e6c8cb7b57be640d042b6148f9baf485`.
Lisens: [third_party/aiguide-dev-LICENSE](../third_party/aiguide-dev-LICENSE).

## Lokal Android-modus

Trykk **AKT/FOTO** for å lage originale akademiske aktfigurer eller behandle egne fotografier som olje-, kull-, sepia- og fotografisk gjengivelse. Ingen av disse funksjonene bruker aiguide eller en språkmodell.

Appens **DNA / ? → Importer grafikkmanus (JSON)** kan fremdeles lese og tegne historiske kunstverk i et begrenset JSON-vektorformat. Disse kan kopieres som DNA v3, åpnes og eksporteres uten nett. Android kjører ikke modellkode, leser ikke nettadresser og evaluerer ikke skript.

## Uavhengig utviklerverktøy, valgfritt

Det opprinnelige Python-biblioteket kan brukes fra Termux/Linux for dem som ønsker ekstern modellgenerering **utenfor** APK-en:

```sh
python -m pip install 'git+https://github.com/bspippi1337/aiguide-dev.git@92609d71e6c8cb7b57be640d042b6148f9baf485'
export OPENAI_API_KEY='YOUR_OWN_KEY'
python tools/aiguide_generate.py "Generate an original visual form" --clipboard
```

Kommandoen er en **separat, frivillig utviklerarbeidsflyt** som trenger egne leverandørtilganger. Den er ikke nødvendig for å bruke Intelligent Kunst. Appen logger ikke API-forespørsler fordi den ikke gjør noen.

## Begrenset bildemanusformat

JSON-skjema versjon 1 kan inneholde 8–90 vektorelementer: rect, ellipse, line, curve, poly og path. Path bruker M/L/Q/C/Z, maks 64 kommandoer per element, og filled path må avsluttes med Z. Alle elementer kan ha valgfri linear eller radial gradient. Farger er #RRGGBB; koordinater og opasitet valideres strengt.

```json
{
  "version": 1,
  "title": "Material study",
  "intent": "An authored relationship between curved volumes",
  "background": "#E7DDC9",
  "elements": [
    {"kind":"path","color":"#26383A","mode":"fill","commands":[
      {"op":"M","x":0.2,"y":0.3},
      {"op":"C","cx1":0.5,"cy1":0.1,"cx2":0.8,"cy2":0.6,"x":0.7,"y":0.7},
      {"op":"L","x":0.3,"y":0.7},{"op":"Z"}
    ]}
  ]
}
```

*Dette er bare et formateksempel. En virkelig gyldig scene krever minst åtte elementer.*

Prøv [det komplette eksempelmanuset](../examples/expressive-vector-scene.json) uten noen modelltilgang. Den lokale vektormotoren kan gjenskape historiske kunstmanus, men er ikke en diffusjonsmodell og lærer ikke fotorealisme av et eksempelbilde.

Verifikasjon: Android CI og `python3 -m unittest discover -s tools -p 'test_aiguide_bridge.py' -v`.
