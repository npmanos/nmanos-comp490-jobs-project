package edu.bridgew.comp490.proj1.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import edu.bridgew.comp490.proj1.data.entities.Job

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
    Scaffold(
        containerColor = color,
        contentColor = contentColor,
        topBar = {
            if (job != null) {
                TopAppBar(
                    title = { Text(job.title.trim()) },

                )
            }
        },
    ) { paddingValues ->
        if (job != null) {
            ConstraintLayout(modifier = Modifier.padding(paddingValues)) {
                if (job.location != null) {
                    val (thumbnail, company, location) = createRefs()

                    BusinessThumbnail(
                        imageUrl = job.thumbnail,
                        modifier = Modifier.constrainAs(thumbnail) {
                            top.linkTo(company.top)
                            bottom.linkTo(location.bottom)
                            start.linkTo(parent.start, 16.dp)
                            height = Dimension.fillToConstraints
                        }
                    )
                    Text(
                        text = job.companyName.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.67f),
                        modifier = Modifier.constrainAs(company) {
                            top.linkTo(parent.top)
                            start.linkTo(thumbnail.end, 8.dp)
                        }
                    )
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
        }
    }
}
