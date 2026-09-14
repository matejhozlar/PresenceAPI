## Version 1.7.0

### Added
- Configurable presence endpoint and heartbeat endpoint paths (relative to `apiUrl`) so the mod can target any backend routes; the presence endpoint reloads without a restart, the heartbeat endpoint requires one
- JSON field naming convention option: `camelCase` (default) or `snake_case` for all request body fields
- Authentication mode option: `jwt` (default, signs each request with an HS256 bearer token) or `none` for backends that require no authorization header
- New optional telemetry fields: player rotation (yaw and pitch), experience level, current health, ping latency, and vanilla play-time stat in ticks
- Final heartbeat sent on server shutdown to keep the backend in sync, including when the periodic heartbeat is disabled
