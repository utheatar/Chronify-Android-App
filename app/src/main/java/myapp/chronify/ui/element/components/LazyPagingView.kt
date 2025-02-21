package myapp.chronify.ui.element.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems


@Composable
fun <T : Any> LazyPagingView(
    lazyItems: LazyPagingItems<T>,
    refreshLoadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
    appendLoadingContent: @Composable () -> Unit = { CircularProgressIndicator() },
    errorContent: @Composable () -> Unit = { ErrorView { lazyItems.retry() } },
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        when {
            lazyItems.loadState.refresh is LoadState.Loading -> refreshLoadingContent()
            lazyItems.loadState.append is LoadState.Loading -> appendLoadingContent()
            lazyItems.loadState.refresh is LoadState.Error -> errorContent()
            else -> {
                content()
            }
        }
    }
}

@Composable
fun ErrorView(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error occurred")
        content()
    }
}