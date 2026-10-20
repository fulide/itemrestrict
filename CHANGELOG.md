# Changelog

## 1.1.0

- **Server-side only**: restrictions are now stored in the vanilla `minecraft:custom_data` component.
- Removed the custom `itemrestrict:restriction` DataComponent registration.
- Clients no longer need the mod to join a server.
- The optional client-side tooltip still works when the mod is installed.
- Added `docs/使用说明.md` and `dist/itemrestrict-1.1.0-serveronly.jar`.

## 1.0.0

- Initial release.
- Stored restrictions in a custom `itemrestrict:restriction` DataComponent.
- Required the mod on both server and client because the custom component type is a synchronized registry entry.
