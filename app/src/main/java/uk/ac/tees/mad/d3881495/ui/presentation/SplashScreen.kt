package uk.ac.tees.mad.d3881495.ui.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import uk.ac.tees.mad.d3881495.NavigationRoute
import uk.ac.tees.mad.d3881495.R
import uk.ac.tees.mad.d3881495.ui.theme.PrimaryBlue

object SplashScreenRoute : NavigationRoute {
    override val route = "splash"
    override val titleRes: Int = R.string.app_name
}

@Composable
fun SplashScreen(onComplete: () -> Unit) {

    val animation = remember {
        Animatable(0f)
    }

    val isVisible = remember {
        mutableStateOf(true)
    }

    AnimatedVisibility(
        visible = isVisible.value,
        enter = expandIn(
            animationSpec = tween(delayMillis = 800)
        ) {
            IntSize(100, 100)
        },
        exit = shrinkVertically(
            animationSpec = tween(delayMillis = 1000)
        )
    ) {
        LaunchedEffect(key1 = true) {
            animation.animateTo(1f, animationSpec = tween(1500))
            delay(1500L)
            isVisible.value = false
            delay(800L)
            onComplete()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBlue),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                LoaderAnimation(
                    modifier = Modifier
                        .size(350.dp),
                    anim = R.raw.sports
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(3f))
                Text(
                    text = "Local Sports Equipment",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(animation.value)
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "Swap, Score, and Play: Your Local Sports Gear Exchange!",
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(animation.value)
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }

        }
    }
}

@Composable
fun LoaderAnimation(modifier: Modifier, anim: Int) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(anim))

    LottieAnimation(
        composition = composition,
        modifier = modifier,
    )
}
