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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dovelhomesso.app.R
import androidx.compose.ui.platform.LocalContext
import com.dovelhomesso.app.ui.components.BreadcrumbView
import com.dovelhomesso.app.ui.components.FileSelector
import com.dovelhomesso.app.ui.components.PositionPickerBottomSheet
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.ui.theme.DocumentColor
import com.dovelhomesso.app.ui.theme.DocumentColorLight
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import com.dovelhomesso.app.util.FileUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    initialSpotId: Long? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("") }
    var person by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Long?>(null) }
    var tags by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val selectedFilePaths = remember { mutableStateListOf<String>() }
    
    var selectedPosition by remember { mutableStateOf<SelectedPosition?>(null) }
    var showPositionPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
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
    val isValid = remember(title, selectedPosition) {
        title.isNotBlank() && selectedPosition != null
    }
    
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nuovo documento",
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
                                viewModel.createDocument(
                                    title = title.trim(),
                                    spotId = selectedPosition!!.spot.id,
                                    docType = docType.takeIf { it.isNotBlank() },
                                    person = person.takeIf { it.isNotBlank() },
                                    expiryDate = expiryDate,
                                    tags = tags.takeIf { it.isNotBlank() },
                                    note = note.takeIf { it.isNotBlank() },
                                    filePaths = selectedFilePaths.toList().takeIf { it.isNotEmpty() }
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isValid) DocumentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
            // Header icon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FileSelector(
                    filePath = null,
                    onFileSelected = { uri ->
                        if (uri != null) {
                            val path = FileUtils.copyUriToInternalStorage(context, uri, "documents")
                            if (path != null && !selectedFilePaths.contains(path)) {
                                selectedFilePaths.add(path)
                            }
                        }
                    },
                    onFileRemoved = {}
                )
                
                if (selectedFilePaths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    selectedFilePaths.forEach { path ->
                        val fileName = try { java.io.File(path).name } catch(e:Exception) { "File" }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AttachFile, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(
                                    onClick = { 
                                        FileUtils.deleteFile(path)
                                        selectedFilePaths.remove(path) 
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Rimuovi",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Form
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title (Required)
                Column {
                    Text(
                        text = "Titolo documento *",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("es. Contratto affitto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DocumentColor,
                            cursorColor = DocumentColor
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
                                DocumentColorLight 
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
                                    DocumentColor 
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
                                        color = DocumentColor
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
                
                // Document Type (Optional)
                OutlinedTextField(
                    value = docType,
                    onValueChange = { docType = it },
                    label = { Text("Tipo documento") },
                    placeholder = { Text("es. Contratto, Fattura") },
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
                
                // Person (Optional)
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text("Intestatario") },
                    placeholder = { Text("A chi appartiene") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                // Expiry Date (Optional)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = if (expiryDate != null) DocumentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scadenza",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = expiryDate?.let { dateFormatter.format(Date(it)) } ?: "Non impostata",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (expiryDate != null) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                        if (expiryDate != null) {
                            IconButton(onClick = { expiryDate = null }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Rimuovi",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
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
    
    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryDate ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
