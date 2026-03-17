#version 150

// ============================================================
//  MCME-Action-Bar – GUI-scale-invariant HUD positioning
//
//  How it works (mirrors BetterHud's technique):
//
// Extending the vanilla default vertex shader to detect HUD characters
//
//  Each HUD icon character has a huge *negative* ascent baked
//  into the resource-pack font.  That pushes its vertex Y far
//  below the visible screen (pos.y >= ui.y), which the shader
//  detects.  The element ID is encoded in those extra pixels,
//  and the shader uses it to reposition the character to a
//  fixed anchor expressed as a *percentage* of ui (the screen
//  size in GUI pixels).  Because ui already accounts for the
//  player's GUI-scale setting, the final screen position is
//  identical at every GUI scale.
//
//  Encoding formula (Java / resource-pack tooling side):
//      ascent = -(((elementId + MAGIC_OFFSET) << HEIGHT_BIT) + ENCODE_FILLER)
//  where
//      HEIGHT_BIT    = 13
//      MAGIC_BIT     = 10
//      MAGIC_OFFSET  = 1024  (= 1 << MAGIC_BIT)
//      ENCODE_FILLER = 4095
//
//  Example – element ID 1 (chat-channel icon):
//      ascent = -(((1 + 1024) << 13) + 4095) = -8 400 895
//
//  Adding a new HUD element:
//    1. Assign the next elementId (2, 3, …).
//    2. Compute its ascent with the formula above.
//    3. Set that ascent in the font provider for the glyph(s).
//    4. Add a new `case` to the switch below.
//    5. The plugin emits the glyph character — no prefix needed.
// ============================================================

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform int FogShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out vec4 vertexColor;
out vec2 texCoord0;
out float vertexDistance;

// ── Encoding constants – must match the font ascent formula ──
#define HEIGHT_BIT    13
#define MAGIC_BIT     10
#define MAGIC_OFFSET  1024   // (1 << MAGIC_BIT)
#define ENCODE_FILLER 4095

// Distance of the action-bar text baseline from the bottom of the
// screen, in GUI pixels.  Must match where Minecraft draws the action
// bar — do NOT change this to move icons; use per-element yOffset instead.
#define ACTION_BAR_Y  49.0

void main() {
    vec3 pos = Position;

    // Screen size in GUI pixels.
    // ProjMat for the GUI pass is orthographic: proj[0][0] = 2 / guiWidth
    vec2 ui = ceil(2.0 / vec2(ProjMat[0][0], -ProjMat[1][1]));

    // ── Detect MCME HUD glyphs ──────────────────────────────────
    bool isBelowScreen = pos.y >= ui.y;
    bool isGuiPass     = ProjMat[3].x == -1.0; // orthographic GUI projection has origin top-left
    if (isBelowScreen && isGuiPass) {

        // Shift out the lower bits to isolate the encoded marker and ID
        int encodedY = int(pos.y) >> HEIGHT_BIT;

        bool hasMagicBit = ((encodedY >> MAGIC_BIT) & 1) == 1;
        if (hasMagicBit) {
            int elementId = encodedY - MAGIC_OFFSET;

            // ── Step 1: move X origin to the left screen edge ──────
            // Action-bar text is centred, so subtracting half the GUI
            // width cancels that centering.  Each vertex then carries
            // only its offset relative to the left edge.
            pos.x -= 0.5 * ui.x;

            // ── Step 2: strip Y encoding & action-bar baseline ─────
            // After this, pos.y holds only the glyph's local vertical
            // offset from the baseline (~0 to −height px).
            pos.y -= float((encodedY << HEIGHT_BIT) + ENCODE_FILLER);
            pos.y -= (ui.y - ACTION_BAR_Y);

            // ── Step 3: anchor + offset per element ─────────────────
            //
            //   xPercent  0 = left edge   50 = centre   100 = right edge
            //   yPercent  0 = top          50 = centre   100 = bottom
            //
            //   xOffset / yOffset: fine nudge *from* the anchor, in GUI pixels.
            //   1 GUI pixel = (GUI scale) physical pixels — so at GUI scale 2 one
            //   unit here is 2 physical pixels, at scale 3 it is 3, etc.
            //   Use whole-number values only to keep glyphs pixel-crisp at all scales.
            //
            //  The PNGs scale with the GUI scale so the offsets works at any GUI scale
            //
            float xPercent, yPercent;
            float xOffset, yOffset;

            switch (elementId) {
                // ── Chat-channel icon — bottom-left corner ──
                case 1:
                    xPercent = 0.0; yPercent = 100.0;
                    xOffset  = 9.0; yOffset  = -10.0;
                    break;

                // ── Add future elements here ────────────────────────
                // case 2:
                //     xPercent = 100.0; yPercent = 100.0;
                //     xOffset = -9.0;   yOffset = -49.0;
                //     break;

                default:
                    // Unrecognised elementId - If an icon is misplaced, check that its
                    // elementId has a case here and that the font ascent uses the matching encoding formula.
                    xPercent = 0.0; yPercent =  50.0;
                    xOffset  = 0.0; yOffset  = -10.0;
                    break;
            }

            pos.x += ui.x * (xPercent / 100.0) + xOffset;
            pos.y += ui.y * (yPercent / 100.0) + yOffset;
        }
    }

    vec4 worldPos = ModelViewMat * vec4(pos, 1.0);

    // Mirrors vanilla — fog_distance() inlined since #moj_import is unavailable in resource packs.
    vertexDistance = FogShape == 0 ? length(worldPos.xyz)
                                   : max(length(worldPos.xz), abs(worldPos.y));

    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0  = UV0;
    gl_Position = ProjMat * worldPos;
}