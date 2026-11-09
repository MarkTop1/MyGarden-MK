package com.codegarden.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** 养护类型对应的线性图标 */
fun careIcon(type: com.codegarden.app.data.model.CareType): ImageVector = when (type) {
    com.codegarden.app.data.model.CareType.WATER -> GardenIcons.Drop
    com.codegarden.app.data.model.CareType.FERTILIZE -> GardenIcons.Grain
    com.codegarden.app.data.model.CareType.PEST -> GardenIcons.Bug
    com.codegarden.app.data.model.CareType.PRUNE -> GardenIcons.Scissors
}

/** 手绘线描风格图标：统一 24 视口、1.8 线宽、圆角端点，用 Icon(tint=...) 上色 */
object GardenIcons {
    val Leaf: ImageVector by lazy {
        stroke("Leaf", "M6 20 C5 12 9 4 19 4 C19 14 14 20 6 20 Z", "M6 20 C10 16 14 11 17 7")
    }
    val Calendar: ImageVector by lazy {
        stroke(
            "Calendar",
            "M5.5 5.5 H18.5 A1.5 1.5 0 0 1 20 7 V19 A1.5 1.5 0 0 1 18.5 20.5 H5.5 A1.5 1.5 0 0 1 4 19 V7 A1.5 1.5 0 0 1 5.5 5.5 Z",
            "M8 3.5 V7", "M16 3.5 V7", "M4 10 H20",
            "M8 14 H8.01", "M12 14 H12.01", "M16 14 H16.01", "M8 17 H8.01", "M12 17 H12.01"
        )
    }
    val BookOpen: ImageVector by lazy {
        stroke(
            "BookOpen",
            "M12 6.5 C10 5 6.5 4.5 4 5.5 V19 C6.5 18 10 18.5 12 20 C14 18.5 17.5 18 20 19 V5.5 C17.5 4.5 14 5 12 6.5 Z",
            "M12 6.5 V20"
        )
    }
    val Person: ImageVector by lazy {
        stroke("Person", "M8.7 8 a3.3 3.3 0 1 0 6.6 0 a3.3 3.3 0 1 0 -6.6 0", "M5.5 20 C5.5 15.8 8.4 13.5 12 13.5 C15.6 13.5 18.5 15.8 18.5 20")
    }
    val Bell: ImageVector by lazy {
        stroke("Bell", "M6 17 V11 C6 7.7 8.7 5 12 5 C15.3 5 18 7.7 18 11 V17 L19.5 19 H4.5 Z", "M10 21 A2 2 0 0 0 14 21", "M12 3 V5")
    }
    val Archive: ImageVector by lazy {
        stroke("Archive", "M4 8 H20 V11 H4 Z", "M5.5 11 V19 A1 1 0 0 0 6.5 20 H17.5 A1 1 0 0 0 18.5 19 V11", "M10 14 H14")
    }
    val Info: ImageVector by lazy {
        stroke("Info", "M3 12 a9 9 0 1 0 18 0 a9 9 0 1 0 -18 0", "M12 11 V16.5", "M12 7.5 H12.01")
    }
    val Camera: ImageVector by lazy {
        stroke(
            "Camera",
            "M4 9 A2 2 0 0 1 6 7 H8 L9.3 5 H14.7 L16 7 H18 A2 2 0 0 1 20 9 V17 A2 2 0 0 1 18 19 H6 A2 2 0 0 1 4 17 Z",
            "M9 13 a3 3 0 1 0 6 0 a3 3 0 1 0 -6 0"
        )
    }
    val ChevronLeft: ImageVector by lazy { stroke("ChevronLeft", "M14.5 5 L8 12 L14.5 19") }
    val ChevronRight: ImageVector by lazy { stroke("ChevronRight", "M9.5 5 L16 12 L9.5 19") }
    val Plus: ImageVector by lazy { stroke("Plus", "M12 5 V19 M5 12 H19") }
    val Pencil: ImageVector by lazy { stroke("Pencil", "M15.5 4.5 L19.5 8.5 L9 19 L4 20 L5 15 Z", "M13.5 6.5 L17.5 10.5") }
    val Trash: ImageVector by lazy {
        stroke(
            "Trash",
            "M5 7 H19",
            "M9.5 7 V5.5 A1 1 0 0 1 10.5 4.5 H13.5 A1 1 0 0 1 14.5 5.5 V7",
            "M6.5 7 L7.3 19.5 A1 1 0 0 0 8.3 20.5 H15.7 A1 1 0 0 0 16.7 19.5 L17.5 7",
            "M10 11 V17 M14 11 V17"
        )
    }
    val Drop: ImageVector by lazy { stroke("Drop", "M12 4 C12 4 6.5 10.5 6.5 15 A5.5 5.5 0 0 0 17.5 15 C17.5 10.5 12 4 12 4 Z") }
    val Sprout: ImageVector by lazy {
        stroke("Sprout", "M12 20 V10", "M12 12 C12 8.5 9 6.5 5 6.5 C5 10.5 8 12.5 12 12 Z", "M12 13 C12 10 14.5 8 18.5 8 C18.5 11.5 16 13.5 12 13.5 Z")
    }
    val Alert: ImageVector by lazy {
        stroke("Alert", "M12 4.5 L20.5 19.5 H3.5 Z", "M12 10 V14", "M12 16.8 H12.01")
    }
    val Search: ImageVector by lazy {
        stroke("Search", "M4.5 10.5 a6 6 0 1 0 12 0 a6 6 0 1 0 -12 0", "M15 15 L20.5 20.5")
    }
    val Clock: ImageVector by lazy {
        stroke("Clock", "M4 12 a8 8 0 1 0 16 0 a8 8 0 1 0 -16 0", "M12 7.5 V12 L15 14")
    }
    val Moon: ImageVector by lazy {
        stroke("Moon", "M20 15.5 C17.5 17.5 14.5 18 12 17 C8.5 15.5 7 11.5 8.5 8 C10.5 5.5 14 5.5 16.5 7 C15.5 4 12 2.5 9 4 C5.5 5.5 4 9.5 5.5 13 C7.5 17.5 13 20 18 18 C19 17.5 19.5 17 20 15.5 Z")
    }
    val Tag: ImageVector by lazy {
        stroke("Tag", "M4 4 H11 L20 13 L13 20 L4 11 Z", "M8 8 H8.01")
    }
    val Flower: ImageVector by lazy {
        stroke(
            "Flower",
            "M10 11.8 a2 2 0 1 0 4 0 a2 2 0 1 0 -4 0",
            "M9.8 6.5 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0",
            "M15 10.3 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0",
            "M13 16.5 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0",
            "M6.6 16.5 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0",
            "M4.6 10.3 a2.2 2.2 0 1 0 4.4 0 a2.2 2.2 0 1 0 -4.4 0"
        )
    }
    val Cactus: ImageVector by lazy {
        stroke("Cactus", "M10 20 V8 a2 2 0 0 1 4 0 V20", "M10 12 C7.5 12 6.5 10.5 6.5 8.5", "M14 14 C16.5 14 17.5 12.5 17.5 10.5", "M8 20 H16")
    }
    val Succulent: ImageVector by lazy {
        stroke(
            "Succulent",
            "M12 12 C12 8.5 10 6.5 7.5 6.5 C7.5 10 9.5 12 12 12 Z",
            "M12 12 C12 8.5 14 6.5 16.5 6.5 C16.5 10 14.5 12 12 12 Z",
            "M12 13 C9 13 6.5 14.5 6 17 C9 17 11.5 15.5 12 13 Z",
            "M12 13 C15 13 17.5 14.5 18 17 C15 17 12.5 15.5 12 13 Z",
            "M12 13 C11 16 11 18 12 20 C13 18 13 16 12 13 Z"
        )
    }
    val Tree: ImageVector by lazy {
        stroke("Tree", "M6.5 9.5 a5.5 5.5 0 1 0 11 0 a5.5 5.5 0 1 0 -11 0", "M12 15 V20", "M9 20 H15")
    }
    val Tulip: ImageVector by lazy {
        stroke(
            "Tulip",
            "M12 20 V12",
            "M8 5.5 C8 9.5 9.8 12 12 12 C14.2 12 16 9.5 16 5.5 C14.5 6.5 13.3 7.2 12 7.2 C10.7 7.2 9.5 6.5 8 5.5 Z",
            "M12 16 C10 16 8.5 14.5 8.5 13",
            "M12 17 C14 17 15.5 15.5 15.5 14"
        )
    }
    val Lotus: ImageVector by lazy {
        stroke(
            "Lotus",
            "M12 14 C10 11.5 10 8.5 12 6 C14 8.5 14 11.5 12 14 Z",
            "M12 14 C9.5 14 6.8 12.3 6.3 9.5 C9.3 9.8 11.3 11.5 12 14 Z",
            "M12 14 C14.5 14 17.2 12.3 17.7 9.5 C14.7 9.8 12.7 11.5 12 14 Z",
            "M5 17.5 C8 19.5 16 19.5 19 17.5"
        )
    }
    val Clover: ImageVector by lazy {
        stroke(
            "Clover",
            "M7.5 8.2 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0",
            "M11.9 8.2 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0",
            "M7.5 12.6 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0",
            "M11.9 12.6 a2.3 2.3 0 1 0 4.6 0 a2.3 2.3 0 1 0 -4.6 0",
            "M12 12 C12 16 11 18 10 20"
        )
    }
    val Sparkle: ImageVector by lazy {
        stroke("Sparkle", "M12 4 L13.5 10.5 L20 12 L13.5 13.5 L12 20 L10.5 13.5 L4 12 L10.5 10.5 Z")
    }
    val Grain: ImageVector by lazy {
        stroke("Grain", "M12 20 V10", "M12 10 C12 6.5 10.5 5 8 5 C8 8 10 9.5 12 10 Z", "M12 11 C12 7.5 14 5.5 16.5 5.5 C16.5 8.5 14.5 10 12 11 Z", "M8 13 C8 15 10 16 12 16", "M16 13 C16 15 14 16 12 16")
    }
    val Wilt: ImageVector by lazy {
        stroke("Wilt", "M12 20 V10", "M12 10 C12 7.5 10.5 6 8 6 C8 9.5 10.5 10.5 12 10.5 Z", "M12 11 C12 8.5 13.5 6.5 16 6.5 C16 9.5 14 11 12 11 Z", "M8 15 C9.5 17 10 18.5 8.5 20", "M16 15 C14.5 17 14 18.5 15.5 20")
    }

