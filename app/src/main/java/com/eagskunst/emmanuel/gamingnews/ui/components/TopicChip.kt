package com.eagskunst.emmanuel.gamingnews.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.eagskunst.emmanuel.gamingnews.core.domain.model.Topic

@Composable
fun TopicChip(
    topic: Topic,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = onRemove,
        label = { Text(topic.name) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove topic"
            )
        }
    )
}
