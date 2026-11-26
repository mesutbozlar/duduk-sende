package com.armeaze.duduksende

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val X1 = 108f

/** Anahtar karelerden t anındaki konumu hesaplar (saha koordinatı: Offset(x, y) metre). */
fun posAt(keys: List<Key>, t: Float): Offset {
    if (keys.isEmpty()) return Offset.Zero
    if (t <= keys[0].t) return Offset(keys[0].x, keys[0].y)
    for (i in 0 until keys.size - 1) {
        val a = keys[i]
        val b = keys[i + 1]
        if (t <= b.t) {
            val f = if (b.t > a.t) (t - a.t) / (b.t - a.t) else 1f
            return Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)
        }
    }
    val l = keys[keys.size - 1]
    return Offset(l.x, l.y)
}

/** Kamera: 0 = üstten, 1 = yayın (yan, perspektifli), 2 = topu izleyen yakın çekim. */
private class Cam(val angle: Int, val w: Float, val h: Float, val x0: Float, val focus: Offset) {
    fun p(x: Float, y: Float): Offset = when (angle) {
        0 -> Offset(y / 68f * w, h * (1f - (x - x0) / (X1 - x0)))
        1 -> Offset((x - x0) / (X1 - x0) * w, h * (0.93f - 0.55f * (y / 68f)))
        else -> {
            val s = w / 26f
            Offset((y - focus.y) * s + w / 2f, h / 2f - (x - focus.x) * s)
        }
    }

    fun depthScale(y: Float): Float = if (angle == 1) 1f - 0.4f * (y / 68f) else 1f

    fun pxPerM(): Float = when (angle) {
        0 -> w / 68f
        1 -> w / (X1 - x0)
        else -> w / 26f
    }
}

private fun DrawScope.seg(cam: Cam, x1: Float, y1: Float, x2: Float, y2: Float, c: Color, sw: Float) {
    drawLine(c, cam.p(x1, y1), cam.p(x2, y2), strokeWidth = sw)
}

private fun DrawScope.drawPitch(cam: Cam) {
    drawRect(Color(0xFF1B5E20))
    val c1 = Color(0xFF2E7D32)
    val c2 = Color(0xFF388E3C)
    for (i in 0 until 21) {
        val a = i * 5f
        val b = a + 5f
        val path = Path()
        val p1 = cam.p(a, 0f)
        path.moveTo(p1.x, p1.y)
        val p2 = cam.p(b, 0f)
        path.lineTo(p2.x, p2.y)
        val p3 = cam.p(b, 68f)
        path.lineTo(p3.x, p3.y)
        val p4 = cam.p(a, 68f)
        path.lineTo(p4.x, p4.y)
        path.close()
        drawPath(path, if (i % 2 == 0) c1 else c2)
    }
    val white = Color(0xE6FFFFFF)
    val sw = 2.5f
    seg(cam, 0f, 0f, 105f, 0f, white, sw)
    seg(cam, 0f, 68f, 105f, 68f, white, sw)
    seg(cam, 105f, 0f, 105f, 68f, white, sw)
    seg(cam, 52.5f, 0f, 52.5f, 68f, white, sw)
    // ceza sahası
    seg(cam, 88.5f, 13.84f, 88.5f, 54.16f, white, sw)
    seg(cam, 88.5f, 13.84f, 105f, 13.84f, white, sw)
    seg(cam, 88.5f, 54.16f, 105f, 54.16f, white, sw)
    // kale alanı
    seg(cam, 99.5f, 24.84f, 99.5f, 43.16f, white, sw)
    seg(cam, 99.5f, 24.84f, 105f, 24.84f, white, sw)
    seg(cam, 99.5f, 43.16f, 105f, 43.16f, white, sw)
    // penaltı yayı
    val a0 = acos((88.5f - 94f) / 9.15f)
    val full = 2f * PI.toFloat()
    var px = 94f + 9.15f * cos(a0)
    var py = 34f + 9.15f * sin(a0)
    for (i in 1..24) {
        val th = a0 + (full - 2f * a0) * i / 24f
        val nx = 94f + 9.15f * cos(th)
        val ny = 34f + 9.15f * sin(th)
        seg(cam, px, py, nx, ny, white, sw)
        px = nx
        py = ny
    }
    drawCircle(white, 3.5f, cam.p(94f, 34f))
    // kale ağı
    val net = Path()
    val n1 = cam.p(105f, 30.34f)
    net.moveTo(n1.x, n1.y)
    val n2 = cam.p(107.5f, 30.34f)
    net.lineTo(n2.x, n2.y)
    val n3 = cam.p(107.5f, 37.66f)
    net.lineTo(n3.x, n3.y)
    val n4 = cam.p(105f, 37.66f)
    net.lineTo(n4.x, n4.y)
    net.close()
    drawPath(net, Color(0x66FFFFFF))
    seg(cam, 105f, 30.34f, 107.5f, 30.34f, white, sw + 1f)
    seg(cam, 105f, 37.66f, 107.5f, 37.66f, white, sw + 1f)
    seg(cam, 107.5f, 30.34f, 107.5f, 37.66f, white, sw + 1f)
}

