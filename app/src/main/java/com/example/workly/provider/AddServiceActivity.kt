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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.workly.theme.*
import com.google.firebase.firestore.FirebaseFirestore

// No hardcoded UI constants here, using MaterialTheme where possible for consistency.

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

    val onSurf = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val surfVar = MaterialTheme.colorScheme.surfaceVariant
    val background = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editMode) "UPDATE SERVICE" else "CREATE SERVICE", fontWeight = FontWeight.Black, color = PremiumWhite, letterSpacing = 1.sp, fontSize = 18.sp) },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, null, tint = PremiumSilver) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        },
        containerColor = PremiumBlack
    ) { padValue ->
        Box(Modifier.fillMaxSize().padding(padValue)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Image picker logic (Titanium-Lux Style)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clickable { picker.launch("image/*") },
                    shape = RoundedCornerShape(28.dp),
                    color = PremiumBlackSurface,
                    border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (imageUri != null) {
                            AsyncImage(imageUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else if (imageUrl.isNotEmpty()) {
                            AsyncImage(imageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(PremiumSilver.copy(alpha = 0.05f), CircleShape)
                                        .border(1.dp, PremiumSilver.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddAPhoto, null, tint = PremiumSilver, modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Capture Elite Service", fontWeight = FontWeight.Black, color = PremiumWhite, fontSize = 16.sp, letterSpacing = 0.5.sp)
                                Text("High-resolution visuals convert better", color = PremiumSilver.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                        
                        // Edit overlay
                        if (imageUri != null || imageUrl.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .border(1.dp, PremiumSilver.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, null, tint = PremiumWhite, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                SectionCard("SERVICE ARCHitecture") {
                    StrongTextField(title, { vm.title.value = it }, "Service Title", "e.g. Master Deep Cleaning")
                    var catOpen by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(catOpen, { catOpen = !catOpen }) {
                        StrongReadonlyField(
                            category, 
                            "Specialization", 
                            catOpen, 
                            Modifier.menuAnchor(),
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = PremiumSilver) }
                        )
                        ExposedDropdownMenu(catOpen, { catOpen = false }, Modifier.background(PremiumBlackSurface).border(1.dp, PremiumSilver.copy(0.1f), RoundedCornerShape(8.dp))) {
                            categories.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = PremiumWhite, fontWeight = FontWeight.Medium) },
                                    onClick = { vm.category.value = it; catOpen = false }
                                )
                            }
                        }
                    }
                }

                SectionCard("LOGISTICS & DURATION") {
                   StrongTextField(
                       location, 
                       { vm.location.value = it }, 
                       "Operational City", 
                       "Primary Service Area",
                       leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = PremiumSilver) },
                       trailingIcon = {
                           if (isLocating) {
                               CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = PremiumSilver)
                           } else {
                               IconButton(onClick = { fetchCityName() }) {
                                   Icon(Icons.Default.MyLocation, null, tint = PremiumSilver)
                               }
                           }
                       }
                   )
                   var durOpen by remember { mutableStateOf(false) }
                   ExposedDropdownMenuBox(durOpen, { durOpen = !durOpen }) {
                       StrongReadonlyField(
                           duration, 
                           "Expected Timeline", 
                           durOpen, 
                           Modifier.menuAnchor(),
                           leadingIcon = { Icon(Icons.Default.Schedule, null, tint = PremiumSilver) }
                       )
                       ExposedDropdownMenu(durOpen, { durOpen = false }, Modifier.background(PremiumBlackSurface).border(1.dp, PremiumSilver.copy(0.1f), RoundedCornerShape(8.dp))) {
                           durations.forEach {
                               DropdownMenuItem(
                                   text = { Text(it, color = PremiumWhite) },
                                   onClick = { vm.duration.value = it; durOpen = false }
                               )
                           }
                       }
                   }
                }

                SectionCard("FINANCIAL VALUATION") {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { c -> c.isDigit() }) vm.price.value = it },
                        label = { Text("Base Price (INR)", fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Black, fontSize = 22.sp, color = PremiumWhite, modifier = Modifier.padding(start = 16.dp)) },
                        colors = strongFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { if (editMode) vm.updateService(serviceId) else vm.publishService() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PremiumSilver,
                        contentColor = PremiumBlack,
                        disabledContainerColor = PremiumSilver.copy(alpha = 0.2f)
                    ),
                    enabled = formValid && state !is AddServiceState.Loading
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (editMode) Icons.Default.VerticalAlignTop else Icons.Default.AutoFixHigh, null)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            if (editMode) "COMMIT UPDATES" else "DEPLOY SERVICE", 
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
                Spacer(Modifier.height(48.dp))
            }

            if (state is AddServiceState.Loading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumSilver)
                }
            }
        }
    }

    if (state is AddServiceState.Success) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = PremiumBlackSurface,
            titleContentColor = PremiumWhite,
            textContentColor = PremiumSilver,
            title = { Text("Protocol Success", fontWeight = FontWeight.Black) },
            text = { Text((state as AddServiceState.Success).message) },
            confirmButton = {
                TextButton(onClick = { vm.resetState(); onBack() }) { 
                    Text("ACKNOWLEDGEd", color = PremiumSilver, fontWeight = FontWeight.Black) 
                }
            }
        )
    }
}

@Composable
private fun SectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = PremiumBlackSurface,
        border = BorderStroke(1.dp, PremiumSilver.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 13.sp, color = PremiumSilver.copy(0.6f), letterSpacing = 2.sp)
            content()
        }
    }
}

@Composable
private fun StrongTextField(
    value: String, 
    onChange: (String) -> Unit, 
    label: String, 
    placeholder: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value, 
        onValueChange = onChange, 
        label = { Text(label, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 11.sp) }, 
        placeholder = { Text(placeholder, color = PremiumSilver.copy(0.3f)) }, 
        modifier = Modifier.fillMaxWidth(), 
        colors = strongFieldColors(), 
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StrongReadonlyField(
    value: String, 
    label: String, 
    expanded: Boolean, 
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value, 
        onValueChange = {}, 
        readOnly = true, 
        label = { Text(label, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 11.sp) }, 
        modifier = modifier.fillMaxWidth(), 
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, 
        colors = strongFieldColors(),
        shape = RoundedCornerShape(20.dp),
        leadingIcon = leadingIcon
    )
}

@Composable
private fun strongFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PremiumWhite,
    unfocusedTextColor = PremiumWhite,
    focusedBorderColor = PremiumSilver,
    unfocusedBorderColor = PremiumSilver.copy(alpha = 0.1f),
    focusedLabelColor = PremiumSilver,
    unfocusedLabelColor = PremiumSilver.copy(alpha = 0.4f),
    cursorColor = PremiumSilver,
    focusedPlaceholderColor = PremiumSilver.copy(0.3f)
)
