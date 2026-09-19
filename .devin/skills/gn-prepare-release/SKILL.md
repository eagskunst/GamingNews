---
name: gn-prepare-release
description: Cut a new GamingNews release - bump the version, update the changelog, generate localized Play Store release notes, commit, tag, and push to trigger the Play Store upload workflow.
when-to-use: When the user wants to release, tag, or publish a new GamingNews version.
user-invocable: true
paths: ["app/build.gradle.kts", "CHANGELOG.md", "distribution/whatsnew/**", ".github/workflows/play-store-release.yml"]
effort: medium
---

# GamingNews Release

Use this skill when the user wants to publish a new GamingNews release. Pushing the
version tag triggers `.github/workflows/play-store-release.yml`, which runs the tests,
builds the AAB, and uploads it to the Google Play production track.

## Steps

1. **Decide the version.** Read `versionCode` and `versionName` in
   `app/build.gradle.kts`. Propose a semantic bump based on the changes
   (patch for fixes/small tweaks, minor for features, major for breaking changes).

2. **Bump the version.** Update `versionCode` (+1) and `versionName` in
   `app/build.gradle.kts`.

3. **Update `CHANGELOG.md`.** Add a `## [X.Y.Z] - YYYY-MM-DD` section at the top
   describing the user-facing changes, following the Keep a Changelog style used by
   previous entries.

4. **Write Play Store release notes.** Create or overwrite the files in
   `distribution/whatsnew/`:
   - `whatsnew-en-US`
   - `whatsnew-es-419`
   - `whatsnew-es-ES`
   - `whatsnew-es-US`

   Each file is plain UTF-8 text, no extension, max 500 characters. Base the notes on
   `git log` since the previous tag. Only describe user-facing changes.

5. **Commit on master** (or the branch the user asked for) and push.

6. **Tag and push the tag** to trigger the release workflow:

   ```bash
   git tag -a vX.Y.Z -m "Release X.Y.Z"
   git push origin master vX.Y.Z
   ```

   The workflow fails if the tag does not match `versionName`, so the tag must be
   `v` + `versionName` exactly.

## Checks

- The tag must equal `versionName` (with a leading `v`) or the workflow aborts.
- `versionCode` must be higher than the last released one or Play rejects the AAB.
- Never commit `keystore.properties`, `local.properties`, `google-services.json`, or
  keystore files.
- If instrumented tests fail in CI, the workflow opens a GitHub issue and does not
  upload; re-run the workflow after investigating.
