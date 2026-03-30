## Version 1.1.0

- Added periodic heartbeat that sends the full online player list to the backend every 5 minutes
- Backend uses the heartbeat to reconcile stale sessions from missed join/leave events
- Heartbeat interval is configurable via `heartbeatIntervalMinutes` (default: 5, set to 0 to disable)
- Heartbeat reuses existing API endpoint and JWT authentication
- Fixed HTTP/1.1 compatibility issues
