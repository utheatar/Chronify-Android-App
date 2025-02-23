package myapp.chronify.ui.element.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.overscroll
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt


enum class SwipeAnchorValue {
    Resting,
    LeftAction,
    RightAction
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableListItem(
    onLeftAction: () -> Unit = {},
    onRightAction: () -> Unit = {},
    leftActionContent: @Composable () -> Unit = {},
    rightActionContent: @Composable () -> Unit = {},
    overscrollAutoAct: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    val dragState = remember {
        val actionOffset = with(density) { 70.dp.toPx() }
        AnchoredDraggableState(
            initialValue = SwipeAnchorValue.Resting,
            anchors = DraggableAnchors {
                SwipeAnchorValue.Resting at 0f
                SwipeAnchorValue.LeftAction at actionOffset
                SwipeAnchorValue.RightAction at -actionOffset
            },
            positionalThreshold = { distance -> distance * 0.25f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = decayAnimationSpec,
        )
    }

    val overScrollEffect = ScrollableDefaults.overscrollEffect()

    Box(modifier = Modifier.fillMaxWidth()) {
        // 主内容
        Box(
            modifier = modifier
                .anchoredDraggable(
                    dragState,
                    Orientation.Horizontal,
                    overscrollEffect = overScrollEffect
                )
                .overscroll(overScrollEffect)
                .offset {
                    IntOffset(
                        x = dragState.requireOffset().roundToInt(),
                        y = 0
                    )
                }
        ) {
            content()
        }

        // 左右动作
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧动作
            AnimatedVisibility(
                visible = dragState.currentValue == SwipeAnchorValue.LeftAction,
                enter = slideInHorizontally(animationSpec = tween()) { it },
                exit = slideOutHorizontally(animationSpec = tween()) { it }
            ) {
                leftActionContent()
            }

            Spacer(modifier = Modifier.weight(1f))

            // 右侧动作
            AnimatedVisibility(
                visible = dragState.currentValue == SwipeAnchorValue.RightAction,
                enter = slideInHorizontally(animationSpec = tween()) { it },
                exit = slideOutHorizontally(animationSpec = tween()) { it }
            ) {
                rightActionContent()
            }
        }
    }

    if (overscrollAutoAct) {
        LaunchedEffect(dragState) {
            snapshotFlow { dragState.settledValue }
                .collectLatest {
                    when (it) {
                        SwipeAnchorValue.LeftAction -> onLeftAction()
                        SwipeAnchorValue.RightAction -> onRightAction()
                        else -> {}
                    }
                    delay(30)
                    dragState.animateTo(SwipeAnchorValue.Resting)
                }
        }
    }

}