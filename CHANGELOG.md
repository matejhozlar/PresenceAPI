## Version 1.5.2

### Fixed
- Fixed a server crash on startup when the configured JWT secret is shorter than 32 characters (256 bits). The mod now logs a clear error pointing to `config/presenceapi-common.toml` and disables itself gracefully, allowing the server to continue running.
