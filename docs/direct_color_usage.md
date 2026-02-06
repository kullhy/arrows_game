# Direct Color Usage in the App

This document lists all direct color usage that should be migrated to use theme colors.

## Summary

| Category | Count |
|----------|-------|
| `Color.*` direct usage | 8 |
| Global color constants (White, HeartRed, etc.) | 60+ |
| Hardcoded hex colors in GameConstants (confetti) | 4 |

---

## 1. Direct `Color.*` Usage

These use Compose's Color class directly instead of theme colors:

### ArrowsBoardRenderer.kt
| Line | Code | Suggested Theme Color |
|------|------|----------------------|
| 167 | `color = Color.Gray` | `themeColors.inactive` or new theme color |

### GameActivity.kt
| Line | Code | Suggested Theme Color |
|------|------|----------------------|
| 324 | `color = Color.Green` (debug overlay) | Debug-only, may keep as-is |

### TapAnimationState.kt
| Line | Code | Suggested Theme Color |
|------|------|----------------------|
| 35 | `color = Color.White.copy(alpha = 1f - value)` | `themeColors.ripple` or similar |

### DebugComponents.kt
| Line | Code | Suggested Theme Color |
|------|------|----------------------|
| 154 | `focusedContainerColor = Color.Transparent` | Keep as Transparent |
| 155 | `unfocusedContainerColor = Color.Transparent` | Keep as Transparent |

### WinCelebrationScreen.kt
| Line | Code | Suggested Theme Color |
|------|------|----------------------|
| 101 | `.background(Color.Black)` | `themeColors.celebrationBackground` |
| 114 | `.background(Color.Black)` | `themeColors.celebrationBackground` |
| 121 | `color = Color.White` | `themeColors.celebrationText` |

---

## 2. Global Color Constants Usage

These use color constants from `Color.kt` instead of theme colors:

### `White` Usage (most common - 40+ occurrences)

| File | Lines | Context |
|------|-------|---------|
| GameActivity.kt | 345, 371, 375, 412 | Loading text, dialog text |
| GameActivityHelpers.kt | 120, 126 | Icon colors |
| GenerateActivity.kt | 153, 157, 216, 217, 225, 309, 343, 377 | Text and icon colors |
| MainActivity.kt | 109, 114 | Logo and text |
| AdSettingsSection.kt | 56, 62, 90, 96 | Icons and text |
| AppNavigationBar.kt | 39, 76, 78, 104, 106, 130, 132 | Navigation colors |
| DebugComponents.kt | 143, 152, 153, 187, 209 | Dialog text |
| GameComponents.kt | 77, 89, 116 | Icons and buttons |
| SettingsBaseComponents.kt | 70, 76, 88, 90, 114, 120, 128, 136 | Settings item colors |
| SettingsComponents.kt | 165, 186, 208, 229 | Dialog text |

### `HeartRed` Usage

| File | Lines | Context |
|------|-------|---------|
| GameActivity.kt | 418 | Restart button text |
| GameComponents.kt | 102 | Heart icon tint |

### `ProgressBarGreen` Usage

| File | Lines | Context |
|------|-------|---------|
| GameActivity.kt | 350, 394 | Progress bar, button color |
| GameComponents.kt | 148 | Progress bar color |

### `InactiveIcon` Usage

| File | Lines | Context |
|------|-------|---------|
| AppNavigationBar.kt | 79, 80, 107, 108, 131, 133 | Unselected navigation items |
| DebugComponents.kt | 203 | Unselected radio button |
| SettingsBaseComponents.kt | 91 | Unchecked switch track |
| SettingsComponents.kt | 182, 225 | Unselected radio buttons |

### `NavigationIndicator` Usage

| File | Lines | Context |
|------|-------|---------|
| AppNavigationBar.kt | 77, 105, 134 | Selected item indicator |

### `LightGray` Usage

| File | Lines | Context |
|------|-------|---------|
| ArrowsBoardRenderer.kt | 237 | Tap area overlay |

### `FlashingRed` Usage

| File | Lines | Context |
|------|-------|---------|
| ArrowsBoardRenderer.kt | 251 | Flashing snake color |

---

## 3. Hardcoded Colors in GameConstants

These are confetti colors defined as hex integers:

| Constant | Value | Color |
|----------|-------|-------|
| `CONFETTI_COLOR_1` | `0xfce18a` | Yellow/Gold |
| `CONFETTI_COLOR_2` | `0xff726d` | Coral/Red |
| `CONFETTI_COLOR_3` | `0xf4306d` | Magenta/Pink |
| `CONFETTI_COLOR_4` | `0xb48def` | Purple |

---

## 4. Recommendations

### High Priority (should use theme colors)
1. **White text** - Should use `themeColors.textPrimary` or `themeColors.onBackground`
2. **ProgressBarGreen** - Should use `themeColors.success` or `themeColors.progressBar`
3. **HeartRed** - Should use `themeColors.error` or `themeColors.heart`
4. **InactiveIcon** - Should use `themeColors.inactive`
5. **NavigationIndicator** - Should use `themeColors.navigationIndicator`
6. **WinCelebrationScreen colors** - Should use `themeColors.celebrationBackground` and `themeColors.celebrationText`

### Medium Priority (consider theming)
1. **LightGray** in tap area - Could use `themeColors.tapAreaOverlay`
2. **FlashingRed** - Could use `themeColors.flashingSnake`

### Low Priority (may keep as-is)
1. **Color.Transparent** - Standard transparent, no need to theme
2. **Debug colors (Color.Green)** - Only shown in debug builds
3. **Confetti colors** - Decorative, could remain fixed or be themed

---

## 5. Suggested ThemeColors Additions

```kotlin
data class ThemeColors(
    // Existing...

    // New additions
    val textPrimary: Color,           // Replace White for text
    val textSecondary: Color,         // Replace White.copy(alpha = 0.7f)
    val success: Color,               // Replace ProgressBarGreen
    val error: Color,                 // Replace HeartRed
    val inactive: Color,              // Replace InactiveIcon
    val navigationIndicator: Color,   // Replace NavigationIndicator
    val tapAreaOverlay: Color,        // Replace LightGray
    val flashingSnake: Color,         // Replace FlashingRed
    val celebrationBackground: Color, // Replace Color.Black in celebration
    val celebrationText: Color,       // Replace Color.White in celebration
)
```
