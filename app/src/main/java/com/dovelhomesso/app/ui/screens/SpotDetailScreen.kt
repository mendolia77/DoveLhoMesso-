package com.dovelhomesso.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.entities.DocumentEntity
import com.dovelhomesso.app.data.entities.ItemEntity
import com.dovelhomesso.app.ui.components.BreadcrumbView
import com.dovelhomesso.app.ui.theme.DocumentColor
import com.dovelhomesso.app.ui.theme.ItemColor
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import com.dovelhomesso.app.ui.components.PositionPickerBottomSheet
import com.dovelhomesso.app.ui.components.SelectedPosition
import com.dovelhomesso.app.ui.components.CreateEntityDialog
import com.dovelhomesso.app.ui.components.CreateContainerDialog
import com.dovelhomesso.app.data.entities.ContainerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    viewModel: MainViewModel,
    spotId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddItem: (Long) -> Unit,
    onNavigateToAddDocument: (Long) -> Unit,
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToDocumentDetail: (Long) -> Unit
) {
    val spot by viewModel.getSpotById(spotId).collectAsState(initial = null)
    val items by viewModel.getItemsBySpot(spotId).collectAsState(initial = emptyList())
    val documents by viewModel.getDocumentsBySpot(spotId).collectAsState(initial = emptyList())
    
    val rooms by viewModel.rooms.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val allSpots by viewModel.spots.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var breadcrumb by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }
    
    var itemToMove by remember { mutableStateOf<ItemEntity?>(null) }
    var documentToMove by remember { mutableStateOf<DocumentEntity?>(null) }
    
    LaunchedEffect(spotId) {
        breadcrumb = viewModel.getBreadcrumb(spotId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(spot?.label ?: "")
                        if (spot != null) {
                            Text(
                                text = spot!!.code,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Cambia vista"
                            )
                        }
                    }
                    
                    IconButton(onClick = {
                        spot?.let { viewModel.toggleSpotFavorite(it) }
                    }) {
                        Icon(
                            imageVector = if (spot?.isFavorite == true) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Preferito",
                            tint = if (spot?.isFavorite == true) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        onNavigateToAddItem(spotId)
                    } else {
                        onNavigateToAddDocument(spotId)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        if (selectedTab == 0) stringResource(R.string.add_item)
                        else stringResource(R.string.add_document)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Breadcrumb
            if (breadcrumb.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    BreadcrumbView(
                        breadcrumb = breadcrumb,
                        code = spot?.code,
                        large = true,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${stringResource(R.string.items)} (${items.size})") },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${stringResource(R.string.documents)} (${documents.size})") },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> {
                    if (isGridView) {
                        ItemsGrid(
                            items = items,
                            onItemClick = onNavigateToItemDetail,
                            onMove = { itemToMove = it }
                        )
                    } else {
                        ItemsList(
                            items = items,
                            onItemClick = onNavigateToItemDetail,
                            onMove = { itemToMove = it }
                        )
                    }
                }
                1 -> DocumentsList(
                    documents = documents,
                    onDocumentClick = onNavigateToDocumentDetail,
                    onMove = { documentToMove = it }
                )
            }
        }
    }
    
    // Move Item Picker
    if (itemToMove != null) {
        var selectedPosition by remember(itemToMove) { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(itemToMove) {
            selectedPosition = viewModel.getSelectedPosition(itemToMove!!.spotId)
        }
        
        PositionPickerBottomSheet(
            rooms = rooms,
            containers = containers,
            spots = allSpots,
            selectedPosition = selectedPosition,
            onPositionSelected = { position ->
                viewModel.updateItem(itemToMove!!.copy(spotId = position.spot.id))
                itemToMove = null
            },
            onCreateRoom = { viewModel.createRoom(it) },
            onCreateContainer = { roomId, name, type -> viewModel.createContainer(roomId, name, type) },
            onCreateSpot = { containerId, label -> viewModel.createSpot(containerId, label) },
            onDismiss = { itemToMove = null }
        )
    }
    
    // Move Document Picker
    if (documentToMove != null) {
        var selectedPosition by remember(documentToMove) { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(documentToMove) {
            selectedPosition = viewModel.getSelectedPosition(documentToMove!!.spotId)
        }
        
        PositionPickerBottomSheet(
            rooms = rooms,
            containers = containers,
            spots = allSpots,
            selectedPosition = selectedPosition,
            onPositionSelected = { position ->
                viewModel.updateDocument(documentToMove!!.copy(spotId = position.spot.id))
                documentToMove = null
            },
            onCreateRoom = { viewModel.createRoom(it) },
            onCreateContainer = { roomId, name, type -> viewModel.createContainer(roomId, name, type) },
            onCreateSpot = { containerId, label -> viewModel.createSpot(containerId, label) },
            onDismiss = { documentToMove = null }
        )
    }
}

@Composable
private fun ItemsGrid(
    items: List<ItemEntity>,
    onItemClick: (Long) -> Unit,
    onMove: (ItemEntity) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_items),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ItemGridCard(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    onMove = { onMove(item) }
                )
            }
        }
    }
}

@Composable
private fun ItemGridCard(
    item: ItemEntity,
    onClick: () -> Unit,
    onMove: (ItemEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imagePath != null) {
                AsyncImage(
                    model = File(item.imagePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(ItemColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = ItemColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.imagePath != null) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (item.imagePath != null) Color.Black.copy(alpha = 0.3f) else Color.Transparent,
                        contentColor = if (item.imagePath != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opzioni")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sposta") },
                        onClick = {
                            showMenu = false
                            onMove(item)
                        },
                        leadingIcon = { Icon(Icons.Default.LowPriority, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemsList(
    items: List<ItemEntity>,
    onItemClick: (Long) -> Unit,
    onMove: (ItemEntity) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_items),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    onMove = { onMove(item) }
                )
            }
        }
    }
}

@Composable
private fun DocumentsList(
    documents: List<DocumentEntity>,
    onDocumentClick: (Long) -> Unit,
    onMove: (DocumentEntity) -> Unit
) {
    if (documents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_documents),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents, key = { it.id }) { document ->
                DocumentCard(
                    document = document,
                    onClick = { onDocumentClick(document.id) },
                    onMove = { onMove(document) }
                )
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: ItemEntity,
    onClick: () -> Unit,
    onMove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!item.category.isNullOrBlank()) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opzioni",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sposta") },
                        onClick = {
                            showMenu = false
                            onMove()
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.LowPriority, contentDescription = null) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onMove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!document.docType.isNullOrBlank()) {
                    Text(
                        text = document.docType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opzioni",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sposta") },
                        onClick = {
                            showMenu = false
                            onMove()
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.LowPriority, contentDescription = null) 
                        }
                    )
                }
            }
        }
    }
}
