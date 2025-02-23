package myapp.chronify.ui.element.exp

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import myapp.chronify.ui.element.components.SwipeAnchorValue
import kotlin.math.roundToInt


enum class SwipeToRevealValue { Read, Resting, Delete }

enum class SwipeableBoxAnchor {
    OverStartToEnd,
    StartToEnd,
    Settled,
    EndToStart,
    OverEndToStart
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSwipeableBoxState(
    actionOffsetDp: Dp = 60.dp,
    overActionOffsetDp: Dp = actionOffsetDp * 4,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay<Float>()
): AnchoredDraggableState<SwipeableBoxAnchor>{

    val density = LocalDensity.current
    val actionOffset = with(density) { actionOffsetDp.toPx() }
    val overActionOffset = with(density) { overActionOffsetDp.toPx() }
    val velocityThreshold = with(density) { 125.dp.toPx() }

    return remember {
        AnchoredDraggableState(
            initialValue = SwipeableBoxAnchor.Settled,
            anchors = DraggableAnchors {
                if (enableDismissFromStartToEnd) {
                    SwipeableBoxAnchor.OverStartToEnd at -overActionOffset
                    SwipeableBoxAnchor.StartToEnd at -actionOffset
                }
                SwipeableBoxAnchor.Settled at 0f
                if (enableDismissFromEndToStart) {
                    SwipeableBoxAnchor.EndToStart at actionOffset
                    SwipeableBoxAnchor.OverEndToStart at overActionOffset
                }
            },
            velocityThreshold = { velocityThreshold },
            positionalThreshold = { distance -> distance * 0.5f },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec,
        )
    }
}

@Preview
@Composable
fun SwipeToDismissBoxPreview(
    backgroundContent: @Composable () -> Unit = { },
    content: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { targetValue ->
            when (targetValue) {
                SwipeToDismissBoxValue.Settled -> true
                SwipeToDismissBoxValue.StartToEnd -> false
                SwipeToDismissBoxValue.EndToStart -> false
            }
        },
        positionalThreshold = { totalDistance ->
            totalDistance * 0.5f
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Green
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
            )
        }) {
        OutlinedCard(shape = RectangleShape) {
            ListItem(
                headlineContent = { Text("Cupcake") },
                supportingContent = { Text("Swipe me left or right!") })
        }
    }

    when (dismissState.currentValue) {
        SwipeToDismissBoxValue.Settled ->
            LaunchedEffect(dismissState) {
                Log.d("SwipeToDismissBoxPreview", "Settled")
                // dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }

        SwipeToDismissBoxValue.StartToEnd ->
            LaunchedEffect(dismissState) {
                Log.d("SwipeToDismissBoxPreview", "StartToEnd")
                // dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }

        SwipeToDismissBoxValue.EndToStart ->
            LaunchedEffect(dismissState) {
                Log.d("SwipeToDismissBoxPreview", "EndToStart")
                // dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableBox(
    dragState: AnchoredDraggableState<SwipeableBoxAnchor>,
    overStartToEndAction: () -> Unit = { },
    overEndToStartAction: () -> Unit = { },
    backgroundContent: @Composable () -> Unit = { },
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {

    Box(modifier = Modifier.fillMaxWidth()) {
        val color by animateColorAsState(
            when (dragState.targetValue) {
                SwipeableBoxAnchor.StartToEnd -> Color.Green.copy(alpha = .5f)
                SwipeableBoxAnchor.OverStartToEnd -> Color.Green
                SwipeableBoxAnchor.Settled -> Color.LightGray
                SwipeableBoxAnchor.OverEndToStart -> Color.Red
                SwipeableBoxAnchor.EndToStart -> Color.Red.copy(alpha = .5f)
            }
        )
        // Background content that doesn't move with the swipe
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .background(color)
        ) { backgroundContent() }

        // Main content that moves with the swipe
        Box(
            modifier = modifier
                .anchoredDraggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                )
                .offset {
                    IntOffset(
                        x = dragState.requireOffset().roundToInt(),
                        y = 0
                    )
                },
            propagateMinConstraints = true
        ) {
            content()
        }
    }

    LaunchedEffect(dragState) {
        snapshotFlow { dragState.settledValue }.collectLatest {
            when (it) {
                SwipeableBoxAnchor.OverStartToEnd -> {
                    overStartToEndAction()
                    delay(30)
                    dragState.animateTo(SwipeableBoxAnchor.Settled)
                }

                SwipeableBoxAnchor.OverEndToStart -> {
                    overEndToStartAction()
                    delay(30)
                    dragState.animateTo(SwipeableBoxAnchor.Settled)
                }

                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun SwipeableBoxPreview() {
    val dragState = rememberSwipeableBoxState(
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    )
    val coroutineScope = rememberCoroutineScope()
    SwipeableBox(
        dragState = dragState,
        backgroundContent = {
        },
        content = {
            OutlinedCard(shape = RectangleShape) {
                ListItem(
                    headlineContent = { Text("Cupcake") },
                    supportingContent = { Text("Swipe me left or right!") })
            }
        }
    )
}


@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryBook3(
    onClickRead: () -> Unit = { Log.d("LibraryBook3", "onClickRead") },
    onClickDelete: () -> Unit = { Log.d("LibraryBook3", "onClickDelete") },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val dragState = remember {
        // define the drag offset for the actions
        val actionOffset = with(density) { 100.dp.toPx() }
        // normally, just need to adjust anchors to the action offset
        AnchoredDraggableState(
            initialValue = SwipeToRevealValue.Resting,
            anchors = DraggableAnchors {
                SwipeToRevealValue.Delete at actionOffset
                SwipeToRevealValue.Resting at 0f
                // ScheduleItemSwipeAnchorValue.Read at -actionOffset
            },
            positionalThreshold = { distance -> distance * 1.0f },
            velocityThreshold = { with(density) { 50.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec,
        )
    }

    // show a bit overscroll ui effect
    // val overScrollEffect = ScrollableDefaults.overscrollEffect()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content that moves with the swipe
        Box(
            modifier = Modifier
                .anchoredDraggable(
                    dragState,
                    Orientation.Horizontal,
                    // overscrollEffect = overScrollEffect
                )
                // .overscroll(overScrollEffect)
                .offset {
                    IntOffset(
                        x = dragState.requireOffset().roundToInt(),
                        y = 0
                    )
                }
                .background(Color.Gray)
                .matchParentSize()
        ) {
            Text("Center")
        }

        // actions container
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Action
            AnimatedVisibility(
                visible = dragState.currentValue == SwipeToRevealValue.Delete,
                enter = slideInHorizontally(animationSpec = tween()) { -it },
                exit = slideOutHorizontally(animationSpec = tween()) { -it }
            ) {
                IconButton(onClick = onClickDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Read Action
            AnimatedVisibility(
                visible = dragState.currentValue == SwipeToRevealValue.Read,
                enter = slideInHorizontally(animationSpec = tween()) { it },
                exit = slideOutHorizontally(animationSpec = tween()) { it }
            ) {
                IconButton(onClick = onClickRead) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Read"
                    )
                }
            }


        }
    }

    // Side effect to launch the animation when the drag state changes
    // LaunchedEffect(dragState) {
    //     snapshotFlow { dragState.settledValue }
    //         .collectLatest {
    //             when (it) {
    //                 SwipeToRevealValue.Read -> onClickRead()
    //                 SwipeToRevealValue.Delete -> onClickDelete()
    //                 else -> {}
    //             }
    //             delay(30)
    //             dragState.animateTo(SwipeToRevealValue.Resting)
    //         }
    // }
}