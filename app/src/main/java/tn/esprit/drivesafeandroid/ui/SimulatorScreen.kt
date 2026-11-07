package tn.esprit.drivesafeandroid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.drivesafeandroid.R
import tn.esprit.drivesafeandroid.sensor.ObdSimulator

@Composable
fun SimulatorScreen(viewModel: LwmsViewModel) {
    val state by viewModel.state.collectAsState()
    val isSimulationActive by viewModel.isSimulationActive.collectAsState()
    val isConnectedToObd by viewModel.isConnectedToObd.collectAsState()
    val speedSourceText by viewModel.speedSourceText.collectAsState()

    val surfaceColor = Color(0xFFF1F5F9)
    val cardBg = Color(0xFFFFFFFF)
    val borderColor = Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFCBDBF5), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_shield_logo),
                        contentDescription = "Shield Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text("LWMS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Driving speed screen", fontSize = 12.sp, color = textSecondary)
                }
            }

            // SIMULATOR ACTIVE blue badge appears if switch is on, OR DRIVING ACTIVE green badge appears if connected
            when {
                isConnectedToObd -> {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), CircleShape))
                            Text("DRIVING ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                }
                isSimulationActive -> {
                    Surface(
                        color = Color(0xFFDBE1FF),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFB4C5FF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(primaryBlue, CircleShape))
                            Text("SIMULATOR ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Speed Source Card with Switch and Connect Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SPEED SOURCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                                Text(speedSourceText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch to activate virtual obd2 simulation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Virtual OBD2 simulation", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                            Switch(
                                checked = isSimulationActive,
                                onCheckedChange = { viewModel.setSimulationActive(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connect to OBD2 Button (toggles connection / disconnection)
                        Button(
                            onClick = { viewModel.toggleObdConnection() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isConnectedToObd) Color(0xFF16A34A) else primaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isConnectedToObd) "Connected to OBD2" else "Connect to OBD2",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Speedometer Display Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CURRENT SPEED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${state.speed.toInt()}",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KM/H",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }

            // Direct Speed Adjustment Buttons
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateSpeed(state.speed - 10.0) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text("− 10 km/h", color = textPrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.updateSpeed(state.speed + 10.0) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ 10 km/h", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ℹ Direct ObdSimulator feed",
                            fontSize = 12.sp,
                            color = textSecondary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Operational Presets
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OPERATIONAL PRESETS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text("Quick Target Shift", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PresetItem("Parked", "0 km/h", state.speed == 0.0) {
                                viewModel.applyPreset(ObdSimulator.Preset.PARKED)
                            }
                            PresetItem("Resid.", "20 km/h", state.speed == 20.0) {
                                viewModel.applyPreset(ObdSimulator.Preset.RESIDENTIAL)
                            }
                            PresetItem("Urban", "45 km/h", state.speed == 45.0) {
                                viewModel.applyPreset(ObdSimulator.Preset.URBAN)
                            }
                            PresetItem("Hwy", "90 km/h", state.speed == 90.0) {
                                viewModel.applyPreset(ObdSimulator.Preset.HIGHWAY)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetItem(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    val primaryBlue = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFDBE1FF) else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) primaryBlue else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) primaryBlue else textPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) primaryBlue else textSecondary)
    }
}
