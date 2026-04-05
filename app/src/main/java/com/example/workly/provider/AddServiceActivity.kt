package com.example.workly.provider

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.workly.theme.WorklyTheme
import com.google.firebase.firestore.FirebaseFirestore

// ── Hard-coded high-contrast colors (no theme dependency for critical UI) ──
private val CardBg = Color.White
private val PageBg = Color(0xFFF0F2F5)
private val TextDark = Color(0xFF111111)
private val TextMuted = Color(0xFF666666)
private val BluePrimary = Color(0xFF1565C0)
private val BlueDark = Color(0xFF0D47A1)
private val GreenSuccess = Color(0xFF2E7D32)

class AddServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themeDataStore = ThemeDataStore(this)
        val initialThemeMode = themeDataStore.getInitialThemeMode()

        setContent {
            val themeMode by themeDataStore.themeModeFlow.collectAsState(initial = initialThemeMode)
            WorklyTheme(themeMode = themeMode) {
                val repo = remember { AddServiceRepository(this@AddServiceActivity) }
                val vm: AddServiceViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T =
                        AddServiceViewModel(repo) as T
                })

                val editMode = intent.getBooleanExtra("EDIT_MODE", false)
                val serviceId = intent.getStringExtra("SERVICE_ID") ?: ""

                LaunchedEffect(editMode, serviceId) {
                    if (editMode && serviceId.isNotEmpty()) {
                        vm.loadService(serviceId)
                    }
                }

                AddServiceScreen(vm, editMode, serviceId) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(vm: AddServiceViewModel, editMode: Boolean = false, serviceId: String = "", onBack: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocating by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Permissions granted
        }
    }

    fun fetchCityName() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        isLocating = true
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea
                        vm.location.value = city ?: ""
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to get city: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLocating = false
                }
            } else {
                isLocating = false
            }
        }
    }

    val state by vm.uiState.collectAsState()
    val title by vm.title.collectAsState()
    val category by vm.category.collectAsState()
    val location by vm.location.collectAsState()
    val duration by vm.duration.collectAsState()
    val price by vm.price.collectAsState()
    val imageUri by vm.imageUri.collectAsState()
    val imageUrl by vm.imageUrl.collectAsState()

    val cities = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Mumbai", "Delhi", "Bangalore", "Pune")
    val durations = listOf("1 hr", "2 hr", "3 hr", "4 hr", "5 hr")
    val categories = listOf("Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        vm.imageUri.value = uri
    }

    val formValid = (imageUri != null || (editMode && imageUrl.isNotEmpty())) && title.isNotBlank() && category.isNotBlank() &&
            location.isNotBlank() && duration.isNotBlank() && price.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "Update Service" else "Create Service", fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextDark) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        },
        containerColor = PageBg
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Image picker logic
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (imageUri == null && imageUrl.isEmpty()) Color(0xFFE8EDF2) else Color.Transparent)
                        .clickable { picker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (imageUrl.isNotEmpty()) {
                        AsyncImage(imageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = BluePrimary, modifier = Modifier.size(44.dp))
                            Text("Tap to upload photo", fontWeight = FontWeight.Bold, color = BluePrimary)
                        }
                    }
                }

                SectionCard("Service Details") {
                    StrongTextField(title, { vm.title.value = it }, "Service Title", "e.g. Expert AC Repair")
                    var catOpen by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(catOpen, { catOpen = !catOpen }) {
                        StrongReadonlyField(category, "Category", catOpen, Modifier.menuAnchor())
                        ExposedDropdownMenu(catOpen, { catOpen = false }, Modifier.background(CardBg)) {
                            categories.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = TextDark) },
                                    onClick = { vm.category.value = it; catOpen = false }
                                )
                            }
                        }
                    }
                }

                SectionCard("Location & Time") {
                   StrongTextField(location, { vm.location.value = it }, "City", "e.g. Surat")
                   var durOpen by remember { mutableStateOf(false) }
                   ExposedDropdownMenuBox(durOpen, { durOpen = !durOpen }) {
                       StrongReadonlyField(duration, "Duration", durOpen, Modifier.menuAnchor())
                       ExposedDropdownMenu(durOpen, { durOpen = false }, Modifier.background(CardBg)) {
                           durations.forEach {
                               DropdownMenuItem(
                                   text = { Text(it, color = TextDark) },
                                   onClick = { vm.duration.value = it; durOpen = false }
                               )
                           }
                       }
                   }
                }

                SectionCard("Pricing") {
                    OutlinedTextField(
                        price, { if (it.all { c -> c.isDigit() }) vm.price.value = it },
                        label = { Text("Base Price (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ") },
                        colors = strongFieldColors(),
                        singleLine = true
                    )
                }

                Button(
                    onClick = { if (editMode) vm.updateService(serviceId) else vm.publishService() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueDark),
                    enabled = formValid && state !is AddServiceState.Loading
                ) {
                    Text(if (editMode) "Update Service" else "Publish Service", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(40.dp))
            }

            if (state is AddServiceState.Loading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }

    if (state is AddServiceState.Success) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Success") },
            text = { Text((state as AddServiceState.Success).message) },
            confirmButton = {
                Button(onClick = { vm.resetState(); onBack() }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun SectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(CardBg), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextDark)
            content()
        }
    }
}

@Composable
private fun StrongTextField(value: String, onChange: (String) -> Unit, label: String, placeholder: String) {
    OutlinedTextField(value, onChange, label = { Text(label) }, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), colors = strongFieldColors(), singleLine = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrongReadonlyField(value: String, label: String, expanded: Boolean, modifier: Modifier = Modifier) {
    OutlinedTextField(value, {}, readOnly = true, label = { Text(label) }, modifier = modifier.fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, colors = strongFieldColors())
}

@Composable
private fun strongFieldColors() = OutlinedTextFieldDefaults.colors(focusedTextColor = TextDark, unfocusedTextColor = TextDark, focusedBorderColor = BluePrimary, unfocusedBorderColor = Color(0xFFCCCCCC))
