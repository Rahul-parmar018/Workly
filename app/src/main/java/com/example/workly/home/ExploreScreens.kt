package com.example.workly.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workly.theme.*

@Composable
fun ExploreScreen() {
    val services = remember { getAllServices() }
    val categories = listOf("All", "Cleaning", "Repair", "Plumbing", "Electric", "Wellness", "Tech", "Auto", "Events")
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = services.filter { service ->
        val matchesCat = selectedCategory == "All" || service.category == selectedCategory
        val matchesSearch = searchQuery.isEmpty() || service.name.contains(searchQuery, true) || service.category.contains(searchQuery, true)
        matchesCat && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        // Title
        Text("Explore", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = ProfessionalBlue, modifier = Modifier.padding(horizontal = 20.dp))
        Text("Discover 33+ services near you", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(14.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Search services...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = ProfessionalBlue
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category filter row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(categories.size) { idx ->
                val cat = categories[idx]
                val isSelected = cat == selectedCategory
                Surface(
                    modifier = Modifier.clickable { selectedCategory = cat },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) ProfessionalBlue else Color.White,
                    shadowElevation = if (isSelected) 6.dp else 1.dp,
                    border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(0.4f)) else null
                ) {
                    Text(
                        cat,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${filtered.size} services", color = TextSecondary, fontSize = 12.sp)
            if (selectedCategory != "All" || searchQuery.isNotEmpty()) {
                TextButton(onClick = { selectedCategory = "All"; searchQuery = "" }) {
                    Text("Clear", color = ProfessionalBlue, fontSize = 12.sp)
                }
            }
        }

        // Grid — reuse PremiumServiceCard from ServicesActivity
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
        ) {
            items(filtered, key = { it.id }) { service ->
                PremiumServiceCard(service)  // Reuses the card from ServicesActivity
            }
        }
    }
}
