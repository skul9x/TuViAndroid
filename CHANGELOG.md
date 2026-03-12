# Changelog

## [2026-03-12]
### Added
- **Supabase Background Sync**: Implemented a non-blocking background synchronization system to upload user data to Supabase.
- **Telemetry Collection**: Added collection of Device Info (Brand, Model, OS) and raw IP Address.
- **Supabase Sync Debug UI**:
    - Created `SyncLogger` for in-memory logging.
    - Implemented `DebugLogScreen` with real-time log terminal, Copy, Share, and "Test Sync" functionality.
    - Integrated Debug Console into `SettingsScreen`.
- **Phone Number Capture**: 
    - Added a manual phone number field in the UI ("Số điện thoại phong thủy") with marketing text to encourage user input.
    - Integrated automatic SIM phone number reading as a fallback.
- **Supabase Integration**: Created `TelemetryRepository`, `SupabaseConfig`, and `DeviceInterceptor` for networking and telemetry.
- **SQL Setup Script**: Created `docs/sql/supabase_setup.sql` for easy database initialization.
- **Permissions**: Added `READ_PHONE_STATE` and `READ_PHONE_NUMBERS` to `AndroidManifest.xml`.

### Changed
- **TelemetryRepository**: Added detailed `SyncLogger` event hooks and a specialized `testSync()` method.
- **Supabase Security**: Reconfigured RLS Policies to allow anonymous inserts securely by granting `USAGE` on schema and using specialized `INSERT` policies.
- **InputScreen UI**: Integrated the Phone Number field into the main input form.
- **TuViViewModel**: Added background sync trigger in the `calculateLaso` flow.
- **Project Structure**: Configured `local.properties` and added `OkHttp` dependency to the project.

### Fixed
- **Supabase Error 42501**: Resolved "new row violates row-level security policy" by changing OkHttp header from `Prefer: return=representation` to `Prefer: return=minimal`.
- **Networking Issues**: Resolved Android cleartext/HTTPS issues by switching to `https` for IP fetching and enabling `usesCleartextTraffic`.
- **Compilation Errors**: Fixed OkHttp syntax and dependency issues in `TelemetryRepository`.

## [2026-03-11]
... (existing entries)
