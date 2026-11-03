package fr.cnam.rushcode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        // Lancement de la Game Loop (tourne en continu en arrière-plan)
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L) // Toutes les 1 seconde

                _uiState.update { currentState ->
                    // Si le joueur est endormi (caféine à 0), pas de prod, ou malus
                    if (currentState.caffeine <= 0f) return@update currentState

                    // Calcul de la nouvelle quantité de LDC avec le passivePower (LPS)
                    val newLdc = currentState.linesOfCode + currentState.passivePower

                    // Baisse douce de la caféine (ex: -0.5% par seconde)
                    val newCaffeine = (currentState.caffeine - 0.5f).coerceAtLeast(0f)

                    // Vérification de la condition de victoire du niveau
                    if (newLdc >= currentState.targetLines) {
                        // Passage au niveau supérieur automatique (on gère la transition ou l'incrément)
                        advanceToNextLevel(currentState.copy(linesOfCode = newLdc, caffeine = newCaffeine))
                    } else {
                        currentState.copy(linesOfCode = newLdc, caffeine = newCaffeine)
                    }
                }
            }
        }
    }

    // Action lorsque le joueur clique gros sur l'écran pour coder
    fun onCodeClicked() {
        _uiState.update { currentState ->
            if (currentState.caffeine <= 0f) return@update currentState // Bloqué si endormi

            val newLdc = currentState.linesOfCode + currentState.clickPower
            if (newLdc >= currentState.targetLines) {
                advanceToNextLevel(currentState.copy(linesOfCode = newLdc))
            } else {
                currentState.copy(linesOfCode = newLdc)
            }
        }
    }

    // Achat d'un upgrade (ex: Script Python, StackOverflow, etc.)
    fun buyUpgrade(upgradeType: String) {
        _uiState.update { state ->
            when (upgradeType) {
                "python" -> {
                    if (state.linesOfCode >= state.pythonScriptCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.pythonScriptCost,
                            pythonScriptCount = state.pythonScriptCount + 1,
                            passivePower = state.passivePower + 2.0, // +2 LPS
                            pythonScriptCost = state.pythonScriptCost * 1.15 // Coût exponentiel x1.15
                        )
                    } else state
                }
                "stackoverflow" -> {
                    if (state.linesOfCode >= state.stackOverflowCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.stackOverflowCost,
                            stackOverflowCount = state.stackOverflowCount + 1,
                            passivePower = state.passivePower + 10.0, // +10 LPS
                            stackOverflowCost = state.stackOverflowCost * 1.15
                        )
                    } else state
                }
                "bedrock" -> {
                    if (state.linesOfCode >= state.bedrockSdkCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.bedrockSdkCost,
                            bedrockSdkCount = state.bedrockSdkCount + 1,
                            passivePower = state.passivePower + 50.0, // +50 LPS
                            bedrockSdkCost = state.bedrockSdkCost * 1.15
                        )
                    } else state
                }
                "alternant" -> {
                    if (state.linesOfCode >= state.alternantCost) {
                        state.copy(
                            linesOfCode = state.linesOfCode - state.alternantCost,
                            alternantCount = state.alternantCount + 1,
                            passivePower = state.passivePower + 200.0, // +200 LPS
                            alternantCost = state.alternantCost * 1.15
                        )
                    } else state
                }
                else -> state
            }
        }
    }

    // Recharger la caféine (via une pause café)
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
            linesOfCode = 0.0 // Reset des LDC pour le nouveau projet ou cumul, au choix (ici reset pour livrer le projet)
        )
    }

    // Action déclenchée par le capteur (secousse)
    fun onShakeDetected() {
        _uiState.update { currentState ->
            // Si on est endormi ou si le Rush Mode est déjà actif, on ignore
            if (currentState.caffeine <= 0f || currentState.isRushModeActive) return@update currentState

            // Bonus d'urgence : +50 LDC direct pour sauver la prod !
            val newLdc = currentState.linesOfCode + 50.0

            // On lance un chrono pour désactiver l'alerte visuelle au bout de 3 secondes
            activateRushModeTimer()

            currentState.copy(
                linesOfCode = newLdc,
                isRushModeActive = true
            )
        }
    }

    private fun activateRushModeTimer() {
        viewModelScope.launch {
            delay(3000L) // Le mode Rush dure 3 secondes
            _uiState.update { it.copy(isRushModeActive = false) }
        }
    }
}