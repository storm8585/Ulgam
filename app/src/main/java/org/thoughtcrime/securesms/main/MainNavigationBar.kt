/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.theme.SignalTheme
import org.signal.core.ui.R as CoreUiR
import org.thoughtcrime.securesms.R

private val NAV_ICON_SIZE = 24.dp

/**
 * Chats list bottom floating capsule navigation bar.
 */
@Composable
fun MainNavigationBar(
  state: MainNavigationBarState,
  onDestinationSelected: (MainListRoute) -> Unit
) {
  val isDark = isSystemInDarkTheme()
  val selectedColor = Color.White
  val unselectedColor = if (isDark) Color(0xFF8E9792) else Color(0xFF6B7770)
  val pillBgColor = Color.White.copy(alpha = if (isDark) 0.16f else 0.18f)

  val selectedIndex = remember(state.destinations, state.currentListLocation) {
    val idx = state.destinations.indexOf(state.currentListLocation)
    if (idx >= 0) idx else 0
  }

  var sourceLocation by remember { mutableStateOf(state.currentListLocation) }
  val sourceIndex = remember(state.destinations, sourceLocation) {
    val idx = state.destinations.indexOf(sourceLocation)
    if (idx >= 0) idx else 0
  }

  val tabCount = if (state.destinations.isNotEmpty()) state.destinations.size else 4
  val tabWidth = 336.dp / tabCount
  val targetOffset = tabWidth * selectedIndex
  val sourceOffset = tabWidth * sourceIndex
  val threshold = tabWidth.value * 0.45f

  val animatedOffsetX by animateDpAsState(
    targetValue = targetOffset,
    animationSpec = tween(
      durationMillis = 280,
      easing = FastOutSlowInEasing
    ),
    label = "appleFluidIndicatorOffset",
    finishedListener = {
      sourceLocation = state.currentListLocation
    }
  )

  val isArrivedAtTarget = kotlin.math.abs(animatedOffsetX.value - targetOffset.value) < threshold
  val isNearSource = kotlin.math.abs(animatedOffsetX.value - sourceOffset.value) < threshold

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = SignalTheme.colors.colorSurface2,
      shadowElevation = 0.dp,
      tonalElevation = 0.dp,
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
      ),
      modifier = Modifier
        .width(344.dp)
        .height(if (state.compact) 48.dp else 56.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(4.dp)
      ) {
        Box(
          modifier = Modifier
            .width(tabWidth)
            .fillMaxHeight()
            .graphicsLayer {
              translationX = animatedOffsetX.toPx()
            }
            .clip(RoundedCornerShape(24.dp))
            .background(pillBgColor)
        )

        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          state.destinations.forEach { destination ->
            val badgeCount = when (destination) {
              MainListRoute.Archive -> error("Not supported")
              MainListRoute.Chats -> state.chatsCount
              MainListRoute.Calls -> state.callsCount
              MainListRoute.Stories -> state.storiesCount
              MainListRoute.Settings -> 0
            }

            val isTarget = state.currentListLocation == destination
            val isSource = sourceLocation == destination
            val selected = if (isTarget) {
              isArrivedAtTarget
            } else if (isSource) {
              isNearSource
            } else {
              false
            }

            NavigationBarTabItem(
              destination = destination,
              selected = selected,
              badgeCount = badgeCount,
              compact = state.compact,
              selectedColor = selectedColor,
              unselectedColor = unselectedColor,
              onClick = { onDestinationSelected(destination) },
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NavigationBarTabItem(
  destination: MainListRoute,
  selected: Boolean,
  badgeCount: Int,
  compact: Boolean,
  selectedColor: Color,
  unselectedColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeContentColor by animateColorAsState(
    targetValue = if (selected) selectedColor else unselectedColor,
    animationSpec = tween(durationMillis = 140),
    label = "tabContentColor"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .drawNavigationBarBadge(count = badgeCount, compact = compact),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
      NavigationDestinationIcon(
        destination = destination,
        selected = selected,
        tint = activeContentColor
      )

      if (!compact) {
        Spacer(modifier = Modifier.height(1.dp))
        Text(
          text = stringResource(destination.label),
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
          ),
          color = activeContentColor,
          maxLines = 1
        )
      }
    }
  }
}

/**
 * Draws badge over navigation bar item. We do this since they're required to be inside a row,
 * and things get really funky or clip weird if we try to use a normal composable.
 */
@Composable
private fun Modifier.drawNavigationBarBadge(count: Int, compact: Boolean): Modifier {
  return if (count <= 0) {
    this
  } else {
    val formatted = formatCount(count)
    val textMeasurer = rememberTextMeasurer()
    val color = colorResource(R.color.ConversationListTabs__unread)
    val textStyle = MaterialTheme.typography.labelMedium
    val textLayoutResult = remember(formatted) {
      textMeasurer.measure(formatted, textStyle)
    }

    var size by remember { mutableStateOf(IntSize.Zero) }

    val padding = with(LocalDensity.current) {
      4.dp.toPx()
    }

    val xOffsetExtra = with(LocalDensity.current) {
      2.dp.toPx()
    }

    val yOffset = with(LocalDensity.current) {
      2.dp.toPx()
    }

    this
      .onSizeChanged {
        size = it
      }
      .drawWithContent {
        drawContent()

        val xOffset = size.width.toFloat() / 2f + xOffsetExtra
        val yRadius = size.height.toFloat() / 2f

        if (size != IntSize.Zero) {
          drawRoundRect(
            color = color,
            topLeft = Offset(xOffset, yOffset),
            size = Size(textLayoutResult.size.width.toFloat() + padding * 2, textLayoutResult.size.height.toFloat()),
            cornerRadius = CornerRadius(yRadius, yRadius)
          )

          drawText(
            textLayoutResult = textLayoutResult,
            color = Color.White,
            topLeft = Offset(xOffset + padding, yOffset)
          )
        }
      }
  }
}

/**
 * Navigation Rail for medium and large form factor devices.
 */
@Composable
fun MainNavigationRail(
  state: MainNavigationBarState,
  mainFloatingActionButtonsCallback: MainFloatingActionButtonsCallback,
  onDestinationSelected: (MainListRoute) -> Unit
) {
  NavigationRail(
    containerColor = SignalTheme.colors.colorSurface1
  ) {
    Spacer(modifier = Modifier.height(40.dp).weight(1f, fill = false))

    MainFloatingActionButtons(
      destination = state.currentListLocation,
      callback = mainFloatingActionButtonsCallback,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(40.dp).weight(1f, fill = false))

    val selectedDestination = if (state.currentListLocation == MainListRoute.Archive) {
      MainListRoute.Chats
    } else {
      state.currentListLocation
    }

    state.destinations.forEachIndexed { idx, destination ->
      val selected = selectedDestination == destination

      Box {
        NavigationRailItem(
          modifier = Modifier.padding(bottom = if (state.destinations.lastIndex == idx) 0.dp else 16.dp),
          icon = {
            NavigationDestinationIcon(
              destination = destination,
              selected = selected
            )
          },
          label = {
            NavigationDestinationLabel(destination)
          },
          selected = selected,
          onClick = {
            onDestinationSelected(destination)
          }
        )

        NavigationRailCountIndicator(
          state = state,
          destination = destination
        )
      }
    }
  }
}

@Composable
private fun BoxScope.NavigationRailCountIndicator(
  state: MainNavigationBarState,
  destination: MainListRoute
) {
  val count = remember(state, destination) {
    when (destination) {
      MainListRoute.Archive -> error("Not supported")
      MainListRoute.Chats -> state.chatsCount
      MainListRoute.Calls -> state.callsCount
      MainListRoute.Stories -> state.storiesCount
      MainListRoute.Settings -> 0
    }
  }

  if (count > 0) {
    Box(
      modifier = Modifier
        .padding(start = 42.dp)
        .height(16.dp)
        .defaultMinSize(minWidth = 16.dp)
        .background(color = colorResource(R.color.ConversationListTabs__unread), shape = RoundedCornerShape(percent = 50))
        .align(Alignment.TopStart)
    ) {
      Text(
        text = formatCount(count),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 4.dp)
      )
    }
  }
}

