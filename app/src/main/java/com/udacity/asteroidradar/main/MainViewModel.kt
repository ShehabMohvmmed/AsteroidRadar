package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi


import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
//import com.udacity.asteroidradar.database.DatabaseAsteroid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    enum class AsteroidFilter { ALL, TODAY, WEEK }
    private var _filterAsteroid = MutableLiveData(AsteroidFilter.ALL)

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _navigateToDetailAsteroid = MutableLiveData<Asteroid?>()
    val navigateToDetailAsteroid: LiveData<Asteroid?>
        get() = _navigateToDetailAsteroid


    val asteroidList = Transformations.switchMap(_filterAsteroid) {
        when (it!!) {
            AsteroidFilter.WEEK -> asteroidRepository.weekAsteroids
            AsteroidFilter.TODAY -> asteroidRepository.todayAsteroids
            else -> asteroidRepository.allAsteroids
        }
    }

    fun onFilterSelected(filter: AsteroidFilter) {
        _filterAsteroid.postValue(filter)
    }
    fun onAsteroidSelected(asteroid: Asteroid) {
        _navigateToDetailAsteroid.value = asteroid
    }
    fun onAsteroidNavigated() {
        _navigateToDetailAsteroid.value = null
    }

    private suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                _pictureOfDay.postValue(
                    AsteroidApi.retrofitService.getPictureOfDay(Constants.API_KEY)
                )
            } catch (err: Exception) {
                Log.e("error", "fun refreshPictureOfDay error")
            }
        }
    }


    init {
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            refreshPictureOfDay()
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("viewModel instance created error")
        }
    }
}