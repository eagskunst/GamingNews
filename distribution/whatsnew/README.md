# Play Store release notes

This directory is used by the `play-store-release` GitHub Action via the `whatsNewDirectory` input of `r0adkll/upload-google-play`.

Before pushing a release tag, create or update files named `whatsnew-<locale>` (no extension) for each supported locale, for example:

- `whatsnew-en-US`
- `whatsnew-es-419`
- `whatsnew-es-ES`
- `whatsnew-es-US`

Each file should contain plain UTF-8 text with a maximum of 500 characters, describing what is new in the release. You can ask Devin to generate these notes and commit them before tagging.
