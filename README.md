# MCME Action Bar

A PaperMC plugin that renders custom HUD elements via the action bar, using a shader-based 
technique to position them anywhere on screen, independent of GUI scale.

## Building
Requires JDK 25. Run `./gradlew build`; the plugin jar is written to `build/libs/`.
`./gradlew runServer` starts a test server with LuckPerms, and `./gradlew spotlessApply` formats the code.

## How it works
The plugin sends special Unicode characters to the action bar. Each character has a huge 
negative font ascent that pushes it off-screen. A custom vertex shader (`text.vsh`) detects these 
off-screen vertices, decodes an element ID from the Y position, and repositions the character to a configured screen anchor.

### Why the action bar instead of boss bars?
BetterHud uses the same shader technique with boss bars. The action bar is simpler: it needs only Paper's
`sendActionBar`, with no boss bars to create and track per player, no boss bar textures to hide, and no
shifting when the game adds its own bars (e.g. raids, the Wither). The trade-off is that action bar
messages fade after ~3 seconds, so they are resent every `update-interval-ticks`, and other plugins'
action bar messages briefly replace the HUD.

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
    public String id() {
        return "my-element"; // Used by /hudmcme and the stored preference
    }

    @Override
    public @Nullable Component getElement(Player player) {
        // zeroWidth appends the element's negative space so it never shifts other elements
        return HudElement.zeroWidth(2, "\uE208");
    }
}
```

```java
actionBarManager.register(new MyHudElement());
```

## Hiding elements
Players can run `/hudmcme <show|hide> <element>` to hide individual elements. The setting is stored as
LuckPerms meta (`mcme-actionbar-hidden-<element>=true`) with no server context, so it follows players across
backends that share a LuckPerms database. Setting that meta on a group hides the element by default for its members.
Without LuckPerms installed, `/hudmcme` is not registered and every element is shown.
