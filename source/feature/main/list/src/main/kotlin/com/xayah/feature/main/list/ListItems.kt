package com.xayah.feature.main.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.model.OpType
import com.xayah.core.model.database.PackageEntity
import com.xayah.core.ui.R
import com.xayah.core.ui.component.BodyMediumText
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.PackageIconImage
import com.xayah.core.ui.component.Surface
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.theme.ThemedColorSchemeKeyTokens
import com.xayah.core.ui.theme.value
import com.xayah.core.ui.token.AnimationTokens
import com.xayah.core.ui.token.SizeTokens

@Composable
fun ListItems(
    viewModel: ListItemsViewModel = hiltViewModel(),
    scrollState: LazyListState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
    ) {
        item(key = "-1") {
            Spacer(modifier = Modifier.size(SizeTokens.Level1))
        }

        listItems(uiState = uiState, viewModel = viewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.listItems(
    uiState: ListItemsUiState,
    viewModel: ListItemsViewModel,
) {
    when (uiState) {
        is ListItemsUiState.Success.Apps -> {
            items(items = uiState.appList, key = { it.id }) { item ->
                Row(modifier = Modifier.animateItemPlacement()) {
                    AppItem(
                        opType = uiState.opType,
                        id = item.id,
                        packageName = item.packageName,
                        label = item.label,
                        flag = item.selectionFlag,
                        selected = item.selected,
                        onClick = {},
                        onChangeFlag = viewModel::onChangeFlag,
                        onSelectedChanged = viewModel::onSelectedChanged
                    )
                }
            }
        }

        is ListItemsUiState.Success.Files -> {
            items(items = uiState.fileList, key = { it.id }) { item ->
                Row(modifier = Modifier.animateItemPlacement()) {
                    FileItem(
                        id = item.id,
                        name = item.name,
                        path = item.path,
                        selected = item.selected,
                        onClick = {},
                        onSelectedChanged = viewModel::onSelectedChanged
                    )
                }
            }
        }

        else -> {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    opType: OpType,
    id: Long,
    packageName: String,
    label: String,
    flag: Int,
    selected: Boolean,
    onChangeFlag: (Long, Int) -> Unit,
    onSelectedChanged: (Long, Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(SizeTokens.Level16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level16)
        ) {
            PackageIconImage(packageName = packageName, size = SizeTokens.Level32, fromLocal = opType == OpType.RESTORE)

            Column(modifier = Modifier.weight(1f)) {
                TitleLargeText(text = label.ifEmpty { stringResource(id = R.string.unknown) }, maxLines = 1)
                BodyMediumText(text = packageName, color = ThemedColorSchemeKeyTokens.Outline.value, maxLines = 1)
            }

            AnimatedDataIndicator(flag) {
                onChangeFlag(id, flag)
            }
            VerticalDivider(
                modifier = Modifier.height(SizeTokens.Level32)
            )
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelectedChanged(id, selected.not()) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    id: Long,
    name: String,
    path: String,
    selected: Boolean,
    onSelectedChanged: (Long, Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(SizeTokens.Level16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level16)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TitleLargeText(text = name.ifEmpty { stringResource(id = R.string.unknown) }, maxLines = 1)
                BodyMediumText(text = path, color = ThemedColorSchemeKeyTokens.Outline.value, maxLines = 1)
            }

            VerticalDivider(
                modifier = Modifier.height(SizeTokens.Level32)
            )
            Checkbox(
                checked = selected,
                onCheckedChange = { onSelectedChanged(id, selected.not()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedDataIndicator(
    flag: Int,
    onClick: (Int) -> Unit,
) {
    AnimatedContent(targetState = flag, label = AnimationTokens.AnimatedContentLabel) { f ->
        val tooltipState = rememberTooltipState()

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(
                        text = when (f) {
                            PackageEntity.FLAG_NONE -> stringResource(id = R.string.no_item_selected)
                            PackageEntity.FLAG_APK -> stringResource(id = R.string.apk_selected)
                            PackageEntity.FLAG_DATA -> stringResource(id = R.string.data_selected)
                            PackageEntity.FLAG_ALL -> stringResource(id = R.string.all_selected)
                            else -> stringResource(id = R.string.custom_selected)
                        },
                    )
                }
            },
            state = tooltipState
        ) {
            IconButton(
                icon = when (f) {
                    PackageEntity.FLAG_NONE -> ImageVector.vectorResource(id = R.drawable.ic_rounded_cancel_circle)
                    PackageEntity.FLAG_APK -> ImageVector.vectorResource(id = R.drawable.ic_rounded_android_circle)
                    PackageEntity.FLAG_DATA -> ImageVector.vectorResource(id = R.drawable.ic_rounded_database_circle)
                    PackageEntity.FLAG_ALL -> ImageVector.vectorResource(id = R.drawable.ic_rounded_check_circle)
                    else -> ImageVector.vectorResource(id = R.drawable.ic_rounded_package_2_circle)
                },
                tint = when (f) {
                    PackageEntity.FLAG_NONE -> ThemedColorSchemeKeyTokens.Error.value
                    PackageEntity.FLAG_ALL -> ThemedColorSchemeKeyTokens.GreenPrimary.value
                    else -> ThemedColorSchemeKeyTokens.YellowPrimary.value
                },
            ) {
                onClick.invoke(f)
            }
        }
    }
}