package com.tmix.education.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tmix.education.data.api.ApiConfig
import com.tmix.education.data.model.Advertisement
import com.tmix.education.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Banner Carousel component for Dashboard screens
 * Fetches and displays advertisement banners in an auto-scrolling horizontal pager
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerCarousel(
    modifier: Modifier = Modifier
) {
    val apiService = remember { ApiConfig.getApiService() }
    var banners by remember { mutableStateOf<List<Advertisement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Fetch banners
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getAdvertisementBanners(5)
            if (response.isSuccessful && response.body()?.data != null) {
                banners = response.body()!!.data!!
            }
        } catch (_: Exception) {}
        isLoading = false
    }
    
    if (banners.isEmpty() && !isLoading) return
    if (isLoading) return
    
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    // Auto-scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            pageSpacing = 8.dp
        ) { page ->
            val banner = banners[page]
            Card(
                shape = TMixShapes.Card,
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TMixNavy,
                                    TMixNavy.copy(alpha = 0.8f),
                                    TMixRed.copy(alpha = 0.6f)
                                )
                            )
                        )
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                banner.title ?: "Thông báo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                banner.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Campaign, null, Modifier.size(28.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
        
        // Page indicator dots
        if (banners.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) TMixRed 
                                else TextSecondary.copy(0.3f)
                            )
                    )
                }
            }
        }
    }
}
