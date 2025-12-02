package com.example.mapbox_offline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TileRegionDownloadScreen(
    viewModel: TileRegionDownloadViewModel = viewModel(),
    onDone: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Offline Regions") },
                actions = {
                    TextButton(onClick = { viewModel.clearAllRegions() }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(8.dp)
        ) {
            items(viewModel.regions) { region ->
                RegionRow(
                    region = region,
                    isDownloading = viewModel.downloadingRegions.contains(region.id),
                    progress = viewModel.downloadProgress[region.id] ?: 0f,
                    refreshTrigger = viewModel.refreshTrigger,
                    isDownloaded = viewModel.downloadedRegions[region.id] ?: false,
                    sizeInMB = viewModel.regionSizes[region.id] ?: 0.0,
                    viewModel = viewModel,
                    onDownload = { viewModel.downloadRegion(region) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun RegionRow(
    region: OfflineRegion,
    isDownloading: Boolean,
    progress: Float,
    refreshTrigger: Boolean,
    isDownloaded: Boolean,
    sizeInMB: Double,
    viewModel: TileRegionDownloadViewModel,
    onDownload: () -> Unit
) {
    LaunchedEffect(key1 = isDownloading, key2 = refreshTrigger) {
        if (!isDownloading) {
            viewModel.checkIfDownloaded(region.id)
        }
    }

    val TAG = "RegionRow-${region.id}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(region.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            when {
                isDownloaded -> Text("Downloaded • ${"%.1f".format(sizeInMB)} MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                isDownloading -> Text("Downloading...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                else -> Text("Not downloaded", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        when {
            isDownloading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(progress = progress, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            isDownloaded -> {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary)
            }
            else -> {
                Button(onClick = onDownload) {
                    Text("Download")
                }
            }
        }
    }
}