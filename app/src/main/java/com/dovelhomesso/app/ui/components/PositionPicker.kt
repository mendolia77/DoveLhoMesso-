package com.dovelhomesso.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.entities.ContainerEntity
import com.dovelhomesso.app.data.entities.ContainerType
import com.dovelhomesso.app.data.entities.HouseRoomEntity
import com.dovelhomesso.app.data.entities.SpotEntity

data class SelectedPosition(
    val room: HouseRoomEntity,
    val container: ContainerEntity,
    val spot: SpotEntity
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionPickerBottomSheet(
    rooms: List<HouseRoomEntity>,
    containers: List<ContainerEntity>,
    spots: List<SpotEntity>,
    selectedPosition: SelectedPosition?,
    onPositionSelected: (SelectedPosition) -> Unit,
    onCreateRoom: (String) -> Unit,
    onCreateContainer: (Long, String, ContainerType) -> Unit,
    onCreateSpot: (Long, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRoom by remember { mutableStateOf(selectedPosition?.room) }
    var selectedContainer by remember { mutableStateOf(selectedPosition?.container) }
    
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showCreateContainerDialog by remember { mutableStateOf(false) }
    var showCreateSpotDialog by remember { mutableStateOf(false) }
    
    // Auto-create/select generic container and spot if confirming early
    var isAutoCreating by remember { mutableStateOf(false) }
    
    val filteredContainers = remember(selectedRoom, containers) {
        selectedRoom?.let { room -> containers.filter { it.roomId == room.id } } ?: emptyList()
    }
    
    val filteredSpots = remember(selectedContainer, spots) {
        selectedContainer?.let { container -> spots.filter { it.containerId == container.id } } ?: emptyList()
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_position),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step 1: Select Room
            SectionHeader(
                title = stringResource(R.string.room),
                onAdd = { showCreateRoomDialog = true },
                isSelected = selectedRoom != null
            )
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 150.dp)
            ) {
                items(rooms) { room ->
                    SelectableListItem(
                        title = room.name,
                        icon = Icons.Default.Home,
                        isSelected = selectedRoom?.id == room.id,
                        onClick = {
                            selectedRoom = room
                            selectedContainer = null
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step 2: Select Container
            SectionHeader(
                title = stringResource(R.string.container),
                onAdd = if (selectedRoom != null) {{ showCreateContainerDialog = true }} else null,
                isSelected = selectedContainer != null,
                enabled = selectedRoom != null
            )
            
            if (selectedRoom != null) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 150.dp)
                ) {
                    items(filteredContainers) { container ->
                        SelectableListItem(
                            title = container.name,
                            subtitle = container.type,
                            icon = Icons.Default.Inventory2,
                            isSelected = selectedContainer?.id == container.id,
                            onClick = { selectedContainer = container }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Step 3: Select Spot
            SectionHeader(
                title = stringResource(R.string.spot),
                onAdd = if (selectedContainer != null) {{ showCreateSpotDialog = true }} else null,
                isSelected = false,
                enabled = selectedContainer != null
            )
            
            if (selectedContainer != null) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 150.dp)
                ) {
                    items(filteredSpots) { spot ->
                        SelectableListItem(
                            title = spot.label,
                            subtitle = spot.code,
                            icon = Icons.Default.Place,
                            isSelected = false,
                            onClick = {
                                if (selectedRoom != null && selectedContainer != null) {
                                    onPositionSelected(
                                        SelectedPosition(
                                            room = selectedRoom!!,
                                            container = selectedContainer!!,
                                            spot = spot
                                        )
                                    )
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Confirm Button for Quick Selection
            Button(
                onClick = {
                    // Logic to auto-create or select generic container/spot
                    if (selectedRoom != null) {
                        isAutoCreating = true
                        
                        // 1. Check if there is already a generic container in this room
                        var targetContainer = filteredContainers.find { it.name.equals("Generico", ignoreCase = true) }
                        
                        if (targetContainer == null) {
                            // Need to create container first - we can't do it synchronously here easily without suspending
                            // So we trigger creation and will handle the rest in a side effect or callback
                            // For now, simpler approach: trigger create container with a special flag/name
                            onCreateContainer(selectedRoom!!.id, "Generico", ContainerType.ALTRO)
                            // The UI will update, and we need to catch it. 
                            // This is a bit complex in Compose without a proper ViewModel here.
                            // Simplified approach: Show a toast or snackbar saying "Created generic container"
                            // ideally we should wait for it.
                            
                            // BETTER APPROACH: Just pass the intention to the parent via a new callback or assume the parent handles "partial" selection
                            // But sticking to the requested "Confirm" button that auto-fills.
                            
                            // Let's use a workaround: create container, and we hope the list updates fast enough or we handle it in next recomposition? No.
                            // We will manually call onCreateContainer.
                        } else {
                            // Container exists, check for generic spot
                            selectedContainer = targetContainer
                            val spotsInContainer = spots.filter { it.containerId == targetContainer.id }
                            var targetSpot = spotsInContainer.find { it.label.equals("Generico", ignoreCase = true) }
                            
                            if (targetSpot == null) {
                                onCreateSpot(targetContainer.id, "Generico")
                            } else {
                                onPositionSelected(
                                    SelectedPosition(
                                        room = selectedRoom!!,
                                        container = targetContainer,
                                        spot = targetSpot
                                    )
                                )
                                onDismiss()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedRoom != null && !isAutoCreating
            ) {
                if (isAutoCreating) {
                    SimpleLoadingIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Conferma selezione rapida")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Side effect to handle auto-creation flow
    LaunchedEffect(containers, spots, isAutoCreating) {
        if (isAutoCreating && selectedRoom != null) {
            val genericContainer = containers.find { 
                it.roomId == selectedRoom!!.id && it.name.equals("Generico", ignoreCase = true) 
            }
            
            if (genericContainer != null) {
                selectedContainer = genericContainer
                val genericSpot = spots.find { 
                    it.containerId == genericContainer.id && it.label.equals("Generico", ignoreCase = true) 
                }
                
                if (genericSpot != null) {
                    onPositionSelected(
                        SelectedPosition(
                            room = selectedRoom!!,
                            container = genericContainer,
                            spot = genericSpot
                        )
                    )
                    onDismiss()
                } else {
                    // Create spot if not exists
                    onCreateSpot(genericContainer.id, "Generico")
                }
            }
        }
    }
    
    // Create Room Dialog
    if (showCreateRoomDialog) {
        CreateEntityDialog(
            title = stringResource(R.string.new_room),
            placeholder = stringResource(R.string.room_name_hint),
            onConfirm = { name ->
                onCreateRoom(name)
                showCreateRoomDialog = false
            },
            onDismiss = { showCreateRoomDialog = false }
        )
    }
    
    // Create Container Dialog
    if (showCreateContainerDialog && selectedRoom != null) {
        CreateContainerDialog(
            onConfirm = { name, type ->
                onCreateContainer(selectedRoom!!.id, name, type)
                showCreateContainerDialog = false
            },
            onDismiss = { showCreateContainerDialog = false }
        )
    }
    
    // Create Spot Dialog
    if (showCreateSpotDialog && selectedContainer != null) {
        CreateEntityDialog(
            title = stringResource(R.string.new_spot),
            placeholder = stringResource(R.string.spot_name_hint),
            onConfirm = { label ->
                onCreateSpot(selectedContainer!!.id, label)
                showCreateSpotDialog = false
            },
            onDismiss = { showCreateSpotDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAdd: (() -> Unit)?,
    isSelected: Boolean,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        if (onAdd != null) {
            TextButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.create))
            }
        }
    }
}

@Composable
private fun SelectableListItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CreateEntityDialog(
    title: String,
    placeholder: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContainerDialog(
    onConfirm: (String, ContainerType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ContainerType.ALTRO) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_container)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.container_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ContainerType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedType) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
