package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.data.model.MembershipPlan
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun MembershipScreen(
    hazeState: HazeState,
    onBack: () -> Unit,
    viewModel: MembershipViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(Unit) {
        viewModel.message.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LaishengLoading(strokeWidth = 2.dp)
                }

                uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        contentPadding = PaddingValues(
                            top = 104.dp,
                            bottom = paddingValues.calculateBottomPadding() + 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            MembershipStatusCard(
                                level = uiState.status?.level ?: "free",
                                expiresAt = uiState.status?.expiresAt,
                                folderLimit = uiState.status?.folderLimit ?: 10,
                                historyRetentionDays = uiState.status?.historyRetentionDays ?: 30,
                                maxPageSize = uiState.status?.maxPageSize ?: 20
                            )
                        }

                        item {
                            Text(
                                text = "会员套餐",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        items(uiState.plans) { plan ->
                            MembershipPlanCard(
                                plan = plan,
                                isLoading = uiState.activatingPlanCode == plan.code,
                                onActivate = { viewModel.activate(plan.code) }
                            )
                        }

                        if (uiState.orders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "开通记录",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            items(uiState.orders) { order ->
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                                    )
                                ) {
                                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(order.planLabel ?: order.planCode ?: "会员订单", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "状态 ${order.status ?: "--"}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        order.createdAt?.let {
                                            Text(
                                                text = "创建于 $it",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SimpleTopBar(hazeState = hazeState, title = "会员中心", onBack = onBack)
        }
    }
}

@Composable
private fun MembershipStatusCard(
    level: String,
    expiresAt: String?,
    folderLimit: Int,
    historyRetentionDays: Int,
    maxPageSize: Int
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("当前等级 ${level.uppercase()}", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text(
                text = expiresAt?.let { "有效期至 $it" } ?: "当前为免费用户，可直接调用开通接口升级。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("收藏夹上限 $folderLimit")
            Text("浏览记录保留 $historyRetentionDays 天")
            Text("单次分页上限 $maxPageSize")
        }
    }
}

@Composable
private fun MembershipPlanCard(
    plan: MembershipPlan,
    isLoading: Boolean,
    onActivate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plan.label, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        text = "代码 ${plan.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("¥${plan.amount}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            Text("收藏夹 ${plan.folderLimit} / 浏览 ${plan.historyRetentionDays} 天 / 分页 ${plan.maxPageSize}")
            Button(onClick = onActivate, enabled = !isLoading) {
                Text(if (isLoading) "提交中" else "立即开通")
            }
        }
    }
}

@Composable
private fun SimpleTopBar(
    hazeState: HazeState,
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                Box(modifier = Modifier.clickable(onClick = onBack).padding(10.dp)) {
                    AppIcon(glyph = AppIcons.ArrowLeft, size = 20.dp)
                }
            }
            Text(
                text = title,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
