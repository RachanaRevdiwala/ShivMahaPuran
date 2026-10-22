package com.shivmahapuran.reader.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shivmahapuran.reader.domain.model.AppLanguage
import com.shivmahapuran.reader.resources.Res
import com.shivmahapuran.reader.resources.app_name
import com.shivmahapuran.reader.resources.invocation
import com.shivmahapuran.reader.resources.loading_error
import com.shivmahapuran.reader.resources.loading_preparing
import com.shivmahapuran.reader.resources.retry
import com.shivmahapuran.reader.ui.components.Gap
import com.shivmahapuran.reader.ui.components.OmGlyph
import com.shivmahapuran.reader.ui.components.OrnamentDivider
import com.shivmahapuran.reader.ui.components.ParchmentBackground
import com.shivmahapuran.reader.ui.components.ParchmentCard
import com.shivmahapuran.reader.ui.theme.PuranTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingScreen() {
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 0.92f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    )
    val glow by pulse.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
    )
    val colors = PuranTheme.colors
    ParchmentBackground {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OmGlyph(110.dp, Modifier.scale(scale).alpha(glow))
            Gap(8.dp)
            Text(stringResource(Res.string.invocation), color = colors.maroon, fontFamily = PuranTheme.fonts.sanskritDevanagari, fontSize = 20.sp)
            Gap(12.dp)
            OrnamentDivider(Modifier.padding(horizontal = 40.dp))
            Gap(12.dp)
            Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium, color = colors.maroon, textAlign = TextAlign.Center)
            Gap(32.dp)
            CircularProgressIndicator(color = colors.saffron, strokeWidth = 2.dp)
            Gap(12.dp)
            Text(stringResource(Res.string.loading_preparing), style = MaterialTheme.typography.bodyMedium, color = colors.inkSoft)
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    val colors = PuranTheme.colors
    ParchmentBackground {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OmGlyph(80.dp, color = colors.gold)
            Gap(16.dp)
            Text(stringResource(Res.string.loading_error), style = MaterialTheme.typography.bodyLarge, color = colors.ink, textAlign = TextAlign.Center)
            Gap(20.dp)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = colors.maroon)) {
                Text(stringResource(Res.string.retry))
            }
        }
    }
}

/**
 * First-launch language picker. Shown before the UI language is known, so every label is trilingual.
 */
@Composable
fun OnboardingScreen(initial: AppLanguage, onChoose: (AppLanguage) -> Unit) {
    var selected by remember { mutableStateOf(initial) }
    val colors = PuranTheme.colors
    val fonts = PuranTheme.fonts
    ParchmentBackground {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            OmGlyph(96.dp)
            Text("ॐ नमः शिवाय", color = colors.maroon, fontFamily = fonts.sanskritDevanagari, fontSize = 20.sp)
            Gap(16.dp)
            Text("श्रीशिवमहापुराणम्", color = colors.maroon, fontFamily = fonts.sanskritDevanagari, fontSize = 34.sp)
            Text("Shiv Maha Puran", color = colors.inkSoft, fontFamily = fonts.latin, fontSize = 18.sp)
            Gap(16.dp)
            OrnamentDivider(Modifier.padding(horizontal = 32.dp))
            Gap(20.dp)
            Text("Choose your language", color = colors.ink, fontFamily = fonts.latin, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("अपनी भाषा चुनें  ·  તમારી ભાષા પસંદ કરો", color = colors.inkSoft, fontSize = 15.sp, textAlign = TextAlign.Center)
            Gap(20.dp)
            AppLanguage.entries.forEach { language ->
                LanguageOption(language, selected == language) { selected = language }
                Gap(12.dp)
            }
            Gap(12.dp)
            Button(
                onClick = { onChoose(selected) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.maroon),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            ) {
                Text(
                    when (selected) {
                        AppLanguage.ENGLISH -> "Continue"
                        AppLanguage.HINDI -> "आगे बढ़ें"
                        AppLanguage.GUJARATI -> "આગળ વધો"
                    },
                    fontFamily = fonts.forLanguage(selected),
                    modifier = Modifier.padding(vertical = 6.dp),
                    fontSize = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
    val colors = PuranTheme.colors
    val sample = when (language) {
        AppLanguage.ENGLISH -> "Read the Shiv Puran in English"
        AppLanguage.HINDI -> "शिव पुराण हिन्दी में पढ़ें"
        AppLanguage.GUJARATI -> "શિવ પુરાણ ગુજરાતીમાં વાંચો"
    }
    ParchmentCard(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .then(if (selected) Modifier.border(2.dp, colors.saffron, RoundedCornerShape(18.dp)) else Modifier)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(language.nativeName, fontFamily = PuranTheme.fonts.forLanguage(language), fontSize = 22.sp, color = colors.maroon, fontWeight = FontWeight.SemiBold)
                Text(sample, fontFamily = PuranTheme.fonts.forLanguage(language), fontSize = 15.sp, color = colors.inkSoft)
            }
            if (selected) Icon(Icons.Filled.CheckCircle, null, tint = colors.saffron)
        }
    }
}
