# Changelog

## 1.1.0-mc.1.21.2

* Fixed update checks failing when update metadata URLs return HTTP redirects.
* Added redirect handling for GitLab latest release permalink URLs.
* Improved update-check errors for redirect loops, unsupported URL schemes, empty responses, non-successful responses, and invalid JSON.
* Blocked unsafe HTTPS-to-HTTP redirects while keeping existing update metadata unchanged.
* No config changes are required.

## 1.0.1.1

* Started publishing stable release builds.
* Added support for Minecraft 1.21.2 on Fabric and NeoForge.
* Updated Gradle wrapper to 8.10.
* Updated Fabric Loader to 0.19.2 and NeoForge to 21.2.1-beta.
* Improved release maintenance for both supported loaders.
