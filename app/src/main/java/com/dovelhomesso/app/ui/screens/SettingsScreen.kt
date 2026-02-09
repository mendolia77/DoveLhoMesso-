package com.dovelhomesso.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.exportimport.BackupManager
import com.dovelhomesso.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val backupStatus by viewModel.backupStatus.collectAsState()
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var hasPin by remember { mutableStateOf(com.dovelhomesso.app.util.PinManager.isPinSet(context)) }

    if (showPinSetup) {
        PinScreen(
            onPinCorrect = {}, // Not used in setup mode
            onPinSet = { newPin ->
                com.dovelhomesso.app.util.PinManager.setPin(context, newPin)
                hasPin = true
                showPinSetup = false
            },
            isSettingPin = true
        )
        // Add a back button handling or similar if needed, but PinScreen takes full control here
        // For better UX, we might want a "Cancel" button in PinScreen, but for now let's rely on system back or add a button in PinScreen later.
        // Actually, let's wrap it in a Surface to ensure it covers everything and maybe add a cancel button if we modify PinScreen.
        // But simply showing it conditionally works.
        return
    }
    
    // Launcher for creating backup file
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BackupManager.MIME_TYPE)
    ) { uri ->
        uri?.let { viewModel.exportBackupToUri(it) }
    }
    
    // Launcher for opening backup file
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackupFromUri(it) }
    }
    
    // Show snackbar on status change
    LaunchedEffect(backupStatus) {
        when (backupStatus) {
            is MainViewModel.BackupStatus.Success,
            is MainViewModel.BackupStatus.Error -> {
                // Status will be shown in UI, reset after delay
                kotlinx.coroutines.delay(3000)
                viewModel.resetBackupStatus()
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
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
        ) {
            // Status Banner
            when (val status = backupStatus) {
                is MainViewModel.BackupStatus.InProgress -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Operazione in corso... attendere prego.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is MainViewModel.BackupStatus.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = status.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                is MainViewModel.BackupStatus.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = status.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                else -> {}
            }
            
            // Backup Section
            Text(
                text = "Sicurezza",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(if (hasPin) "Rimuovi PIN" else "Imposta PIN") },
                supportingContent = { Text(if (hasPin) "Disabilita la protezione con PIN" else "Proteggi l'accesso ai dati") },
                leadingContent = {
                    Icon(if (hasPin) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null)
                },
                modifier = Modifier.clickableIfEnabled(backupStatus !is MainViewModel.BackupStatus.InProgress) {
                    if (hasPin) {
                        // Remove PIN
                        com.dovelhomesso.app.util.PinManager.removePin(context)
                        hasPin = false
                    } else {
                        // Set PIN
                        showPinSetup = true
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Backup e Cloud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Export
            ListItem(
                headlineContent = { Text("Backup su Cloud / File") },
                supportingContent = { Text("Salva su Google Drive, Dropbox o dispositivo") },
                leadingContent = {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                },
                modifier = Modifier.clickableIfEnabled(backupStatus !is MainViewModel.BackupStatus.InProgress) {
                    val filename = viewModel.generateBackupFilename()
                    createDocumentLauncher.launch(filename)
                }
            )
            
            // Import
            ListItem(
                headlineContent = { Text("Ripristina da Cloud / File") },
                supportingContent = { Text("Importa da Google Drive, Dropbox o dispositivo") },
                leadingContent = {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                },
                modifier = Modifier.clickableIfEnabled(backupStatus !is MainViewModel.BackupStatus.InProgress) {
                    openDocumentLauncher.launch(arrayOf(BackupManager.MIME_TYPE, "application/json"))
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Danger Zone
            Text(
                text = "Zona pericolosa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Reset
            ListItem(
                headlineContent = { 
                    Text(
                        text = stringResource(R.string.reset_data),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                supportingContent = { Text("Elimina tutti i dati dell'app") },
                leadingContent = {
                    Icon(
                        Icons.Default.DeleteForever, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickableIfEnabled(backupStatus !is MainViewModel.BackupStatus.InProgress) {
                    showResetDialog = true
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // About Section
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = { Text("${stringResource(R.string.version)} 1.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
            )
        }
    }
    
    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.reset_data)) },
            text = { Text(stringResource(R.string.reset_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun Modifier.clickableIfEnabled(enabled: Boolean, onClick: () -> Unit): Modifier {
    return if (enabled) {
        this.clickable { onClick() }
    } else {
        this
    }
}
