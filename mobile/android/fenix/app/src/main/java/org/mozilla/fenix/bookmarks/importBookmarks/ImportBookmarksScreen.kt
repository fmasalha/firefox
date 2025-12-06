package org.mozilla.fenix.bookmarks.importBookmarks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mozilla.components.ui.icons.R as iconsR
import org.mozilla.fenix.theme.FirefoxTheme

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// This file does nothing right now
// Added and realized its too much for a PoC right now.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

@Composable
internal fun ImportBookmarksScreen(
    onPickRequested: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onPickRequested()
    }

    Scaffold(
        topBar = {
            ImportBookmarksTopBar(onBack = onBack)
        },

        ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportBookmarksTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Import Bookmarks",
                style = FirefoxTheme.typography.headline5,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(iconsR.drawable.mozac_ic_back_24),
                    contentDescription = "tttttttttttttttttttttttttttttttttttttttttttt",
                )
            }
        },
        windowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp,
        ),
    )
}
