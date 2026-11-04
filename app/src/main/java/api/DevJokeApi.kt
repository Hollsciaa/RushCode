package fr.cnam.rushcode.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Le modèle de données : L'API nous renvoie un JSON contenant un champ "joke"
data class JokeResponse(
    val joke: String
)

// 2. L'interface réseau : On définit la route (le endpoint) à appeler
interface DevJokeService {
    // On filtre pour n'avoir que des blagues de programmation en une seule phrase
    @GET("joke/Programming?type=single")
    suspend fun getDeveloperJoke(): JokeResponse
}

// 3. Le Client Singleton : L'objet unique qui va gérer notre connexion à Internet
object ApiClient {
    private const val BASE_URL = "https://v2.jokeapi.dev/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Gson permet de transformer automatiquement le JSON d'internet en objet Kotlin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val devJokeService: DevJokeService by lazy {
        retrofit.create(DevJokeService::class.java)
    }
}