private fun DrawScope.drawOffsideLine(cam: Cam, cdata: Clip) {
    val xs = cdata.tracks.filter { it.team == 1 || it.team == 2 }
        .map { posAt(it.keys, cdata.keyT).x }.sortedDescending()
    if (xs.size < 2) return
    val lx = xs[1]
    drawLine(
        Color(0xFFFFC928), cam.p(lx, 0f), cam.p(lx, 68f), strokeWidth = 3.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
    )
}

private fun DrawScope.drawActor(cam: Cam, team: Int, x: Float, y: Float, isDown: Boolean) {
    val body = when (team) {
        0 -> Color(0xFF3B82F6)
        1 -> Color(0xFFEF4444)
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFF111827)
    }
    val ring = if (team == 3) Color(0xFFFFC928) else Color.White
    val o = cam.p(x, y)
    val r = max(7.dp.toPx(), 1.1f * cam.pxPerM()) * cam.depthScale(y)
    if (cam.angle == 1) {
        if (isDown) {
            drawOval(body, Offset(o.x - r * 1.2f, o.y - r * 0.8f), Size(r * 2.4f, r * 0.9f))
        } else {
            drawOval(Color(0x55000000), Offset(o.x - r * 0.9f, o.y - r * 0.25f), Size(r * 1.8f, r * 0.5f))
            drawRoundRect(body, Offset(o.x - r * 0.7f, o.y - r * 2.2f), Size(r * 1.4f, r * 2.2f), CornerRadius(r * 0.5f, r * 0.5f))
            drawCircle(Color(0xFFF3C9A0), r * 0.45f, Offset(o.x, o.y - r * 2.7f))
        }
    } else {
        if (isDown) {
            drawOval(body, Offset(o.x - r * 1.3f, o.y - r * 0.6f), Size(r * 2.6f, r * 1.2f))
        } else {
            drawCircle(body, r, o)
            drawCircle(ring, r, o, style = Stroke(2f))
        }
    }
}

private fun DrawScope.drawBall(cam: Cam, x: Float, y: Float) {
    val o0 = cam.p(x, y)
    val r = max(4.5.dp.toPx(), 0.5f * cam.pxPerM()) * cam.depthScale(y)
    val o = if (cam.angle == 1) Offset(o0.x, o0.y - r) else o0
    drawCircle(Color(0x55000000), r, Offset(o.x + 2f, o.y + 2f))
    drawCircle(Color.White, r, o)
    drawCircle(Color(0xFF111827), r, o, style = Stroke(1.5f))
}

