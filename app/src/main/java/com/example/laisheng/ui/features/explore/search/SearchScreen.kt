package com.example.laisheng.ui.features.explore.search

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.components.UserItem
import com.example.laisheng.ui.features.explore.EmptySearchState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    userId: String,
    onBackClick: () -> Unit,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val momentResults by viewModel.momentSearchResults.collectAsState()
    val userResults by viewModel.userSearchResults.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("搜索瞬间或用户...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            } else {
                                IconButton(onClick = { viewModel.performSearch() }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                viewModel.performSearch()
                                keyboardController?.hide()
                            }
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        )
                    )
                }

                TabRow(selectedTabIndex = searchType.ordinal) {
                    Tab(
                        selected = searchType == SearchViewModel.SearchType.MOMENTS,
                        onClick = { viewModel.onSearchTypeChange(SearchViewModel.SearchType.MOMENTS) },
                        text = { Text("瞬间") }
                    )
                    Tab(
                        selected = searchType == SearchViewModel.SearchType.USERS,
                        onClick = { viewModel.onSearchTypeChange(SearchViewModel.SearchType.USERS) },
                        text = { Text("用户") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
             val history by viewModel.searchHistory.collectAsState()
            
             if (searchQuery.isEmpty()) {
                 // Search History UI
                 Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text("搜索历史", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                         if (history.isNotEmpty()) {
                             TextButton(onClick = { viewModel.clearHistory() }) {
                                 Text("清除", color = MaterialTheme.colorScheme.error)
                             }
                         }
                     }
                     Spacer(modifier = Modifier.height(8.dp))
                     
                     if (history.isEmpty()) {
                         Text("暂无搜索历史", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                     } else {
                         @OptIn(ExperimentalLayoutApi::class)
                         FlowRow(
                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                             verticalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             history.forEach { item ->
                                 InputChip(
                                     selected = false,
                                     onClick = { 
                                         viewModel.onSearchQueryChange(item) 
                                         viewModel.performSearch(item)
                                         keyboardController?.hide()
                                     },
                                     label = { Text(item) },
                                     trailingIcon = {
                                         Icon(
                                             Icons.Default.Close,
                                             contentDescription = "Remove",
                                             modifier = Modifier.size(16.dp).clickable { viewModel.removeFromHistory(item) }
                                         )
                                     },
                                     shape = RoundedCornerShape(16.dp)
                                 )
                             }
                         }
                     }
                 }
             } else if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LaishengLoading()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (searchType == SearchViewModel.SearchType.MOMENTS) {
                        if (momentResults.isEmpty() && searchQuery.isNotEmpty()) {
                            item { EmptySearchState() }
                        } else {
                            itemsIndexed(momentResults) { _, moment ->
                                with(sharedTransitionScope) {
                                    PostCard(
                                        moment = moment,
                                        modifier = Modifier.sharedElement(
                                            rememberSharedContentState(key = "search-item-${moment.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        ),
                                        onCardClick = { onMomentClick(moment.id) },
                                        onUserClick = { onUserClick(moment.userId) },
                                        onLikeClick = { viewModel.onLikeClick(userId, moment.id) },
                                        onCommentClick = { onMomentClick(moment.id) },
                                        onBookmarkClick = {
                                            viewModel.onBookmarkClick(userId, moment.id) {
                                                // Dialog logic simplified/omitted for search results for now to avoid complexity
                                                // Or we could pass a callback from parent if really needed.
                                            }
                                        }
                                    )
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    } else {
                        if (userResults.isEmpty() && searchQuery.isNotEmpty()) {
                            item { EmptySearchState() }
                        } else {
                            items(userResults) { user ->
                                UserItem(
                                    user = user,
                                    onUserClick = { onUserClick(user.id) }
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}
