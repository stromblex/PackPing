# Changelog

## 1.1.0-mc.1.21.7

* Fixed update checks failing when update metadata URLs return HTTP redirects.
* Added redirect handling for GitLab latest release permalink URLs.
* Improved update-check errors for redirect loops, unsupported URL schemes, empty responses, non-successful responses, and invalid JSON.
* Blocked unsafe HTTPS-to-HTTP redirects while keeping existing update metadata unchanged.
* No config changes are required.
