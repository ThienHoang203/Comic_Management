package com.thien203.comicmanagement.ui.screens.genre_manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thien203.comicmanagement.data.local.entity.FormatEntity
import com.thien203.comicmanagement.data.local.entity.GenreEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreManagerDialog(
    genres: List<GenreEntity>,
    formats: List<FormatEntity>,
    onAddGenre: (String, String) -> Unit,
    onUpdateGenre: (GenreEntity) -> Unit,
    onDeleteGenre: (GenreEntity) -> Unit,
    onAddFormat: (String, String) -> Unit,
    onUpdateFormat: (FormatEntity) -> Unit,
    onDeleteFormat: (FormatEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Genre States
    var newGenreName by remember { mutableStateOf("") }
    var newGenreShortName by remember { mutableStateOf("") }
    var genreToEdit by remember { mutableStateOf<GenreEntity?>(null) }
    var genreToDelete by remember { mutableStateOf<GenreEntity?>(null) }
    var editGenreName by remember { mutableStateOf("") }
    var editGenreShortName by remember { mutableStateOf("") }

    // Format States
    var newFormatName by remember { mutableStateOf("") }
    var newFormatShortName by remember { mutableStateOf("") }
    var formatToEdit by remember { mutableStateOf<FormatEntity?>(null) }
    var formatToDelete by remember { mutableStateOf<FormatEntity?>(null) }
    var editFormatName by remember { mutableStateOf("") }
    var editFormatShortName by remember { mutableStateOf("") }

    val defaultGenreNames = setOf(
        "Hành động (Action)", "Phiêu lưu (Adventure)", "Hài hước (Comedy)", "Tình cảm (Romance)",
        "Kỳ ảo (Fantasy)", "Viễn tưởng (Sci-Fi)", "Trinh thám (Mystery)", "Giật gân / Tâm lý (Thriller)",
        "Kinh dị (Horror)", "Cuộc sống (Slice of Life)", "Học đường (School Life)", "Thể thao (Sports)",
        "Dị giới / Chuyển sinh (Isekai)", "Trùng sinh / Hồi sinh (Regression)", "Siêu nhiên (Supernatural)",
        "Lịch sử (Historical)", "Võ thuật / Chưởng (Martial Arts)", "Khí chất / Tu tiên (Xianxia)",
        "Mecha / Robot", "Âm nhạc / Nghệ thuật (Music)", "Trò chơi / Game (Gaming)",
        "Ẩm thực / Nấu ăn (Cooking)", "Y học / Bác sĩ (Medical)", "Shonen", "Shojo", "Seinen", "Josei",
        "Đa vũ trụ / Hệ thống (System)", "Kịch tính / Drama", "Đời thường / Sinh tồn (Survival)",
        "Manga (Truyện Nhật)", "Manhwa (Truyện Hàn)", "Manhua (Truyện Trung)", "Comic (Truyện Âu Mỹ)",
        "Manga", "Manhwa", "Manhua", "Comic"
    )

    val (defaultGenres, customGenres) = genres.partition { defaultGenreNames.contains(it.name) }

    // Confirm Delete Genre Dialog
    if (genreToDelete != null) {
        AlertDialog(
            onDismissRequest = { genreToDelete = null },
            title = { Text("Xác nhận xóa thể loại") },
            text = { Text("Bạn có chắc chắn muốn xóa thể loại '${genreToDelete!!.name}' khỏi hệ thống?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGenre(genreToDelete!!)
                        genreToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { genreToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Confirm Delete Format Dialog
    if (formatToDelete != null) {
        AlertDialog(
            onDismissRequest = { formatToDelete = null },
            title = { Text("Xác nhận xóa định dạng") },
            text = { Text("Bạn có chắc chắn muốn xóa định dạng '${formatToDelete!!.name}' khỏi hệ thống?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFormat(formatToDelete!!)
                        formatToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { formatToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Sub-dialog for editing genre name & short name
    if (genreToEdit != null) {
        AlertDialog(
            onDismissRequest = { genreToEdit = null },
            title = { Text("Chỉnh sửa thể loại") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editGenreName,
                        onValueChange = { editGenreName = it },
                        label = { Text("Tên thể loại đầy đủ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGenreShortName,
                        onValueChange = { editGenreShortName = it },
                        label = { Text("Tên viết tắt (hiển thị thẻ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editGenreName.isNotBlank() && genreToEdit != null) {
                            onUpdateGenre(
                                genreToEdit!!.copy(
                                    name = editGenreName.trim(),
                                    shortName = editGenreShortName.ifBlank { editGenreName }.trim()
                                )
                            )
                            genreToEdit = null
                        }
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { genreToEdit = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Sub-dialog for editing format
    if (formatToEdit != null) {
        AlertDialog(
            onDismissRequest = { formatToEdit = null },
            title = { Text("Chỉnh sửa định dạng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editFormatName,
                        onValueChange = { editFormatName = it },
                        label = { Text("Tên định dạng đầy đủ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFormatShortName,
                        onValueChange = { editFormatShortName = it },
                        label = { Text("Tên viết tắt (hiển thị thẻ)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editFormatName.isNotBlank() && formatToEdit != null) {
                            onUpdateFormat(
                                formatToEdit!!.copy(
                                    name = editFormatName.trim(),
                                    shortName = editFormatShortName.ifBlank { editFormatName }.trim()
                                )
                            )
                            formatToEdit = null
                        }
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { formatToEdit = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Quản lý Danh mục") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
            ) {
                // Tab Selection
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Thể loại (${genres.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Định dạng (${formats.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: GENRES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = newGenreName,
                                onValueChange = { newGenreName = it },
                                label = { Text("Tên thể loại mới") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newGenreShortName,
                                onValueChange = { newGenreShortName = it },
                                label = { Text("Tên viết tắt (ví dụ: Action)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newGenreName.isNotBlank()) {
                                    onAddGenre(newGenreName.trim(), newGenreShortName.trim())
                                    newGenreName = ""
                                    newGenreShortName = ""
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm thể loại")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Section 1: Custom Genres
                        if (customGenres.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Thể loại bạn tạo (${customGenres.size}):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(customGenres, key = { "custom_${it.id}" }) { genre ->
                                GenreItemRow(
                                    genre = genre,
                                    onEdit = {
                                        genreToEdit = genre
                                        editGenreName = genre.name
                                        editGenreShortName = genre.shortName
                                    },
                                    onDelete = { genreToDelete = genre }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // Section 2: Default System Genres
                        item {
                            Text(
                                text = "Thể loại hệ thống (${defaultGenres.size}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(defaultGenres, key = { "default_${it.id}" }) { genre ->
                            GenreItemRow(
                                genre = genre,
                                onEdit = {
                                    genreToEdit = genre
                                    editGenreName = genre.name
                                    editGenreShortName = genre.shortName
                                },
                                onDelete = { genreToDelete = genre }
                            )
                        }
                    }
                } else {
                    // TAB 1: FORMATS
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = newFormatName,
                                    onValueChange = { newFormatName = it },
                                    label = { Text("Tên định dạng mới") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newFormatShortName,
                                    onValueChange = { newFormatShortName = it },
                                    label = { Text("Tên viết tắt (ví dụ: LN)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newFormatName.isNotBlank()) {
                                        onAddFormat(newFormatName.trim(), newFormatShortName.trim())
                                        newFormatName = ""
                                        newFormatShortName = ""
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm định dạng")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(formats, key = { it.id }) { fmt ->
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fmt.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = fmt.shortName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                formatToEdit = fmt
                                                editFormatName = fmt.name
                                                editFormatShortName = fmt.shortName
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Sửa định dạng",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(onClick = { formatToDelete = fmt }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Xóa định dạng",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun GenreItemRow(
    genre: GenreEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = genre.shortName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Sửa thể loại",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa thể loại",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    HorizontalDivider()
}
