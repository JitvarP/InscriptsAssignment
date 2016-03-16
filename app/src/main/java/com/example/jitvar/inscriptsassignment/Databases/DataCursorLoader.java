package com.example.jitvar.inscriptsassignment.Databases;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by jitvar on 6/3/16.
 */
public class DataCursorLoader extends AsyncTaskLoader<Cursor> {
    public static final int CLIENT_ID = 0;
    public static final int CLIENT_NAME = 1;
    public static final int CLIENT_SIRET = 2;
    public static final int OWN_CLIENT = 3;
    public static final int CLIENT_ACTIVE = 4;
    public static final int CLIENT_ADDRESS_ID = 5;
    public static final int CLIENT_TYPE_ID = 6;

    private Cursor mCursor;
    private String table;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String orderBy;
    private String rawQuery;

    private DataCursorLoader(Context context) {
        super(context);
    }

    public DataCursorLoader(Context context, String table, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        this(context);
        this.table = table;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
    }

    public DataCursorLoader(Context context, String rawQuery, String[] selectionArgs) {
        this(context);
        this.rawQuery = rawQuery;
        this.selectionArgs = selectionArgs;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        Cursor cursor;
        if (rawQuery == null) {
            cursor = MessageDAO.getDbHelper()
                    .getReadableDatabase()
                    .query(table,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            orderBy);
        }
        else {
            cursor = MessageDAO.getDbHelper()
                    .getReadableDatabase()
                    .rawQuery(rawQuery,selectionArgs);
        }
        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
