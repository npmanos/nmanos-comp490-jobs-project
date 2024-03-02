package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.skydoves.landscapist.coil3.CoilImage
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons

@Composable
fun CompanyLogo(
    modifier: Modifier = Modifier,
    imageUrl: String?
) {
    if (imageUrl != null) {
        CoilImage(
            modifier = Modifier.clip(CircleShape).then(modifier),
            imageModel = { imageUrl },
            loading = { CompanyLogoPlaceholder() },
            failure = { CompanyLogoPlaceholder() },
        )
    } else {
        Box(modifier = modifier) { CompanyLogoPlaceholder() }
    }
}

@Composable
fun CompanyLogoPlaceholder() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .aspectRatio(1.0f, true)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = MaterialIcons.Business,
            contentDescription = null,
        )
    }
}
