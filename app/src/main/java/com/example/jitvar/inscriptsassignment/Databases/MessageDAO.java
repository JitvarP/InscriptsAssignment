package com.example.jitvar.inscriptsassignment.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.jitvar.inscriptsassignment.Model.MessageVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jitvar on 13/3/16.
 */
public class MessageDAO {

    private SQLiteDatabase database;
    private static DBHelper dbHelper;

    private String[] allColumns = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_MESSAGE,
            DBHelper.COLUMN_ROLE,
            DBHelper.COLUMN_TIMESTAMP
    };

    public MessageDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void persistMessage(MessageVO messageVO) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MESSAGE, messageVO.getMessage());
        values.put(DBHelper.COLUMN_ROLE, messageVO.getRole());
        values.put(DBHelper.COLUMN_TIMESTAMP, messageVO.getTimestamp());
        database.insert(DBHelper.TABLE_MESSAGE, null, values);
    }

    public List<MessageVO> getAllMessages() {
        List<MessageVO> messageVOs = new ArrayList<>();
        Cursor cursor = database.query(DBHelper.TABLE_MESSAGE,allColumns, null, null, null, null, DBHelper.COLUMN_TIMESTAMP);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MessageVO messageVO = cursorToMessage(cursor);
            messageVOs.add(messageVO);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messageVOs;
    }

    //Assumming that the time stamp is unique for every message ideally every message showld have the unique resource id
    public MessageVO getMessageByTimestamp(long timestamp){

        String whereClause = DBHelper.COLUMN_TIMESTAMP +" = "+timestamp;
        Cursor cursor = database.query(DBHelper.TABLE_MESSAGE, allColumns, whereClause, null, null, null, DBHelper.COLUMN_TIMESTAMP);
        cursor.moveToFirst();
        MessageVO messageVO = null;
        if(!cursor.isAfterLast()){
            messageVO = cursorToMessage(cursor);
        }
        cursor.close();
        return messageVO;
    }


    private MessageVO cursorToMessage(Cursor cursor) {
        MessageVO messageVO = new MessageVO();
        messageVO.setId(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
        messageVO.setMessage(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MESSAGE)));
        messageVO.setRole(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ROLE)));
        messageVO.setTimestamp(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_TIMESTAMP)));
        return messageVO;
    }

    public static DBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DBHelper dbHelper) {
        MessageDAO.dbHelper = dbHelper;
    }
}
