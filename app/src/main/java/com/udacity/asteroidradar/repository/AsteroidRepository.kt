package com.udacity.asteroidradar.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsteroidRepository(private val database: AsteroidDatabase) {

    val allAsteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
            it.asDomainModel()
        }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val asteroids = AsteroidApi.retrofitService.getAsteroids(Constants.API_KEY)
                val result = parseAsteroidsJsonResult(JSONObject(asteroids))
                database.asteroidDao.insertAsteroids(*result.asDatabaseModel())
            } catch (err: Exception) {
                Log.e("error"," fun refreshAsteroids error")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private val startDate = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private val endDate = LocalDateTime.now().minusDays(7)

    @RequiresApi(Build.VERSION_CODES.O)
    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroidsDay(startDate.format(DateTimeFormatter.ISO_DATE))) {
            it.asDomainModel()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    val weekAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(
            database.asteroidDao.getAsteroidsDate(
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE)
            )
        ) {
            it.asDomainModel()
        }
}
