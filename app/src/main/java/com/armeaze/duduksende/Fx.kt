package com.armeaze.duduksende

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Piece(val x: Float, val y0: Float, val speed: Float, val color: Int, val phase: Float)

/** Başarılı sonuçta ekranda 3,5 saniye süren konfeti yağmuru (dokunmaları engellemez). */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val colors = remember {
        listOf(Color(0xFFFFC928), Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF22C55E), Color.White)
    }
    val pieces = remember {
        List(70) { Piece(Random.nextFloat(), Random.nextFloat(), 0.6f + Random.nextFloat() * 0.6f, Random.nextInt(5), Random.nextFloat() * 6.28f) }
    }
    val prog = remember { Animatable(0f) }
    LaunchedEffect(Unit) { prog.animateTo(1f, tween(3500, easing = LinearEasing)) }
    if (prog.value < 1f) {
        Canvas(modifier.fillMaxSize()) {
            for (c in pieces) {
                val y = (-0.15f - c.y0 * 0.4f + prog.value * 1.6f * c.speed) * size.height
                val x = c.x * size.width + sin(prog.value * 12f + c.phase) * 18f
                rotate(c.phase * 57f + prog.value * 360f * c.speed, Offset(x, y)) {
                    drawRect(colors[c.color], Offset(x, y), Size(10f, 16f))
                }
            }
        }
    }
}

/** Kategori renk çifti (üst, alt). */
fun catColors(cat: Int): Pair<Color, Color> = when (cat) {
    0 -> Pair(Color(0xFF1B5E20), Color(0xFF2E7D32))
    1 -> Pair(Color(0xFF7A5C00), Color(0xFFB8860B))
    2 -> Pair(Color(0xFF0B4F6C), Color(0xFF1B7FA3))
    3 -> Pair(Color(0xFF5B1A1A), Color(0xFF9B2C2C))
    4 -> Pair(Color(0xFF2D2A66), Color(0xFF4B46A8))
    else -> Pair(Color(0xFF5A2A82), Color(0xFF8E44C0))
}

/** Her kategori için kodla çizilmiş küçük bir illüstrasyon. */
@Composable
fun CategoryArt(cat: Int, modifier: Modifier = Modifier) {
    val cols = catColors(cat)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(cols.first, cols.second)))
        val white = Color(0xE6FFFFFF)
        when (cat) {
            0 -> { // ofsayt çizgisi
                drawLine(Color(0xFFFFC928), Offset(w * 0.05f, h * 0.5f), Offset(w * 0.95f, h * 0.5f), strokeWidth = 3f)
                drawCircle(Color(0xFFEF4444), h * 0.11f, Offset(w * 0.30f, h * 0.55f))
                drawCircle(Color(0xFFEF4444), h * 0.11f, Offset(w * 0.55f, h * 0.60f))
                drawCircle(Color(0xFF3B82F6), h * 0.11f, Offset(w * 0.78f, h * 0.30f))
                drawCircle(Color.White, h * 0.07f, Offset(w * 0.45f, h * 0.82f))
            }
            1 -> { // sarı ve kırmızı kart
                rotate(-14f, Offset(w * 0.38f, h * 0.5f)) {
                    drawRoundRect(Color(0xFFFFC928), Offset(w * 0.28f, h * 0.18f), Size(w * 0.18f, h * 0.64f), CornerRadius(8f, 8f))
                }
                rotate(14f, Offset(w * 0.62f, h * 0.5f)) {
                    drawRoundRect(Color(0xFFEF4444), Offset(w * 0.52f, h * 0.18f), Size(w * 0.18f, h * 0.64f), CornerRadius(8f, 8f))
                }
            }
            2 -> { // köşe yayı ve top
                drawArc(white, 180f, 90f, false, Offset(-h * 0.5f, h * 0.5f), Size(h, h), style = Stroke(3f))
                drawLine(white, Offset(w * 0.04f, h * 0.5f), Offset(w * 0.04f, h * 0.1f), strokeWidth = 3f)
                drawCircle(Color.White, h * 0.10f, Offset(w * 0.30f, h * 0.70f))
                drawLine(white, Offset(w * 0.34f, h * 0.66f), Offset(w * 0.75f, h * 0.25f), strokeWidth = 2f)
                drawCircle(Color(0xFFFFC928), h * 0.06f, Offset(w * 0.78f, h * 0.22f))
            }
            3 -> { // kale ve penaltı noktası
                drawRect(white, Offset(w * 0.25f, h * 0.12f), Size(w * 0.5f, h * 0.38f), style = Stroke(3f))
                drawCircle(Color(0xFFF59E0B), h * 0.08f, Offset(w * 0.5f, h * 0.40f))
                drawCircle(white, 4f, Offset(w * 0.5f, h * 0.78f))
                drawCircle(Color.White, h * 0.09f, Offset(w * 0.5f, h * 0.66f))
            }
            4 -> { // orta yuvarlak
                drawLine(white, Offset(w * 0.05f, h * 0.5f), Offset(w * 0.95f, h * 0.5f), strokeWidth = 3f)
                drawCircle(white, h * 0.34f, Offset(w * 0.5f, h * 0.5f), style = Stroke(3f))
                drawCircle(Color.White, h * 0.08f, Offset(w * 0.5f, h * 0.5f))
            }
            else -> { // yenilik yıldızı
                val c = Offset(w * 0.5f, h * 0.5f)
                val path = Path()
                for (i in 0 until 8) {
                    val r = if (i % 2 == 0) h * 0.42f else h * 0.16f
                    val a = (i * PI / 4.0 - PI / 2.0).toFloat()
                    val px = c.x + r * cos(a)
                    val py = c.y + r * sin(a)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, Color(0xFFFFC928))
                drawCircle(Color.White, 4f, Offset(w * 0.2f, h * 0.3f))
                drawCircle(Color.White, 3f, Offset(w * 0.8f, h * 0.7f))
            }
        }
    }
}