    // ---------- 养护动作 ----------
    val Bug: ImageVector by lazy {
        stroke(
            "Bug",
            "M8 12 a4 4 0 1 0 8 0 a4 4 0 1 0 -8 0",
            "M9.5 8.5 V6.6 A2.5 2.5 0 0 1 14.5 6.6 V8.5",
            "M12 8.5 V16",
            "M8.6 10.6 L4.8 9.2", "M15.4 10.6 L19.2 9.2",
            "M8.4 13 L4.4 13.6", "M15.6 13 L19.6 13.6",
            "M9 15.6 L5.6 18", "M15 15.6 L18.4 18"
        )
    }
    val Scissors: ImageVector by lazy {
        stroke(
            "Scissors",
            "M7.6 7.4 L16.2 16", "M16.4 7.4 L7.8 16",
            "M4 19 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0",
            "M15 19 a2.5 2.5 0 1 0 5 0 a2.5 2.5 0 1 0 -5 0"
        )
    }

    // ---------- 天气 ----------
    val Sun: ImageVector by lazy {
        stroke(
            "Sun",
            "M6.5 12 a5.5 5.5 0 1 0 11 0 a5.5 5.5 0 1 0 -11 0",
            "M12 2.6 V4.6", "M12 19.4 V21.4", "M2.6 12 H4.6", "M19.4 12 H21.4",
            "M5.4 5.4 L6.9 6.9", "M17.1 17.1 L18.6 18.6", "M18.6 5.4 L17.1 6.9", "M6.9 17.1 L5.4 18.6"
        )
    }
    val CloudSun: ImageVector by lazy {
        stroke(
            "CloudSun",
            "M4.9 6.4 a3.1 3.1 0 1 0 6.2 0 a3.1 3.1 0 1 0 -6.2 0",
            "M8 1.4 V2.8", "M2.4 3.6 L3.4 4.6", "M13.6 3.6 L12.6 4.6",
            "M8.6 19.6 H16.3 A3 3 0 0 0 16.6 13.7 A4.3 4.3 0 0 0 9.4 13.5 A3.2 3.2 0 0 0 8.6 19.6 Z"
        )
    }
    val Cloud: ImageVector by lazy {
        stroke("Cloud", "M7 18.6 H16.8 A3.3 3.3 0 0 0 17.2 12 A4.7 4.7 0 0 0 8.1 11.7 A3.5 3.5 0 0 0 7 18.6 Z")
    }
    val Rain: ImageVector by lazy {
        stroke(
            "Rain",
            "M7 15.6 H16.5 A3 3 0 0 0 16.8 9.7 A4.3 4.3 0 0 0 9.3 9.5 A3.2 3.2 0 0 0 7 15.6 Z",
            "M8.6 18 L7.9 20.6", "M12 18 L11.3 20.6", "M15.4 18 L14.7 20.6"
        )
    }
    val Snow: ImageVector by lazy {
        stroke(
            "Snow",
            "M7 14.6 H16.5 A3 3 0 0 0 16.8 8.7 A4.3 4.3 0 0 0 9.3 8.5 A3.2 3.2 0 0 0 7 14.6 Z",
            "M7.6 17.6 V20.6", "M6.2 18.3 L9 19.9", "M9 18.3 L6.2 19.9",
            "M16 17.6 V20.6", "M14.6 18.3 L17.4 19.9", "M17.4 18.3 L14.6 19.9"
        )
    }
    val Lightning: ImageVector by lazy {
        stroke(
            "Lightning",
            "M7 15 H16 A3 3 0 0 0 16.3 9.2 A4.3 4.3 0 0 0 9.2 9 A3.2 3.2 0 0 0 7 15 Z",
            "M13 15.4 L10.4 19.6 L13.6 19.6 L11.4 23"
        )
    }
    val Fog: ImageVector by lazy {
        stroke(
            "Fog",
            "M7.5 14.4 H16.6 A3 3 0 0 0 16.9 8.6 A4.3 4.3 0 0 0 9.4 8.4 A3.2 3.2 0 0 0 7.5 14.4 Z",
            "M5 17.4 H19", "M7 20.6 H17"
        )
    }
    val Thermometer: ImageVector by lazy {
        stroke(
            "Thermometer",
            "M9.1 6.2 A2.2 2.2 0 0 1 13.5 6.2 V14.4 A4 4 0 1 1 9.1 14.4 Z",
            "M11.3 9.4 V16.2"
        )
    }
    val Pin: ImageVector by lazy {
        stroke(
            "Pin",
            "M12 20.6 C12 20.6 18 14.6 18 10 A6 6 0 0 0 6 10 C6 14.6 12 20.6 12 20.6 Z",
            "M9.9 10 a2.1 2.1 0 1 0 4.2 0 a2.1 2.1 0 1 0 -4.2 0"
        )
    }

    private fun stroke(name: String, vararg paths: String): ImageVector {
        val builder = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        val parser = PathParser()
        paths.forEach { d ->
            builder.addPath(
                pathData = parser.parsePathString(d).toNodes().toList(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
        return builder.build()
    }
}