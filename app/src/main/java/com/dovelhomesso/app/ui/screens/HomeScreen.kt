package com.dovelhomesso.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.SearchResultType
import com.dovelhomesso.app.data.entities.ContainerEntity
import com.dovelhomesso.app.data.entities.SpotEntity
import com.dovelhomesso.app.data.repositories.AppRepository
import com.dovelhomesso.app.ui.components.*
import com.dovelhomesso.app.ui.theme.*
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToAddItem: () -> Unit,
    onNavigateToAddDocument: () -> Unit,
    onNavigateToRooms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToDocumentDetail: (Long) -> Unit,
    onNavigateToSpotDetail: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recentEntries by viewModel.recentEntries.collectAsState()
    val favoriteContainers by viewModel.favoriteContainers.collectAsState()
    val favoriteSpots by viewModel.favoriteSpots.collectAsState()
    
    val rooms by viewModel.rooms.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val spots by viewModel.spots.collectAsState()
    
    var itemToMoveId by remember { mutableStateOf<Long?>(null) }
    var documentToMoveId by remember { mutableStateOf<Long?>(null) }
    
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            viewModel.findSpotByCode(result.contents)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.foundSpotEvent.collect { spotId ->
            onNavigateToSpotDetail(spotId)
        }
    }
    
    // Load recent entries when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadRecentEntries()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Animated Header with gradient and particles
            item {
                AnimatedGradientHeader(
                    onSettingsClick = onNavigateToSettings,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.search(it) },
                    onScanClick = {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Inquadra il QR Code")
                        options.setBeepEnabled(true)
                        scanLauncher.launch(options)
                    }
                )
            }
            
            // Show search results if searching
            if (searchQuery.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = isSearching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Animated loading indicator
                            SimpleLoadingIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = !isSearching && searchResults.isEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        EmptySearchResult()
                    }
                }
                
                itemsIndexed(searchResults) { index, result ->
                    FadeInBox(delayMillis = index * 50) {
                        SearchResultCard(
                            result = result,
                            onClick = {
                                when (result.type) {
                                    SearchResultType.ITEM -> onNavigateToItemDetail(result.id)
                                    SearchResultType.DOCUMENT -> onNavigateToDocumentDetail(result.id)
                                    SearchResultType.SPOT -> onNavigateToSpotDetail(result.id)
                                }
                            },
                            onMove = {
                                when (result.type) {
                                    SearchResultType.ITEM -> itemToMoveId = result.id
                                    SearchResultType.DOCUMENT -> documentToMoveId = result.id
                                    SearchResultType.SPOT -> {}
                                }
                            }
                        )
                    }
                }
            } else {
                // Quick Action Cards with staggered animation
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ScaleInBox(delayMillis = 100) {
                        QuickActionsSection(
                            onAddItem = onNavigateToAddItem,
                            onAddDocument = onNavigateToAddDocument,
                            onRooms = onNavigateToRooms
                        )
                    }
                }
                
                // Favorites Section with animation
                if (favoriteContainers.isNotEmpty() || favoriteSpots.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        FadeInBox(delayMillis = 200) {
                            SectionTitle(
                                icon = Icons.Filled.Star,
                                title = stringResource(R.string.favorites),
                                iconTint = FavoriteColor
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SlideInFromBottom(delayMillis = 250) {
                            FavoritesRow(
                                containers = favoriteContainers,
                                spots = favoriteSpots,
                                onSpotClick = onNavigateToSpotDetail
                            )
                        }
                    }
                }
                
                // Recent Section with staggered animation
                if (recentEntries.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        FadeInBox(delayMillis = 300) {
                            SectionTitle(
                                icon = Icons.Filled.History,
                                title = stringResource(R.string.recent),
                                iconTint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    itemsIndexed(recentEntries) { index, entry ->
                        FadeInBox(delayMillis = 350 + index * 50) {
                            RecentEntryCard(
                                entry = entry,
                                onClick = {
                                    when (entry.type) {
                                        SearchResultType.ITEM -> onNavigateToItemDetail(entry.id)
                                        SearchResultType.DOCUMENT -> onNavigateToDocumentDetail(entry.id)
                                        SearchResultType.SPOT -> onNavigateToSpotDetail(entry.id)
                                    }
                                },
                                onMove = {
                                    when (entry.type) {
                                        SearchResultType.ITEM -> itemToMoveId = entry.id
                                        SearchResultType.DOCUMENT -> documentToMoveId = entry.id
                                        SearchResultType.SPOT -> {}
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Empty state with animation
                if (favoriteContainers.isEmpty() && favoriteSpots.isEmpty() && recentEntries.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                        ScaleInBox(delayMillis = 200) {
                            EmptyHomeState()
                        }
                    }
                }
            }
        }
    }
    
    // Move Item Picker
    if (itemToMoveId != null) {
        val item by viewModel.getItemById(itemToMoveId!!).collectAsState(initial = null)
        var selectedPosition by remember(item) { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(item) {
            item?.let {
                selectedPosition = viewModel.getSelectedPosition(it.spotId)
            }
        }
        
        if (item != null) {
            PositionPickerBottomSheet(
                rooms = rooms,
                containers = containers,
                spots = spots,
                selectedPosition = selectedPosition,
                onPositionSelected = { position ->
                    viewModel.moveItem(itemToMoveId!!, position.spot.id)
                    itemToMoveId = null
                },
                onCreateRoom = { viewModel.createRoom(it) },
                onCreateContainer = { roomId, name, type -> viewModel.createContainer(roomId, name, type) },
                onCreateSpot = { containerId, label -> viewModel.createSpot(containerId, label) },
                onDismiss = { itemToMoveId = null }
            )
        }
    }
    
    // Move Document Picker
    if (documentToMoveId != null) {
        val document by viewModel.getDocumentById(documentToMoveId!!).collectAsState(initial = null)
        var selectedPosition by remember(document) { mutableStateOf<SelectedPosition?>(null) }
        
        LaunchedEffect(document) {
            document?.let {
                selectedPosition = viewModel.getSelectedPosition(it.spotId)
            }
        }
        
        if (document != null) {
            PositionPickerBottomSheet(
                rooms = rooms,
                containers = containers,
                spots = spots,
                selectedPosition = selectedPosition,
                onPositionSelected = { position ->
                    viewModel.moveDocument(documentToMoveId!!, position.spot.id)
                    documentToMoveId = null
                },
                onCreateRoom = { viewModel.createRoom(it) },
                onCreateContainer = { roomId, name, type -> viewModel.createContainer(roomId, name, type) },
                onCreateSpot = { containerId, label -> viewModel.createSpot(containerId, label) },
                onDismiss = { documentToMoveId = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedGradientHeader(
    onSettingsClick: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onScanClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1).copy(alpha = 0.9f + gradientOffset * 0.1f),
                        Color(0xFF1976D2)
                    )
                )
            )
    ) {
        // Animated particles in background
        ParticleEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            particleColor = Color.White.copy(alpha = 0.2f),
            particleCount = 15
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 32.dp)
                .padding(horizontal = 20.dp)
        ) {
            // Top row with animated title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Animated title
                    var titleVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(100)
                        titleVisible = true
                    }
                    AnimatedVisibility(
                        visible = titleVisible,
                        enter = fadeIn() + slideInHorizontally { -50 }
                    ) {
                        Text(
                            text = "DoveL'hoMesso",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = titleVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 200)) + 
                                slideInHorizontally(animationSpec = tween(500, delayMillis = 200)) { -30 }
                    ) {
                        Text(
                            text = "Trova tutto, subito",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Animated settings button
                val settingsInteraction = remember { MutableInteractionSource() }
                val isPressed by settingsInteraction.collectIsPressedAsState()
                val settingsScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "settings"
                )
                
                IconButton(
                    onClick = onSettingsClick,
                    interactionSource = settingsInteraction,
                    modifier = Modifier
                        .size(44.dp)
                        .scale(settingsScale)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Impostazioni",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated search bar
            var searchBarVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(300)
                searchBarVisible = true
            }
            
            AnimatedVisibility(
                visible = searchBarVisible,
                enter = fadeIn() + slideInVertically { 30 } + scaleIn(initialScale = 0.95f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    placeholder = {
                        Text(
                            text = "Cerca oggetti e documenti...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        // Animated search icon
                        val searchIconScale by animateFloatAsState(
                            targetValue = if (searchQuery.isNotEmpty()) 1.1f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "searchIcon"
                        )
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(searchIconScale)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancella",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            IconButton(onClick = onScanClick) {
                                Icon(
                                    imageVector = Icons.Filled.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddItem: () -> Unit,
    onAddDocument: () -> Unit,
    onRooms: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Azioni rapide",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedQuickActionCard(
                icon = Icons.Outlined.Inventory2,
                label = "Oggetto",
                description = "Aggiungi nuovo",
                color = ItemColor,
                backgroundColor = ItemColorLight,
                onClick = onAddItem,
                modifier = Modifier.weight(1f),
                delayMillis = 0
            )
            AnimatedQuickActionCard(
                icon = Icons.Outlined.Description,
                label = "Documento",
                description = "Aggiungi nuovo",
                color = DocumentColor,
                backgroundColor = DocumentColorLight,
                onClick = onAddDocument,
                modifier = Modifier.weight(1f),
                delayMillis = 100
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Browse rooms button with animation
        AnimatedBrowseRoomsCard(onClick = onRooms)
    }
}

@Composable
private fun AnimatedQuickActionCard(
    icon: ImageVector,
    label: String,
    description: String,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        label = "cardElevation"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .height(120.dp)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated icon container
                val iconRotation by animateFloatAsState(
                    targetValue = if (isPressed) 10f else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "iconRotation"
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer { rotationZ = iconRotation }
                        .background(
                            color = color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = color.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedBrowseRoomsCard(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "roomsScale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated home icon
            val infiniteTransition = rememberInfiniteTransition(label = "home")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "homeIcon"
            )
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(iconScale)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Esplora stanze",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sfoglia la gerarchia completa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Animated chevron
            val chevronOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "chevron"
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { translationX = chevronOffset }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pulsing icon
        val infiniteTransition = rememberInfiniteTransition(label = "sectionIcon")
        val iconScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconPulse"
        )
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(22.dp)
                .scale(iconScale)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FavoritesRow(
    containers: List<ContainerEntity>,
    spots: List<SpotEntity>,
    onSpotClick: (Long) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(spots) { index, spot ->
            FadeInBox(delayMillis = index * 50) {
                AnimatedFavoriteChip(
                    icon = Icons.Filled.Place,
                    label = spot.label,
                    code = spot.code,
                    onClick = { onSpotClick(spot.id) }
                )
            }
        }
        itemsIndexed(containers) { index, container ->
            FadeInBox(delayMillis = (spots.size + index) * 50) {
                AnimatedFavoriteChip(
                    icon = Icons.Filled.Inventory2,
                    label = container.name,
                    code = null,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun AnimatedFavoriteChip(
    icon: ImageVector,
    label: String,
    code: String?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale"
    )
    
    Card(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FavoriteColor.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated star
            val starRotation by animateFloatAsState(
                targetValue = if (isPressed) 20f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "star"
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = FavoriteColor,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = starRotation }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                if (code != null) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: com.dovelhomesso.app.data.SearchResult,
    onClick: () -> Unit,
    onMove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "resultScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        label = "resultElevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated type icon
            val iconColor = when (result.type) {
                SearchResultType.ITEM -> ItemColor
                SearchResultType.DOCUMENT -> DocumentColor
                SearchResultType.SPOT -> MaterialTheme.colorScheme.primary
            }
            val iconBg = when (result.type) {
                SearchResultType.ITEM -> ItemColorLight
                SearchResultType.DOCUMENT -> DocumentColorLight
                SearchResultType.SPOT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            }
            
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = iconBg, shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (result.type) {
                        SearchResultType.ITEM -> Icons.Filled.Inventory2
                        SearchResultType.DOCUMENT -> Icons.Filled.Description
                        SearchResultType.SPOT -> Icons.Filled.Place
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!result.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = result.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (!result.matchDetails.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.matchDetails,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            if (result.type != SearchResultType.SPOT) {
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
}

@Composable
private fun RecentEntryCard(
    entry: AppRepository.RecentEntry,
    onClick: () -> Unit,
    onMove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "recentScale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = when (entry.type) {
                            SearchResultType.ITEM -> ItemColorLight
                            SearchResultType.DOCUMENT -> DocumentColorLight
                            SearchResultType.SPOT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entry.type) {
                        SearchResultType.ITEM -> Icons.Outlined.Inventory2
                        SearchResultType.DOCUMENT -> Icons.Outlined.Description
                        SearchResultType.SPOT -> Icons.Outlined.Place
                    },
                    contentDescription = null,
                    tint = when (entry.type) {
                        SearchResultType.ITEM -> ItemColor
                        SearchResultType.DOCUMENT -> DocumentColor
                        SearchResultType.SPOT -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.breadcrumb,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (entry.type != SearchResultType.SPOT) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opzioni",
                            tint = MaterialTheme.colorScheme.outlineVariant
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
}

@Composable
private fun EmptySearchResult() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated search icon
        val infiniteTransition = rememberInfiniteTransition(label = "emptySearch")
        val iconOffset by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "searchOff"
        )
        
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer { translationY = iconOffset }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nessun risultato",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Prova con altre parole chiave",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun EmptyHomeState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating animated icon
        val infiniteTransition = rememberInfiniteTransition(label = "emptyHome")
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float"
        )
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
        
        Box(contentAlignment = Alignment.Center) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { alpha = glowAlpha }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { translationY = -floatOffset }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Inizia a organizzare",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aggiungi il tuo primo oggetto o documento\nper trovarlo sempre quando ne hai bisogno",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
