package com.thien203.comicmanagement.ui.screens.add_edit_manga

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thien203.comicmanagement.ui.components.ImagePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditMangaScreen(
    viewModel: AddEditMangaViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allGenres by viewModel.allGenres.collectAsState()
    val allFormats by viewModel.allFormats.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val titleRequester = remember { BringIntoViewRequester() }
    val totalVolumesRequester = remember { BringIntoViewRequester() }

    var selectedImageUri by remember(uiState.isEditing, uiState.coverUri) { mutableStateOf<Uri?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Chỉnh Sửa Tác Phẩm" else "Thêm Tác Phẩm Mới",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveManga(
                        context = context,
                        newPickerUri = selectedImageUri,
                        onSuccess = { onNavigateBack() },
                        onErrorFound = { field ->
                            coroutineScope.launch {
                                when (field) {
                                    FieldWithError.TITLE -> titleRequester.bringIntoView()
                                    FieldWithError.TOTAL_VOLUMES -> totalVolumesRequester.bringIntoView()
                                }
                            }
                        }
                    )
                },
                icon = { Icon(imageVector = Icons.Default.Save, contentDescription = null) },
                text = { Text("Lưu tác phẩm") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val isWide = maxWidth > 520.dp

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isWide) {
                        // Wide screen / Landscape: ImagePicker next to main fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            ImagePicker(
                                currentImageUri = selectedImageUri?.toString() ?: uiState.coverUri,
                                onImageSelected = { uri -> selectedImageUri = uri }
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.title,
                                    onValueChange = { viewModel.onTitleChanged(it) },
                                    label = { Text("Tên bộ truyện / tác phẩm (*)") },
                                    isError = uiState.titleError != null,
                                    supportingText = {
                                        if (uiState.titleError != null) {
                                            Text(
                                                text = uiState.titleError!!,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(titleRequester),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = uiState.author,
                                    onValueChange = { viewModel.onAuthorChanged(it) },
                                    label = { Text("Tác giả") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = uiState.publisher,
                                    onValueChange = { viewModel.onPublisherChanged(it) },
                                    label = { Text("Nhà xuất bản") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    } else {
                        // Narrow screen / Vertical phone
                        ImagePicker(
                            currentImageUri = selectedImageUri?.toString() ?: uiState.coverUri,
                            onImageSelected = { uri -> selectedImageUri = uri }
                        )

                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.onTitleChanged(it) },
                            label = { Text("Tên bộ truyện / tác phẩm (*)") },
                            isError = uiState.titleError != null,
                            supportingText = {
                                if (uiState.titleError != null) {
                                    Text(
                                        text = uiState.titleError!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(titleRequester),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.author,
                            onValueChange = { viewModel.onAuthorChanged(it) },
                            label = { Text("Tác giả") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.publisher,
                            onValueChange = { viewModel.onPublisherChanged(it) },
                            label = { Text("Nhà xuất bản") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Format Selector (Loại hình / Định dạng)
                    Text(
                        text = "Định dạng / Loại hình:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allFormats.forEach { fmt ->
                            FilterChip(
                                selected = uiState.selectedFormatId == fmt.id,
                                onClick = { viewModel.onFormatSelected(fmt.id) },
                                label = { Text(fmt.name) }
                            )
                        }
                    }

                    // Remaining fields
                    OutlinedTextField(
                        value = uiState.totalVolumes,
                        onValueChange = { viewModel.onTotalVolumesChanged(it) },
                        label = { Text("Tổng số tập dự kiến (*)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.totalVolumesError != null,
                        supportingText = {
                            if (uiState.totalVolumesError != null) {
                                Text(
                                    text = uiState.totalVolumesError!!,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(totalVolumesRequester),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.defaultPrice,
                        onValueChange = { viewModel.onDefaultPriceChanged(it) },
                        label = { Text("Giá tiền mặc định mỗi tập (đ)") },
                        placeholder = { Text("Ví dụ: 25000 hoặc 30000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChanged(it) },
                        label = { Text("Tóm tắt / Ghi chú") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Genre Selector
                    Text(
                        text = "Chọn thể loại nội dung:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allGenres.forEach { genre ->
                            val isSelected = uiState.selectedGenreIds.contains(genre.id)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleGenre(genre.id) },
                                label = { Text(genre.shortName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
