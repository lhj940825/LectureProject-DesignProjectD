package com.example.hojun.healthcareserviceprojectd;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hanbyung-ik on 2017. 12. 4..
 */

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table talk ("
                + " _id integer PRIMARY KEY autoincrement, "
                + " youId text, "
                + " whose integer, "
                + " Message text);");
    }

    public boolean getdata() {
        SQLiteDatabase db = getReadableDatabase();
        return db.isOpen();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertRecord(String yourId, int whose, String Message) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into talk(youId, whose, Message) values (" +
                "'" + yourId + "', " + whose + ", '" + Message + "');" );
    }

    public String getMessage(String youId) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String Message = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT whose, Message FROM talk WHERE youId = '" + youId +"';", null);
        while (cursor.moveToNext()) {
            if(cursor.getInt(0) == 0) {
                Message += "  " + cursor.getString(1) + ":::";
            }
            else {
                Message += "  " + cursor.getString(1) + ":::";
            }
        }
        if(Message.equals(""))
            return null;
        else
            return Message;
    }
}
