package com.example.workly.data

import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

object MockDataSeeder {

    fun seedMultiServices() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        
        // Define some power-providers who offer multiple services
        val providers = listOf(
            mapOf("id" to "pro_cleaning_master", "name" to "Elite Sanctum Professionals"),
            mapOf("id" to "pro_repair_guru", "name" to "Alpha Technical Solutions")
        )

        val cleaningServices = listOf(
            mapOf(
                "title" to "Elite Home Sterilization",
                "category" to "Cleaning",
                "price" to 4999.0,
                "duration" to "6 Hours",
                "description" to "Hospital-grade sterilization for your living space.",
                "imageUrl" to "https://images.unsplash.com/photo-1581578731548-c64695cc6954?auto=format&fit=crop&w=1200",
                "iconName" to "Cleaning"
            ),
            mapOf(
                "title" to "Premium Sofa & Carpet Triage",
                "category" to "Cleaning",
                "price" to 1499.0,
                "duration" to "2 Hours",
                "description" to "Deep steam cleaning for upholstery and premium fabrics.",
                "imageUrl" to "https://images.unsplash.com/photo-1550963295-019d8a8a61c5?auto=format&fit=crop&w=1200",
                "iconName" to "Cleaning"
            ),
            mapOf(
                "title" to "Industrial Kitchen Degreasing",
                "category" to "Kitchen",
                "price" to 2299.0,
                "duration" to "3 Hours",
                "description" to "Heavy-duty degreasing for high-traffic kitchens.",
                "imageUrl" to "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=1200",
                "iconName" to "Kitchen"
            )
        )

        val repairServices = listOf(
            mapOf(
                "title" to "AC Precision Diagnostic",
                "category" to "AC Repair",
                "price" to 899.0,
                "duration" to "1.5 Hours",
                "description" to "Full system diagnostic and chemical cleaning.",
                "imageUrl" to "https://images.unsplash.com/photo-1581094288338-2314dddb7ecb?auto=format&fit=crop&w=1200",
                "iconName" to "AC Repair"
            ),
            mapOf(
                "title" to "Smart Home Load Balancing",
                "category" to "Electric",
                "price" to 1299.0,
                "duration" to "2 Hours",
                "description" to "Optimizing home circuits for maximum safety.",
                "imageUrl" to "https://images.unsplash.com/photo-1621905231291-0074d241d044?auto=format&fit=crop&w=1200",
                "iconName" to "Electric"
            )
        )

        val specializedServices = listOf(
            mapOf(
                "title" to "Full House Premium Painting",
                "category" to "Painting",
                "price" to 8500.0,
                "duration" to "2 Days",
                "description" to "Professional 3-coat finish with dust protection.",
                "imageUrl" to "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?auto=format&fit=crop&w=1200",
                "iconName" to "Painting"
            ),
            mapOf(
                "title" to "Luxury Furniture Restoration",
                "category" to "Carpentry",
                "price" to 4500.0,
                "duration" to "4 Hours",
                "description" to "Detail-oriented restoration of wooden furniture.",
                "imageUrl" to "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?auto=format&fit=crop&w=1200",
                "iconName" to "Carpentry"
            )
        )

        // Seed Cleaning Pro Portfolio
        cleaningServices.forEachIndexed { idx, s ->
            val id = "mock_cleaning_$idx"
            db.collection("services").document(id).set(s + mapOf(
                "id" to id,
                "providerId" to providers[0]["id"]!!,
                "providerName" to providers[0]["name"]!!,
                "isActive" to true,
                "createdAt" to com.google.firebase.Timestamp.now()
            ))
        }

        // Seed Repair Pro Portfolio
        repairServices.forEachIndexed { idx, s ->
            val id = "mock_repair_$idx"
            db.collection("services").document(id).set(s + mapOf(
                "id" to id,
                "providerId" to providers[1]["id"]!!,
                "providerName" to providers[1]["name"]!!,
                "isActive" to true,
                "createdAt" to com.google.firebase.Timestamp.now()
            ))
        }

        // Seed Specialized Portfolio
        specializedServices.forEachIndexed { idx, s ->
            val id = "mock_special_$idx"
            db.collection("services").document(id).set(s + mapOf(
                "id" to id,
                "providerId" to providers[1]["id"]!!,
                "providerName" to providers[1]["name"]!!,
                "isActive" to true,
                "createdAt" to com.google.firebase.Timestamp.now()
            ))
        }

        // --- ── ELITE ORDER SEEDING (Financial Data) ─────────────────────
        val orderStatuses = listOf("completed", "accepted", "completed", "completed")
        val orderCategories = listOf("Cleaning", "Electric", "Repair", "Cleaning")
        val orderPrices = listOf(4500.0, 1200.0, 900.0, 2500.0)
        
        for (i in 0..10) {
            val orderId = UUID.randomUUID().toString()
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, - (i % 7))
            
            val order = mapOf(
                "id" to orderId,
                "providerId" to providers[0]["id"]!!, // Seed data mainly for one pro to see charts
                "providerName" to providers[0]["name"]!!,
                "serviceName" to "Elite Task #$i",
                "serviceCategory" to orderCategories[i % orderCategories.size],
                "price" to orderPrices[i % orderPrices.size],
                "status" to orderStatuses[i % orderStatuses.size],
                "userId" to "default_user",
                "userName" to "Premium Client",
                "createdAt" to com.google.firebase.Timestamp(cal.time)
            )
            db.collection("orders").document(orderId).set(order)
        }

        // --- ── CUSTOM USER SEEDING ──────────────────────────────────────
        if (currentUser != null) {
            // Check if user is provider before seeding orders
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "user"
                if (role == "provider") {
                    for (i in 0..5) {
                        val orderId = "custom_" + UUID.randomUUID().toString()
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, - (i % 3))
                        
                        val order = mapOf(
                            "id" to orderId,
                            "providerId" to currentUser.uid,
                            "providerName" to (userDoc.getString("name") ?: "Elite Pro"),
                            "serviceName" to "Personal Strategic Task #$i",
                            "serviceCategory" to "Luxury Service",
                            "price" to (2000.0 + (i * 500)),
                            "status" to "completed",
                            "userId" to "default_client",
                            "userName" to "High Net Worth Individual",
                            "createdAt" to com.google.firebase.Timestamp(cal.time)
                        )
                        db.collection("orders").document(orderId).set(order)
                    }
                }
            }
        }
    }
}
