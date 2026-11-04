package fr.cnam.rushcode.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// 1. Modèle de données pour un joueur au classement
data class LeaderboardEntry(
    val id: String? = null,
    val name: String,
    val level: Int,
    val linesOfCode: Double
)

// 2. Interface des routes API Perso (GET pour lire, POST pour envoyer)
interface LeaderboardService {
    @GET("Leaderboards")
    suspend fun getTopPlayers(): List<LeaderboardEntry>

    @POST("Leaderboards")
    suspend fun submitScore(@Body entry: LeaderboardEntry): LeaderboardEntry

    // Nouvelle route : met à jour le joueur existant au lieu d'en créer un nouveau
    @PUT("Leaderboards/{id}")
    suspend fun updateScore(@Path("id") id: String, @Body entry: LeaderboardEntry): LeaderboardEntry
}

// 3. Client Retrofit dédié au Leaderboard
object LeaderboardApiClient {
    private const val BASE_URL = "https://6ae9e0fa3d3dd3d37be5cdbe.mockapi.io/api/v1/"

    val service: LeaderboardService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LeaderboardService::class.java)
    }
}