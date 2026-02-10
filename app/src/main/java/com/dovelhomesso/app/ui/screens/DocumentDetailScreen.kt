package com.dovelhomesso.app.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.entities.DocumentEntity
import com.dovelhomesso.app.ui.components.BreadcrumbView
import com.dovelhomesso.app.ui.components.PositionPickerBottomSheet
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.ui.components.SimpleLoadingIndicator
import com.dovelhomesso.app.ui.theme.DocumentColor
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    viewModel: MainViewModel,
    documentId: Long,
    onNavigateBack: () -> Unit
) {
    val document by viewModel.getDocumentById(documentId).collectAsState(initial = null)
    
    var breadcrumb by remember { mutableStateOf("") }
    var spotCode by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePositionPicker by remember { mutableStateOf(false) }
    
    val rooms by viewModel.rooms.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val spots by viewModel.spots.collectAsState()
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    LaunchedEffect(document?.spotId) {
        document?.let {
            breadcrumb = viewModel.getBreadcrumb(it.spotId)
            val spot = viewModel.repository.getSpotById(it.spotId)
            spotCode = spot?.code ?: ""
        }
    }
    
    if (document == null) {
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
    
    val currentDocument = document!!
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.documents)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.duplicateDocument(currentDocument)
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
            // Header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = DocumentColor.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = DocumentColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = currentDocument.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // File Attachment
            val filesList = remember(currentDocument.filePaths) {
                if (!currentDocument.filePaths.isNullOrEmpty()) {
                    try {
                        com.google.gson.Gson().fromJson(currentDocument.filePaths, Array<String>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList<String>()
                    }
                } else {
                    emptyList()
                }
            }

            if (filesList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        filesList.forEachIndexed { index, path ->
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                                )
                            }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                // Preview Thumbnail for Images
                                val isImage = try {
                                    val mimeType = java.net.URLConnection.guessContentTypeFromName(path)
                                    mimeType?.startsWith("image/") == true || path.endsWith(".jpg", ignoreCase = true) || path.endsWith(".png", ignoreCase = true) || path.endsWith(".jpeg", ignoreCase = true)
                                } catch (e: Exception) { false }

                                if (isImage) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                // Open Image
                                                try {
                                                    val file = File(path)
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "image/*")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                    )
                                } else {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Text(
                                    text = try { File(path).name } catch(e:Exception) { "File" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        val file = File(path)
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apri file")
                            }
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
            if (!currentDocument.docType.isNullOrBlank() || 
                !currentDocument.person.isNullOrBlank() || 
                currentDocument.expiryDate != null ||
                !currentDocument.tags.isNullOrBlank()) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!currentDocument.docType.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.doc_type),
                                value = currentDocument.docType
                            )
                        }
                        if (!currentDocument.person.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.person),
                                value = currentDocument.person
                            )
                        }
                        if (currentDocument.expiryDate != null) {
                            val isExpired = currentDocument.expiryDate < System.currentTimeMillis()
                            DetailRow(
                                label = stringResource(R.string.expiry_date),
                                value = dateFormatter.format(Date(currentDocument.expiryDate)),
                                isWarning = isExpired
                            )
                        }
                        if (!currentDocument.tags.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.tags),
                                value = currentDocument.tags
                            )
                        }
                    }
                }
            }
            
            // Note
            if (!currentDocument.note.isNullOrBlank()) {
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
                            text = currentDocument.note,
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
            text = { Text(stringResource(R.string.delete_document_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument(currentDocument)
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
        EditDocumentDialog(
            document = currentDocument,
            onConfirm = { updatedDocument ->
                viewModel.updateDocument(updatedDocument)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
    
    // Change Position Picker
    if (showChangePositionPicker) {
        var selectedPosition by remember { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(Unit) {
            selectedPosition = viewModel.getSelectedPosition(currentDocument.spotId)
        }
        
        PositionPickerBottomSheet(
            rooms = rooms,
            containers = containers,
            spots = spots,
            selectedPosition = selectedPosition,
            onPositionSelected = { position ->
                viewModel.updateDocument(currentDocument.copy(spotId = position.spot.id))
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
}

@Composable
private fun DetailRow(
    label: String, 
    value: String,
    isWarning: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDocumentDialog(
    document: DocumentEntity,
    onConfirm: (DocumentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(document.title) }
    var docType by remember { mutableStateOf(document.docType ?: "") }
    var person by remember { mutableStateOf(document.person ?: "") }
    var tags by remember { mutableStateOf(document.tags ?: "") }
    var note by remember { mutableStateOf(document.note ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = docType,
                    onValueChange = { docType = it },
                    label = { Text(stringResource(R.string.doc_type)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text(stringResource(R.string.person)) },
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
                    if (title.isNotBlank()) {
                        onConfirm(
                            document.copy(
                                title = title.trim(),
                                docType = docType.takeIf { it.isNotBlank() },
                                person = person.takeIf { it.isNotBlank() },
                                tags = tags.takeIf { it.isNotBlank() },
                                note = note.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
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
