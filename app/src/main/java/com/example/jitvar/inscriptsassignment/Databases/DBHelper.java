package com.example.jitvar.inscriptsassignment.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jitvar on 13/3/16.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Messages.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGE = "messages";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    //Column name Strings
    public static final String COLUMN_ID = "_id";


    private static final String DATABASE_CREATE = "create table " + TABLE_MESSAGE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MESSAGE + " text ,"
            + COLUMN_ROLE + " text not null ,"
            + COLUMN_TIMESTAMP + " integer"
            + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        onCreate(db);
    }


}
