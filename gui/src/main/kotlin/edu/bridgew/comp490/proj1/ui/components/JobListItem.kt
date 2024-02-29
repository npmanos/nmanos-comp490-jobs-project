package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.ui.HorizontalSpacer
import edu.bridgew.comp490.proj1.ui.MaterialIcons

@Composable
fun JobListItem(job: Job) = ListItem(
    headlineContent = {
        Text(
            text = job.title,
            style = MaterialTheme.typography.bodyLarge,
        )
    },
    supportingContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = MaterialIcons.Domain,
                contentDescription = null,
                modifier = Modifier.size(17.dp).alignByBaseline(),
            )
            HorizontalSpacer(4.dp)
            Text(
                text = job.companyName.trim(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alignByBaseline(),
            )

            if (job.location != null) {
                HorizontalSpacer(12.dp)
                Icon(
                    imageVector = MaterialIcons.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp).alignByBaseline(),
                )
                HorizontalSpacer(4.dp)
                Text(
                    text = job.location!!.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alignByBaseline(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    },
    colors = JobListItemColors(selected = false)
)

@Composable
private fun JobListItemColors(selected: Boolean) = ListItemDefaults.colors(
    containerColor = MaterialTheme.colorScheme.background,
    headlineColor = contentColorFor(MaterialTheme.colorScheme.background),
    supportingColor = contentColorFor(MaterialTheme.colorScheme.background),
)
