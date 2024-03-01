package edu.bridgew.comp490.proj1.ui.utils

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import edu.bridgew.comp490.proj1.relativeTimeString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HorizontalSpacer(width: Dp, required: Boolean = true) = when(required) {
    true -> Spacer(modifier = Modifier.requiredWidth(width))
    false -> Spacer(modifier = Modifier.width(width))
}

@Composable
fun HorizontalSpacer(min: Dp, max: Dp, required: Boolean = true) = when(required) {
    true -> Spacer(modifier = Modifier.requiredWidthIn(min, max))
    false -> Spacer(modifier = Modifier.widthIn(min, max))
}

@Composable
fun VerticalSpacer(height: Dp, required: Boolean = true) = when(required) {
    true -> Spacer(modifier = Modifier.requiredHeight(height))
    false -> Spacer(modifier = Modifier.height(height))
}

@Composable
fun VerticalSpacer(min: Dp, max: Dp, required: Boolean = true) = when(required) {
    true -> Spacer(modifier = Modifier.requiredHeightIn(min, max))
    false -> Spacer(modifier = Modifier.heightIn(min, max))
}

typealias MaterialIcons = Icons.Rounded

val LocalDateTime.relativeTimeStringGui: String
    get() {
        val aWeekAgo = LocalDateTime.now().minusDays(6)

        if (this.isBefore(aWeekAgo)) {
            return this.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        } else {
            return this.relativeTimeString
        }
    }