@Composable
private fun NavigationDestinationIcon(
  destination: MainListRoute,
  selected: Boolean,
  tint: Color = MaterialTheme.colorScheme.onSurface
) {
  val iconRes = when (destination) {
    MainListRoute.Chats, MainListRoute.Archive -> if (selected) R.drawable.symbol_chat_fill_24 else R.drawable.symbol_chat_24
    MainListRoute.Calls -> if (selected) R.drawable.symbol_phone_fill_white_24 else R.drawable.symbol_phone_outline_24
    MainListRoute.Stories -> if (selected) R.drawable.symbol_stories_fill_24 else R.drawable.symbol_stories_24
    MainListRoute.Settings -> CoreUiR.drawable.symbol_settings_android_24
  }

  Icon(
    painter = painterResource(id = iconRes),
    contentDescription = stringResource(destination.contentDescription),
    tint = tint,
    modifier = Modifier.size(NAV_ICON_SIZE)
  )
}

@Composable
private fun NavigationDestinationLabel(destination: MainListRoute) {
  Text(stringResource(destination.label))
}

@Composable
private fun formatCount(count: Int): String {
  if (count > 99) {
    return stringResource(R.string.ConversationListTabs__99p)
  }
  return count.toString()
}

@DayNightPreviews
@Preview(device = "spec:parent=pixel_7,orientation=landscape")
@Composable
private fun MainNavigationRailPreview() {
  Previews.Preview {
    var selected by remember { mutableStateOf(MainListRoute.Chats) }

    MainNavigationRail(
      state = MainNavigationBarState(
        chatsCount = 500,
        callsCount = 10,
        storiesCount = 5,
        currentListLocation = selected
      ),
      mainFloatingActionButtonsCallback = MainFloatingActionButtonsCallback.Empty,
      onDestinationSelected = { selected = it }
    )
  }
}

@DayNightPreviews
@Composable
private fun MainNavigationBarPreview() {
  Previews.Preview {
    var selected by remember { mutableStateOf(MainListRoute.Chats) }

    MainNavigationBar(
      state = MainNavigationBarState(
        chatsCount = 500,
        callsCount = 10,
        storiesCount = 5,
        currentListLocation = selected,
        compact = false
      ),
      onDestinationSelected = { selected = it }
    )
  }
}
