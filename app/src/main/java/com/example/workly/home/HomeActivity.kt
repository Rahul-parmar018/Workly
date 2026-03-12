package com.example.workly.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workly.auth.LoginActivity
import com.example.workly.theme.*
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorklyTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: HomeViewModel = viewModel()) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    // No FAB — WorkLog removed as per user request
    Scaffold(containerColor = BackgroundGray) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

            // Subtle background mesh
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ProfessionalBlue.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.85f, 0f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(EnergyOrange.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(0f, size.height * 0.5f),
                        radius = size.width * 0.5f
                    )
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)) {
                Crossfade(targetState = selectedItem, label = "ContentFade") { target ->
                    when (target) {
                        0 -> HomeScreenContent(
                            innerPadding = innerPadding,
                            viewModel = viewModel,
                            onSeeAllServices = {
                                context.startActivity(Intent(context, ServicesActivity::class.java))
                            }
                        )
                        1 -> ExploreScreen()
                        2 -> InboxScreen()
                        3 -> ProfileScreenContent(onLogout = { performLogout(context) })
                    }
                }
            }

            // Floating bottom nav
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                FloatingBottomBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        }
    }
}

fun performLogout(context: Context) {
    FirebaseAuth.getInstance().signOut()
    val intent = Intent(context, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}