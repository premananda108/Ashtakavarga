package ua.pp.soulrise.ashtakavarga.common

import android.graphics.Color
import ua.pp.soulrise.ashtakavarga.ui.PlanetSignActivity

object Planet {
    const val SUN = 1
    const val MOON = 2
    const val MARS = 3
    const val MERCURY = 4
    const val JUPITER = 5
    const val VENUS = 6
    const val SATURN = 7
    const val HOUSE = 8
}

object ZodiacSign {
    const val ARIES = 1
    const val TAURUS = 2
    const val GEMINI = 3
    const val CANCER = 4
    const val LEO = 5
    const val VIRGO = 6
    const val LIBRA = 7
    const val SCORPIO = 8
    const val SAGITTARIUS = 9
    const val CAPRICORN = 10
    const val AQUARIUS = 11
    const val PISCES = 12
}

enum class QualityLevel(val level: Int, override val description: String, override val color: Int) : PlanetSignActivity.QualityInfoProvider<QualityLevel> {
    WORST(0, "😖 хуже не бывает", Color.parseColor("#FF0000")), // Ярко-красный
    VERY_BAD(1, "😫 еще хуже", Color.parseColor("#FF0000")),
    WORSE(2, "😞 совсем плохо", Color.parseColor("#FF0000")),
    BAD(3, "🙁 плохо", Color.parseColor("#FF0000")),
    AVERAGE(4, "😐 средне", Color.parseColor("#FFA500")), // Оранжевый
    BETTER(5, "🙂 лучше", Color.parseColor("#00FF00")),
    EVEN_BETTER(6, "😊 еще лучше", Color.parseColor("#00FF00")),
    VERY_GOOD(7, "😃 очень хорошо", Color.parseColor("#00FF00")),
    EXCELLENT(8, "🤩 высший класс", Color.parseColor("#00FF00")); // Ярко-зеленый

    companion object {
        fun fromLevel(level: Int): QualityLevel? = entries.find { it.level == level }
    }
}

enum class modQuality(val level: Int, override val description: String, override val color: Int) : PlanetSignActivity.QualityInfoProvider<modQuality> {
    DETERIORATION(0, "🙁 ухудшение", Color.parseColor("#FF4400")), // Красный
    UNCHANGED(1, "😐 без изменений", Color.parseColor("#FFA500")), // Оранжевый
    ENHANCEMENT(2, "🙂 улучшение", Color.parseColor("#88FF00")); // Зеленый

    companion object {
        fun fromLevel(level: Int): modQuality? = entries.find { it.level == level }
    }
}

enum class curQuality(val level: Int, override val description: String, override val color: Int) : PlanetSignActivity.QualityInfoProvider<curQuality> {
    NEGATIVE(0, "🙁 негатив", Color.parseColor("#FF4400")), // Красный (0-3)
    MEDIUM(1, "😐 средне", Color.parseColor("#FFA500")), // Оранжевый (4)
    POSITIVE(2, "🙂 позитив", Color.parseColor("#88FF00")); // Зеленый (5+)

    companion object {
        fun fromLevel(level: Int): curQuality? = entries.find { it.level == level }
    }
}