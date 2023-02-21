package com.zacharee1.systemuituner.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zacharee1.systemuituner.R
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

interface IntroPage {
    val canMoveForward: @Composable () -> Boolean
    val slideColor: @Composable () -> Color

    @Composable
    fun Render(modifier: Modifier)
}

open class SimpleIntroPage(
    val title: String,
    val description: String,
    val icon: Painter,
    override val slideColor: @Composable () -> Color,
    override val canMoveForward: @Composable () -> Boolean = { true },
    val scrollable: Boolean = true,
    val extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) : IntroPage {
    @Composable
    override fun Render(modifier: Modifier) {
        Column(
            modifier = modifier.then(if (scrollable) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier
            }),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = description,
            )

            extraContent?.invoke(this)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroSlider(
    pages: List<IntroPage>,
    onExit: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberPagerState()
    val count = pages.size
    val currentPage by remember {
        derivedStateOf { pages[state.currentPage] }
    }
    val canMoveForward = currentPage.canMoveForward()
    val slideColor = currentPage.slideColor()
    val currentColor by animateColorAsState(targetValue = slideColor)
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.currentPage, state.targetPage, canMoveForward) {
        if (state.targetPage > state.currentPage && !canMoveForward) {
            state.animateScrollToPage(state.currentPage)
        }
    }

    Surface(modifier = modifier, color = currentColor) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                pageCount = count,
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                pages[it].Render(modifier = Modifier)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val showAsBack by remember {
                    derivedStateOf { state.currentPage > 0 }
                }
                val showAsNext by remember {
                    derivedStateOf { state.currentPage < count - 1 }
                }

                IconButton(
                    onClick = {
                        if (showAsBack) {
                            scope.launch {
                                state.animateScrollToPage(max(state.currentPage - 1, 0))
                            }
                        } else {
                            onExit()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (showAsBack) Icons.Default.ArrowBack else Icons.Default.Close,
                        contentDescription = stringResource(id = if (showAsBack) R.string.previous else R.string.exit)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalPagerIndicator(
                    pagerState = state,
                    pageCount = count,
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        if (showAsNext) {
                            if (canMoveForward) {
                                scope.launch {
                                    state.animateScrollToPage(min(state.currentPage + 1, count - 1))
                                }
                            }
                        } else {
                            onDone()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (showAsNext) Icons.Default.ArrowForward else Icons.Default.Done,
                        contentDescription = stringResource(id = if (showAsNext) R.string.next else R.string.done)
                    )
                }
            }
        }
    }
}