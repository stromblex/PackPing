# Changelog

## 1.3.0-mc26.1.x-release

* Fixed server-defined toast notices so Minecraft 26.1.x entries can show a toast even when the installed pack version is current.
* Kept fullscreen and chat update prompts limited to entries with a newer version.
* Allowed toast-only current-version notices without requiring update download metadata.
* Rebuilt release artifacts for Minecraft 26.1.x.
* No local config changes are required.

## 1.2.0-mc26.1.x-release

* Fixed update checks selecting the wrong release metadata when multiple entries target the same Minecraft version.
* Added optional `loader` matching for server update JSON entries.
* Rebuilt release artifacts for Minecraft 26.1.x.
* No local config changes are required.

## 1.1.0-mc.26.1.x

* Fixed update checks failing when update metadata URLs return HTTP redirects.
* Added redirect handling for GitLab latest release permalink URLs.
* Improved update-check errors for redirect loops, unsupported URL schemes, empty responses, non-successful responses, and invalid JSON.
* Blocked unsafe HTTPS-to-HTTP redirects while keeping existing update metadata unchanged.
* No config changes are required.

## 1.0.1.11

* Added support for Minecraft 26.1.x.
* Updated internal dependencies for Minecraft 26.1.2.
* Refreshed update notification rendering and chat messages for current client APIs.
* Refreshed release metadata for the 26.1 release files.
* No config changes are required.

## 1.0.1.10

* Added support for Minecraft 1.21.11.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.11 release files.
* No config changes are required.

## 1.0.1.9

* Added support for Minecraft 1.21.10.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.10 release files.
* No config changes are required.

## 1.0.1.8

* Added support for Minecraft 1.21.9.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.9 release files.
* No config changes are required.

## 1.0.1.7

* Added support for Minecraft 1.21.8.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.8 release files.
* No config changes are required.

## 1.0.1.6

* Added support for Minecraft 1.21.7.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.7 release files.
* No config changes are required.

## 1.0.1.5

* Added support for Minecraft 1.21.6.
* Updated internal dependencies for the current Minecraft version.
* Refreshed release metadata for the 1.21.6 release files.
* No config changes are required.

## 1.0.1.4

* Added support for Minecraft 1.21.5.
* Updated internal dependencies for the current Minecraft version.
* Refreshed build tooling for the 1.21.5 release files.
* No config changes are required.

## 1.0.1.3

* Added support for Minecraft 1.21.4.
* Rebuilt release files for the latest supported Minecraft version.
* No config changes are required.

## 1.0.1.2

* Added support for Minecraft 1.21.3.
* Updated internal dependencies.
* Refreshed the build setup for current loader versions.
* No config changes are required.

## 1.0.1.1

* Started publishing stable release builds.
* Added support for Minecraft 1.21.2 on Fabric and NeoForge.
* Updated Gradle wrapper to 8.10.
* Updated Fabric Loader to 0.19.2 and NeoForge to 21.2.1-beta.
* Improved release maintenance for both supported loaders.

## 1.0.1

* Removed chat separators from update messages.
* Added clickable download link support.
* Updated internal version metadata.
* Made update messages cleaner and easier to use in-game.

## 1.0.0

* Initial public release.
* Added in-game update notifications for Minecraft modpacks.
* Added support for configurable update metadata.
* Added lightweight update checking for modpack authors.
* Added Fabric and NeoForge release builds.
