# Pepper Morphe Patches

> 📦 **Also available for ReVanced** → [pepper-revanced-patches](https://github.com/PawiX25/pepper-revanced-patches) — same patch set, upstream [ReVanced Patcher](https://github.com/ReVanced/revanced-patcher) bundle for use with [ReVanced Manager](https://github.com/ReVanced/revanced-manager).

Morphe-framework port of [pepper-revanced-patches](https://github.com/PawiX25/pepper-revanced-patches).

This is a 1:1 port of the upstream ReVanced patch bundle to the
[Morphe](https://github.com/MorpheApp) patcher framework. The patch logic,
fingerprints, and every injected smali instruction are unchanged — the only
edits are package and import renames required to compile against
`app.morphe.patcher` instead of `app.revanced.patcher`. Patched APKs are
expected to be byte-equivalent to those produced by the ReVanced build.

For the full description of every patch (Hide ads, Compact deal cards,
all T1–T10 telemetry patches, dependency graph, verification commands, etc.)
see the upstream README:
https://github.com/PawiX25/pepper-revanced-patches#readme

## Supported apps

| Region | Package | App name |
|---|---|---|
| 🇵🇱 Poland | `com.tippingcanoe.pepperpl` | Pepper PL |
| 🇳🇱 Netherlands | `com.tippingcanoe.peppernl` | Pepper NL |
| 🇩🇪 Germany | `com.tippingcanoe.mydealz` | Mydealz |
| 🇬🇧 UK | `com.tippingcanoe.hukd` | HotUKDeals |
| 🇫🇷 France | `com.dealabs.apps.android` | Dealabs |
| 🇲🇽 Mexico | `com.tippingcanoe.promodescuentos` | PromoDescuentos |
| 🇪🇸 Spain | `com.chollometro` | Chollometros |
| 🇦🇹 Austria | `com.preisjaeger` | Preisjäger |
| 🇸🇪 Sweden | `se.pepperdeals` | Pepper SE |
| 🇺🇸 USA | `com.pepperdeals` | Pepper.com |

## Use with Morphe Manager

In Morphe Manager → **Sources → Patches**, add this repository as a custom
source. The release workflow publishes `patches-bundle.json` and
`patches-list.json` against every push to `main`/`dev`.

## Build from source

Requires **JDK 17** and a GitHub PAT with `read:packages` scope, exported as
`GITHUB_ACTOR` + `GITHUB_TOKEN` (the Morphe patcher dependency is hosted on
GitHub Packages). See
[Morphe patcher setup](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md#-prepare-the-environment).

```bash
./gradlew :patches:buildAndroid generatePatchesList
# Output: patches/build/libs/pepper-morphe-patches-<version>.mpp
```

## Mapping from upstream

| ReVanced | Morphe |
|---|---|
| `app.revanced.patcher.*` | `app.morphe.patcher.*` |
| `app.revanced.patches.pepper.*` | `app.pepper.patches.*` |
| `app.revanced.com.android.tools.smali.*` | `com.android.tools.smali.*` |
| `bytecodePatch(use = false, ...)` | `bytecodePatch(default = false, ...)` |
| `.rvp` bundle (`buildPatchBundle`) | `.mpp` bundle (`buildAndroid`) |

All patch bodies — fingerprint queries, smali stubs, register growth,
dependency graphs, every injected `const-string` / field / annotation — are
identical to upstream. Names that ship in the final DEX (e.g.
`revanced_pepper`, `revancedMockHwid`, `revanced-blocked-pepper-ocular`)
are intentionally **not** renamed, to keep the patched output byte-identical
to the ReVanced build.

## Patches

<!-- PATCHES_START -->
> **[v1.0.0](https://github.com/PawiX25/pepper-morphe-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;161 patches total
<details>
<summary>📦 Pepper PL&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Pepper NL&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Mydealz&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 HotUKDeals&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 PromoDescuentos&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Chollometros&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Dealabs&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Preisjäger&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Pepper.com&nbsp;&nbsp;•&nbsp;&nbsp;17 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Disable PAIRIP license check](#disable-pairip-license-check) | Removes Google Play's install-source DRM check from the US Pepper.com build, allowing sideloaded APKs to open. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<details>
<summary>📦 Pepper SE&nbsp;&nbsp;•&nbsp;&nbsp;16 patches</summary>
<br>

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Always show event-theming icons](#always-show-event-theming-icons) | Shows every event-themed app icon in the picker, even when its event is not currently active. |  |
| [Block Pepper analytics-event-report tracker](#block-pepper-analytics-event-report-tracker) | Stops Pepper's own behavioural tracker (thread visits, shares, push clicks, search suggestions) from reaching the server. |  |
| [Compact deal cards](#compact-deal-cards) | Shrinks Pepper deal-list cards and their loading skeletons with targeted XML resource edits. |  |
| [Disable Adjust SDK](#disable-adjust-sdk) | Stops the Adjust SDK from initialising and tracking events. |  |
| [Disable Google Mobile Ads SDK init](#disable-google-mobile-ads-sdk-init) | Stops the Google Mobile Ads SDK from initialising. |  |
| [Disable Google Mobile Ads ad-load entry points](#disable-google-mobile-ads-ad-load-entry-points) | Blocks Google's Ad SDK from fetching native ads after init. |  |
| [Enable debug menu](#enable-debug-menu) | Re-enables the hidden debug menu in the main activity. | • Debug menu resource ID (hex) |
| [Fix spacing around hidden ad cells](#fix-spacing-around-hidden-ad-cells) | Removes the empty space and shadow divider left behind by hidden banner-ad cells in deal-detail screens. | • small_ad view-type R.id (hex)<br>• medium_ad view-type R.id (hex)<br>• large_ad view-type R.id (hex) |
| [Hide banner ads in feed](#hide-banner-ads-in-feed) | Removes the banner ads in the deal feed. |  |
| [Keep event icon after restart](#keep-event-icon-after-restart) | Keeps the chosen event-themed app icon after the event ends. |  |
| [Kill Datatransport upload pipeline](#kill-datatransport-upload-pipeline) | Blocks Crashlytics report and Firebase log uploads from leaving the device. |  |
| [Kill Pepper first-party pixel tracking](#kill-pepper-first-party-pixel-tracking) | Blocks Pepper's first-party pixel-tracking pings and replaces the device-fingerprint header with a per-install random UUID. |  |
| [Neuter tracker auto-init ContentProviders](#neuter-tracker-auto-init-contentproviders) | Stops the Vungle, Adjust, and Facebook Audience Network SDKs from auto-initialising at app start. |  |
| [Redirect tracker URLs to localhost](#redirect-tracker-urls-to-localhost) | Redirects every known tracker and analytics URL in the app to localhost so it cannot reach the network. |  |
| [Skip Usercentrics consent screen](#skip-usercentrics-consent-screen) | Skips the Usercentrics consent screen and its loading wait on cold start. |  |
| [Unlock tier-locked icons](#unlock-tier-locked-icons) | Unlocks all membership-tier app icons in the icon picker. |  |

</details>

<!-- PATCHES_END -->

## License

[GPL-3.0](LICENSE). Patches originate from
[PawiX25/pepper-revanced-patches](https://github.com/PawiX25/pepper-revanced-patches)
(GPL-3.0). This port is not affiliated with Atolls, ReVanced, or Morphe.
