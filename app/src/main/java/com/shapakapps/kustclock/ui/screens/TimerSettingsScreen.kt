package com.shapakapps.kustclock.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shapakapps.kustclock.R
import com.shapakapps.kustclock.model.CustomTimeControl
import com.shapakapps.kustclock.model.IncrementType
import com.shapakapps.kustclock.model.Stage
import com.shapakapps.kustclock.model.TimeControl
import com.shapakapps.kustclock.model.TimeControlRepository
import com.shapakapps.kustclock.util.splitHms

private class StageDraft {
    var moves by mutableStateOf("40")
    var minutes by mutableStateOf("60")
}

private class PlayerDraft {
    var hours by mutableStateOf("0")
    var minutes by mutableStateOf("5")
    var seconds by mutableStateOf("0")
    var incrementType by mutableStateOf(IncrementType.FISCHER)
    var incrementSeconds by mutableStateOf("0")
    var advanced by mutableStateOf(false)
    val stages = mutableStateListOf<StageDraft>()

    fun fillFrom(control: TimeControl) {
        val first = control.stages.first()
        val hms = splitHms(first.durationMillis)
        hours = hms.hours.toString()
        minutes = hms.minutes.toString()
        seconds = hms.seconds.toString()
        incrementType = control.incrementType
        incrementSeconds = (control.incrementMillis / 1000).toString()
        stages.clear()
        if (control.stages.size > 1) {
            advanced = true
            control.stages.forEach { stage ->
                stages.add(
                    StageDraft().apply {
                        moves = stage.moves.toString()
                        minutes = (stage.durationMillis / 60_000).toString()
                    }
                )
            }
        }
    }

    fun totalMillis(): Long {
        return if (!advanced) {
            (hours.toIntOrNull() ?: 0) * 3_600_000L +
                (minutes.toIntOrNull() ?: 0) * 60_000L +
                (seconds.toIntOrNull() ?: 0) * 1_000L
        } else {
            stages.sumOf { (it.minutes.toIntOrNull() ?: 0).coerceAtLeast(0) * 60_000L }
        }
    }

    fun toControl(name: String): TimeControl {
        val incMillis = (incrementSeconds.toIntOrNull() ?: 0).coerceAtLeast(0) * 1_000L
        return if (!advanced) {
            TimeControl(name, listOf(Stage(0, totalMillis())), incrementType, incMillis)
        } else {
            val stageList = stages.map {
                Stage((it.moves.toIntOrNull() ?: 0).coerceAtLeast(0), (it.minutes.toIntOrNull() ?: 0).coerceAtLeast(0) * 60_000L)
            }
            TimeControl(name, stageList, incrementType, incMillis)
        }
    }
}

@Composable
fun TimerSettingsScreen(customId: Long?, onDone: () -> Unit) {
    val existing = remember(customId) { customId?.let { TimeControlRepository.findCustom(it) } }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var sameAsPlayerOne by remember { mutableStateOf(existing == null || existing.one == existing.two) }
    val playerOneDraft = remember { PlayerDraft().apply { existing?.one?.let { fillFrom(it) } } }
    val playerTwoDraft = remember { PlayerDraft().apply { existing?.two?.let { fillFrom(it) } } }
    var showDiscard by remember { mutableStateOf(false) }
    var errorRes by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        if (customId == null) showDiscard = true else onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (customId == null) showDiscard = true else onDone()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
            Text(
                text = stringResource(R.string.custom_time),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            PlayerEditor(title = stringResource(R.string.player_one), draft = playerOneDraft)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sameAsPlayerOne,
                    onCheckedChange = { sameAsPlayerOne = it }
                )
                Text(text = stringResource(R.string.same_as_player_one), fontSize = 14.sp)
            }
            if (!sameAsPlayerOne) {
                PlayerEditor(title = stringResource(R.string.player_two), draft = playerTwoDraft)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorRes = R.string.toast_requesting_name
                        return@Button
                    }
                    if (playerOneDraft.totalMillis() <= 0L || (!sameAsPlayerOne && playerTwoDraft.totalMillis() <= 0L)) {
                        errorRes = R.string.please_set_time
                        return@Button
                    }
                    val one = playerOneDraft.toControl(name)
                    val two = if (sameAsPlayerOne) one else playerTwoDraft.toControl(name)
                    TimeControlRepository.saveCustom(
                        CustomTimeControl(
                            id = existing?.id ?: System.currentTimeMillis(),
                            name = name,
                            one = one,
                            two = two
                        )
                    )
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text(stringResource(R.string.discard_custom_time)) },
            confirmButton = {
                TextButton(onClick = onDone) { Text(stringResource(R.string.action_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscard = false }) { Text(stringResource(R.string.action_keep)) }
            }
        )
    }

    errorRes?.let { res ->
        AlertDialog(
            onDismissRequest = { errorRes = null },
            text = { Text(stringResource(res)) },
            confirmButton = {
                TextButton(onClick = { errorRes = null }) { Text(stringResource(R.string.action_ok)) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerEditor(title: String, draft: PlayerDraft) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = stringResource(R.string.time), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(value = draft.hours, label = stringResource(R.string.hour), modifier = Modifier.weight(1f)) { draft.hours = it }
            NumberField(value = draft.minutes, label = stringResource(R.string.minute), modifier = Modifier.weight(1f)) { draft.minutes = it }
            NumberField(value = draft.seconds, label = stringResource(R.string.second), modifier = Modifier.weight(1f)) { draft.seconds = it }
        }
        Text(text = stringResource(R.string.increment), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IncrementType.entries.forEach { type ->
                FilterChip(
                    selected = draft.incrementType == type,
                    onClick = { draft.incrementType = type },
                    label = {
                        Text(
                            stringResource(
                                when (type) {
                                    IncrementType.NONE -> R.string.increment_none
                                    IncrementType.FISCHER -> R.string.increment_fischer
                                    IncrementType.BRONSTEIN -> R.string.increment_bronstein
                                    IncrementType.DELAY -> R.string.increment_delay
                                }
                            ),
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }
        Text(
            text = stringResource(
                when (draft.incrementType) {
                    IncrementType.NONE -> R.string.increment_none_desc
                    IncrementType.FISCHER -> R.string.increment_fischer_desc
                    IncrementType.BRONSTEIN -> R.string.increment_bronstein_desc
                    IncrementType.DELAY -> R.string.increment_delay_desc
                }
            ),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (draft.incrementType != IncrementType.NONE) {
            NumberField(
                value = draft.incrementSeconds,
                label = stringResource(R.string.seconds),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) { draft.incrementSeconds = it }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = draft.advanced,
                onCheckedChange = { draft.advanced = it }
            )
            Text(text = stringResource(R.string.advanced_mode), fontSize = 14.sp)
        }
        if (draft.advanced) {
            draft.stages.forEachIndexed { index, stage ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.stage, index + 1),
                        fontSize = 13.sp,
                        modifier = Modifier.width(72.dp)
                    )
                    NumberField(
                        value = stage.moves,
                        label = stringResource(R.string.moves),
                        modifier = Modifier.weight(1f)
                    ) { stage.moves = it }
                    NumberField(
                        value = stage.minutes,
                        label = stringResource(R.string.minute),
                        modifier = Modifier.weight(1f)
                    ) { stage.minutes = it }
                    if (draft.stages.size > 1) {
                        IconButton(onClick = { draft.stages.removeAt(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete)
                            )
                        }
                    }
                }
            }
            if (draft.stages.size < 3) {
                TextButton(onClick = { draft.stages.add(StageDraft()) }) {
                    Text(stringResource(R.string.action_add_stage))
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() }.take(3)) },
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

