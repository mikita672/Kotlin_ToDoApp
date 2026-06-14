package com.mdzeviatau.todoapp.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.mdzeviatau.todoapp.ui.screens.getFileName
import com.mdzeviatau.todoapp.ui.screens.isImage
import com.mdzeviatau.todoapp.ui.screens.openFile
import com.mdzeviatau.todoapp.ui.screens.saveUriToInternalStorage
import java.io.File
import java.util.UUID
import androidx.core.net.toUri

@Composable
fun AttachmentSection(
    attachments: List<String>, onAttachmentsChange: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
            if (uri != null) {
                val internalUri = saveUriToInternalStorage(context, uri)
                if (internalUri != null) {
                    onAttachmentsChange(attachments + internalUri.toString())
                }
            }
        })

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(), onResult = { success ->
            if (success && tempImageUri != null) {
                onAttachmentsChange(attachments + tempImageUri.toString())
            }
        })

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(), onResult = { uri ->
            if (uri != null) {
                val internalUri = saveUriToInternalStorage(context, uri)
                if (internalUri != null) {
                    onAttachmentsChange(attachments + internalUri.toString())
                }
            }
        })

    if (fullScreenImageUri != null) {
        FullScreenImageDialog(
            uriString = fullScreenImageUri!!, onDismiss = { fullScreenImageUri = null })
    }

    Text("Attachments", style = MaterialTheme.typography.labelLarge)
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (attachments.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachments) { uriString ->
                        val uri = uriString.toUri()
                        AttachmentItem(uri = uri, onDelete = {
                            onAttachmentsChange(attachments.filter { it != uriString })
                        }, onClick = {
                            if (isImage(context, uri)) {
                                fullScreenImageUri = uriString
                            } else {
                                openFile(context, uri)
                            }
                        })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val uri = try {
                            val directory = File(context.filesDir, "attachments")
                            if (!directory.exists()) directory.mkdirs()
                            val file = File(directory, "IMG_${UUID.randomUUID()}.jpg")
                            FileProvider.getUriForFile(
                                context, "com.mdzeviatau.todoapp.fileprovider", file
                            )
                        } catch (e: Exception) {
                            Log.e("AttachmentSection", "Error creating image file", e)
                            null
                        }
                        if (uri != null) {
                            tempImageUri = uri
                            takePictureLauncher.launch(uri)
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Photo")
                }
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Photo")
                }
                Button(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add File")
                }
            }
        }
    }
}

@Composable
fun AttachmentItem(uri: Uri, onDelete: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val isImage = remember(uri) { isImage(context, uri) }
    val fileName = remember(uri) { getFileName(context, uri) }

    Box(modifier = Modifier.size(80.dp)) {
        if (isImage) {
            AsyncImage(
                model = uri,
                contentDescription = "Attachment preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(0.dp))
                    .clickable { onClick() },
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete attachment",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun FullScreenImageDialog(uriString: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }) {
            AsyncImage(
                model = uriString,
                contentDescription = "Full Screen Attachment",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
