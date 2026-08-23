---
name: gn-prepare-release
description: Prepare a GamingNews release by validating the version, generating localized Play Store release notes, and checking the CI workflow is ready.
when-to-use: When the user wants to prepare, validate, or tag a new GamingNews release.
user-invocable: true
paths: ["app/build.gradle.kts", "distribution/whatsnew/**", ".github/workflows/play-store-release.yml"]
effort: medium
---

# GamingNews Release Preparation

Use this skill when the user wants to cut a new release of the GamingNews Android app.

## Pre-release checks

1. **Inspect `app/build.gradle.kts`**
   - Read `versionCode` and `versionName`.
   - Verify `versionCode` is higher than the previous release.
   - Verify `versionName` matches the intended Git tag (e.g. `v2.5.0` → `versionName = "2.5.0"`).

2. **Review recent changes**
   - Run `git log` to see commits since the last version tag.
   - Identify the user-facing changes that belong in the release notes.

3. **Validate the Play Store release notes directory**
   - Ensure `distribution/whatsnew/` exists.
   - Ensure the required locale files are present:
     - `whatsnew-en-US`
     - `whatsnew-es-419`
     - `whatsnew-es-ES`
     - `whatsnew-es-US`
   - Each file must be plain UTF-8 text with no extension and at most 500 characters.

4. **Verify GitHub secrets (checklist only)**
   - `PLAY_SERVICE_ACCOUNT_JSON`
   - `SIGNING_KEYSTORE_BASE64`
   - `SIGNING_KEY_ALIAS`
   - `SIGNING_KEY_PASSWORD`
   - `SIGNING_STORE_PASSWORD`
   - `GOOGLE_SERVICES_JSON`
   - `TWITCH_CLIENT_ID`
   - `TWITCH_CLIENT_SECRET`
   - `ISSUE_ASSIGNEE` (variable, optional)

## Release steps

1. If the user has not already decided the version, propose a semantic version bump (`major.minor.patch`) based on recent commits.
2. Update `versionCode` and `versionName` in `app/build.gradle.kts` if needed.
3. Generate localized release notes in `distribution/whatsnew/whatsnew-<locale>`.
   - Keep each under 500 characters.
   - Focus on user-facing changes.
   - Do not include XML tags or markup; the files are plain text.
4. Ask the user to review the generated notes and the version change.
5. After approval, commit the changes to a branch, push, and create the version tag (e.g. `v2.5.0`) to trigger `.github/workflows/play-store-release.yml`.

## Anti-patterns

- Do not tag without updating `versionName` and `versionCode` first.
- Do not commit the signing keystore, `keystore.properties`, `local.properties`, or `google-services.json`.
- Do not leave `whatsnew-<locale>` files empty or with placeholder text when uploading to Play.
