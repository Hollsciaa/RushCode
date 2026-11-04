package fr.cnam.rushcode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.cnam.rushcode.api.ApiClient
import fr.cnam.rushcode.api.LeaderboardApiClient
import fr.cnam.rushcode.api.LeaderboardEntry
import fr.cnam.rushcode.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        // Lancement de la Game Loop locale
        startGameLoop()
        // Lancement de la synchronisation automatique du Leaderboard toutes les 5 secondes
        startLeaderboardSyncLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L) // Toutes les 1 seconde

                _uiState.update { currentState ->
                    // Si le joueur est endormi (caféine à 0), pas de prod
                    if (currentState.caffeine <= 0f) return@update currentState

                    // Calcul de la nouvelle quantité de LDC avec le passivePower (LPS)
                    val newLdc = currentState.linesOfCode + currentState.passivePower

                    // Baisse douce de la caféine (-0.5% par seconde)
                    val newCaffeine = (currentState.caffeine - 0.5f).coerceAtLeast(0f)

                    // Vérification de la condition de victoire du niveau
                    if (newLdc >= currentState.targetLines) {
                        advanceToNextLevel(currentState.copy(linesOfCode = newLdc, caffeine = newCaffeine))
                    } else {
                        currentState.copy(linesOfCode = newLdc, caffeine = newCaffeine)
                    }
                }
            }
        }
    }

    // Boucle de synchronisation avec l'API toutes les 5 secondes
    private fun startLeaderboardSyncLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                submitCurrentScore("Esteban - Dev")
                fetchLeaderboard()
                delay(5000L) // Attend 5 secondes avant la prochaine sync
            }
        }
    }

    // Action lorsque le joueur clique sur l'écran pour coder
    fun onCodeClicked() {
        _uiState.update { currentState ->
            if (currentState.caffeine <= 0f) return@update currentState

            val newLdc = currentState.linesOfCode + currentState.clickPower
            if (newLdc >= currentState.targetLines) {
                advanceToNextLevel(currentState.copy(linesOfCode = newLdc))
            } else {
                currentState.copy(linesOfCode = newLdc)
            }
        }
    }

    // Achat d'un upgrade
    fun buyUpgrade(upgradeType: String) {
        _uiState.update { state ->
            when (upgradeType) {
                "python" -> {
                    if (state.linesOfCode >= state.pythonScriptCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.pythonScriptCost,
                            pythonScriptCount = state.pythonScriptCount + 1,
                            passivePower = state.passivePower + 2.0,
                            pythonScriptCost = state.pythonScriptCost * 1.15
                        )
                    } else state
                }
                "stackoverflow" -> {
                    if (state.linesOfCode >= state.stackOverflowCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.stackOverflowCost,
                            stackOverflowCount = state.stackOverflowCount + 1,
                            passivePower = state.passivePower + 10.0,
                            stackOverflowCost = state.stackOverflowCost * 1.15
                        )
                    } else state
                }
                "bedrock" -> {
                    if (state.linesOfCode >= state.bedrockSdkCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.bedrockSdkCost,
                            bedrockSdkCount = state.bedrockSdkCount + 1,
                            passivePower = state.passivePower + 50.0,
                            bedrockSdkCost = state.bedrockSdkCost * 1.15
                        )
                    } else state
                }
                "alternant" -> {
                    if (state.linesOfCode >= state.alternantCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.alternantCost,
                            alternantCount = state.alternantCount + 1,
                            passivePower = state.passivePower + 200.0,
                            alternantCost = state.alternantCost * 1.15
                        )
                    } else state
                }
                else -> state
            }
        }
    }

    // Recharger la caféine
    fun drinkCoffee() {
        _uiState.update { it.copy(caffeine = 100f) }
    }

    private fun advanceToNextLevel(state: GameState): GameState {
        val nextLevel = state.currentLevel + 1
        val (nextTarget, nextName) = when (nextLevel) {
            2 -> Pair(1500.0, "Le script Python basique")
            3 -> Pair(5000.0, "L'API Node.js, Express & PostgreSQL")
            4 -> Pair(15000.0, "L'application Full-Stack React & Prisma")
            5 -> Pair(50000.0, "Le pipeline d'intégration IA via le SDK Bedrock")
            else -> Pair(state.targetLines * 2.5, "Projet Enterprise Niveau $nextLevel")
        }

        return state.copy(
            currentLevel = nextLevel,
            targetLines = nextTarget,
            levelName = nextName,
            linesOfCode = 0.0
        )
    }

    // Action déclenchée par le capteur (secousse accéléromètre)
    fun onShakeDetected() {
        _uiState.update { currentState ->
            if (currentState.caffeine <= 0f || currentState.isRushModeActive) return@update currentState

            val newLdc = currentState.linesOfCode + 50.0
            activateRushModeTimer()

            currentState.copy(
                linesOfCode = newLdc,
                isRushModeActive = true
            )
        }
    }

    private fun activateRushModeTimer() {
        viewModelScope.launch {
            delay(3000L)
            _uiState.update { it.copy(isRushModeActive = false) }
        }
    }

    // Déclenche l'appel API public (JokeAPI)
    fun triggerBossEvent() {
        if (_uiState.value.isBossEventActive) return

        _uiState.update { it.copy(isBossEventActive = true, currentExcuse = "Génération de l'excuse en cours...") }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.devJokeService.getDeveloperJoke()
                _uiState.update { it.copy(currentExcuse = response.joke) }
            } catch (e: Exception) {
                _uiState.update { it.copy(currentExcuse = "C'est la faute de la connexion Wi-Fi, chef !") }
            }
        }
    }

    fun dismissBossEvent() {
        _uiState.update { it.copy(isBossEventActive = false, currentExcuse = "") }
    }

    // Récupérer le classement depuis l'API Perso
    fun fetchLeaderboard() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val players = LeaderboardApiClient.service.getTopPlayers()
                _uiState.update {
                    it.copy(
                        leaderboardList = players.sortedByDescending { p -> p.level },
                        isLoadingLeaderboard = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingLeaderboard = false) }
            }
        }
    }

    // Envoyer son score (met à jour le joueur existant avec PUT ou crée avec POST)
    fun submitCurrentScore(playerName: String = "Esteban - Dev") {
        val current = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entry = LeaderboardEntry(
                    name = playerName,
                    level = current.currentLevel,
                    linesOfCode = current.linesOfCode
                )

                if (current.localPlayerId != null) {
                    LeaderboardApiClient.service.updateScore(current.localPlayerId, entry)
                } else {
                    val response = LeaderboardApiClient.service.submitScore(entry)
                    response.id?.let { id ->
                        _uiState.update { it.copy(localPlayerId = id) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}