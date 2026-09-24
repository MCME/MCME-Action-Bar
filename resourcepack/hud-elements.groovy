// To add a HUD element:
//   1. Add an entry to hudElements below
//   2. Run Gradle task generateFontJson to regenerate default.json
//   3. Add a matching case to text..vsh

hudElements = [
    [
        elementId: 1,
        comment  : "Chat-channel icons — bottom-left corner (case 1 in text.vsh)",
        glyphs   : [
            [char: "\uE200", image: "mcme:channel_icons/question_mark.png"],
            [char: "\uE201", image: "mcme:channel_icons/channel_l.png"   ],
            [char: "\uE202", image: "mcme:channel_icons/channel_t.png"   ],
            [char: "\uE203", image: "mcme:channel_icons/channel_j.png"   ],
            [char: "\uE204", image: "mcme:channel_icons/channel_s.png"   ],
            [char: "\uE205", image: "mcme:channel_icons/channel_m.png"   ],
            [char: "\uE206", image: "mcme:channel_icons/channel_h.png"   ],
            [char: "\uE207", image: "mcme:channel_icons/channel_r.png"   ],
        ]
    ]
]