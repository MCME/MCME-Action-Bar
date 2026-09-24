# MCME Action Bar

A PaperMC plugin that renders custom HUD elements via the action bar, using a shader-based 
technique to position them anywhere on screen, independent of GUI scale.

## How it works
The plugin sends special Unicode characters to the action bar. Each character has a huge 
negative font ascent that pushes it off-screen. A custom vertex shader (`text.vsh`) detects these 
off-screen vertices, decodes an element ID from the Y position, and repositions the character to a configured screen anchor.

## Adding a new HUD element
Three things need to happen: resource pack setup, shader positioning, and plugin code.

### 1. Resource pack
Add a new element entry to `resourcepack/hud-elements.groovy` and specify its glyph(s), then run `./gradlew generateFontJson` to regenerate `default.json`. The Gradle task computes the encoded font ascent automatically.

### 2. Shader
Add a `case` to the switch in `text.vsh` for your element ID:

```glsl
case 2:
    xPercent = 100.0; yPercent = 100.0;  // bottom-right anchor
    xOffset = -20.0;  yOffset = -27.0;   // nudge in GUI pixels
    break;
```

### 3. Plugin
Implement the `HudElement` interface and register it with `ActionBarManager`:

```java
public class MyHudElement implements HudElement {
    @Override
    public @Nullable Component getElement(Player player) {
        return Component.text("\uE208");
    }
}
```

```java
actionBarManager.register(new MyHudElement());
```