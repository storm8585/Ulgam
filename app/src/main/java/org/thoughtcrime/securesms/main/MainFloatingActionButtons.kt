/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.signal.core.ui.NavigationType
import org.signal.core.ui.compose.DayNightPreviews
import org.signal.core.ui.compose.Previews
import org.signal.core.ui.compose.SignalIcons
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R
import kotlin.math.roundToInt
import org.signal.core.ui.R as CoreUiR

private val ACTION_BUTTON_SIZE = 56.dp
private val ACTION_BUTTON_SPACING = 16.dp

interface MainFloatingActionButtonsCallback {
  fun onNewChatClick()
  fun onNewCallClick()
  fun onCameraClick(destination: MainListRoute)

  object Empty : MainFloatingActionButtonsCallback {
    override fun onNewChatClick() = Unit
    override fun onNewCallClick() = Unit
    override fun onCameraClick(destination: MainListRoute) = Unit
  }
}

@Composable
fun MainFloatingActionButtons(
  destination: MainListRoute,
  callback: MainFloatingActionButtonsCallback,
  modifier: Modifier = Modifier,
  navigationType: NavigationType = NavigationType.rememberNavigationType()
) {
  if (navigationType != NavigationType.RAIL) {
    return
  }

  val shadowElevation: Dp = remember(navigationType) {
    when (navigationType) {
      NavigationType.RAIL -> 0.dp
      NavigationType.BAR -> 4.dp
    }
  }

  Box(
    modifier = modifier.padding(ACTION_BUTTON_SPACING)
  ) {
    PrimaryActionButton(
      destination = destination,
      onNewChatClick = callback::onNewChatClick,
      onCameraClick = callback::onCameraClick,
      onNewCallClick = callback::onNewCallClick,
      elevation = shadowElevation
    )
  }
}

@Composable
private fun PrimaryActionButton(
  destination: MainListRoute,
  elevation: Dp,
  onNewChatClick: () -> Unit = {},
  onCameraClick: (MainListRoute) -> Unit = {},
  onNewCallClick: () -> Unit = {}
) {
  val onClick = remember(destination) {
    when (destination) {
      MainListRoute.Archive -> onNewChatClick
      MainListRoute.Chats -> onNewChatClick
      MainListRoute.Calls -> onNewCallClick
      MainListRoute.Stories -> {
        { onCameraClick(destination) }
      }
      MainListRoute.Settings -> ({})
    }
  }

  val isDark = isSystemInDarkTheme()
  val containerColor = if (isDark) Color(0xFF388E68) else Color(0xFF2D6A4F)

  MainFloatingActionButton(
    onClick = onClick,
    shadowElevation = elevation,
    colors = IconButtonDefaults.filledIconButtonColors(
      containerColor = containerColor,
      contentColor = Color.White
    ),
    icon = {
      AnimatedContent(destination) { targetState ->
        val (icon, contentDescriptionId) = when (targetState) {
          MainListRoute.Archive -> CoreUiR.drawable.symbol_edit_24 to R.string.conversation_list_fragment__fab_content_description
          MainListRoute.Chats -> CoreUiR.drawable.symbol_edit_24 to R.string.conversation_list_fragment__fab_content_description
          MainListRoute.Calls -> R.drawable.symbol_phone_plus_24 to R.string.CallLogFragment__start_a_new_call
          MainListRoute.Stories -> CoreUiR.drawable.symbol_camera_24 to R.string.conversation_list_fragment__open_camera_description
          MainListRoute.Settings -> CoreUiR.drawable.symbol_settings_android_24 to R.string.text_secure_normal__menu_settings
        }

        Icon(
          imageVector = ImageVector.vectorResource(icon),
          contentDescription = stringResource(contentDescriptionId),
          tint = Color.White
        )
      }
    }
  )
}

@Composable
private fun MainFloatingActionButton(
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  shadowElevation: Dp = 4.dp,
  colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors()
) {
  FilledTonalIconButton(
    onClick = onClick,
    shape = CircleShape,
    modifier = modifier
      .size(ACTION_BUTTON_SIZE)
      .shadow(shadowElevation, CircleShape),
    enabled = true,
    colors = colors
  ) {
    icon()
  }
}

@DayNightPreviews
@Composable
private fun MainFloatingActionButtonsNavigationRailPreview() {
  var currentDestination by remember { mutableStateOf(MainListRoute.Chats) }
  val callback = remember {
    object : MainFloatingActionButtonsCallback {
      override fun onCameraClick(destination: MainListRoute) {
        currentDestination = MainListRoute.Calls
      }

      override fun onNewChatClick() {
        currentDestination = MainListRoute.Stories
      }

      override fun onNewCallClick() {
        currentDestination = MainListRoute.Chats
      }
    }
  }

  Previews.Preview {
    MainFloatingActionButtons(
      destination = currentDestination,
      callback = callback,
      navigationType = NavigationType.RAIL
    )
  }
}

@DayNightPreviews
@Composable
private fun MainFloatingActionButtonsNavigationBarPreview() {
  var currentDestination by remember { mutableStateOf(MainListRoute.Chats) }
  val callback = remember {
    object : MainFloatingActionButtonsCallback {
      override fun onCameraClick(destination: MainListRoute) {
        currentDestination = MainListRoute.Calls
      }

      override fun onNewChatClick() {
        currentDestination = MainListRoute.Stories
      }

      override fun onNewCallClick() {
        currentDestination = MainListRoute.Chats
      }
    }
  }

  Previews.Preview {
    MainFloatingActionButtons(
      destination = currentDestination,
      callback = callback,
      navigationType = NavigationType.BAR
    )
  }
}
