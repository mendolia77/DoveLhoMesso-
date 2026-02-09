package com.dovelhomesso.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.entities.ItemEntity
import com.dovelhomesso.app.ui.components.BreadcrumbView
import com.dovelhomesso.app.ui.components.PositionPickerBottomSheet
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.ui.components.SimpleLoadingIndicator
import com.dovelhomesso.app.ui.theme.ItemColor
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: MainViewModel,
    itemId: Long,
    onNavigateBack: () -> Unit
) {
    val item by viewModel.getItemById(itemId).collectAsState(initial = null)
    
    var breadcrumb by remember { mutableStateOf("") }
    var spotCode by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showLendingDialog by remember { mutableStateOf(false) }
    var showChangePositionPicker by remember { mutableStateOf(false) }
    
    val rooms by viewModel.rooms.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val spots by viewModel.spots.collectAsState()
    
    LaunchedEffect(item?.spotId) {
        item?.let {
            breadcrumb = viewModel.getBreadcrumb(it.spotId)
            val spot = viewModel.repository.getSpotById(it.spotId)
            spotCode = spot?.code ?: ""
        }
    }
    
        if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SimpleLoadingIndicator(
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }
    
    val currentItem = item!!
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.items)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { showLendingDialog = true }) {
                        Icon(
                            imageVector = if (currentItem.isLent) Icons.Default.Person else Icons.Default.PersonAdd,
                            contentDescription = "Gestisci Prestito",
                            tint = if (currentItem.isLent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { 
                        viewModel.duplicateItem(currentItem)
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.duplicate))
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Image
            if (currentItem.imagePath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = File(currentItem.imagePath),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Name overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentItem.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            } else {
                // Header without Image
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ItemColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = ItemColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = currentItem.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Lending Status
            if (currentItem.isLent) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "In prestito a: ${currentItem.lentTo ?: "Sconosciuto"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        if (currentItem.lentDate != null) {
                            val date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(currentItem.lentDate))
                            Text(
                                text = "Dal: $date",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 32.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Position
            Card(
                onClick = { showChangePositionPicker = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.position),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showChangePositionPicker = true }) {
                            Text("Cambia")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BreadcrumbView(
                        breadcrumb = breadcrumb,
                        code = spotCode,
                        large = true
                    )
                }
            }
            
            // Details
            if (!currentItem.category.isNullOrBlank() || 
                !currentItem.keywords.isNullOrBlank() || 
                !currentItem.tags.isNullOrBlank()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!currentItem.category.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.category),
                                value = currentItem.category
                            )
                        }
                        if (!currentItem.keywords.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.keywords),
                                value = currentItem.keywords
                            )
                        }
                        if (!currentItem.tags.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.tags),
                                value = currentItem.tags
                            )
                        }
                    }
                }
            }
            
            // Note
            if (!currentItem.note.isNullOrBlank()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.notes),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentItem.note,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_item_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(currentItem)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Edit Dialog
    if (showEditDialog) {
        EditItemDialog(
            item = currentItem,
            onConfirm = { updatedItem ->
                viewModel.updateItem(updatedItem)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
    
    // Change Position Picker
    if (showChangePositionPicker) {
        var selectedPosition by remember { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(Unit) {
            selectedPosition = viewModel.getSelectedPosition(currentItem.spotId)
        }
        
        PositionPickerBottomSheet(
            rooms = rooms,
            containers = containers,
            spots = spots,
            selectedPosition = selectedPosition,
            onPositionSelected = { position ->
                viewModel.updateItem(currentItem.copy(spotId = position.spot.id))
            },
            onCreateRoom = { viewModel.createRoom(it) },
            onCreateContainer = { roomId, name, type -> 
                viewModel.createContainer(roomId, name, type) 
            },
            onCreateSpot = { containerId, label -> 
                viewModel.createSpot(containerId, label) 
            },
            onDismiss = { showChangePositionPicker = false }
        )
    }
    
    // Lending Dialog
    if (showLendingDialog) {
        LendingDialog(
            item = currentItem,
            onConfirm = { updatedItem ->
                viewModel.updateItem(updatedItem)
                showLendingDialog = false
            },
            onDismiss = { showLendingDialog = false }
        )
    }
}

@Composable
private fun LendingDialog(
    item: ItemEntity,
    onConfirm: (ItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var isLent by remember { mutableStateOf(item.isLent) }
    var lentTo by remember { mutableStateOf(item.lentTo ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestione Prestito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isLent = !isLent }
                ) {
                    Checkbox(checked = isLent, onCheckedChange = { isLent = it })
                    Text("Oggetto in prestito")
                }
                
                if (isLent) {
                    OutlinedTextField(
                        value = lentTo,
                        onValueChange = { lentTo = it },
                        label = { Text("Prestato a") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        item.copy(
                            isLent = isLent,
                            lentTo = if (isLent) lentTo.takeIf { it.isNotBlank() } else null,
                            lentDate = if (isLent) (item.lentDate ?: System.currentTimeMillis()) else null
                        )
                    )
                },
                enabled = !isLent || lentTo.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun EditItemDialog(
    item: ItemEntity,
    onConfirm: (ItemEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category ?: "") }
    var keywords by remember { mutableStateOf(item.keywords ?: "") }
    var tags by remember { mutableStateOf(item.tags ?: "") }
    var note by remember { mutableStateOf(item.note ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.category)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text(stringResource(R.string.keywords)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text(stringResource(R.string.tags)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.notes)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            item.copy(
                                name = name.trim(),
                                category = category.takeIf { it.isNotBlank() },
                                keywords = keywords.takeIf { it.isNotBlank() },
                                tags = tags.takeIf { it.isNotBlank() },
                                note = note.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
