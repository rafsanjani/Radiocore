package com.foreverrafs.radiocore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import static com.foreverrafs.radiocore.data.NewsDatabaseContract.NewsInfo;

public class NewsOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "RadioCore.db";
    private static final int DATABASE_VERSION = 2;

    public NewsOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(NewsInfo.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NewsInfo.TABLE_NAME);
        this.onCreate(sqLiteDatabase);
    }


}
