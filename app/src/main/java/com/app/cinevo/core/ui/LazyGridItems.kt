package com.app.cinevo.core.ui

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable

/** An item spanning every column of the grid, such as a header or a status row. */
fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }, content = content)
