package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.AppStrings
import com.example.core.TripleDateInfo
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SoftGoldBright

@Composable
fun IslamicHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepForestGreen, EmeraldGreen)
                    )
                )
                .padding(18.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (trailing != null) {
                    trailing()
                }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = SoftGold,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedEmeraldProgress(
    progressFraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val animated by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450),
        label = "progressAnim"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, SoftGold)
                    )
                )
        )
    }
}

@Composable
fun ProgressRing(
    percent: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp
) {
    val animated by animateFloatAsState(
        targetValue = (percent.coerceIn(0, 100) / 100f),
        animationSpec = tween(500),
        label = "ringAnim"
    )
    val trackColor = Color.White.copy(alpha = 0.22f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 9.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = SoftGoldBright,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PureWhite
            )
        }
    }
}

@Composable
fun DateNavigatorStrip(
    tripleDate: TripleDateInfo?,
    isToday: Boolean,
    lang: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevDay,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = AppStrings.t(lang, "আগের দিন", "Previous Day")
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (tripleDate == null) "" else {
                        val wd = if (lang == "en") tripleDate.weekdayEn else tripleDate.weekdayBn
                        "$wd, ${tripleDate.gregorianDisplay}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (tripleDate != null) {
                    Text(
                        text = "${tripleDate.banglaDisplay} • ${tripleDate.hijriDisplay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (!isToday) {
                FilledTonalButton(
                    onClick = onGoToday,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(AppStrings.t(lang, "আজ", "Today"), style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(
                onClick = onNextDay,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = AppStrings.t(lang, "পরের দিন", "Next Day")
                )
            }
        }
    }
}

data class BarDatum(val label: String, val value: Float, val displayValue: String)

@Composable
fun SimpleBarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
    maxValue: Float = 100f
) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val safeMax = maxValue.coerceAtLeast(1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { d ->
                val ratio = (d.value / safeMax).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = d.displayValue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = track,
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            val barH = (size.height * ratio).coerceAtLeast(if (d.value > 0) 8f else 2f)
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(SoftGold, primary)),
                                topLeft = Offset(0f, size.height - barH),
                                size = Size(size.width, barH),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = d.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DualBarChart(
    labels: List<String>,
    seriesA: List<Float>,
    seriesB: List<Float>,
    labelA: String,
    labelB: String,
    modifier: Modifier = Modifier
) {
    val colorA = MaterialTheme.colorScheme.primary
    val colorB = MaterialTheme.colorScheme.error
    val maxVal = (seriesA + seriesB).maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(colorA, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(labelA, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(colorB, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(labelB, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            labels.forEachIndexed { idx, lbl ->
                val aRatio = ((seriesA.getOrNull(idx) ?: 0f) / maxVal).coerceIn(0f, 1f)
                val bRatio = ((seriesB.getOrNull(idx) ?: 0f) / maxVal).coerceIn(0f, 1f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .fillMaxHeight(aRatio.coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(colorA)
                        )
                        Spacer(Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .fillMaxHeight(bRatio.coerceAtLeast(0.03f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(colorB)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(lbl, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = message,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun SplashScreenOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepForestGreen, EmeraldGreen, DeepForestGreen)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SoftGold.copy(alpha = 0.2f))
                    .border(2.dp, SoftGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = "আমার জীবন",
                    tint = SoftGoldBright,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "আমার জীবন",
                style = MaterialTheme.typography.displayMedium,
                color = PureWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "My Life • Islamic Life & Amal Manager",
                style = MaterialTheme.typography.titleSmall,
                color = SoftGoldBright
            )
        }
    }
}

@Composable
fun PinLockScreen(
    lang: String,
    onVerifyPin: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "App Lock",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = AppStrings.t(lang, "আমার জীবন — অ্যাপ লক", "My Life — App Lock"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = AppStrings.t(lang, "প্রবেশ করতে আপনার ৪-সংখ্যার PIN দিন", "Enter your 4-digit PIN to unlock"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                            pin = it
                            error = false
                        }
                    },
                    label = { Text(AppStrings.t(lang, "PIN কোড", "PIN Code")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_lock_input")
                )
                if (error) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = AppStrings.t(lang, "ভুল PIN! আবার চেষ্টা করুন।", "Incorrect PIN. Try again."),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (!onVerifyPin(pin)) error = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pin_unlock_button")
                ) {
                    Text(AppStrings.t(lang, "আনলক করুন", "Unlock"))
                }
            }
        }
    }
}
