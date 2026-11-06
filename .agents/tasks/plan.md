# Dexum HUD Implementation Plan

## Overview
Implement a comprehensive HUD system for the Dexum Minecraft client inspired by Rich client's HUD. The system includes:
1. A sophisticated watermark display (logo, ping, FPS, coordinates, time, player face)
2. A module list (arraylist-style active module display) 
3. Glassmorphism effects and custom rendering pipelines
4. Draggable HUD elements with persistent positioning
5. Edit mode with element resizing

## Architecture Decisions

### 1. **Base HUD Element Class Architecture**
Create an `AbstractHudElement` base class that all HUD components extend. This provides:
- Position and dimension management (x, y, width, height)
- Drag-and-drop support with persistent positioning
- Visibility and animation state
- Shader/pipeline rendering capabilities

**Rationale**: Rich client uses this pattern effectively. Dexum doesn't have a draggable HUD framework yet, so we build one. It allows independent HUD elements to coexist and be managed uniformly.

### 2. **Rendering Pipeline Integration**
Use Dexum's existing `RenderUtil` class which already provides:
- `glassmorphism()` - frosted glass effect with blur and layered shadows
- `rect()` - rounded rectangle drawing
- `outline()` - outline drawing with custom radius per corner
- `texture()` - texture rendering with color modulation
- `textureUV()` - UV-mapped texture drawing (for player skins)

**Rationale**: The infrastructure exists. We leverage it rather than reinventing shader support. RenderUtil abstracts the rendering pipeline via GuiGraphicsExtractor context.

### 3. **Event System Integration**
Hook into Dexum's existing event system:
- Listen to `Render2DEvent` (fired each render tick with GuiGraphicsExtractor context)
- Use `EventBus.register()` and `@EventHandler` annotation pattern already established

**Rationale**: Clean, already proven pattern in Watermark.java and FriendHUD.java. No new integration point needed.

### 4. **Position Persistence**
Store HUD element positions in the existing settings infrastructure:
- `FloatSetting` for posX, posY per HUD element
- Load on module enable, save on drag completion
- Each element registers its settings via `addSettings()`

**Rationale**: Dexum already has `Setting`, `BooleanSetting`, `FloatSetting` in the settings system. No need for external config files.

### 5. **Module List Rendering**
Iterate through `FunctionManager.getFunctions()` filtered by enabled status.
Layout vertically with smooth animations for entry/exit.

**Rationale**: Direct access to module state. Can query enabled status anytime.

## Implementation Steps

### Step 1: Create AbstractHudElement base class
File: `src/main/java/dexum/client/system/hud/AbstractHudElement.java`

Abstract class defining the HUD element interface:
- Fields: `x`, `y`, `width`, `height` (float), `name` (String), dragging/resizing state
- Methods: `drawElement()`, `onDragStart()`, `onDragUpdate()`, `onDragEnd()`, `mouseClicked()`, `mouseReleased()`
- Provides position clamping, drag handling with GLFW input
- Stores position settings via Settings API

**Verification**: `npm run compile` (or gradle build) - no compile errors.

---

### Step 2: Create HudElementManager singleton class
File: `src/main/java/dexum/client/system/hud/HudElementManager.java`

Central registry for all HUD elements:
- Static `register(AbstractHudElement)` to add elements
- Static `getAll()` returning list of registered elements
- Static `update(Render2DEvent)` iterates all elements and calls their draw method
- Static `onMouseEvent()` for click/release dispatch

**Rationale**: Centralized management. Single entry point for rendering and event handling.

**Verification**: Compile check.

---

### Step 3: Enhance existing Watermark module
File: `src/main/java/dexum/client/system/features/render/Watermark.java`

Refactor existing Watermark to extend AbstractHudElement:
- Move position logic into AbstractHudElement (already has it locally)
- Register with HudElementManager in `onEnable()`
- Implement `drawElement()` with current rendering code
- Add settings for watermark scale, toggle components (ping/fps/coords/time) visibility
- Add drag handle UI in edit mode

**Rationale**: The Watermark already has good drag logic and rendering code. We refactor it to the new architecture rather than rewrite.

**Verification**: Run game, enable Watermark, verify it renders and drags correctly, check no regression in appearance.

---

### Step 4: Create ModuleList HUD element
File: `src/main/java/dexum/client/system/hud/ModuleList.java`

Display all enabled modules in an arraylist-style layout:
- Extends AbstractHudElement
- Queries `FunctionManager.getFunctions()` each frame
- Filters by `isEnabled()`
- Renders each module name with glassmorphism container
- Smooth entry/exit animations (scale/alpha fade)
- Settings: toggle sort by category, toggle background, font size adjustment

**Rationale**: Similar to Rich's HotKeys element but simpler (no keybind display). Dexum has animation utilities and the font rendering via Msdf.

**Verification**: Run game, enable several modules, toggle them on/off, verify list updates and animates.

---

### Step 5: Create Notifications/Alerts HUD element (optional, similar to Rich)
File: `src/main/java/dexum/client/system/hud/Notifications.java`

Display brief notifications (e.g., "Friend joined", "Alert triggered"):
- Extends AbstractHudElement
- Queue-based system for notifications
- Fade in/out with smooth animations
- Auto-dismiss after 3 seconds
- Extends from existing chat/event system

**Rationale**: Rich has a Notifications element. It adds polish and feedback. Dexum's event system can be tapped.

**Verification**: Trigger notifications via event, verify they appear and fade out correctly.

---

### Step 6: Create CombatInfo HUD element (targeting status)
File: `src/main/java/dexum/client/system/hud/CombatInfo.java`

