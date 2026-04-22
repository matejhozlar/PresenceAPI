## Version 1.4.0

### Fixed
- Fixed phantom join and leave events being sent to the backend for fake players. Chunk loader mods (such as CRFP) insert fake players into the server player list to keep chunks loaded — these were previously treated as real sessions, polluting the presence log with entries that don't correspond to actual players.
