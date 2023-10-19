package com.example.note

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context:Context):
    SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object {
        val DATABASE_VERSION=1
        val DATABASE_NAME="NoteDatabase.db"
        val TABLE_CONTACTS="NoteData"
        fun Boolean.toInt()=if (this) 1 else 0
    }

    override fun onCreate(db:SQLiteDatabase?) {
        val CREATE_CONTACTS_TABLE=
            ("CREATE TABLE $TABLE_CONTACTS(path TEXT,name TEXT,done INTEGER,time0 TEXT,time1 TEXT,time INTEGER,note TEXT)")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db:SQLiteDatabase?,oldVersion:Int,newVersion:Int) {}

    fun insertNote(use:NoteData):Long {
        val db=this.writableDatabase
        val contentValues=ContentValues().apply {
            put("path",use.path)
            put("name",use.name)
            put("done",use.done.toInt())
            put("time0",use.time0)
            put("time1",use.time1)
            put("time",use.time)
            put("note",use.note)
        }
        val success=db.insert(TABLE_CONTACTS,null,contentValues)
        db.close()
        return success
    }

    fun updateNote(old:NoteData,use:NoteData):Int {
        val db=this.writableDatabase
        val contentValues=ContentValues().apply {
            put("path",use.path)
            put("name",use.name)
            put("done",use.done.toInt())
            put("time0",use.time0)
            put("time1",use.time1)
            put("time",use.time)
            put("note",use.note)
        }
        val success=
            db.update(TABLE_CONTACTS,contentValues,"path='${old.path}' AND name='${old.name}'",null)
        db.close()
        return success
    }

    fun deleteNote(old:NoteData):Int {
        val db=this.writableDatabase
        val success=db.delete(TABLE_CONTACTS,"path='${old.path}' AND name='${old.name}'",null)
        db.close()
        return success
    }

    fun tolistNote():List<NoteData> {
        val db=this.readableDatabase
        val cursor:Cursor=db.query(TABLE_CONTACTS,null,null,null,null,null,null)
        val NoteList=ArrayList<NoteData>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val use=NoteData()
            use.path=cursor.getString(0)
            use.name=cursor.getString(1)
            use.done=cursor.getInt(2)==1
            use.time0=cursor.getString(3)
            use.time1=cursor.getString(4)
            use.time=cursor.getInt(5)
            use.note=cursor.getString(6)
            NoteList.add(use)
            cursor.moveToNext()
        }
        cursor.close()
        return NoteList
    }

}