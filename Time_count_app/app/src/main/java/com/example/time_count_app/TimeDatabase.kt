package com.example.time_count_app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TimeDatabase(context: Context): SQLiteOpenHelper(context,"db",null,1) {
    companion object{
        const val parent = "parent"
        const val child = "child"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            it.execSQL("create table $parent( id integer primary key, name text)")
            it.execSQL("create table $child( id integer primary key autoincrement, parent integer,number integer, time integer)")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}