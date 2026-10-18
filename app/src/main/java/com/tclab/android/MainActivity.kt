
package com.tclab.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val colors = darkColorScheme(
                primary = Color(0xFF9AD6B0),
                secondary = Color(0xFFB8DCC8),
                background = Color(0xFF101613),
                surface = Color(0xFF19231D)
            )

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "TC LAB",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Tissue Culture Laboratory",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "OFFLINE-FIRST WORKSPACE",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Text("Plan experiments. Record observations. Learn from evidence.")

        SectionCard(
            "Plant library",
            "Geneva apple rootstocks • Pomegranate • Seedless breadfruit"
        )

        SectionCard(
            "Culture protocols",
            "Organize reviewed protocols, sources, and revisions."
        )

        SectionCard(
            "Media & chemicals",
            "Plan formulations and track laboratory stock and safety information."
        )

        SectionCard(
            "Experiments",
            "Organize batches, observations, photographs, and culture history."
        )

        SectionCard(
            "Tasks & reminders",
            "Keep track of planned and completed laboratory tasks."
        )

        SectionCard(
            "Research notebook",
            "Separate published evidence, your measurements, and calculations."
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Scientific note: Results and protocols can vary by species, cultivar, explant, and laboratory conditions. Verify sources and safety requirements before use.",
            fontSize = 13.sp
        )
    }
}

@Composable
fun SectionCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp
            )
        }
    }
}

