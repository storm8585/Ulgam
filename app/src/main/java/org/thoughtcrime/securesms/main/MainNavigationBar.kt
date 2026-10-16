/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R

private val LOTTIE_SIZE = 24.dp

/**
 * Chats list bottom floating capsule navigation bar.
 */
@Composable
fun MainNavigationBar(
  state: MainNavigationBarState,
  onDestinationSelected: (MainListRoute) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(percent = 50),
      color = SignalTheme.colors.colorSurface2,
      shadowElevation = 0.dp,
      tonalElevation = 0.dp,
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
      ),
      modifier = Modifier
        .widthIn(min = 230.dp, max = 280.dp)
        .height(if (state.compact) 46.dp else 56.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        state.destinations.forEach { destination ->
          val badgeCount = when (destination) {
            MainListRoute.Archive -> error("Not supported")
            MainListRoute.Chats -> state.chatsCount
            MainListRoute.Calls -> state.callsCount
            MainListRoute.Stories -> state.storiesCount
          }

          val selected = state.currentListLocation == destination

          val capsuleScaleX by animateFloatAsState(
            targetValue = if (selected) 1f else 0.45f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMediumLow
            ),
            label = "capsuleScaleX"
          )
          val capsuleScaleY by animateFloatAsState(
            targetValue = if (selected) 1f else 0.7f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMediumLow
            ),
            label = "capsuleScaleY"
          )
          val capsuleAlpha by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = tween(durationMillis = 150),
            label = "capsuleAlpha"
          )
          val contentScale by animateFloatAsState(
            targetValue = if (selected) 1f else 0.94f,
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessMediumLow
            ),
            label = "contentScale"
          )
          val activeContentColor by animateColorAsState(
            targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            label = "tabContentColor"
          )

          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                onDestinationSelected(destination)
              }
              .drawNavigationBarBadge(count = badgeCount, compact = state.compact),
            contentAlignment = Alignment.Center
          ) {
            if (capsuleAlpha > 0.005f) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .graphicsLayer {
                    scaleX = capsuleScaleX
                    scaleY = capsuleScaleY
                    alpha = capsuleAlpha
                  }
                  .clip(RoundedCornerShape(percent = 50))
                  .background(MaterialTheme.colorScheme.secondaryContainer)
              )
            }

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                  scaleX = contentScale
                  scaleY = contentScale
                }
                .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
              NavigationDestinationIcon(
                destination = destination,
                selected = selected
              )

              if (!state.compact) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = stringResource(destination.label),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                  ),
                  color = activeContentColor,
                  maxLines = 1
                )
              }
            }
          }
        }
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
  selected: Boolean
) {
  val dynamicProperties = rememberLottieDynamicProperties(
    rememberLottieDynamicProperty(
      property = LottieProperty.COLOR_FILTER,
      value = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
        MaterialTheme.colorScheme.onSurface.hashCode(),
        BlendModeCompat.SRC_ATOP
      ),
      keyPath = arrayOf("**")
    )
  )

  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(destination.icon))
  val progress by animateFloatAsState(targetValue = if (selected) 1f else 0f, animationSpec = tween(durationMillis = composition?.duration?.toInt() ?: 0))

  LottieAnimation(
    composition = composition,
    progress = { if (selected) progress else 0f },
    dynamicProperties = dynamicProperties,
    modifier = Modifier.size(LOTTIE_SIZE)
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
