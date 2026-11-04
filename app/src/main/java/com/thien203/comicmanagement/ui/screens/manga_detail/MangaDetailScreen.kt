package com.thien203.comicmanagement.ui.screens.manga_detail

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import com.thien203.comicmanagement.ui.components.VolumeItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MangaDetailScreen(
    viewModel: MangaDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val detail by viewModel.workDetail.collectAsState()
    val allFormats by viewModel.allFormats.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf<String?>(null) }

    var volumeToEditPrice by remember { mutableStateOf<VolumeEntity?>(null) }
    var editPriceText by remember { mutableStateOf("") }

    // Dialog for editing individual volume price
    if (volumeToEditPrice != null) {
        AlertDialog(
            onDismissRequest = { volumeToEditPrice = null },
            title = { Text("Chỉnh sửa giá Tập ${volumeToEditPrice!!.volumeNumber}") },
            text = {
                OutlinedTextField(
                    value = editPriceText,
                    onValueChange = { editPriceText = it },
                    label = { Text("Giá tiền tập này (đ)") },
                    placeholder = { Text("Ví dụ: 25000 hoặc 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newPrice = editPriceText.toDoubleOrNull() ?: 0.0
                        viewModel.updateVolumePrice(volumeToEditPrice!!.id, newPrice)
                        volumeToEditPrice = null
                    }
                ) {
                    Text("Lưu giá")
                }
            },
            dismissButton = {
                TextButton(onClick = { volumeToEditPrice = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Full screen image preview modal with smooth scale & fade animation
    AnimatedVisibility(
        visible = previewImageUri != null,
        enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.88f, animationSpec = tween(250, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.88f, animationSpec = tween(200, easing = FastOutSlowInEasing))
    ) {
        Dialog(
            onDismissRequest = { previewImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.93f))
                    .clickable { previewImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = previewImageUri,
                    contentDescription = "Xem ảnh bìa đầy đủ",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { previewImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng xem ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            title = { Text("Đổi Định dạng / Loại hình") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allFormats.forEach { fmt ->
                        FilterChip(
                            selected = detail?.work?.formatId == fmt.id,
                            onClick = {
                                viewModel.updateComicFormat(fmt.id)
                                showFormatDialog = false
                            },
                            label = { Text(fmt.name) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFormatDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa tác phẩm này khỏi thư viện?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteWork {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(detail?.work?.title ?: "Chi Tiết Tác Phẩm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    detail?.let {
                        IconButton(onClick = { onNavigateToEdit(it.work.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val work = detail!!.work
            val format = detail!!.format
            val genres = detail!!.genres
            val volumes = detail!!.volumes

            val ownedCount = remember(volumes) { volumes.count { it.isOwned } }
            val totalCount = volumes.size
            val progress = remember(ownedCount, totalCount) {
                if (totalCount > 0) ownedCount.toFloat() / totalCount else 0f
            }

            val totalSpent = remember(volumes) { volumes.filter { it.isOwned }.sumOf { it.price } }
            val avgPrice = remember(volumes) { if (volumes.isNotEmpty()) volumes.map { it.price }.average() else 0.0 }

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "CollectionProgress"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                // Unified Scrollable LazyVerticalGrid for 100% responsiveness on all screen sizes & orientations
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 175.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 800.dp)
                ) {
                    // Span 1: Header Info Row
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Cover Image (Clickable for full screen zoom)
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = !work.coverUri.isNullOrBlank()) {
                                        previewImageUri = work.coverUri
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!work.coverUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = work.coverUri,
                                        contentDescription = work.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Zoom hint icon on bottom right of cover
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(topStart = 6.dp),
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = "Phóng to",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .size(16.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Info Column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = work.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable { showFormatDialog = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = format?.name ?: "Định dạng khác",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Đổi định dạng",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (work.author.isNotBlank()) {
                                    Text(
                                        text = "Tác giả: ${work.author}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (work.publisher.isNotBlank()) {
                                    Text(
                                        text = "NXB: ${work.publisher}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Genres
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    genres.forEach { genre ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(genre.shortName, fontSize = 11.sp) },
                                            modifier = Modifier.height(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Span 2: Description
                    if (work.description.isNotBlank()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = work.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Span 3: Collection Progress & Price Card
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Tiến độ sưu tầm",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Đã có $ownedCount / $totalCount tập (${(progress * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                if (totalSpent > 0 || avgPrice > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Đã chi: ${String.format(Locale.getDefault(), "%,.0f", totalSpent)} đ",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Giá TB: ${String.format(Locale.getDefault(), "%,.0f", avgPrice)} đ/tập",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Span 4: Title Header for Volumes with Quick + / - Buttons
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Danh sách tập ($totalCount tập):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedIconButton(
                                    onClick = { viewModel.removeOneVolume() },
                                    enabled = totalCount > 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Giảm 1 tập",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "$totalCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                FilledTonalIconButton(
                                    onClick = { viewModel.addOneVolume() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tăng 1 tập",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Volume Items (Responsive Grid)
                    items(volumes, key = { it.id }) { volume ->
                        VolumeItem(
                            volume = volume,
                            onToggleOwnership = { isOwned ->
                                viewModel.toggleVolumeOwnership(volume.id, isOwned)
                            },
                            onSelectCoverImage = { uri ->
                                viewModel.updateVolumeCover(context, volume.id, uri)
                            },
                            onPreviewCoverImage = { coverUri ->
                                previewImageUri = coverUri
                            },
                            onEditPriceClick = {
                                volumeToEditPrice = volume
                                editPriceText = if (volume.price > 0) String.format(Locale.US, "%.0f", volume.price) else ""
                            }
                        )
                    }
                }
            }
        }
    }
}
