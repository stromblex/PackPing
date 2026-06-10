# Changelog

## 1.1.0-mc.1.21.5

* Fixed update checks failing when update metadata URLs return HTTP redirects.
* Added redirect handling for GitLab latest release permalink URLs.
* Improved update-check errors for redirect loops, unsupported URL schemes, empty responses, non-successful responses, and invalid JSON.
* Blocked unsafe HTTPS-to-HTTP redirects while keeping existing update metadata unchanged.
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
