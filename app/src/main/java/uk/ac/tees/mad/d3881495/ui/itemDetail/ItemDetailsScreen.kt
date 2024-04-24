package uk.ac.tees.mad.d3881495.ui.itemDetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.domain.ItemResponse
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemDetailScreen(
    viewModel: ItemDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val itemState = viewModel.itemDetail.collectAsState(initial = null)
    val item = itemState.value?.isSuccess

    val context = LocalContext.current
    val pagerState = rememberPagerState {
        item?.images?.size ?: 0
    }
    var showPopup by rememberSaveable { mutableStateOf(false) }
    val showPopupLoading by rememberSaveable { mutableStateOf(false) }



    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            val data =
                                Uri.parse("mailto:${item?.listedBy?.email}?subject=${"For buying ${item?.name}, listed on Local Sport Item Exchange"}&body=")
                            intent.setData(data)
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No email app", Toast.LENGTH_SHORT)
                                .show()
                        } catch (t: Throwable) {
                            Log.d("Message ERROR", t.message.toString())
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Contact", fontSize = 20.sp, color = Color.White)
            }
        }
    ) {

        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (itemState.value?.isLoading == true) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                Column(Modifier.fillMaxSize()) {

                    Column(
                        Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeaderContent(onBack = onBack, pagerState = pagerState, item = item)
                        PageIndicator(
                            pageCount = item?.images?.size ?: 0,
                            currentPage = pagerState.currentPage,
                            modifier = Modifier.padding(vertical = 4.dp)

                        )
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                                Text(
                                    text = "${item?.name}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(Modifier.padding(vertical = 16.dp)) {
                                    Text(
                                        text = "${item?.description}",
                                        style = TextStyle(
                                            color = Color.Gray.copy(alpha = 0.9f),
                                            fontSize = 18.sp
                                        )
                                    )
                                }
                            }
                        }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                Modifier.width(300.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Type:", fontSize = 18.sp, fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${item?.type}", fontSize = 20.sp,
                                )

                            }
                            Row(
                                Modifier.width(300.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Condition:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${item?.condition}", fontSize = 20.sp,
                                )

                            }
                            Row(
                                Modifier.width(300.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Listed on:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${
                                        item?.listedDate?.let {
                                            SimpleDateFormat(
                                                "dd/MM/yyyy",
                                                Locale.getDefault()
                                            ).format(item.listedDate)
                                        }
                                    }",
                                    fontSize = 20.sp,
                                )

                            }
                        }

                        Column(Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "Listed by: ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ListedByUserCard(item = item)
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(pageCount) {
            IndicatorDot(isSelected = it == currentPage)
        }
    }
}

@Composable
fun IndicatorDot(
    isSelected: Boolean
) {
    val width =
        animateDpAsState(targetValue = if (isSelected) 10.dp else 8.dp, label = "Indicator width")
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(width.value)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.5f
                )
            )
    )

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderContent(
    onBack: () -> Unit,
    pagerState: PagerState,
    item: ItemResponse?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {
        IconButton(
            onClick = onBack, modifier = Modifier
                .padding(16.dp)
                .zIndex(100f)
                .clip(CircleShape)
                .background(
                    Color.Gray.copy(0.5f)
                )
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Back",
                modifier = Modifier.size(30.dp)
            )
        }
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { currentPage ->
            if (item != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).crossfade(true)
                        .data(item.images[currentPage])
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }

}


@Composable
fun ListedByUserCard(
    item: ItemResponse?
) {
    Column(
        Modifier
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row {
                Text(
                    text = "Name: ",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "${item?.listedBy?.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row {
                Text(
                    text = "Email: ",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "${item?.listedBy?.email}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

object ItemDetailsRoute : NavigationRoute {
    override val route: String = "item_detail"
    override val titleRes: Int = R.string.item_details
    const val itemIdArg = "itemId"
    val routeWithArgs = "$route/{$itemIdArg}"
}
