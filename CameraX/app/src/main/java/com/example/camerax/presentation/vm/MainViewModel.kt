package com.example.camerax.presentation.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    private val _smilingProbability = MutableLiveData(0f)
    val smilingProbability = Transformations.map(_smilingProbability) {
        if (it < 0.5f) {
            0.5f
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