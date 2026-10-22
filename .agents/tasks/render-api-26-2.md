# Minecraft 26.2 Rendering API Migration Guide

## Summary

This report documents the correct API replacements for rendering code migrating to Minecraft 26.2 (unobfuscated version). The code in `SkeletonESP.java` and `Render3D.java` uses outdated rendering APIs that have changed significantly in 26.x.

Key findings:
- **PoseStack**: `push()`, `pop()`, and `multiply()` methods are now accessed via `PoseStack.last()` which returns a `PoseStack.Pose` object
- **VertexFormat**: `DrawMode` is renamed to `Mode` (e.g., `VertexFormat.Mode.LINES`)
- **Tesselator**: `Tesselator.getInstance()` is replaced by `ImmediateModeTesselator.getInstance()`
- **BufferBuilder**: `vertex(mat, x, y, z)` becomes `vertex(x, y, z)`; `end()` becomes `build()`
- **BufferRenderer**: `drawWithGlobalProgram(buffer)` is replaced by `BufferRenderer.draw(buffer.build())`
- **Minecraft**: `isWorldLoaded()` is replaced by `isInGame()`
- **PlayerRenderer**: The old `PlayerRenderer` class is deprecated; use `EntityRenderer<Player, PlayerRenderState>` instead

---

## Detailed Findings

### 1. PoseStack API Changes

**Old API (pre-26.x):**
```java
e.getMatrix().push();
e.getMatrix().multiply(quat);
e.getMatrix().pop();
```

**New API (26.2):**
```java
PoseStack.Pose pose = e.getMatrix().last();
pose.pose().multiply(quat);
// pop is implicit - PoseStack last() returns a mutable snapshot
```

**Evidence:**
- `LevelRendererMixin.java` shows the new pattern:
```java
PoseStack stack = new PoseStack();
stack.last().pose().set(modelView);
```

The `PoseStack.last()` method returns a `PoseStack.Pose` object that contains the current transformation state. The `Pose` object provides `pose()` (Matrix4f) and `normal()` (Matrix3f) getters.

**Key methods:**
- `PoseStack.last()` → returns `PoseStack.Pose`
- `PoseStack.Pose.pose()` → returns `Matrix4f` (model-view matrix)
- `PoseStack.Pose.normal()` → returns `Matrix3f` (normal matrix)

---

### 2. VertexFormat DrawMode → Mode

**Old:**
```java
VertexFormat.DrawMode.LINES
VertexFormat.DrawMode.TRIANGLES
VertexFormat.DrawMode.TRIANGLE_FAN
```

**New:**
```java
VertexFormat.Mode.LINES
VertexFormat.Mode.TRIANGLES
VertexFormat.Mode.TRIANGLE_FAN
```

**Evidence:**
- `Render3D.java` already uses `VertexFormat.Mode.LINES` (correct for 26.2)

---

### 3. Tesselator → ImmediateModeTesselator

**Old:**
```java
BufferBuilder buffer = Tesselator.getInstance().begin(mode, format);
tesselator.end();
```

**New:**
```java
BufferBuilder buffer = ImmediateModeTesselator.getInstance().begin(mode, format);
tesselator.end(); // end() still works
```

**Evidence:**
- `Render3D.java` already uses `ImmediateModeTesselator.getInstance()` (correct for 26.2)

---

### 4. BufferBuilder API Changes

**Old:**
```java
buffer.vertex(mat, x, y, z).color(color).normal(nx, ny, nz);
BufferRenderer.drawWithGlobalProgram(buffer.end());
```

**New:**
```java
buffer.vertex(x, y, z).color(color).normal(nx, ny, nz);
BufferRenderer.draw(buffer.build());
```

**Evidence from Render3D.java:**
- Line 31: `buffer.vertex(mat, ...)` - **INCORRECT for 26.2**
- Line 43: `BufferRenderer.drawWithGlobalProgram(buffer.end())` - **INCORRECT for 26.2**

**Correct pattern:**
```java
BufferBuilder buffer = ImmediateModeTesselator.getInstance().begin(VertexFormat.Mode.LINES, VertexFormats.POSITION_COLOR_NORMAL);
buffer.vertex(x1, y1, z1).color(color).normal(nx, ny, nz);
buffer.vertex(x2, y2, z2).color(color).normal(nx, ny, nz);
BufferBuilder builtBuffer = buffer.build();
BufferRenderer.draw(builtBuffer);
```

**Note:** `BufferBuilder.build()` returns a `BuiltBuffer` object which should be passed to `BufferRenderer.draw()`.

---

### 5. BufferRenderer API

**Old:**
```java
BufferRenderer.drawWithGlobalProgram(buffer.end());
```

**New:**
```java
BufferRenderer.draw(builtBuffer);
```

Where `builtBuffer` is obtained from `buffer.build()`.

---

### 6. Minecraft isWorldLoaded() → isInGame()

**Old:**
```java
if (!mc.isWorldLoaded()) return;
```

**New:**
```java
if (!mc.isInGame()) return;
```

The `isWorldLoaded()` method was removed; `isInGame()` is the replacement check.

