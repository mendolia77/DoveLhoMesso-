package com.dovelhomesso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun BreadcrumbView(
    breadcrumb: String,
    code: String? = null,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val parts = breadcrumb.split(" > ")
    
    val containerModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    } else {
        modifier.padding(vertical = 4.dp)
    }
    
    Column(modifier = containerModifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            parts.forEachIndexed { index, part ->
                val icon = when (index) {
                    0 -> Icons.Default.Home
                    1 -> Icons.Default.Inventory2
                    else -> Icons.Default.Place
                }
                
                BreadcrumbSegment(
                    text = part,
                    icon = icon,
                    large = large,
                    isLast = index == parts.lastIndex
                )
                
                if (index < parts.lastIndex) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(if (large) 20.dp else 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (code != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                style = if (large) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun BreadcrumbSegment(
    text: String,
    icon: ImageVector,
    large: Boolean,
    isLast: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (large) 18.dp else 14.dp),
            tint = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = if (large) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
            fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SimpleBreadcrumb(
    roomName: String,
    containerName: String,
    spotLabel: String,
    modifier: Modifier = Modifier
) {
    BreadcrumbView(
        breadcrumb = "$roomName > $containerName > $spotLabel",
        modifier = modifier
    )
}
