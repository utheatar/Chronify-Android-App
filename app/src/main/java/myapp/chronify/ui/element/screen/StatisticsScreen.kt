package myapp.chronify.ui.element.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.first
import myapp.chronify.R.dimen
import myapp.chronify.R.string
import myapp.chronify.data.nife.Nife
import myapp.chronify.ui.element.ScrollableEventCalendar
import myapp.chronify.ui.element.components.AppTopBar
import myapp.chronify.ui.element.components.LazyPagingView
import myapp.chronify.ui.element.components.OutLinedTextFieldWithSuggestion
import myapp.chronify.ui.navigation.NavigationRoute
import myapp.chronify.ui.viewmodel.AppViewModelProvider
import myapp.chronify.ui.viewmodel.StatisticsViewModel
import java.time.LocalDate

object StatisticsScreenRoute : NavigationRoute {
    override val route = "statistics"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToEdit: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val lazyItems = viewModel.nifesPagingData.collectAsLazyPagingItems()
    // val dateEventMap by viewModel.dateEventMap.collectAsState()
    var isCalendarView by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = stringResource(string.statistics),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        StatisticsContent(
            uiState = uiState,
            lazyItems = lazyItems,
            convertToMap = { viewModel.convertToDateEventMap(it) },
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            navigateToEdit = navigateToEdit,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        )
    }
}

@Composable
fun StatisticsContent(
    uiState: StatisticsViewModel.StatisticsUiState,
    lazyItems: LazyPagingItems<Nife>,
    convertToMap: (lazyItems: LazyPagingItems<Nife>) -> Map<LocalDate, List<Nife>>,
    onSearchQueryChange: (String) -> Unit,
    navigateToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = modifier
            .padding(contentPadding)
            .padding(dimensionResource(dimen.padding_small))
            .fillMaxSize()
    ) {
        // 搜索框
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutLinedTextFieldWithSuggestion(
                initialValue = uiState.searchQuery,
                suggestions = uiState.suggestions,
                onValueChange = {
                    onSearchQueryChange(it)
                },
                label = { Text(stringResource(string.search_schedule)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            // 视图切换按钮
            // IconButton(onClick = { viewModel.toggleViewMode() }) {
            //     Icon(
            //         imageVector =
            //         if (uiState.isCalendarView)
            //             Icons.Default.ShoppingCart
            //         else Icons.Default.DateRange,
            //         contentDescription = stringResource(string.toggle_statistics)
            //     )
            // }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyPagingView(
            lazyItems = lazyItems,
            modifier = Modifier.fillMaxWidth()
        ) {
            ScrollableEventCalendar(
                markers = convertToMap(lazyItems),
                onMenuItemClick = { nife -> navigateToEdit(nife.id) },
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    StatisticsScreen()
}