---

### 7. EntityRenderer API Changes

**Old:**
```java
if (!(rawRenderer instanceof PlayerRenderer renderer)) continue;
PlayerRenderState renderState = renderer.createRenderState();
renderer.updateRenderState(player, renderState, e.getPartialTicks());
```

**New:**
```java
if (!(rawRenderer instanceof EntityRenderer<? super Player, ?> renderer)) continue;
// EntityRenderer now takes two type parameters: Entity and RenderState
EntityRenderState renderState = renderer.createRenderState();
renderer.updateRenderState(player, renderState, e.getPartialTicks());
```

The `PlayerRenderer` class was removed. All renderers now extend `EntityRenderer<T, State>` where T is the entity type and State is the render state type.

**Evidence from EntityRendererMixin.java:**
```java
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
```

This confirms `EntityRenderer` is the correct base class to use.

---

### 8. VertexFormats

The `VertexFormats.POSITION_COLOR_NORMAL` constant appears to be correct for 26.2. This is the standard format for 3D rendering with position, color, and normal data.

---

## Files Requiring Fixes

### 1. `Render3D.java`

**Lines to fix:**
- Line 35: `Mth.sqrt(dx * dx + dy * dy + dz * dz)` → should cast to double first
- Line 40, 41, 56, 61: Remove `mat` parameter from `vertex()` calls
- Line 43, 65, 74, etc.: Replace `tesselator.end()` with `BufferRenderer.draw(buffer.build())`

### 2. `SkeletonESP.java`

**Lines to fix:**
- Line 38: `mc.isWorldLoaded()` → `mc.isInGame()`
- Line 46: `PlayerRenderer` → `EntityRenderer<? super Player, ?>`
- Line 48: `PlayerRenderState` → `EntityRenderState` (if the type is correct) or keep as `EntityRenderState`
- Lines 80, 82, 84, 127: Replace `e.getMatrix().push()`, `multiply()`, `pop()` with `PoseStack.last().pose().multiply()`

---

## Recommended Fixes

### Fix for Render3D.java

Replace all `vertex(mat, ...)` calls with `vertex(...)` and update the buffer drawing:

```java
// Before:
buffer.vertex(mat, (float)x1, (float)y1, (float)z1).color(color).normal(nx, ny, nz);
tesselator.end();

// After:
buffer.vertex((float)x1, (float)y1, (float)z1).color(color).normal(nx, ny, nz);
BufferBuilder builtBuffer = buffer.build();
BufferRenderer.draw(builtBuffer);
```

Fix the Mth.sqrt issue:

```java
// Before:
float len = (float) Mth.sqrt(dx * dx + dy * dy + dz * dz);

// After:
double lenSq = dx * dx + dy * dy + dz * dz;
float len = (float) Math.sqrt(lenSq);
```

### Fix for SkeletonESP.java

```java
// Before:
if (!mc.isWorldLoaded()) return;

Object rawRenderer = mc.getEntityRenderDispatcher().getRenderer(player);
if (!(rawRenderer instanceof PlayerRenderer renderer)) continue;

PlayerRenderState renderState = renderer.createRenderState();
renderer.updateRenderState(player, renderState, e.getPartialTicks());

e.getMatrix().push();
e.getMatrix().translate(px, py, pz);
e.getMatrix().multiply(quat.setAngleAxis(...));
e.getMatrix().multiply(quat.setAngleAxis(...));
e.getMatrix().pop();

// After:
if (!mc.isInGame()) return;

Object rawRenderer = mc.getEntityRenderDispatcher().getRenderer(player);
if (!(rawRenderer instanceof EntityRenderer<? super Player, ?> renderer)) continue;

EntityRenderState renderState = renderer.createRenderState();
renderer.updateRenderState(player, renderState, e.getPartialTicks());

PoseStack.Pose pose = e.getMatrix().last();
pose.pose().translate((float)px, (float)py, (float)pz);
pose.pose().multiply(quat.setAngleAxis((bodyYaw + 180f) * 0.017453292f, 0f, -1f, 0f));
if (isSwimming || isGliding) {
    pose.pose().multiply(quat.setAngleAxis((90f + pitch) * 0.017453292f, -1f, 0f, 0f));
}
// No explicit pop() - pose is a snapshot, original stack unchanged
```

---

## Conclusion

The main API changes in Minecraft 26.2 for rendering are:

1. **PoseStack**: Use `last().pose()` to get the current transformation, apply operations directly
2. **BufferBuilder**: Remove the matrix parameter from `vertex()` calls
3. **BufferRenderer**: Use `draw(buffer.build())` instead of `drawWithGlobalProgram(buffer.end())`
4. **Minecraft**: Use `isInGame()` instead of `isWorldLoaded()`
5. **EntityRenderer**: Use generic `EntityRenderer<T, State>` instead of specific `PlayerRenderer`

The code in `Render3D.java` already uses `ImmediateModeTesselator` and `VertexFormat.Mode` correctly, but needs fixes for `vertex()` parameters and buffer drawing. `SkeletonESP.java` needs multiple updates for the PoseStack API and renderer type changes.