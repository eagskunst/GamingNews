package com.eagskunst.emmanuel.gamingnews.ui.releases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eagskunst.emmanuel.gamingnews.R
import com.eagskunst.emmanuel.gamingnews.core.domain.model.GameRelease
import com.eagskunst.emmanuel.gamingnews.core.domain.model.ReminderOffset
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Offset picker shown when the user follows a release: which reminders to get, relative to
 * the release day. "On release day" starts selected so a quick confirm still does something
 * useful; confirm stays disabled while nothing is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderOffsetSheet(
    release: GameRelease,
    onConfirm: (Set<ReminderOffset>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(setOf(ReminderOffset.RELEASE_DAY)) }
    val dateHint = remember(release.releaseDate) {
        SimpleDateFormat("MMM d", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(release.releaseDate)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = stringResource(R.string.reminder_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = release.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(R.string.reminder_sheet_date_hint, dateHint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OFFSET_CHOICES.forEach { offset ->
                val checked = offset in selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = checked,
                            role = Role.Checkbox,
                            onValueChange = { isChecked ->
                                selected = if (isChecked) selected + offset else selected - offset
                            }
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Text(
                        text = stringResource(offset.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onConfirm(selected) },
                    enabled = selected.isNotEmpty()
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

/** Display order for the offset checkboxes, nearest reminder first. */
private val OFFSET_CHOICES = listOf(
    ReminderOffset.RELEASE_DAY,
    ReminderOffset.ONE_DAY_BEFORE,
    ReminderOffset.ONE_WEEK_BEFORE
)

private val ReminderOffset.labelRes: Int
    get() = when (this) {
        ReminderOffset.RELEASE_DAY -> R.string.reminder_offset_release_day
        ReminderOffset.ONE_DAY_BEFORE -> R.string.reminder_offset_day_before
        ReminderOffset.ONE_WEEK_BEFORE -> R.string.reminder_offset_week_before
    }
