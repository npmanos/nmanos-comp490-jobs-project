package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import edu.bridgew.comp490.proj1.data.entities.Extension
import edu.bridgew.comp490.proj1.data.entities.Job
import edu.bridgew.comp490.proj1.data.entities.JobHighlight
import edu.bridgew.comp490.proj1.data.entities.PostedAt
import edu.bridgew.comp490.proj1.data.entities.Salary
import edu.bridgew.comp490.proj1.data.entities.ScheduleType
import edu.bridgew.comp490.proj1.ui.utils.MaterialIcons
import edu.bridgew.comp490.proj1.ui.utils.relativeTimeStringGui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetails(
    job: Job?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(color),
) = Surface(
    modifier = modifier,
    shape = shape,
    color = color,
    contentColor = contentColor,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = color,
        contentColor = contentColor,
        topBar = {
            if (job != null) {
                TopAppBar(
                    title = { Text(job.title.trim()) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = color,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
    ) { paddingValues ->
        if (job != null) {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        start = paddingValues.calculateStartPadding(LocalLayoutDirection.current) + 16.dp,
                        end = paddingValues.calculateEndPadding(LocalLayoutDirection.current) + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    )
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                JobCompanyLocationLogo(
                    job = job,
                )

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth()
                )

                if (!job.detectedExtensions.isNullOrEmpty()) {
                    JobExtensions(
                        extensions = job.detectedExtensions!!,
                    )
                }

                if (!job.jobHighlights.isNullOrEmpty()) {
                    Text(
                        text = "Job highlights",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    job.jobHighlights!!.forEach { JobHighlightItem(it) }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (job.description != null) {
                    Text(
                        text = "Job description",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        text = job.description!!,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun JobCompanyLocationLogo(job: Job, modifier: Modifier = Modifier) = ConstraintLayout(modifier) {
    val (thumbnail, company, location) = createRefs()
    val companyTextStyle = if (job.location != null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge

    CompanyLogo(
        imageUrl = job.thumbnail,
        modifier = Modifier.constrainAs(thumbnail) {
            if (job.location != null) {
                top.linkTo(company.top)
                bottom.linkTo(location.bottom)
            } else {
                top.linkTo(company.top, 4.dp)
                bottom.linkTo(company.bottom, 4.dp)
            }

            start.linkTo(parent.start)
            height = Dimension.fillToConstraints
        }
    )

    Text(
        text = job.companyName.trim(),
        style = companyTextStyle,
        color = LocalContentColor.current.copy(alpha = 2f / 3f),
        modifier = Modifier.constrainAs(company) {
            top.linkTo(parent.top)
            if (job.location != null) {
                start.linkTo(thumbnail.end, 8.dp)
            }
        }
    )

    if (job.location != null) {
        Text(
            text = job.location!!.trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalContentColor.current.copy(alpha = 0.67f),
            modifier = Modifier.constrainAs(location) {
                top.linkTo(company.bottom)
                start.linkTo(company.start)
            }
        )
    }
}

@Composable
private fun JobExtensions(extensions: List<Extension>, modifier: Modifier = Modifier) = ConstraintLayout(modifier) {
    val (postedAt, postedAtIcon, salary, salaryIcon, schedule, scheduleIcon) = createRefs()
    val postedAtExt = extensions.fastFirstOrNull { it is PostedAt } as PostedAt?
    val salaryExt = extensions.fastFirstOrNull { it is Salary } as Salary?
    val scheduleExt = extensions.fastFirstOrNull { it is ScheduleType } as ScheduleType?
    val color = LocalContentColor.current.copy(alpha = 2f / 3f)
    val style = MaterialTheme.typography.bodyMedium

    if (postedAtExt != null) {
        Icon(
            imageVector = MaterialIcons.Schedule,
            contentDescription = null,
            modifier = Modifier.constrainAs(postedAtIcon) {
                start.linkTo(parent.start)
                top.linkTo(postedAt.top)
                bottom.linkTo(postedAt.bottom)

                height = Dimension.fillToConstraints
            },
            tint = color
        )

        Text(
            text = postedAtExt.date.relativeTimeStringGui,
            style = style,
            color = color,
            modifier = Modifier.constrainAs(postedAt) {
                start.linkTo(postedAtIcon.end, 8.dp)
                top.linkTo(parent.top)
            }
        )
    }

    if (salaryExt != null) {
        Icon(
            imageVector = MaterialIcons.AttachMoney,
            contentDescription = null,
            modifier = Modifier.constrainAs(salaryIcon) {
                val ref = if (postedAtExt != null) postedAt else parent

                start.linkTo(ref.end, 16.dp)
                top.linkTo(salary.top)
                bottom.linkTo(salary.bottom)

                height = Dimension.fillToConstraints
            },
            tint = color
        )

        Text(
            text = salaryExt.salaryRange,
            style = style,
            color = color,
            modifier = Modifier.constrainAs(salary) {
                start.linkTo(salaryIcon.end)
                top.linkTo(parent.top)
            }
        )
    }

    if (scheduleExt != null) {
        Icon(
            imageVector = MaterialIcons.DateRange,
            contentDescription = null,
            modifier = Modifier.constrainAs(scheduleIcon) {
                val ref = if (salaryExt != null) {
                    salary
                } else if (postedAtExt != null) {
                    postedAt
                } else {
                    parent
                }

                start.linkTo(ref.end, 16.dp)
                top.linkTo(schedule.top)
                bottom.linkTo(schedule.bottom)

                height = Dimension.fillToConstraints
            },
            tint = color,
        )

        Text(
            text = scheduleExt.type,
            style = style,
            color = color,
            modifier = Modifier.constrainAs(schedule) {
                start.linkTo(scheduleIcon.end, 8.dp)
                top.linkTo(parent.top)
            }
        )
    }
}

@Composable
private fun JobHighlightItem(highlight: JobHighlight, modifier: Modifier = Modifier) = ConstraintLayout(modifier) {
    val (titleLabel, bodyText) = createRefs()

    if (highlight.title != null) {
        Text(
            text = highlight.title!!,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.constrainAs(titleLabel) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                horizontalBias = 0f

                top.linkTo(parent.top)
            },
        )
    }

    Text(
        text = makeBulletedList(highlight.items),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.constrainAs(bodyText) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            horizontalBias = 0f

            if (highlight.title != null) {
                top.linkTo(titleLabel.bottom, 8.dp)
            } else {
                top.linkTo(parent.top)
            }
        }
    )
}
