package com.example.starterkitdemoapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Feature
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlin.math.roundToInt


@Composable
fun DrawerView(feature: Feature?, onClose: () -> Unit) {
    // Unpacking the GeoJson feature's properties object
    // Learn more about GeoJson https://docs.mapbox.com/help/glossary/geojson/
    val properties = feature?.properties()
    val storeName = properties?.get("storeName")?.asString ?: "Unknown Store"
    val address = properties?.get("address")?.asString ?: "No address provided"
    val city = properties?.get("city")?.asString ?: "No city provided"
    val postalCode = properties?.get("postalCode")?.asString ?: "No postal code provided"
    val phoneFormatted = properties?.get("phoneFormatted")?.asString ?: "No phone provided"
    val rating = properties?.get("rating")?.asDouble ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp)
        ) {
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(icon = Icons.Filled.Home, value = "$address, $city $postalCode")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Filled.Phone, value = phoneFormatted)
            Spacer(modifier = Modifier.height(8.dp))
            Rating(rating)
            Spacer(modifier = Modifier.height(16.dp))
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (-16).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, value: String) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = value,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Rating(rating: Double?) {
    val roundedRating = (rating ?: 0.0).roundToInt()

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display the rating text (e.g. "Rating: 4.2")
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Rating: ")
                }
                append(rating?.let { String.format("%.1f", it) } ?: "0")
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Loop through 5 icons (paw prints)
        for (i in 0 until 5) {
            val iconColor = if (i < roundedRating) MaterialTheme.colorScheme.primary else Color.Gray

            Icon(
                imageVector = Icons.Filled.Pets, // Use your custom paw icon here
                contentDescription = "Paw print",
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

