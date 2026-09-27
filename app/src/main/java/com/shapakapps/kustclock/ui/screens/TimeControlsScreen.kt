package com.shapakapps.kustclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shapakapps.kustclock.R
import com.shapakapps.kustclock.model.CustomTimeControl
import com.shapakapps.kustclock.model.TimeControlRepository
import com.shapakapps.kustclock.ui.theme.TextSecondary
import com.shapakapps.kustclock.util.controlGlyph
import com.shapakapps.kustclock.util.summarizeControl

@Composable
fun TimeControlsScreen(
    onOpenClock: (Long) -> Unit,
    onEditCustom: (Long) -> Unit,
    onNewCustom: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var deleteTarget by remember { mutableStateOf<CustomTimeControl?>(null) }
    val customList = remember(refresh) { TimeControlRepository.customControls() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.app_settings))
            }
        }
        SegmentTabs(tab = tab, onSelect = { tab = it })
        if (tab == 0) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(TimeControlRepository.presets) { index, preset ->
                    PresetCard(
                        name = preset.name,
                        glyph = controlGlyph(preset),
                        onClick = { onOpenClock(index.toLong()) }
                    )
                }
            }
        } else {
            if (customList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_custom_list),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    customList.forEach { item ->
                        CustomRow(
                            item = item,
                            onOpen = { onOpenClock(item.id) },
                            onEdit = { onEditCustom(item.id) },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.navigationBarsPadding().padding(20.dp)) {
            Button(
                onClick = onNewCustom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_custom_time),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(text = stringResource(R.string.app_settings), fontSize = 16.sp)
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_custom_time)) },
            confirmButton = {
                TextButton(onClick = {
                    TimeControlRepository.deleteCustom(target.id)
                    refresh++
                    deleteTarget = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.action_keep)) }
            }
        )
    }
}

@Composable
private fun SegmentTabs(tab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(stringResource(R.string.presets), stringResource(R.string.custom))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = tab == index
            val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = content,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun PresetCard(name: String, glyph: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = glyph, fontSize = 34.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun CustomRow(
    item: CustomTimeControl,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summarizeControl(item.one),
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
        }
    }
}
