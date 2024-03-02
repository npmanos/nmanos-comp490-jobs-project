package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.ui.utils.HorizontalSpacer
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons
import edu.bridgew.comp490.proj1.ui.utils.relativeTimeStringGui

@Composable
fun JobListItem(
    job: Job,
    selected: Boolean = false,
    onClick: (Job) -> Unit = {},
) {
    Card(
        onClick = { onClick(job) },
    ) {
        ListItem(
            colors = JobListItemColors(selected),
            leadingContent = {
                CompanyLogo(
                    imageUrl = job.thumbnail,
                    modifier = Modifier.size(40.dp),
                )
            },
            headlineContent = {
                Text(
                    text = job.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                BoxWithConstraints {
                    val maxWidth = this.maxWidth
                    Column {
                        val companyWidth = if (job.location != null) 0.5f else 1.0f

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = MaterialIcons.Domain,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            HorizontalSpacer(4.dp)
                            Text(
                                text = job.companyName.trim(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        if (job.location != null) {
                            HorizontalSpacer(8.dp, 12.dp, required = false)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = MaterialIcons.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                HorizontalSpacer(4.dp)
                                Text(
                                    text = job.location!!.trim(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!job.detectedExtensions.isNullOrEmpty()) {
                        val postedAt: PostedAt? = job.detectedExtensions!!.fastFirstOrNull { it is PostedAt } as PostedAt?

                        postedAt?.date?.let {
                            Text(
                                text = it.relativeTimeStringGui,
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun JobListItemColors(selected: Boolean) = if (selected) {
    ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    )
} else {
    ListItemDefaults.colors()
}
