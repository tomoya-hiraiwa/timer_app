package com.example.time_count_app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Time

class HistoryVIewModel: ViewModel() {
    private var _parentData = mutableListOf<ParentData>()
    val parentData = MutableLiveData<MutableList<ParentData>>()

    suspend fun getParentData(database: TimeDatabase){
        val db = database.readableDatabase
        var dataList = mutableListOf<ParentData>()
        withContext(Dispatchers.IO) {
            val cur = db.query(TimeDatabase.parent, null, null, null, null, null, null)
            while (cur.moveToNext()){
                val id = cur.getInt(cur.getColumnIndexOrThrow("id"))
                val name = cur.getString(cur.getColumnIndexOrThrow("name"))
                dataList.add(ParentData(id,name))
            }
        }
        withContext(Dispatchers.Main){
            _parentData = dataList
            parentData.value = _parentData
        }
    }
    suspend fun deleteData(database: TimeDatabase){
        val db = database.writableDatabase
        withContext(Dispatchers.IO){
            db.delete(TimeDatabase.parent,null,null)
            db.delete(TimeDatabase.child,null,null)
        }
        getParentData(database)
    }
}