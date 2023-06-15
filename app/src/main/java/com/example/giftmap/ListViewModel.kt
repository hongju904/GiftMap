package com.example.giftmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    private val repo = Repo()
    fun fetchData(): LiveData<MutableList<ItemData>> {
        val mutableData = MutableLiveData<MutableList<ItemData>>()
        repo.getData().observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}