package com.example.workly.provider

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.workly.data.Service
import com.example.workly.theme.*
import java.io.File

class MyServicesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                val repo = remember { AddServiceRepository(this@MyServicesActivity) }
                val vm: MyServicesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T =
                        MyServicesViewModel(repo) as T
                })
                MyServicesScreen(vm, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyServicesScreen(vm: MyServicesViewModel, onBack: () -> Unit) {
    val servicesList by vm.services.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Listed Services", 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PremiumWhite,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PremiumSilver)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumBlack,
                    titleContentColor = PremiumWhite
                )
            )
        },
        containerColor = PremiumBlack
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumSilver)
            } else if (servicesList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(PremiumBlackSurface, CircleShape)
                            .border(1.dp, PremiumSilver.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = PremiumSilver.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No services listed yet", 
                        color = PremiumSilver, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Your premium services will appear here", 
                        color = PremiumSilver.copy(alpha = 0.5f), 
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(servicesList, key = { it.id }) { service ->
                        ServiceCard(service, vm)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, vm: MyServicesViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val title = service.title
    val price = service.price.toInt()
    val imagePath = service.imagePath
    val imageUrl = service.imageUrl

    val imageSource = if (imagePath.isNotEmpty()) {
        val file = File(imagePath)
        if (file.exists()) android.net.Uri.fromFile(file) else imageUrl
    } else {
        imageUrl
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumBlackSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🖼️ Service Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = imageSource,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_report_image)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 17.sp, 
                    color = PremiumWhite,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹$price", 
                    fontSize = 15.sp, 
                    color = PremiumSilver, 
                    fontWeight = FontWeight.ExtraBold
                )
                
                Text(
                    text = "PREMIUM SERVICE", 
                    fontSize = 10.sp, 
                    color = PremiumSilver.copy(alpha = 0.5f), 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // ⚙️ Actions: Edit & Delete
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    // Logic to open Edit
                    val intent = Intent(context, AddServiceActivity::class.java).apply {
                        putExtra("SERVICE_ID", service.id)
                        putExtra("EDIT_MODE", true)
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PremiumSilver, modifier = Modifier.size(18.dp))
                }
                
                IconButton(onClick = {
                    vm.deleteService(service.id)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252).copy(0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
