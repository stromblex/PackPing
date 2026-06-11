# Changelog

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
