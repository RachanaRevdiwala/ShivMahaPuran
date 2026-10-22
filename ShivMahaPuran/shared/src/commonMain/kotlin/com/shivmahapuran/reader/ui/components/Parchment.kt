package com.shivmahapuran.reader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.bg_parchment
import com.shivmahapuran.reader.ui.theme.PuranTheme
import com.shivmahapuran.reader.util.localized
import org.jetbrains.compose.resources.painterResource

/**
 * Recolors the parchment image into Neelkantha blue: the image's luminance is mapped onto a
 * gradient from [dark] (burnt edges) to [light] (paper), keeping its texture. A color matrix works
 * on every Android version, unlike non-Porter-Duff blend modes.
 */
fun neelkanthaTint(dark: Color, light: Color): ColorFilter {
    fun row(d: Float, l: Float): FloatArray {
        val k = (l - d)
        return floatArrayOf(0.299f * k, 0.587f * k, 0.114f * k, 0f, d * 255f)
    }
    return ColorFilter.colorMatrix(
        ColorMatrix(
            row(dark.red, light.red) + row(dark.green, light.green) + row(dark.blue, light.blue) +
                floatArrayOf(0f, 0f, 0f, 1f, 0f)
        )
    )
}

/** The blue-tinted parchment backdrop shared by every screen, with a soft vignette and night-reading dimmer. */
@Composable
fun ParchmentBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = PuranTheme.colors
    val tint = remember(colors.parchmentDark, colors.parchmentLight) { neelkanthaTint(colors.parchmentDark, colors.parchmentLight) }
    Box(modifier.fillMaxSize().background(colors.parchmentLight)) {
        Image(
            painter = painterResource(Res.drawable.bg_parchment),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            colorFilter = tint,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    0.55f to Color.Transparent,
                    1f to Color(0x33102A4F),
                    radius = 1600f,
                )
            )
        )
        if (colors.night) Box(Modifier.fillMaxSize().background(colors.pageOverlay))
        content()
    }
}

/** Gold rule with a lotus-bud diamond, used between sections like a manuscript ornament. */
@Composable
fun OrnamentDivider(modifier: Modifier = Modifier, color: Color = PuranTheme.colors.gold) {
    Canvas(modifier.fillMaxWidth().height(16.dp)) {
        val cy = size.height / 2
        val cx = size.width / 2
        val gap = 18.dp.toPx()
        val stroke = 1.2.dp.toPx()
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, color), startX = 0f, endX = cx - gap),
            start = Offset(0f, cy), end = Offset(cx - gap, cy), strokeWidth = stroke,
        )
        drawLine(
            brush = Brush.horizontalGradient(listOf(color, Color.Transparent), startX = cx + gap, endX = size.width),
            start = Offset(cx + gap, cy), end = Offset(size.width, cy), strokeWidth = stroke,
        )
        val r = 6.dp.toPx()
        val diamond = Path().apply {
            moveTo(cx, cy - r); lineTo(cx + r, cy); lineTo(cx, cy + r); lineTo(cx - r, cy); close()
        }
        drawPath(diamond, color)
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(cx - gap + 4.dp.toPx(), cy))
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(cx + gap - 4.dp.toPx(), cy))
    }
}

/** Saffron medallion with a gold ring showing the chapter number in the reader's numerals. */
@Composable
fun ChapterMedallion(number: Int, language: AppLanguage, size: Dp = 52.dp, modifier: Modifier = Modifier) {
    val colors = PuranTheme.colors
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(colors.saffron, colors.maroon)))
            .border(2.dp, colors.gold, CircleShape)
            .padding(3.dp)
            .border(0.8.dp, colors.onAccent.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.localized(language),
            color = colors.onAccent,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp,
            fontFamily = PuranTheme.fonts.forLanguage(language),
        )
    }
}

/** Translucent card that lets the parchment show through, edged in gold. */
@Composable
fun ParchmentCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = PuranTheme.colors
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp)),
        content = content,
    )
}

/**
 * An opaque book leaf: its own parchment sheet (so a turning page hides the one beneath),
 * dimmed for night reading, edged with a double gold frame.
 */
@Composable
fun Modifier.bookLeaf(): Modifier {
    val colors = PuranTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val tint = remember(colors.parchmentDark, colors.parchmentLight) { neelkanthaTint(colors.parchmentDark, colors.parchmentLight) }
    return this
        .clip(shape)
        .background(colors.parchmentLight)
        .paint(painterResource(Res.drawable.bg_parchment), contentScale = ContentScale.Crop, colorFilter = tint)
        .then(if (colors.night) Modifier.background(colors.pageOverlay) else Modifier)
        .bookPageFrame(colors.gold, colors.night)
}

/** A book page: double gold frame with corner flourishes. */
fun Modifier.bookPageFrame(color: Color, night: Boolean): Modifier = drawBehind {
    val outer = 1.6.dp.toPx()
    val inner = 0.8.dp.toPx()
    val inset = 6.dp.toPx()
    val radius = CornerRadius(14.dp.toPx())
    drawRoundRect(
        color = if (night) Color(0x22000000) else Color(0x40F4F8FC),
        cornerRadius = radius,
    )
    drawRoundRect(color = color, style = Stroke(outer), cornerRadius = radius)
    drawRoundRect(
        color = color.copy(alpha = 0.6f),
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        style = Stroke(inner),
        cornerRadius = CornerRadius(10.dp.toPx()),
    )
    val d = 5.dp.toPx()
    listOf(
        Offset(inset, inset), Offset(size.width - inset, inset),
        Offset(inset, size.height - inset), Offset(size.width - inset, size.height - inset),
    ).forEach { c ->
        val p = Path().apply { moveTo(c.x, c.y - d); lineTo(c.x + d, c.y); lineTo(c.x, c.y + d); lineTo(c.x - d, c.y); close() }
        drawPath(p, color)
    }
}

/** Large decorative "ॐ" glyph. */
@Composable
fun OmGlyph(size: Dp = 64.dp, modifier: Modifier = Modifier, color: Color = PuranTheme.colors.saffron) {
    Text(
        text = "ॐ",
        color = color,
        fontSize = size.value.sp,
        fontFamily = PuranTheme.fonts.sanskritDevanagari,
        modifier = modifier,
    )
}

@Composable
fun ThinProgress(fraction: Float, modifier: Modifier = Modifier) {
    val colors = PuranTheme.colors
    Box(modifier.height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.goldSoft)) {
        Box(
            Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(4.dp)
                .background(Brush.horizontalGradient(listOf(colors.gold, colors.saffron)))
        )
    }
}

@Composable
fun Gap(height: Dp = 0.dp, width: Dp = 0.dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.height(height).width(width))
}
