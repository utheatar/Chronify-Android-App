package myapp.chronify.ui.element.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import myapp.chronify.R.dimen
import myapp.chronify.R.string
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeType
import myapp.chronify.ui.element.AddNifeBottomSheet
import myapp.chronify.ui.element.components.AppTopBar
import myapp.chronify.ui.element.components.ErrorView
import myapp.chronify.ui.element.exp.SwipeableListItem
import myapp.chronify.ui.navigation.NavigationRoute
import myapp.chronify.ui.viewmodel.AppViewModelProvider
import myapp.chronify.ui.viewmodel.MarkerViewModel
import myapp.chronify.ui.viewmodel.ListFilter
import myapp.chronify.utils.toFriendlyString
import java.time.LocalDateTime

object MarkerScreenRoute : NavigationRoute {
    override val route = "marker"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerScreen(
    viewModel: MarkerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToEdit: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val lazyItems = viewModel.nifesPagingData.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = stringResource(string.marker_title),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                // onClick = navigateToAddScreen,
                onClick = { showBottomSheet = true },
                modifier = Modifier.padding(dimensionResource(dimen.padding_large))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(string.add_button)
                )
            }
        }
    ) { innerPadding ->
        MarkerScreenContent(
            lazyItems = lazyItems,
            onItemClick = navigateToEdit,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        )
        // Bottom sheet
        if (showBottomSheet) {
            AddNifeBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                },
            )
        }
    }

}

@Composable
fun MarkerScreenContent(
    lazyItems: LazyPagingItems<Nife>,
    viewModel: MarkerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onItemClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding()
        ),
    ) {
        // TODO: a visualization schedule

        val currentFilter by viewModel.currentFilter.collectAsState()
        ScheduleListFilter(
            currentFilter = currentFilter,
            onFilterChanged = { viewModel.updateFilter(it) },
            modifier = Modifier
            // .fillMaxWidth()
            // .padding(horizontal = dimensionResource(dimen.padding_small))
        )

        // 加载状态
        when {
            lazyItems.loadState.refresh is LoadState.Loading -> {
                Text("Loading...")
            }

            lazyItems.loadState.append is LoadState.Loading -> {
                Text("Loading more...")
            }

            lazyItems.loadState.refresh is LoadState.Error -> {
                ErrorView { lazyItems.retry() }
            }
        }

        // List
        if (lazyItems.itemCount == 0) {
            Text(
                text = stringResource(string.tip_no_schedule),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            NifeList(
                lazyItems = lazyItems,
                viewModel = viewModel,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun ScheduleListFilter(
    currentFilter: ListFilter,
    onFilterChanged: (ListFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        ListFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = currentFilter == filter,
                onClick = { onFilterChanged(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ListFilter.entries.size
                ),
            ) {
                Text(
                    text = when (filter) {
                        ListFilter.UNFINISHED -> stringResource(string.unfinished)
                        ListFilter.FINISHED -> stringResource(string.finished)
                        ListFilter.ALL -> stringResource(string.all)
                    }
                )
            }
        }
    }
}

@Composable
private fun NifeList(
    lazyItems: LazyPagingItems<Nife>,
    viewModel: MarkerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onItemClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(dimen.padding_small)),
    ) {

        items(
            count = lazyItems.itemCount,
            key = { index -> lazyItems[index]?.id ?: index }) { index ->
            lazyItems[index]?.let { nife ->
                NifeItem(
                    item = nife,
                    onCheck = {
                        coroutineScope.launch {
                            viewModel.updateNife(
                                it.copy(
                                    isFinished = !it.isFinished,
                                    endDT = if (!it.isFinished) LocalDateTime.now() else null
                                )
                            )
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            viewModel.deleteNife(nife)
                        }
                    },
                    modifier = Modifier
                        // set padding between items
                        .padding(dimensionResource(dimen.padding_tiny))
                        .clickable { onItemClick(nife.id) }
                )
            }
        }


    }
}




@Composable
private fun NifeItem(
    item: Nife,
    onDelete: (Nife) -> Unit = {},
    onCheck: (Nife) -> Unit = {},
    modifier: Modifier = Modifier
) {
    SwipeableListItem(
        onRightAction = { onDelete(item) },
        rightActionContent = {
            IconButton(onClick = { onDelete(item) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    ) {
        NifeCard(
            item = item,
            onCheck = { onCheck(item) },
            modifier = modifier
        )
    }
}


@Composable
private fun NifeCard(
    item: Nife,
    onCheck: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(dimen.padding_small))
            ) {
                // title
                Text(
                    text = item.title,
                    maxLines = 3,   // 限制最大行数
                    overflow = TextOverflow.Ellipsis, // 超出部分显示省略号
                    style = MaterialTheme.typography.titleLarge
                )
                // beginDT or endDT
                if (item.beginDT != null || item.endDT != null) {
                    NifeDTText(
                        nife = item,
                        ifRenderOutdated = item.type == NifeType.REMINDER && !item.isFinished
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // type text
                Text(
                    text = item.type.name,
                    style = MaterialTheme.typography.titleMedium
                )
                // isFinished checkbox
                Checkbox(
                    checked = item.isFinished,
                    onCheckedChange = { onCheck(it) }
                )
            }
        }
    }
}

@Composable
fun NifeDTText(
    nife: Nife,
    placeholderStr: String = "",
    ifRenderOutdated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        if (nife.beginDT == null && nife.endDT == null) {
            append(placeholderStr)
        } else {
            if (nife.beginDT != null) {
                if (nife.beginDT < LocalDateTime.now() && ifRenderOutdated) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(nife.beginDT.toFriendlyString())
                    }
                } else {
                    append(nife.beginDT.toFriendlyString())
                }
            } else {
                append("?")
            }
            if (nife.endDT != null && nife.endDT != nife.beginDT) {
                append(" >>> ")
                append(nife.endDT.toFriendlyString())
            }
        }
    }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MarkerScreen()
}
