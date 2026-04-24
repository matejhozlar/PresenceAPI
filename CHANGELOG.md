## Version 1.5.1

### Fixed
- Fixed a server startup crash on fresh installs caused by the default `jwtSecret` placeholder being too short. The new default value meets the 256-bit minimum required by HS256, so the server no longer throws a `WeakKeyException` before operators have a chance to set their own secret.
