package com.example.camerax.presentation.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.camerax.BASE_OPACITY
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    private val _smilingProbability = MutableLiveData(0f)
    val smilingProbability = Transformations.map(_smilingProbability) {
        if (it < BASE_OPACITY) {
            BASE_OPACITY
        } else {
            it
        }
    }
    val semaphoreProbability: LiveData<Float>
        get() = _smilingProbability

    fun setSmilingProbability(newProbability: Float) {
        _smilingProbability.value = newProbability
    }
}