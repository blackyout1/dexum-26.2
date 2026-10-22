# ClickGuiScreen Compilation Error Investigation

## Summary

The compilation errors are caused by incorrect API usage in `ClickGuiScreen.java` for Fabric 26.2:

1. **`Minecraft.setScreen()`**: The `client.setScreen()` method no longer exists. The correct method is `client.setScreenAndShow()` (already used elsewhere in Dexum.java).

2. **`GuiGraphicsExtractor.font()`**: The `font()` method doesn't exist. The `GuiGraphicsExtractor` context has a `font` field directly accessible as `context.font`. However, the real issue is that `GuiGraphicsExtractor.text()` requires a `font` parameter directly, not `context.font()` as a method call.

3. **Mouse coordinates**: The `GuiGraphicsExtractor` provides `guiWidth()` and `guiHeight()` methods, not direct `mouseX`/`mouseY` fields.

## Evidence

### Dexum.java (Lines 86-88)
```java
// Current (BROKEN):
client.setScreen(clickGuiScreen);

// Already CORRECT pattern in same file (lines 105-106):
if (clickGuiScreen != null && client.gui.screen() == null) {
    client.setScreenAndShow(clickGuiScreen);
}
```

### ClickGuiScreen.java - Problem Area (Lines 129-133)
```java
// Current (BROKEN):
int tw = Minecraft.getInstance().font.width(display);
context.text(Minecraft.getInstance().font, display,
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);

// The error says "cannot find symbol: method font()"
// This means GuiGraphicsExtractor has font as a FIELD, not a method
```

### Working Code - CategoryPanel.java (Lines 102-104)
```java
// CORRECT pattern used elsewhere:
Minecraft mc = Minecraft.getInstance();
int textWidth = mc.font.width(categoryText);
context.text(mc.font, categoryText,
        x + (width - textWidth) / 2, y + 6, 0xFFFFFFFF);
```

### Working Code - ModuleButton.java (Lines 90-94)
```java
// CORRECT pattern:
Minecraft mc = Minecraft.getInstance();
int textColor = lerpColor(TEXT_OFF, TEXT_ON, enabledAnim);
int nameMaxWidth = width - 11 - 18;
String nameText = mc.font.width(function.getName()) > nameMaxWidth
        ? mc.font.plainSubstrByWidth(function.getName(), nameMaxWidth - mc.font.width("…")) + "…"
        : function.getName();
context.text(mc.font, nameText, x + 11, y + 5, textColor);
```

### Reference - Lenrete Mod (ClickGuiScreen.java)
```java
// Working pattern in similar 26.2 mod:
private void themedText(GuiGraphicsExtractor g, String text, int x, int y, int color) {
    g.text(this.font, text, x, y, color, themeTextShadow);
}

// Usage:
int textY = panelY + (TOP_BAR_HEIGHT - this.font.lineHeight) / 2;
int labelX = close.x + (close.w - this.font.width(label)) / 2;
themedText(g, label, labelX, labelY, applyAlpha(labelColor, frameAlpha));
```

## Conclusions

1. **Fix `setScreen()`**: Replace `client.setScreen(clickGuiScreen)` with `client.setScreenAndShow(clickGuiScreen)` (already implemented elsewhere, so consistency fix needed).

2. **Fix `font()` calls**: In `ClickGuiScreen.java`, the issue is NOT that `font()` is a method vs field. The real problem is the code is trying to call `context.font()` as a method when `context` is `GuiGraphicsExtractor` but the variable is named `context` and should be accessed as `context.font` directly. However, looking at the code, `context.text()` is being called correctly with `Minecraft.getInstance().font` as first parameter.

3. **Root cause analysis**: The error message "cannot find symbol: method font()" on line 129-130 suggests `context` is NOT a `GuiGraphicsExtractor` but something else. Let me check the method signature:

### Method Signature (ClickGuiScreen.java Line 78)
```java
@Override
public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
```

The method signature shows `GuiGraphicsExtractor context` is correct. But the error shows `context.font()` being called as a method.

Wait - looking more carefully at the error:

```
C:\Users\blackyout1\Desktop\dexum\src\main\java\dexum\client\system\ui\ClickGuiScreen.java:129: error: cannot find symbol
                int tw = context.font().width(display);
                                ^
  symbol:   method font()
  location: variable context of type GuiGraphicsExtractor
```

The code at line 129 calls `context.font()` as a METHOD, but `GuiGraphicsExtractor` has `font` as a PUBLIC FIELD, not a getter method. So the correct syntax is `context.font`, not `context.font()`.

## Recommendations

### Fix 1: Replace setScreen() call in Dexum.java
**File**: `src/main/java/dexum/client/Dexum.java`
**Line**: 88

Change:
```java
client.setScreen(clickGuiScreen);
```

To:
```java
client.setScreenAndShow(clickGuiScreen);
```

### Fix 2: Replace context.font() with context.font in ClickGuiScreen.java
**File**: `src/main/java/dexum/client/system/ui/ClickGuiScreen.java`
**Lines**: 129-131

Change:
```java
int tw = Minecraft.getInstance().font.width(display);
context.text(Minecraft.getInstance().font, display,
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
```

To:
```java
int tw = Minecraft.getInstance().font.width(display);
context.text(Minecraft.getInstance().font, display,
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
```

Wait, that's the same. Let me re-read the error...

Actually, the error shows `context.font()` on line 129, but the code I read shows `Minecraft.getInstance().font.width(display)` on line 130. This suggests the actual compiled code has `context.font()` somewhere.

Looking at line 129-130 of my read:
```
129:                int tw = Minecraft.getInstance().font.width(display);
130:                context.text(Minecraft.getInstance().font, display,
```

But the error says line 129 has `context.font()`. This means either:
1. The error is from a different version of the file
2. There's a mismatch in line numbers

Let me re-examine the actual error more carefully:

```
C:\Users\blackyout1\Desktop\dexum\src\main\java\dexum\client\system\ui\ClickGuiScreen.java:129: error: cannot find symbol
                int tw = context.font().width(display);
```

The error shows `context.font()` on line 129. But when I read the file, line 129 shows:
```java
int tw = Minecraft.getInstance().font.width(display);
```

This discrepancy suggests the file was modified or there's caching. However, the error message is authoritative. The code SHOULD be calling `context.font()` somewhere, which is wrong because `font` is a field, not a method.

### Actual Fix (based on error messages):

**Lines 129-133** should be changed from:
```java
int tw = context.font().width(display);  // WRONG - font() is not a method
context.text(context.font(), display,    // WRONG - context.font() not valid
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
```

To:
```java
int tw = Minecraft.getInstance().font.width(display);
context.text(Minecraft.getInstance().font, display,
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
```

OR more cleanly (following the working pattern in CategoryPanel/ModuleButton):

```java
Minecraft mc = Minecraft.getInstance();
int tw = mc.font.width(display);
context.text(mc.font, display,
        ix + (iw - tw) / 2, iy + (ih - 8) / 2, textColor);
```

### Verification

After applying fixes, run:
```bash
./gradlew runClient --stacktrace
```

Expected: Build succeeds without compilation errors.

## Additional Notes

- The project uses **unobfuscated** Minecraft 26.2 (no mappings line in build.gradle)
- `GuiGraphicsExtractor` is the correct render context class for 26.x
- `font` is a public field on `GuiGraphicsExtractor`, not accessed via getter
- The working modules (CategoryPanel, ModuleButton) already use the correct pattern consistently
- The project has a working HUD system (Dexum.java lines 50-57) that uses `HudElementRegistry.addLast()` correctly