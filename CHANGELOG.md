## Version 1.5.0

### Changed
- JWT tokens sent to the backend now include a scoped audience claim (`createrington.mod`), improving compatibility with CRNet 3.0 and ensuring mod-issued tokens cannot be replayed against other Createrington services.
