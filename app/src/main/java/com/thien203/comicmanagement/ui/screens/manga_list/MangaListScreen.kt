package com.thien203.comicmanagement.ui.screens.manga_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thien203.comicmanagement.data.local.relation.WorkWithGenres
import com.thien203.comicmanagement.data.model.SortOrder
import com.thien203.comicmanagement.ui.components.WorkCard
import com.thien203.comicmanagement.ui.screens.genre_manager.GenreManagerDialog
import com.thien203.comicmanagement.ui.screens.genre_manager.GenreViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MangaListScreen(
    viewModel: MangaListViewModel,
    genreViewModel: GenreViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val genres by genreViewModel.genres.collectAsState()
    val formats by genreViewModel.formats.collectAsState()
    var showGenreDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showFormatFilterSheet by remember { mutableStateOf(false) }
    var showGenreFilterSheet by remember { mutableStateOf(false) }
    var genreSheetSearchQuery by remember { mutableStateOf("") }

    var workToDelete by remember { mutableStateOf<WorkWithGenres?>(null) }

    val gridState = rememberLazyGridState()

    val totalSeries = uiState.works.size
    val totalOwnedVolumes = remember(uiState.works) {
        uiState.works.sumOf { workWithGenres ->
            workWithGenres.volumes.count { it.isOwned }
        }
    }
    val totalSpent = remember(uiState.works) {
        uiState.works.sumOf { workWithGenres ->
            workWithGenres.volumes.filter { it.isOwned }.sumOf { it.price }
        }
    }

    val selectedFormat = remember(uiState.selectedFormatId, formats) {
        formats.find { it.id == uiState.selectedFormatId }
    }

    val selectedGenres = remember(uiState.selectedGenreIds, genres) {
        genres.filter { uiState.selectedGenreIds.contains(it.id) }
    }

    val hasActiveFilters = uiState.selectedFormatId != null || uiState.selectedGenreIds.isNotEmpty() || uiState.searchQuery.isNotBlank()

    // Auto-scroll back to item 0 cleanly without trigger animation loops
    LaunchedEffect(uiState.sortOrder, uiState.selectedGenreIds, uiState.selectedFormatId) {
        if (uiState.works.isNotEmpty() && gridState.firstVisibleItemIndex > 0) {
            gridState.scrollToItem(0)
        }
    }

    // Modal Bottom Sheet: Format Filter Grid Sheet
    if (showFormatFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormatFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chọn Định dạng Tác phẩm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (uiState.selectedFormatId != null) {
                        TextButton(
                            onClick = {
                                showFormatFilterSheet = false
                                viewModel.onFormatSelected(null)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Bỏ chọn", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    FilledTonalButton(
                        onClick = { showFormatFilterSheet = false },
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Xong", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedFormatId == null,
                            onClick = {
                                showFormatFilterSheet = false
                                viewModel.onFormatSelected(null)
                            },
                            label = { Text("Tất cả định dạng") }
                        )

                        formats.forEach { format ->
                            FilterChip(
                                selected = uiState.selectedFormatId == format.id,
                                onClick = {
                                    showFormatFilterSheet = false
                                    viewModel.onFormatSelected(format.id)
                                },
                                label = { Text(format.shortName) },
                                leadingIcon = if (uiState.selectedFormatId == format.id) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet: Multi-Select Genre Filter Grid Sheet
    if (showGenreFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showGenreFilterSheet = false
                genreSheetSearchQuery = ""
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.selectedGenreIds.isNotEmpty()) "Chọn Thể loại (${uiState.selectedGenreIds.size})" else "Chọn Thể loại",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (uiState.selectedGenreIds.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                viewModel.clearGenreFilters()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Bỏ chọn", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    FilledTonalButton(
                        onClick = {
                            showGenreFilterSheet = false
                            genreSheetSearchQuery = ""
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Xong", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search field inside Genre Bottom Sheet
                OutlinedTextField(
                    value = genreSheetSearchQuery,
                    onValueChange = { genreSheetSearchQuery = it },
                    placeholder = { Text("Tìm thể loại...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (genreSheetSearchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa",
                                modifier = Modifier.clickable { genreSheetSearchQuery = "" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                val filteredGenres = remember(genres, genreSheetSearchQuery) {
                    if (genreSheetSearchQuery.isBlank()) genres
                    else genres.filter {
                        it.name.contains(genreSheetSearchQuery, ignoreCase = true) ||
                                it.shortName.contains(genreSheetSearchQuery, ignoreCase = true)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedGenreIds.isEmpty(),
                            onClick = {
                                viewModel.clearGenreFilters()
                            },
                            label = { Text("Tất cả thể loại") }
                        )

                        filteredGenres.forEach { genre ->
                            val isSelected = uiState.selectedGenreIds.contains(genre.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.toggleGenreFilter(genre.id)
                                },
                                label = { Text(genre.shortName) },
                                leadingIcon = if (isSelected) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirm Delete Dialog
    if (workToDelete != null) {
        AlertDialog(
            onDismissRequest = { workToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa tác phẩm '${workToDelete!!.work.title}' khỏi thư viện?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWork(workToDelete!!.work.id, workToDelete!!.work.coverUri)
                        workToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { workToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (showGenreDialog) {
        GenreManagerDialog(
            genres = genres,
            formats = formats,
            onAddGenre = { name, shortName -> genreViewModel.addGenre(name, shortName) },
            onUpdateGenre = { genreViewModel.updateGenre(it) },
            onDeleteGenre = { genreViewModel.deleteGenre(it) },
            onAddFormat = { name, shortName -> genreViewModel.addFormat(name, shortName) },
            onUpdateFormat = { genreViewModel.updateFormat(it) },
            onDeleteFormat = { genreViewModel.deleteFormat(it) },
            onDismiss = { showGenreDialog = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Bộ Sưu Tập Tác Phẩm", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showGenreDialog = true }) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = "Quản lý thể loại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm tác phẩm")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 900.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Stats Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalSeries",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tác phẩm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(32.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalOwnedVolumes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tập đã mua",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (totalSpent > 0) {
                            VerticalDivider(modifier = Modifier.height(32.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%,.0f đ", totalSpent),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Đã chi tiêu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Search Bar + Sort Menu Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Tìm tên, tác giả, NXB...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa tìm kiếm",
                                    modifier = Modifier.clickable { viewModel.onSearchQueryChanged("") }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        FilledTonalIconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sắp xếp"
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            Text(
                                text = "Sắp xếp theo:",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider()
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.displayName,
                                            fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (uiState.sortOrder == order) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        viewModel.onSortOrderSelected(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Enhanced Scrollable Single-Row Filter Controls
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Format Filter Button
                    item {
                        FilterChip(
                            selected = selectedFormat != null,
                            onClick = { showFormatFilterSheet = true },
                            label = {
                                Text(
                                    text = if (selectedFormat != null) "Định dạng: ${selectedFormat.shortName}" else "Định dạng (Tất cả)",
                                    fontWeight = if (selectedFormat != null) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Bộ lọc định dạng",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = if (selectedFormat != null) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Bỏ lọc định dạng",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.onFormatSelected(null) }
                                    )
                                }
                            } else null
                        )
                    }

                    // Multi-Select Genre Filter Button
                    item {
                        val genreText = when {
                            uiState.selectedGenreIds.isEmpty() -> "Thể loại (Tất cả)"
                            uiState.selectedGenreIds.size == 1 -> "Thể loại: ${selectedGenres.firstOrNull()?.shortName ?: ""}"
                            else -> "Thể loại (${uiState.selectedGenreIds.size})"
                        }
                        FilterChip(
                            selected = uiState.selectedGenreIds.isNotEmpty(),
                            onClick = { showGenreFilterSheet = true },
                            label = {
                                Text(
                                    text = genreText,
                                    fontWeight = if (uiState.selectedGenreIds.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Bộ lọc thể loại",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = if (uiState.selectedGenreIds.isNotEmpty()) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Bỏ lọc thể loại",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.clearGenreFilters() }
                                    )
                                }
                            } else null
                        )
                    }

                    // Clear All Filters Button
                    if (hasActiveFilters) {
                        item {
                            AssistChip(
                                onClick = {
                                    viewModel.onFormatSelected(null)
                                    viewModel.clearGenreFilters()
                                    viewModel.onSearchQueryChanged("")
                                },
                                label = { Text("Xóa bộ lọc", fontWeight = FontWeight.Bold, maxLines = 1) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterAltOff,
                                        contentDescription = "Xóa lọc",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // List / Grid
                if (uiState.works.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "Không tìm thấy kết quả phù hợp" else "Chưa có tác phẩm nào trong bộ sưu tập.\nBấm '+' để thêm mới!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.works, key = { it.work.id }) { workWithGenres ->
                            WorkCard(
                                workWithGenres = workWithGenres,
                                onClick = { onNavigateToDetail(workWithGenres.work.id) },
                                onEditClick = { onNavigateToEdit(workWithGenres.work.id) },
                                onDeleteClick = { workToDelete = workWithGenres },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}
