package com.thien203.comicmanagement.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thien203.comicmanagement.data.local.entity.VolumeEntity
import java.util.Locale

@Composable
fun VolumeItem(
    volume: VolumeEntity,
    onToggleOwnership: (Boolean) -> Unit,
    onSelectCoverImage: (Uri) -> Unit,
    onPreviewCoverImage: ((String) -> Unit)? = null,
    onEditPriceClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onSelectCoverImage(uri)
            }
        }
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (volume.isOwned) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume Cover Thumbnail Box
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (!volume.coverUri.isNullOrBlank() && onPreviewCoverImage != null) {
                            onPreviewCoverImage(volume.coverUri)
                        } else {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!volume.coverUri.isNullOrBlank()) {
                    AsyncImage(
                        model = volume.coverUri,
                        contentDescription = "Bìa tập ${volume.volumeNumber}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Small edit icon button overlay to change photo
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(20.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Sửa ảnh tập",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Thêm bìa tập",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info & Toggle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tập ${volume.volumeNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Editable Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = onEditPriceClick != null) {
                        onEditPriceClick?.invoke()
                    }
                ) {
                    Text(
                        text = if (volume.price > 0) "${String.format(Locale.getDefault(), "%,.0f", volume.price)} đ" else "Thêm giá tiền",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (volume.price > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = if (volume.price > 0) FontWeight.Normal else FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa giá tập",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                FilterChip(
                    selected = volume.isOwned,
                    onClick = { onToggleOwnership(!volume.isOwned) },
                    label = {
                        Text(
                            text = if (volume.isOwned) "Đã mua" else "Còn thiếu",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (volume.isOwned) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    leadingIcon = if (volume.isOwned) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
