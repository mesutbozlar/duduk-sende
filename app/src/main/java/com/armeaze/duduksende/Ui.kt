package com.armeaze.duduksende

import android.content.Context
import android.content.Intent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun tx(tr: Boolean, en: String, t: String): String = if (tr) t else en

object C {
    val Navy = Color(0xFF0A1628)
    val Card = Color(0xFF12243F)
    val Yellow = Color(0xFFFFC928)
    val Green = Color(0xFF1F9D6B)
    val Red = Color(0xFFC94A4A)
    val Muted = Color(0xFF9AA7BC)
    val Border = Color(0xFF24405F)
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    border: Color = C.Border,
    container: Color = C.Card,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val m = if (onClick != null) modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() } else modifier
    Card(
        modifier = m,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = Color.White),
        border = BorderStroke(1.dp, border),
        content = content
    )
}

@Composable
fun Bar(frac: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(C.Border)) {
        Box(Modifier.fillMaxWidth(frac.coerceIn(0f, 1f)).height(6.dp).background(color))
    }
}

@Composable
fun Tag(text: String, color: Color = C.Yellow) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun StatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$emoji $value", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = C.Muted)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp)
    ) { Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun BackBtn(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(42.dp)
    ) { Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun LangBtn(label: String, selected: Boolean, onClick: () -> Unit) {
    val pad = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
    if (selected) Button(onClick = onClick, contentPadding = pad, modifier = Modifier.height(36.dp)) { Text(label) }
    else OutlinedButton(onClick = onClick, contentPadding = pad, modifier = Modifier.height(36.dp)) { Text(label) }
}

@Composable
fun LangSwitch(tr: Boolean, onLang: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        LangBtn("EN", !tr) { onLang("en") }
        LangBtn("TR", tr) { onLang("tr") }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 16.sp, color = C.Muted)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

fun shareText(ctx: Context, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(i, null))
}
