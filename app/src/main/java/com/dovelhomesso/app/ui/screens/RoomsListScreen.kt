package com.dovelhomesso.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dovelhomesso.app.R
import com.dovelhomesso.app.data.entities.HouseRoomEntity
import com.dovelhomesso.app.ui.components.CreateEntityDialog
import com.dovelhomesso.app.ui.components.FadeInBox
import com.dovelhomesso.app.ui.components.ParticleEffect
import com.dovelhomesso.app.ui.viewmodels.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsListScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRoom: (Long) -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<HouseRoomEntity?>(null) }
    
    // FAB animation
    val fabScale by animateFloatAsState(
        targetValue = if (showCreateDialog) 0f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab"
    )
    
    Scaffold(
        topBar = {
            // Animated gradient top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.rooms),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Indietro",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            // Animated FAB
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.scale(fabScale),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "addIcon")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 2000
                            0f at 0
                            15f at 100
                            -15f at 200
                            0f at 300
                            0f at 2000
                        }
                    ),
                    label = "rotation"
                )
                Icon(
                    Icons.Default.Add, 
                    contentDescription = stringResource(R.string.new_room),
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }
    ) { paddingValues ->
        if (rooms.isEmpty()) {
            // Animated empty state
            EmptyRoomsState(
                paddingValues = paddingValues,
                onCreateClick = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(rooms, key = { _, room -> room.id }) { index, room ->
                    FadeInBox(delayMillis = index * 80) {
                        AnimatedRoomItem(
                            room = room,
                            onClick = { onNavigateToRoom(room.id) },
                            onDelete = { roomToDelete = room }
                        )
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Create Room Dialog with animation
    AnimatedVisibility(
        visible = showCreateDialog,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        CreateEntityDialog(
            title = stringResource(R.string.new_room),
            placeholder = stringResource(R.string.room_name_hint),
            onConfirm = { name ->
                viewModel.createRoom(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
    
    // Delete Confirmation Dialog
    roomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_room_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRoom(room)
                        roomToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyRoomsState(
    paddingValues: PaddingValues,
    onCreateClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                // Floating animated icon
                val infiniteTransition = rememberInfiniteTransition(label = "empty")
                val floatOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "float"
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer { translationY = -floatOffset }
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Nessuna stanza",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Crea la tua prima stanza per iniziare\nad organizzare i tuoi oggetti",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Animated button
                val buttonInteraction = remember { MutableInteractionSource() }
                val isPressed by buttonInteraction.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "button"
                )
                
                Button(
                    onClick = onCreateClick,
                    interactionSource = buttonInteraction,
                    modifier = Modifier.scale(buttonScale),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.new_room))
                }
            }
        }
    }
}

@Composable
private fun AnimatedRoomItem(
    room: HouseRoomEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        label = "cardElevation"
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
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon container
            val iconRotation by animateFloatAsState(
                targetValue = if (isPressed) 10f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "iconRotation"
            )
            
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer { rotationZ = iconRotation }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tocca per esplorare",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Animated delete button
            val deleteInteraction = remember { MutableInteractionSource() }
            val deletePressed by deleteInteraction.collectIsPressedAsState()
            val deleteScale by animateFloatAsState(
                targetValue = if (deletePressed) 0.8f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "delete"
            )
            
            IconButton(
                onClick = onDelete,
                interactionSource = deleteInteraction,
                modifier = Modifier.scale(deleteScale)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
            
            // Animated chevron
            val infiniteTransition = rememberInfiniteTransition(label = "chevron")
            val chevronOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "chevronMove"
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.graphicsLayer { translationX = chevronOffset }
            )
        }
    }
}
