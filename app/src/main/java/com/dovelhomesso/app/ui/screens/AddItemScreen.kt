package com.dovelhomesso.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dovelhomesso.app.R
import com.dovelhomesso.app.ui.components.BreadcrumbView
import com.dovelhomesso.app.ui.components.PositionPickerBottomSheet
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.ui.theme.ItemColor
import androidx.compose.ui.platform.LocalContext
import com.dovelhomesso.app.ui.components.ImageSelector
import com.dovelhomesso.app.util.FileUtils
import com.dovelhomesso.app.ui.theme.ItemColorLight
import com.dovelhomesso.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    initialSpotId: Long? = null
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    
    var selectedPosition by remember { mutableStateOf<SelectedPosition?>(null) }
    var showPositionPicker by remember { mutableStateOf(false) }
    
    val rooms by viewModel.rooms.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val spots by viewModel.spots.collectAsState()
    
    // Load initial position if provided
    LaunchedEffect(initialSpotId) {
        initialSpotId?.let { spotId ->
            selectedPosition = viewModel.getSelectedPosition(spotId)
        }
    }
    
    // Debug helper to ensure recomposition happens
    val isValid = remember(name, selectedPosition) {
        name.isNotBlank() && selectedPosition != null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nuovo oggetto",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isValid) {
                                viewModel.createItem(
                                    name = name.trim(),
                                    spotId = selectedPosition!!.spot.id,
                                    category = category.takeIf { it.isNotBlank() },
                                    keywords = keywords.takeIf { it.isNotBlank() },
                                    tags = tags.takeIf { it.isNotBlank() },
                                    note = note.takeIf { it.isNotBlank() },
                                    imagePath = imagePath
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isValid) ItemColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Text(
                            "Salva",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                ImageSelector(
                    imagePath = imagePath,
                    onImageSelected = { uri ->
                        if (uri != null) {
                            val path = FileUtils.copyUriToInternalStorage(context, uri, "images")
                            imagePath = path
                        }
                    },
                    onImageRemoved = {
                        if (imagePath != null) {
                            FileUtils.deleteFile(imagePath!!)
                            imagePath = null
                        }
                    }
                )
            }
            
            // Form
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Name (Required)
                Column {
                    Text(
                        text = "Nome oggetto *",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("es. Caricatore portatile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ItemColor,
                            cursorColor = ItemColor
                        )
                    )
                }
                
                // Position Selector (Required)
                Column {
                    Text(
                        text = "Posizione *",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showPositionPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPosition != null) 
                                ItemColorLight 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedPosition != null) 
                                    Icons.Filled.CheckCircle 
                                else 
                                    Icons.Outlined.Place,
                                contentDescription = null,
                                tint = if (selectedPosition != null) 
                                    ItemColor 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (selectedPosition != null) {
                                    Text(
                                        text = "${selectedPosition!!.room.name} > ${selectedPosition!!.container.name} > ${selectedPosition!!.spot.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = selectedPosition!!.spot.code,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ItemColor
                                    )
                                } else {
                                    Text(
                                        text = "Tocca per selezionare",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                Text(
                    text = "Dettagli opzionali",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Category (Optional)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    placeholder = { Text("es. Elettronica") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                // Tags (Optional)
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tag") },
                    placeholder = { Text("tag1, tag2, tag3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                // Note (Optional)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    placeholder = { Text("Aggiungi note...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Position Picker Bottom Sheet
    if (showPositionPicker) {
        PositionPickerBottomSheet(
            rooms = rooms,
            containers = containers,
            spots = spots,
            selectedPosition = selectedPosition,
            onPositionSelected = { position ->
                selectedPosition = position
            },
            onCreateRoom = { name ->
                viewModel.createRoom(name)
            },
            onCreateContainer = { roomId, name, type ->
                viewModel.createContainer(roomId, name, type)
            },
            onCreateSpot = { containerId, label ->
                viewModel.createSpot(containerId, label)
            },
            onDismiss = { showPositionPicker = false }
        )
    }
}
