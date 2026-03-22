package com.example.workly.provider

import android.Manifest
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
        setContent {
            WorklyTheme {
                val repo = remember { AddServiceRepository(this@AddServiceActivity) }
                val vm: AddServiceViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T =
                        AddServiceViewModel(repo) as T
                })
                AddServiceScreen(vm) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(vm: AddServiceViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isLocating by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Permissions granted, triggered by the click usually
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
                Toast.makeText(context, "Location not available. Ensure GPS is ON.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            isLocating = false
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val state by vm.uiState.collectAsState()
    val title by vm.title.collectAsState()
    val category by vm.category.collectAsState()
    val location by vm.location.collectAsState()
    val duration by vm.duration.collectAsState()
    val price by vm.price.collectAsState()
    val imageUri by vm.imageUri.collectAsState()

    val cities = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Mumbai", "Delhi", "Bangalore", "Pune")
    val durations = listOf("1 hr", "2 hr", "3 hr", "4 hr", "5 hr")
    val categories = listOf("Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        vm.imageUri.value = uri
    }

    val formValid = imageUri != null && title.isNotBlank() && category.isNotBlank() &&
            location.isNotBlank() && duration.isNotBlank() && price.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Service", fontWeight = FontWeight.Bold, color = TextDark) },
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
                // ═══════════════ IMAGE UPLOAD (TOP — MANDATORY) ═══════════════
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (imageUri == null) Color(0xFFE8EDF2) else Color.Transparent)
                        .then(
                            if (imageUri == null)
                                Modifier.border(2.dp, BluePrimary.copy(0.4f), RoundedCornerShape(16.dp))
                            else Modifier
                        )
                        .clickable { picker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        // Edit badge
                        Surface(
                            Modifier.align(Alignment.BottomEnd).padding(10.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(0.65f)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.padding(8.dp).size(18.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = BluePrimary, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to upload photo", fontWeight = FontWeight.Bold, color = BluePrimary, fontSize = 15.sp)
                            Text("Required for listing", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                // ═══════════════ SERVICE INFO ═══════════════
                SectionCard("Service Details") {
                    StrongTextField(title, { vm.title.value = it }, "Service Title", "e.g. Expert AC Repair")

                    var catOpen by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(catOpen, { catOpen = !catOpen }) {
                        StrongReadonlyField(category, "Category", catOpen, Modifier.menuAnchor())
                        ExposedDropdownMenu(catOpen, { catOpen = false }, Modifier.background(CardBg)) {
                            categories.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = TextDark, fontWeight = FontWeight.Medium) },
                                    onClick = { vm.category.value = it; catOpen = false }
                                )
                            }
                        }
                    }
                }

                // ═══════════════ LOCATION & DURATION ═══════════════
                SectionCard("Location & Time") {
                    var locOpen by remember { mutableStateOf(false) }
                    val filtered = cities.filter { it.contains(location, true) }
                    ExposedDropdownMenuBox(locOpen, { locOpen = !locOpen }) {
                        OutlinedTextField(
                            location, { vm.location.value = it; locOpen = true },
                            label = { Text("City", color = TextMuted) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = strongFieldColors(),
                            singleLine = true,
                            trailingIcon = {
                                if (isLocating) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = BluePrimary)
                                } else {
                                    IconButton(onClick = { fetchCityName() }) {
                                        Icon(Icons.Default.MyLocation, "Auto Location", tint = BluePrimary)
                                    }
                                }
                            }
                        )
                        if (filtered.isNotEmpty() && location.isNotEmpty()) {
                            ExposedDropdownMenu(locOpen, { locOpen = false }, Modifier.background(CardBg)) {
                                filtered.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it, color = TextDark, fontWeight = FontWeight.Medium) },
                                        onClick = { vm.location.value = it; locOpen = false }
                                    )
                                }
                            }
                        }
                    }

                    var durOpen by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(durOpen, { durOpen = !durOpen }) {
                        StrongReadonlyField(duration, "Duration", durOpen, Modifier.menuAnchor())
                        ExposedDropdownMenu(durOpen, { durOpen = false }, Modifier.background(CardBg)) {
                            durations.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = TextDark, fontWeight = FontWeight.Medium) },
                                    onClick = { vm.duration.value = it; durOpen = false }
                                )
                            }
                        }
                    }
                }

                // ═══════════════ PRICING ═══════════════
                SectionCard("Pricing") {
                    OutlinedTextField(
                        price, { if (it.all { c -> c.isDigit() }) vm.price.value = it },
                        label = { Text("Base Price (₹)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = TextDark) },
                        colors = strongFieldColors(),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ═══════════════ PUBLISH BUTTON ═══════════════
                Button(
                    onClick = { vm.publishService() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueDark,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBDBDBD)
                    ),
                    enabled = formValid && state !is AddServiceState.Loading
                ) {
                    Text("Publish Service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(40.dp))
            }

            // ═══════════════ LOADING OVERLAY (BLOCKS UI) ═══════════════
            if (state is AddServiceState.Loading) {
                Surface(
                    Modifier.fillMaxSize().clickable(enabled = true) {},
                    color = Color.Black.copy(0.45f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(CardBg)) {
                            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = BluePrimary, strokeWidth = 4.dp)
                                Spacer(Modifier.height(20.dp))
                                Text("Publishing your service...", fontWeight = FontWeight.Bold, color = TextDark)
                                Text("This may take a moment", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════ ERROR DIALOG ═══════════════
    if (state is AddServiceState.Error) {
        AlertDialog(
            onDismissRequest = { vm.resetState() },
            title = { Text("Something went wrong", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
            text = { Text((state as AddServiceState.Error).message, color = TextDark, fontSize = 14.sp) },
            confirmButton = {
                Button(onClick = { vm.resetState(); vm.publishService() }, colors = ButtonDefaults.buttonColors(BluePrimary)) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.resetState() }) { Text("Cancel", color = TextMuted) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardBg
        )
    }

    // ═══════════════ SUCCESS DIALOG ═══════════════
    if (state is AddServiceState.Success) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Published!", fontWeight = FontWeight.Bold, color = GreenSuccess) },
            text = { Text((state as AddServiceState.Success).message, color = TextDark) },
            confirmButton = {
                Button(onClick = { vm.resetState(); onBack() }, colors = ButtonDefaults.buttonColors(GreenSuccess)) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = CardBg
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// REUSABLE COMPONENTS — HIGH CONTRAST
// ══════════════════════════════════════════════════════════════════

@Composable
fun SectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextDark)
            content()
        }
    }
}

@Composable
fun StrongTextField(value: String, onChange: (String) -> Unit, label: String, placeholder: String) {
    OutlinedTextField(
        value, onChange,
        label = { Text(label, color = TextMuted) },
        placeholder = { Text(placeholder, color = Color(0xFFAAAAAA)) },
        modifier = Modifier.fillMaxWidth(),
        colors = strongFieldColors(),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrongReadonlyField(value: String, label: String, expanded: Boolean, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value, {},
        readOnly = true,
        label = { Text(label, color = TextMuted) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        colors = strongFieldColors()
    )
}

@Composable
fun strongFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    focusedBorderColor = BluePrimary,
    unfocusedBorderColor = Color(0xFFCCCCCC),
    focusedLabelColor = BluePrimary,
    unfocusedLabelColor = TextMuted,
    cursorColor = BluePrimary
)
