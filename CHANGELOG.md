## Version 1.3.0

### Added
- Added a periodic heartbeat that sends the full online player list to the backend at a configurable interval. This allows the backend to reconcile stale sessions caused by missed leave events.
- The heartbeat interval is configurable via `heartbeatIntervalMinutes` in the config (default: 5 minutes, set to 0 to disable).

### Changed
- The `endpoint` config key has been replaced with three separate options: `apiUrl` (the base URL of your backend), `presenceEndpoint` (path for join/leave events, default `/api/presence`), and `heartbeatEndpoint` (path for heartbeat syncs, default `/api/presence/heartbeat`). Existing config files will need to be updated.
- Backend error responses are now logged with the HTTP status code and any error message returned by the backend, making it easier to diagnose integration issues.

### Fixed
- Fixed HTTP/1.1 compatibility issues that caused errors with certain backend servers.
- Fixed a missing config section bracket that could cause config parsing to fail silently.
