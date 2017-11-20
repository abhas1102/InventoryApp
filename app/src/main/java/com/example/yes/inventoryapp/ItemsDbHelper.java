package com.example.yes.inventoryapp;



/**
 * Created by yes on 11/19/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ItemsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ItemsDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "storehouse.db";

    private static final int DATABASE_VERSION = 1;

    public ItemsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemsContract.ProductEntry.TABLE_NAME;


    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                ItemsContract.ProductEntry.TABLE_NAME + "(" +
                ItemsContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_MODEL + " TEXT, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_GRADE + " INTEGER NOT NULL, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                ItemsContract.ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT, " +
                ItemsContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT NOT NULL, " +
                ItemsContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0);";
        Log.v(LOG_TAG, SQL_CREATE_PRODUCTS_TABLE);

        /**
         * Create table
         */
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
