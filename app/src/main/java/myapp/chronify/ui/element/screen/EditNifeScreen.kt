package myapp.chronify.ui.element.screen

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import myapp.chronify.R.dimen
import myapp.chronify.R.string
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeType
import myapp.chronify.ui.element.components.AppTopBar
import myapp.chronify.ui.element.DateTimePickerDialogLDT
import myapp.chronify.ui.element.components.EnumDropdown
import myapp.chronify.ui.element.components.OutLinedTextFieldWithSuggestion
import myapp.chronify.ui.navigation.NavigationRoute
import myapp.chronify.ui.viewmodel.AppViewModelProvider
import myapp.chronify.ui.viewmodel.NifeEditViewModel
import myapp.chronify.ui.viewmodel.NifeUpsertViewModel
import myapp.chronify.utils.toFriendlyString
import java.time.LocalDateTime

object EditNifeScreenRoute : NavigationRoute {
    override val route = "editNife"
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNifeScreen(
    navigateBack: () -> Unit,
    viewModel: NifeEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    // 由于viewModel中的uiState是异步加载，而在异步任务完成之前，uiState是空的，所以需要判断是否加载完成
    val isLoading = uiState.nife.id == 0 // 表示数据未加载

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(string.edit_title),
                centeredTitle = false,
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = uiState.isValid,
                        onClick = {
                            coroutineScope.launch { viewModel.save() }
                            navigateBack()
                        }) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(string.submit)
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            // 显示加载状态
            Text(text = "Loading...", modifier = Modifier.fillMaxWidth())
        } else {
            EditContent(
                uiState = uiState,
                onUiStateChange = viewModel::updateUiState,
                modifier = Modifier
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        top = innerPadding.calculateTopPadding()
                    )
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            )
        }

    }
}

@Composable
fun EditContent(
    uiState: NifeUpsertViewModel.NifeUiState,
    onUiStateChange: (Nife) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(dimen.padding_large)),
        modifier = modifier.padding(dimensionResource(dimen.padding_medium))
    ) {
        NifeInputForm(
            nife = uiState.nife,
            onValueChange = onUiStateChange,
            suggestions = uiState.suggestedTitles,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NifeInputForm(
    nife: Nife,
    onValueChange: (Nife) -> Unit,
    suggestions: List<String>,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(dimen.padding_medium))
    ) {
        // Title
        OutLinedTextFieldWithSuggestion(
            initialValue = nife.title,
            onValueChange = { onValueChange(nife.copy(title = it)) },
            suggestions = suggestions,
            label = { Text(stringResource(string.title_req)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = true
        )

        // Type and isFinished
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnumDropdown(
                label = stringResource(string.type_req),
                initialValue = nife.type,
                onValueSelected = {
                    onValueChange(
                        nife.copy(
                            type = it,
                            isFinished = when (it) {
                                NifeType.RECORD -> true
                                else -> false
                            },
                            endDT = when (it) {
                                NifeType.RECORD -> LocalDateTime.now()
                                else -> nife.endDT
                            }
                        )
                    )
                },
                modifier = Modifier.weight(1f, true)
            )
            Spacer(modifier = Modifier.width(dimensionResource(dimen.padding_medium)))
            Switch(
                checked = nife.isFinished,
                onCheckedChange = {
                    onValueChange(
                        nife.copy(
                            isFinished = it,
                            endDT = if (it) LocalDateTime.now() else null
                        )
                    )
                },
                modifier = Modifier
            )
        }

        // beginDT and endDT
        val beginDT by remember { mutableStateOf(LocalDateTime.now()) }
        val endDT by remember { mutableStateOf(LocalDateTime.now()) }
        var showBeginDTPicker by remember { mutableStateOf(false) }
        var showEndDTPicker by remember { mutableStateOf(false) }

        // BeginDT
        OutlinedTextField(
            value = nife.beginDT?.toFriendlyString() ?: "",
            onValueChange = { },
            label = { Text(stringResource(string.beginDT_req)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                // disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(beginDT) {
                    awaitEachGesture {
                        // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                        // in the Initial pass to observe events before the text field consumes them
                        // in the Main pass.
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showBeginDTPicker = true
                        }
                    }
                }
        )
        if (showBeginDTPicker) {
            DateTimePickerDialogLDT(
                onDismiss = { showBeginDTPicker = false },
                onConfirm = { begin: LocalDateTime?, _: LocalDateTime? ->
                    onValueChange(nife.copy(beginDT = begin))
                    showBeginDTPicker = false
                },
            )
        }
        // EndDT
        OutlinedTextField(
            value = nife.endDT?.toFriendlyString() ?: "",
            onValueChange = { },
            label = { Text(stringResource(string.endDT_req)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                // disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(endDT) {
                    awaitEachGesture {
                        // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                        // in the Initial pass to observe events before the text field consumes them
                        // in the Main pass.
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showEndDTPicker = true
                        }
                    }
                }
        )
        if (showEndDTPicker) {
            DateTimePickerDialogLDT(
                onDismiss = { showEndDTPicker = false },
                onConfirm = { end: LocalDateTime?, _: LocalDateTime? ->
                    onValueChange(nife.copy(endDT = end))
                    showEndDTPicker = false
                },
            )
        }

        // TODO: interval

        // description
        OutlinedTextField(
            value = nife.description,
            onValueChange = { onValueChange(nife.copy(description = it)) },
            label = { Text(stringResource(string.description_req)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = false
        )

        // TODO: location with GPS
        OutlinedTextField(
            value = nife.location,
            onValueChange = { onValueChange(nife.copy(location = it)) },
            label = { Text(stringResource(string.location_req)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            singleLine = true
        )
    }

}