Display current combat target info (similar to Rich's TargetHud):
- Extends AbstractHudElement
- Shows target name, health, distance, ping
- Hidden when no target
- Glassmorphism style with glow effect
- Color-coded health bar

**Rationale**: Rich has TargetHud. Dexum is a combat-focused client with AimAssist/Aura. Combat context display is valuable.

**Verification**: Target a player entity, verify HUD appears with correct info, verify it hides when no target.

---

### Step 7: Create HudEditMode feature
File: `src/main/java/dexum/client/system/features/render/HudEditMode.java`

Toggle feature to enable/disable HUD element dragging and resizing:
- Extends Function, Category.RENDER
- When enabled: show drag handles on all HUD elements, highlight elements on hover
- When disabled: lock positions, hide handles
- Broadcast event on enable/disable so HUD elements can respond
- Save positions to settings when exiting edit mode

**Rationale**: Users need a way to arrange HUD elements. A dedicated mode makes it clean and discoverable.

**Verification**: Enable HudEditMode, verify drag handles appear on HUD elements, drag them, disable mode, verify positions persist, restart game and verify positions load.

---

### Step 8: Integrate HudElementManager into Render2DEvent event dispatch
Files: `src/main/java/dexum/client/mixin/screen/GuiMixin.java` (or wherever Render2DEvent is fired)

Ensure HudElementManager is called:
- In the Render2DEvent fire location, call `HudElementManager.update(event)` after or before other handlers
- Ensure mouse events are dispatched to HudElementManager

**Rationale**: Need to inject HUD rendering and input into the render loop.

**Verification**: Compile, run game with HUD elements, verify they render and respond to input.

---

### Step 9: Add configuration persistence
File: `src/main/java/dexum/client/system/hud/HudConfig.java`

Persistence layer for HUD element positions and settings:
- Load positions from a config file (or Settings API) on client startup
- Save on exit or when dragging completes
- Per-element settings (scale, visibility toggles, etc.)

**Rationale**: Users expect HUD configuration to survive restarts.

**Verification**: Drag HUD elements, restart game, verify positions are restored.

---

### Step 10: Implement shader-based effects (advanced rendering)
Files: `src/main/java/dexum/client/utils/render/pipeline/GlassmorphismPipeline.java` (extend existing)

Enhance rendering with custom shaders:
- Frosted glass (blur + semi-transparent overlay)
- Gradient fills (vertical/horizontal)
- Glow outlines (soft shadow effect)
- Blur backgrounds behind elements

**Rationale**: Rich's HUD uses these effects heavily. Dexum's RenderUtil already has some (glassmorphism exists), but we add:
- Gradient support
- Better glow/shadow
- Optional blur on/off per element

**Verification**: Run game with complex HUD setup, verify visual quality matches Rich client style.

---

### Step 11: Add HUD screenshot/debug visualization
File: `src/main/java/dexum/client/system/hud/HudDebugOverlay.java`

Optional debug feature to visualize HUD element bounds and state:
- Outline mode: show bounding boxes for all HUD elements
- State display: show element name, position, size, animation state
- Hotkey to toggle

**Rationale**: Helps with layout tweaking and debugging rendering issues.

**Verification**: Toggle debug mode, verify bounding boxes and info display correctly.

---

## File Structure Summary

```
src/main/java/dexum/client/system/hud/
├── AbstractHudElement.java         (base class for all HUD elements)
├── HudElementManager.java          (central registry and renderer)
├── HudEditMode.java                (feature to toggle edit mode) 
├── ModuleList.java                 (enabled modules display)
├── Notifications.java              (notification queue display)
├── CombatInfo.java                 (target info display)
├── HudConfig.java                  (position persistence)
└── HudDebugOverlay.java            (debug visualization)

src/main/java/dexum/client/system/features/render/
├── Watermark.java                  (refactored to extend AbstractHudElement)
└── HudEditMode.java                (also registered as a feature)

src/main/java/dexum/client/utils/render/pipeline/
└── (existing pipelines enhanced as needed)
```

## Integration Points

1. **Render2DEvent**: HudElementManager listens and renders all HUD elements
2. **FunctionManager**: Used to retrieve enabled modules for ModuleList
3. **EventBus**: HUD elements register for events, edit mode broadcasts enable/disable
4. **Settings API**: HUD positions and visibility toggles stored as FloatSetting/BooleanSetting
5. **RenderUtil**: All rendering goes through existing utilities (rect, outline, glassmorphism, texture)

## Testing Strategy

1. **Compile test**: `gradle build` - all files compile without error
2. **Visual test**: Launch game, enable HUD elements one by one
3. **Drag test**: Enable HudEditMode, drag elements around, toggle mode off, restart, verify positions load
4. **Performance test**: Enable all HUD elements simultaneously, monitor FPS (should have minimal impact)
5. **Edge cases**: 
   - Very small screen resolution
   - HUD element positioned off-screen (should clamp)
   - Rapid toggle on/off
   - Multiple elements overlapping

## Design Rationale Summary

- **Why extend existing Watermark?** It already works; refactoring is safer than replacement.
- **Why use AbstractHudElement pattern?** Proven in Rich client; allows uniform management of independent UI elements.
- **Why HudElementManager as singleton?** Centralized control; single point for rendering, input, persistence.
- **Why attach to Render2DEvent?** Already fires each frame with the context we need; no new event type required.
- **Why Settings API for persistence?** Dexum already has a robust settings system; reuse rather than add new config format.
- **Why glassmorphism/shader effects?** Rich uses them heavily for visual appeal; Dexum already has RenderUtil support, we just leverage it.
