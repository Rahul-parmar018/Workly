package com.example.workly.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object ServiceIconMapper {
    fun getServiceIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Cleaning" -> Icons.Default.CleaningServices
            "Electric" -> Icons.Default.ElectricalServices
            "Plumbing" -> Icons.Default.Plumbing
            "Kitchen" -> Icons.Default.Countertops
            "AC Repair" -> Icons.Default.AcUnit
            "Painting" -> Icons.Default.FormatPaint
            "Carpentry" -> Icons.Default.Handyman
            "Support" -> Icons.Default.SupportAgent
            "Repair" -> Icons.Default.Build
            "Laundry" -> Icons.Default.LocalLaundryService
            "Pest" -> Icons.Default.BugReport
            "Wellness" -> Icons.Default.SelfImprovement
            else -> Icons.Default.MiscellaneousServices
        }
    }
}
