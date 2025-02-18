package myapp.chronify.ui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.chronify.R
import myapp.chronify.R.string
import myapp.chronify.R.dimen
import myapp.chronify.data.nife.NifeType
import myapp.chronify.data.nife.getIcon
import myapp.chronify.data.nife.getLocalizedName
import myapp.chronify.ui.element.components.TimePolymer
import myapp.chronify.ui.element.components.DateRange
import myapp.chronify.ui.element.components.SimpleDateRangePicker
import myapp.chronify.ui.element.screen.NifeDTText
import myapp.chronify.ui.viewmodel.AppViewModelProvider
import myapp.chronify.ui.viewmodel.NifeAddViewModel
import myapp.chronify.utils.toLocalTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNifeBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit = {}
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        AddNifeBottomSheetContent(onDismissRequest = onDismissRequest)
    }
}

@Composable
fun AddNifeBottomSheetContent(
    viewModel: NifeAddViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState
    var showDateTimePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // bottom sheet title and save button
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(string.add_schedule_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
            TextButton(
                enabled = uiState.isValid,
                onClick = {
                    coroutineScope.launch {
                        viewModel.save()
                    }
                    onDismissRequest()
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) { Text(stringResource(string.submit)) }
        }
        // title and isFinished
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoFocusedOutlineTextField(
                onValueChange = { text ->
                    viewModel.updateUiState(uiState.nife.copy(title = text))
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(dimensionResource(dimen.padding_tiny)))
            Checkbox(
                checked = uiState.nife.isFinished,
                onCheckedChange = {
                    viewModel.updateUiState(
                        uiState.nife.copy(
                            isFinished = it,
                            endDT = if (it) LocalDateTime.now() else null
                        )
                    )
                },
                modifier = Modifier.size(24.dp)
            )
        }
        // type, date&time picker
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            NifeTypeDropdownChip(
                initialType = uiState.nife.type,
                onSelect = { type ->
                    viewModel.updateUiState(
                        uiState.nife.copy(
                            type = type,
                            isFinished = when (type) {
                                NifeType.RECORD -> true
                                else -> false
                            },
                            endDT = when (type) {
                                NifeType.RECORD -> LocalDateTime.now()
                                else -> uiState.nife.endDT
                            }
                        )
                    )
                }
            )

            DateTimeChip(
                label = {
                    NifeDTText(
                        nife = uiState.nife,
                        placeholderStr = stringResource(string.date_time_picker_label),
                        ifRenderOutdated = true
                    )
                },
                isSelected = !(uiState.nife.beginDT == null && uiState.nife.endDT == null),
                onClick = { showDateTimePicker = true },
                onClose = {
                    viewModel.updateUiState(
                        uiState.nife.copy(
                            beginDT = null,
                            endDT = null
                        )
                    )
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    if (showDateTimePicker) {
        DateTimePickerDialogLDT(
            onDismiss = { showDateTimePicker = false },
            onConfirm = { beginDT: LocalDateTime?, endDT: LocalDateTime? ->
                viewModel.updateUiState(
                    uiState.nife.copy(
                        beginDT = beginDT,
                        endDT = endDT ?: beginDT
                    )
                )
                showDateTimePicker = false
            }
        )
    }
}

@Composable
private fun AutoFocusedOutlineTextField(
    onValueChange: (String) -> Unit,
    initialTextString: String = "",
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val textFieldValueState = remember { mutableStateOf(TextFieldValue(initialTextString)) }

    LaunchedEffect(Unit) {
        delay(300) // Optional delay to ensure the TextField is fully composed
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = textFieldValueState.value,
        onValueChange = {
            onValueChange(it.text)
            textFieldValueState.value = it
        },
        label = { stringResource(string.title_req) },
        // colors = OutlinedTextFieldDefaults.colors(
        //     focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        //     unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        //     disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        // ),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    textFieldValueState.value = textFieldValueState.value.copy(
                        selection = TextRange(
                            0,
                            textFieldValueState.value.text.length
                        )
                    )
                }
            }
    )
}


@Composable
fun NifeTypeDropdownChip(
    initialType: NifeType,
    onSelect: (NifeType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(initialType) }
    val types = NifeType.entries

    Box(
        modifier = modifier
    ) {
        AssistChip(
            onClick = { expanded = !expanded },
            label = { Text(selectedType.getLocalizedName()) },
            leadingIcon = {
                Icon(
                    painterResource(selectedType.getIcon()),
                    contentDescription = selectedType.getLocalizedName(),
                    Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.getLocalizedName()) },
                    leadingIcon = {
                        Icon(
                            painterResource(type.getIcon()),
                            contentDescription = type.getLocalizedName(),
                        )
                    },
                    onClick = {
                        selectedType = type
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateTimeChip(
    label: @Composable () -> Unit,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onClose: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = isSelected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.calendar_add_on_24px),
                contentDescription = stringResource(string.date_time_picker_label),
                Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        trailingIcon = {
            if (isSelected)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(string.clear_date_time),
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialogLDT(
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime?, LocalDateTime?) -> Unit = { _, _ -> onDismiss() }
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                // .width(IntrinsicSize.Min)
                .fillMaxWidth()
                // .height(IntrinsicSize.Min)
                // .height(600.dp)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {

                var selectedIndex by remember { mutableIntStateOf(0) }
                val options = listOf("Date", "Time")

                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                        ) { Text(label) }
                    }
                }

                var selectedDateRange by remember { mutableStateOf(DateRange()) }
                val calender = Calendar.getInstance()
                val timePickerState = rememberTimePickerState(
                    initialHour = calender.get(Calendar.HOUR_OF_DAY),
                    initialMinute = calender.get(Calendar.MINUTE),
                    is24Hour = false,
                )

                when (selectedIndex) {
                    // pick date
                    0 -> {
                        SimpleDateRangePicker(
                            initialDateRange = selectedDateRange,
                            startFromSunday = false,
                            onDateRangeSelected = { dr ->
                                selectedDateRange = dr
                            },
                        )
                    }
                    // pick time
                    1 -> {
                        TimePolymer(timePickerState)
                    }

                    else -> {}
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(string.cancel)) }
                    TextButton(
                        onClick = {
                            onConfirm(
                                if (selectedDateRange.startDate != null) {
                                    merge_LD_TPS(
                                        selectedDateRange.startDate!!,
                                        timePickerState
                                    )
                                } else {
                                    null
                                },
                                if (selectedDateRange.endDate != null) {
                                    merge_LD_TPS(
                                        selectedDateRange.endDate!!,
                                        timePickerState
                                    )
                                } else {
                                    null
                                }
                            )
                        }
                    ) { Text(stringResource(string.confirm)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun merge_LD_TPS(date: LocalDate, timeState: TimePickerState): LocalDateTime {
    return LocalDateTime.of(date, timeState.toLocalTime())
}
