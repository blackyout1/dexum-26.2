# Texture Atlas Duplicate Sprite Analysis Report

## Executive Summary

The duplicate sprite warnings shown in the log output are **NOT originating from the Dexum mod** located at `c:\Users\blackyout1\Desktop\dexum`. This investigation found that the Dexum mod contains only its own textures (under the `dexum` namespace) and has no custom texture atlas definitions. The duplicate sprite warnings are caused by **other mods loaded in the same Minecraft instance** that share the same namespace resources or have overlapping texture definitions.

---

## Section 1: Atlas Definition Files Found

### Dexum Mod (`c:\Users\blackyout1\Desktop\dexum\src\main\resources`)

**Atlas Definition Files Found: NONE**

The Dexum mod does not define any custom texture atlas files (e.g., `atlases/gui.json`, `atlases/items.json`, `atlases/blocks.json`). It only contains:

- Font resources: `assets/dexum/font/*.json` + `.png` files (bold, icons, montserrat)
- Shader resources: `assets/dexum/shaders/*.fsh`, `*.vsh` files
- Texture resources: `assets/dexum/textures/logo.png`, `assets/dexum/textures/icons/*.png`

**No atlas JSON files exist in the project.** Minecraft's default atlas behavior applies to this mod's resources.

### Expected Atlas Locations (Not Present)

Standard Minecraft mod texture atlas definitions would be located at:
- `assets/{namespace}/atlases/gui.json`
- `assets/{namespace}/atlases/items.json`
- `assets/{namespace}/atlases/blocks.json`

None of these were found in the Dexum mod.

---

## Section 2: Java Sprite Registration Code Found

### Dexum Mod (`c:\Users\blackyout1\Desktop\dexum\src\main\java`)

**Sprite Registration Code Found: NONE**

The investigation searched for:
- `TextureAtlasSprite` references
- `SpriteIdentifier` usage
- `SpriteSource` usage
- `registerAtlasSprite` calls
- `addSprite` or `addAtlas` method calls
- References to `minecraft:textures/atlas/` paths

**Result: No sprite registration code was found in the Dexum mod's Java source files.**

The mod appears to be a client-side utility mod that uses Minecraft's default rendering pipeline and does not register custom textures or atlases.

---

## Section 3: Root Cause Analysis

### The Real Source of Duplicates

The duplicate sprite warnings mentioned in the log are from these namespaces:
- `minecraft:item/*` (Minecraft items)
- `civilization:item/*` (Civilization mod)
- `astrum:item/*` (Astrum mod)
- `bettermodel:item/*` (BetterModel mod)
- `minecraft:em/*` (likely Emerald Mod or similar)

**These textures are NOT part of the Dexum mod.** They belong to OTHER mods that are installed in the same Minecraft instance as Dexum.

### Likely Causes of Duplicates (External Mods)

Based on the warning patterns, the duplicates are likely caused by:

1. **Multiple mods defining the same texture path in different atlases** - For example:
   - Civilization mod might define `civilization:item/ui/social` in BOTH `atlases/gui.json` AND `atlases/items.json`
   - BetterModel mod might define `bettermodel:item/kill_effect_circular_circular` in both `atlases/items.json` AND `atlases/gui.json`

2. **Mod dependency conflicts** - One mod might ship textures in the wrong atlas, and another mod (or resource pack) might override them to a different atlas location

3. **Resource pack interference** - A resource pack loaded alongside the mods might be duplicating sprite definitions

### Why These Warnings Occur

Minecraft 1.21+ enforces that each texture path can only exist in ONE atlas. When the same path is found in multiple atlases (e.g., `civilization:item/ui/social` appears in both `gui.png` and `items.png` atlases), Minecraft logs the warning. These warnings will become hard errors in future versions.

---

## Section 4: Per-Duplicate Recommendations

Since the duplicates originate from EXTERNAL mods (not Dexum), here's how to address them:

### For Mod Developers (Civilization, Astrum, BetterModel, etc.)

| Texture Path | Current Duplicates | Correct Atlas | Fix Location |
|-------------|-------------------|---------------|--------------|
| `civilization:item/ui/social` | gui.png, items.png | items.png (if used in 3D models) or gui.png (if GUI-only) | civilization mod's `assets/civilization/atlases/` |
| `astrum:item/case/end_question_mark` | gui.png, items.png | items.png | astrum mod's `assets/astrum/atlases/` |
| `bettermodel:item/kill_effect_circular_circular` | gui.png, items.png | items.png | bettermodel mod's `assets/bettermodel/atlases/` |
| `minecraft:item/netherite_boots/trim/minecraft/snout` | blocks.png, gui.png | items.png (vanilla bug) | N/A - report to Mojang/Minecraft |
| `minecraft:em/*` paths | Multiple | Varies by usage | Varies by mod |

### For the User (Dexum Mod User)

**Dexum is NOT causing these warnings.** To resolve the duplicate sprite warnings:

1. **Identify which mod(s) are causing duplicates**:
   - Look at the namespace in each warning (e.g., `civilization:...` means the Civilization mod is responsible)
   - Check which mods are installed in your `mods` folder

2. **Update mods to latest versions**:
   - Many mod developers have already fixed these issues in newer versions
   - Check mod pages (CurseForge, Modrinth) for updates

3. **Report to mod developers** (if not updated):
   - Provide the exact warning message
   - Explain that texture paths must only appear in ONE atlas
   - Suggest they check their `atlases/` JSON files for duplicate entries

4. **Temporary workaround** (if needed):
   - Remove the conflicting mod(s) if the functionality isn't critical
   - Use only one mod that provides similar features

---

## Section 5: Concrete Next Steps

### Immediate Actions for the User

1. **Verify which mods are installed**:
   ```
   Check the `mods` folder in your Minecraft instance
   ```

2. **Identify the conflicting mods**:
   - Extract the namespace from each warning (e.g., `civilization:` → Civilization mod)
   - Check if those mods have newer versions available

3. **Update or report to mod developers**:
   - Update to latest versions first
   - If still broken, report the issue with the full warning log

### For Dexum Mod Developers (If You Want to Ensure Clean Logs)

1. **Verify Dexum doesn't add duplicate textures**:
   - No atlas JSON files exist in the project (confirmed)
   - No sprite registration code found (confirmed)
   - Dexum should not contribute to these warnings

2. **Recommendation**: No action needed for Dexum - it's not the source of duplicates

### Technical Verification Steps

To verify the investigation findings:

1. **Check for atlas JSON files**:
   ```bash
   find . -path "*/assets/*/atlases/*.json" -type f
   ```
   Expected result: No files (Dexum uses default atlases)

2. **Search for sprite registration in Java**:
   ```bash
   grep -r "SpriteIdentifier\|TextureAtlasSprite\|addSprite" src/main/java
   ```
   Expected result: No matches (Dexum doesn't register sprites)

3. **Verify resources structure**:
   ```bash
   find src/main/resources/assets -type d -name "atlases"
   ```
   Expected result: No `atlases` directories found

---

## Conclusion

The duplicate sprite warnings are **caused by other mods in the Minecraft instance**, not by the Dexum mod at `c:\Users\blackyout1\Desktop\dexum`. This mod contains no custom texture atlas definitions and no sprite registration code that would cause such warnings.

**The Dexum mod is innocent.** The warnings originate from mods like Civilization, Astrum, BetterModel, and others that load alongside it.

**Resolution path**:
1. Identify the conflicting external mods from the warning namespace
2. Update those mods to their latest versions
3. If issues persist, report to the respective mod developers with the warning details

No changes are required to the Dexum mod codebase.
