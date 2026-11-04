package fr.cnam.rushcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.cnam.rushcode.viewmodel.GameViewModel
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import fr.cnam.rushcode.sensor.ShakeDetector

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du service matériel d'Android
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Création de notre détecteur. Quand il s'active, il appelle le ViewModel !
        shakeDetector = ShakeDetector {
            gameViewModel.onShakeDetected()
        }

        setContent {
            RushCodeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) // Dark mode développeur
                ) {
                    GameScreen(viewModel = gameViewModel)
                }
            }
        }
    }

    // Quand l'appli passe au premier plan : on allume le capteur
    override fun onResume() {
        super.onResume()
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Quand l'appli est en arrière-plan : on coupe le capteur pour sauver la batterie
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }
}


@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête : Niveau et Projet
        item {
            Text(
                text = "NIVEAU ${state.currentLevel}",
                color = Color(0xFF03DAC6), // Vert terminal / cyan
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = state.levelName,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Alerte visuelle du Rush Mode
        if (state.isRushModeActive) {
            item {
                Text(
                    text = "🚨 RUSH MODE ACTIVÉ : +50 LDC ! 🚨",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Compteur de LDC & Objectif
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Lignes de Code (LDC)", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.linesOfCode.toInt()} / ${state.targetLines.toInt()}",
                        color = Color(0xFF4CAF50),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Puissance : +${state.clickPower} /clic | +${state.passivePower} /sec",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Jauge de Caféine
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "☕ Caféine / Énergie", color = Color.LightGray, fontSize = 14.sp)
                    Text(text = "${state.caffeine.toInt()}%", color = Color(0xFFFF9800), fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { state.caffeine / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFF9800),
                    trackColor = Color.DarkGray
                )
                if (state.caffeine <= 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💤 ENDORMI SUR LE CLAVIER ! Fais une pause café !",
                        color = Color(0xFFCF6679),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // BOUTON DE TEST API
        item {
            Button(
                onClick = { viewModel.triggerBossEvent() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "🚨 TEST API : LE CHEF DÉBARQUE ! 🚨", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // L'AFFICHAGE DE L'APPEL API
        if (state.isBossEventActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFCF6679)), // Rouge clair
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LE CHEF EST LÀ ! DIVERSION !",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // L'excuse récupérée depuis Internet !
                        Text(
                            text = "\"${state.currentExcuse}\"",
                            color = Color.Black,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.dismissBossEvent() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Chef reparti, retourner coder", color = Color.White)
                        }
                    }
                }
            }
        }

        // Gros Bouton de Code (Action principale)
        item {
            Button(
                onClick = { viewModel.onCodeClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3700B3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                Text(text = "💻 CODER (TAP !)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bouton Pause Café
        item {
            OutlinedButton(
                onClick = { viewModel.drinkCoffee() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "☕ Pause Café (Recharger 100%)", color = Color(0xFFFF9800))
            }
        }

        // Boutique / Upgrades
        item {
            Text(
                text = "BOUTIQUE D'ALTERNANT",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            UpgradeItem(
                name = "🐍 Script Python bancal (+2 LPS)",
                cost = state.pythonScriptCost,
                count = state.pythonScriptCount,
                canBuy = state.linesOfCode >= state.pythonScriptCost,
                onBuy = { viewModel.buyUpgrade("python") }
            )
        }

        item {
            UpgradeItem(
                name = "🌐 StackOverflow 3 écrans (+10 LPS)",
                cost = state.stackOverflowCost,
                count = state.stackOverflowCount,
                canBuy = state.linesOfCode >= state.stackOverflowCost,
                onBuy = { viewModel.buyUpgrade("stackoverflow") }
            )
        }

        item {
            UpgradeItem(
                name = "🤖 SDK Bedrock (Cloud) (+50 LPS)",
                cost = state.bedrockSdkCost,
                count = state.bedrockSdkCount,
                canBuy = state.linesOfCode >= state.bedrockSdkCost,
                onBuy = { viewModel.buyUpgrade("bedrock") }
            )
        }

        item {
            UpgradeItem(
                name = "🧑‍💻 Collègue en alternance (+200 LPS)",
                cost = state.alternantCost,
                count = state.alternantCount,
                canBuy = state.linesOfCode >= state.alternantCost,
                onBuy = { viewModel.buyUpgrade("alternant") }
            )
        }

        // SECTION CLASSEMENT (API PERSO)
        item {
            Text(
                text = "🏆 CLASSEMENT GLOBAL (API PERSO)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.submitCurrentScore("Esteban - Dev") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Envoyer mon score", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.fetchLeaderboard() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Charger Top", color = Color.Black, fontSize = 12.sp)
                }
            }
        }

        // SECTION CLASSEMENT (AUTOMATIQUE)
        item {
            Text(
                text = "🏆 CLASSEMENT GLOBAL EN DIRECT",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Affichage de la liste dynamique du classement
        if (state.isLoadingLeaderboard && state.leaderboardList.isEmpty()) {
            item {
                CircularProgressIndicator(color = Color(0xFF03DAC6))
            }
        } else {
            items(state.leaderboardList.size) { index ->
                val player = state.leaderboardList[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${player.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Niv. ${player.level} (${player.linesOfCode.toInt()} LDC)",
                            color = Color(0xFF03DAC6)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradeItem(
    name: String,
    cost: Double,
    count: Int,
    canBuy: Boolean,
    onBuy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "Coût : ${cost.toInt()} LDC | Possédés : $count", color = Color.Gray, fontSize = 12.sp)
            }
            Button(
                onClick = onBuy,
                enabled = canBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF03DAC6),
                    disabledContainerColor = Color.DarkGray
                )
            ) {
                Text(text = "Acheter", color = Color.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RushCodeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212)
        ),
        content = content
    )
}