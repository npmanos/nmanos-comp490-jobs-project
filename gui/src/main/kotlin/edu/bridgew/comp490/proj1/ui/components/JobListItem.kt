package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.relativeTimeString
import edu.bridgew.comp490.proj1.ui.HorizontalSpacer
import edu.bridgew.comp490.proj1.ui.MaterialIcons
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun JobListItem(
    job: Job,
) = Card {
    ListItem(
        headlineContent = {
            Text(
                text = job.title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = MaterialIcons.Domain,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                HorizontalSpacer(4.dp)
                Text(
                    text = job.companyName.trim(),
                )

                if (job.location != null) {
                    HorizontalSpacer(8.dp, 12.dp)
                    Icon(
                        imageVector = MaterialIcons.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    HorizontalSpacer(4.dp)
                    Text(
                        text = job.location!!.trim(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
                        val aWeekAgo = LocalDate.now().minusDays(6)

                        val postedAtStr = if (it.toLocalDate().isBefore(aWeekAgo)) {
                            it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                        } else {
                            it.relativeTimeString
                        }

                        Text(
                            text = postedAtStr
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun JobListItemColors(selected: Boolean) = ListItemDefaults.colors(
    containerColor = MaterialTheme.colorScheme.background,
    headlineColor = contentColorFor(MaterialTheme.colorScheme.background),
    supportingColor = contentColorFor(MaterialTheme.colorScheme.background),
)
