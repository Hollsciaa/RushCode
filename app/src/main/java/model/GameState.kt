package fr.cnam.rushcode.model

data class GameState(
    val linesOfCode: Double = 0.0,      // En Double pour gérer proprement les incréments si besoin
    val targetLines: Double = 500.0,    // Objectif du niveau 1
    val currentLevel: Int = 1,          // Niveau actuel
    val clickPower: Double = 1.0,       // LDC générées par clic
    val passivePower: Double = 0.0,     // LDC générées par seconde (LPS)
    val caffeine: Float = 100.0f,       // Jauge de caféine (0 à 100)

    // Niveaux et leurs objectifs respectifs
    val levelName: String = "Le formulaire HTML/CSS",

    // Quantité d'upgrades achetés
    val pythonScriptCount: Int = 0,
    val stackOverflowCount: Int = 0,
    val bedrockSdkCount: Int = 0,
    val alternantCount: Int = 0,

    // Coûts actuels des upgrades
    val pythonScriptCost: Double = 15.0,
    val stackOverflowCost: Double = 100.0,
    val bedrockSdkCost: Double = 500.0,
    val alternantCost: Double = 2000.0
)