package com.example.time_count_app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration

class TimeViewModel : ViewModel() {
    val isScale = MutableLiveData<Boolean>()
    private var _isScale = false
    private var _onPause = false
    val onPause = MutableLiveData<Boolean>()
    private var _timeCount = 1
    val timeCount = MutableLiveData<Int>()
    val timeList = MutableLiveData<MutableList<TimeData>>()
    var scope: CoroutineScope? = null
    private var _timeList = mutableListOf<TimeData>()
    val time = MutableLiveData<Int>()
    private var _time = 0

    fun measureTime() {
        scope = CoroutineScope(Dispatchers.Main)
        scope?.launch {
            while (true) {
                kotlinx.coroutines.time.delay(Duration.ofMillis(1000))
                _time += 1
                time.value = _time
                println(time.value)
                if (_time == 3600){
                    stopTime()
                }
            }
        }

        _isScale = true
        _onPause = false
        onPause.value = _onPause
        isScale.value = _isScale
        println(isScale.value)
    }

    fun stopTime() {
        val data = TimeData(_timeCount, _time)
        _time = 0
        time.value = _time
        _timeCount += 1
        timeCount.value = _timeCount
        _timeList.add(data)
        timeList.value = _timeList
        _onPause = false
        onPause.value = _onPause
        _isScale = false
        isScale.value = _isScale
        scope?.cancel()
    }

    fun pauseTime() {
        _onPause = true
        onPause.value = _onPause
        scope?.cancel()
    }

    fun initTime(){
        _timeList.clear()
        timeList.value = _timeList
        _timeCount = 1
        timeCount.value = _timeCount
    }
    fun removeList(index: Int){
       val removeData = _timeList.removeAt(index)
        if (removeData.no !=  _timeCount){
            for (i in index until _timeList.size){
                _timeList[i].no = _timeList[i].no -1
            }
        }
        _timeCount  -= 1
        timeCount.value = _timeCount
        timeList.value = _timeList
    }
}

data class TimeData(var no: Int, val time: Int)