@Composable
fun ClipCanvas(cdata: Clip, t: Float, angle: Int, mirror: Boolean, showLine: Boolean, modifier: Modifier = Modifier) {
    Canvas(
        modifier.fillMaxWidth().aspectRatio(68f / (X1 - cdata.x0)).clip(RoundedCornerShape(14.dp))
    ) {
        val b = posAt(cdata.ball, t)
        val by = if (mirror) 68f - b.y else b.y
        val cam = Cam(angle, size.width, size.height, cdata.x0, Offset(b.x, by))
        drawPitch(cam)
        if (showLine) drawOffsideLine(cam, cdata)
        val actors = cdata.tracks.map { tr ->
            val p = posAt(tr.keys, t)
            Triple(tr, p.x, if (mirror) 68f - p.y else p.y)
        }.sortedByDescending { it.third }
        for (a in actors) {
            drawActor(cam, a.first.team, a.second, a.third, a.first.downAt >= 0f && t >= a.first.downAt)
        }
        drawBall(cam, b.x, by)
    }
}

/**
 * Animasyonlu pozisyon oynatıcı.
 * varMode = false: yalnızca yayın kamerası + oynat/tekrar.
 * varMode = true : VAR araçları (3 kamera, ağır çekim, kaydırma, kritik ana git, ofsayt çizgisi).
 */
@Composable
fun ClipPlayer(cdata: Clip, mirror: Boolean, varMode: Boolean, tr: Boolean) {
    var t by remember(cdata) { mutableStateOf(0f) }
    var playing by remember(cdata) { mutableStateOf(true) }
    var speed by remember(cdata) { mutableStateOf(1f) }
    var angle by remember(cdata) { mutableStateOf(1) }
    var line by remember(cdata) { mutableStateOf(false) }

    LaunchedEffect(cdata, playing, speed) {
        if (playing) {
            var last = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                t += (now - last) / 1_000_000_000f * speed
                last = now
                if (t >= cdata.duration) {
                    t = cdata.duration
                    playing = false
                    break
                }
            }
        }
    }

    val cam = if (varMode) angle else 1
    val showLine = varMode && line && cdata.offside

    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            ClipCanvas(cdata, t, cam, mirror, showLine)
            if (varMode) {
                Box(
                    Modifier.align(Alignment.TopStart).padding(8.dp)
                        .clip(RoundedCornerShape(6.dp)).background(Color(0xCC000000))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text("VAR", color = C.Yellow, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    if (!playing && t >= cdata.duration - 0.01f) {
                        t = 0f
                        playing = true
                    } else {
                        playing = !playing
                    }
                },
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.height(40.dp)
            ) { Text(if (playing) "⏸" else if (t >= cdata.duration - 0.01f) "↻" else "▶", fontSize = 18.sp) }
            Spacer(Modifier.width(10.dp))
            if (varMode) {
                Slider(
                    value = t.coerceIn(0f, cdata.duration),
                    onValueChange = { v ->
                        t = v
                        playing = false
                    },
                    valueRange = 0f..cdata.duration,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(Modifier.weight(1f)) { Bar(t / cdata.duration, C.Yellow) }
            }
        }
        if (varMode) {
            Spacer(Modifier.height(6.dp))
            Text(tx(tr, "Camera", "Kamera"), color = C.Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LangBtn(tx(tr, "Broadcast", "Yayın"), angle == 1) { angle = 1 }
                LangBtn(tx(tr, "Top", "Üstten"), angle == 0) { angle = 0 }
                LangBtn(tx(tr, "Close-up", "Yakın"), angle == 2) { angle = 2 }
            }
            Spacer(Modifier.height(6.dp))
            Text(tx(tr, "Speed", "Hız"), color = C.Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LangBtn("1x", speed == 1f) { speed = 1f }
                LangBtn("0.5x", speed == 0.5f) { speed = 0.5f }
                LangBtn("0.25x", speed == 0.25f) { speed = 0.25f }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LangBtn("⏱ " + tx(tr, "Key moment", "Kritik an"), false) {
                    t = cdata.keyT
                    playing = false
                }
                if (cdata.offside) {
                    LangBtn("📏 " + tx(tr, "Offside line", "Ofsayt çizgisi"), line) { line = !line }
                }
            }
        }
    }
}
