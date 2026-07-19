package com.pourfect.domain

enum class GrindMethod(val label: String) {
    V60("V60 pour over"),
    AEROPRESS("AeroPress"),
    ESPRESSO("Espresso"),
    FRENCH_PRESS("French press"),
    COLD_BREW("Cold brew")
}

data class GrinderInfo(
    val id: String,
    val brand: String,
    val model: String,
    val note: String,
    val settings: Map<GrindMethod, String>
)

/**
 * Starting points gathered from maker manuals and widely used community
 * charts. They are starting points, not gospel: burrs vary unit to unit,
 * so the dial-in advisor's click adjustments matter more than the number
 * you start from.
 */
object GrinderCatalog {

    val all: List<GrinderInfo> = listOf(
        GrinderInfo(
            id = "comandante-c40", brand = "Comandante", model = "C40 MK4",
            note = "Clicks counted from tightest zero.",
            settings = mapOf(
                GrindMethod.V60 to "22–30 clicks · start 24",
                GrindMethod.AEROPRESS to "18–25 clicks",
                GrindMethod.ESPRESSO to "8–12 clicks",
                GrindMethod.FRENCH_PRESS to "30–35 clicks",
                GrindMethod.COLD_BREW to "35–40 clicks"
            )
        ),
        GrinderInfo(
            id = "timemore-c2", brand = "Timemore", model = "Chestnut C2",
            note = "Clicks from zero. Avoid going under 5 clicks.",
            settings = mapOf(
                GrindMethod.V60 to "18–24 clicks · start 21",
                GrindMethod.AEROPRESS to "15–20 clicks",
                GrindMethod.ESPRESSO to "8–11 clicks",
                GrindMethod.FRENCH_PRESS to "24–28 clicks",
                GrindMethod.COLD_BREW to "28–32 clicks"
            )
        ),
        GrinderInfo(
            id = "timemore-c3", brand = "Timemore", model = "Chestnut C3 / C3 Pro",
            note = "Clicks from zero. Avoid going under 5 clicks.",
            settings = mapOf(
                GrindMethod.V60 to "16–22 clicks · start 19",
                GrindMethod.AEROPRESS to "14–19 clicks",
                GrindMethod.ESPRESSO to "5–9 clicks",
                GrindMethod.FRENCH_PRESS to "22–26 clicks",
                GrindMethod.COLD_BREW to "26–30 clicks"
            )
        ),
        GrinderInfo(
            id = "timemore-c3s-pro", brand = "Timemore", model = "Chestnut C3S Pro",
            note = "S2C burrs, clicks from zero.",
            settings = mapOf(
                GrindMethod.V60 to "16–22 clicks · start 19",
                GrindMethod.AEROPRESS to "14–19 clicks",
                GrindMethod.ESPRESSO to "5–9 clicks",
                GrindMethod.FRENCH_PRESS to "22–26 clicks",
                GrindMethod.COLD_BREW to "26–30 clicks"
            )
        ),
        GrinderInfo(
            id = "timemore-c5s-pro", brand = "Timemore", model = "Chestnut C5S Pro",
            note = "Same S2C click system as the C3S family. If your manual differs, trust the manual.",
            settings = mapOf(
                GrindMethod.V60 to "15–22 clicks · start 18",
                GrindMethod.AEROPRESS to "14–19 clicks",
                GrindMethod.ESPRESSO to "6–9 clicks",
                GrindMethod.FRENCH_PRESS to "22–26 clicks",
                GrindMethod.COLD_BREW to "26–30 clicks"
            )
        ),
        GrinderInfo(
            id = "1zpresso-jx", brand = "1Zpresso", model = "JX",
            note = "Counted in rotations from zero, bottom dial.",
            settings = mapOf(
                GrindMethod.V60 to "2.4–3.0 rotations · start 2.6",
                GrindMethod.AEROPRESS to "2.2–2.8 rotations",
                GrindMethod.ESPRESSO to "1.0–1.4 rotations",
                GrindMethod.FRENCH_PRESS to "3.2–3.8 rotations",
                GrindMethod.COLD_BREW to "3.6–4.2 rotations"
            )
        ),
        GrinderInfo(
            id = "1zpresso-jxpro", brand = "1Zpresso", model = "JX-Pro / J-Max",
            note = "Finer steps than the JX; rotations from zero.",
            settings = mapOf(
                GrindMethod.V60 to "2.6–3.4 rotations · start 3.0",
                GrindMethod.AEROPRESS to "2.4–3.0 rotations",
                GrindMethod.ESPRESSO to "1.2–1.8 rotations",
                GrindMethod.FRENCH_PRESS to "3.6–4.2 rotations",
                GrindMethod.COLD_BREW to "4.0–4.6 rotations"
            )
        ),
        GrinderInfo(
            id = "1zpresso-jultra", brand = "1Zpresso", model = "J-Ultra",
            note = "8 microns per click; read as rotations-numbers-clicks on the external dial.",
            settings = mapOf(
                GrindMethod.V60 to "1.8–2.5 rotations · start 2-0-3",
                GrindMethod.AEROPRESS to "1.6–2.2 rotations",
                GrindMethod.ESPRESSO to "0.7–1.1 rotations",
                GrindMethod.FRENCH_PRESS to "2.6–3.2 rotations",
                GrindMethod.COLD_BREW to "3.0–3.6 rotations"
            )
        ),
        GrinderInfo(
            id = "1zpresso-kmax", brand = "1Zpresso", model = "K-Max / K-Ultra",
            note = "External top dial numbers.",
            settings = mapOf(
                GrindMethod.V60 to "6–9 on the dial · start 7.5",
                GrindMethod.AEROPRESS to "5–8 on the dial",
                GrindMethod.ESPRESSO to "2–4 on the dial",
                GrindMethod.FRENCH_PRESS to "9–11 on the dial",
                GrindMethod.COLD_BREW to "10–12 on the dial"
            )
        ),
        GrinderInfo(
            id = "1zpresso-q2", brand = "1Zpresso", model = "Q2 / Q Air",
            note = "Rotations from zero, bottom dial.",
            settings = mapOf(
                GrindMethod.V60 to "1.5–2.2 rotations · start 1.8",
                GrindMethod.AEROPRESS to "1.3–1.8 rotations",
                GrindMethod.ESPRESSO to "0.5–0.9 rotations",
                GrindMethod.FRENCH_PRESS to "2.2–2.8 rotations",
                GrindMethod.COLD_BREW to "2.6–3.2 rotations"
            )
        ),
        GrinderInfo(
            id = "kingrinder-k6", brand = "Kingrinder", model = "K6",
            note = "16 microns per click, external adjustment.",
            settings = mapOf(
                GrindMethod.V60 to "60–90 clicks · start 75",
                GrindMethod.AEROPRESS to "50–70 clicks",
                GrindMethod.ESPRESSO to "20–40 clicks",
                GrindMethod.FRENCH_PRESS to "90–110 clicks",
                GrindMethod.COLD_BREW to "100–120 clicks"
            )
        ),
        GrinderInfo(
            id = "kingrinder-k4", brand = "Kingrinder", model = "K4",
            note = "16 microns per click, external adjustment.",
            settings = mapOf(
                GrindMethod.V60 to "60–90 clicks · start 75",
                GrindMethod.AEROPRESS to "50–70 clicks",
                GrindMethod.ESPRESSO to "20–40 clicks",
                GrindMethod.FRENCH_PRESS to "90–110 clicks",
                GrindMethod.COLD_BREW to "100–120 clicks"
            )
        ),
        GrinderInfo(
            id = "kingrinder-k2", brand = "Kingrinder", model = "K2",
            note = "18 microns per click, internal adjustment.",
            settings = mapOf(
                GrindMethod.V60 to "50–75 clicks · start 60",
                GrindMethod.AEROPRESS to "40–60 clicks",
                GrindMethod.ESPRESSO to "15–30 clicks",
                GrindMethod.FRENCH_PRESS to "75–95 clicks",
                GrindMethod.COLD_BREW to "85–105 clicks"
            )
        )
    )

    fun byId(id: String): GrinderInfo? = all.firstOrNull { it.id == id }
}
