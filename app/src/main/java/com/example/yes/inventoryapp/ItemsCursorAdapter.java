package com.example.yes.inventoryapp;

import android.widget.CursorAdapter;

/**
 * Created by yes on 11/19/2017.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yes.inventoryapp.ItemsContract;

import static android.content.ContentValues.TAG;

public class ItemsCursorAdapter extends CursorAdapter {

    public ItemsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //Use of bindview will started from here
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView modelTextView = (TextView) view.findViewById(R.id.item_model);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_current_quantity);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.product_image);
        ImageButton buyImageButton = (ImageButton) view.findViewById(R.id.item_buy_button);

        // Find the columns of product attributes that we want.
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(ItemsContract.ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ItemsContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int modelColumnIndex = cursor.getColumnIndex(ItemsContract.ProductEntry.COLUMN_PRODUCT_MODEL);
        int priceColumnIndex = cursor.getColumnIndex(ItemsContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemsContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int photoColumnIndex = cursor.getColumnIndex(ItemsContract.ProductEntry.COLUMN_PRODUCT_PICTURE);

        String productName = cursor.getString(nameColumnIndex);
        String productModel = cursor.getString(modelColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int quantityProduct = cursor.getInt(quantityColumnIndex);
        String imageUriString = cursor.getString(photoColumnIndex);
        Uri productImageUri = Uri.parse(imageUriString);

        // If the product model is empty string or null, then hide TextView
        if (TextUtils.isEmpty(productModel)) {
            modelTextView.setVisibility(View.GONE);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        modelTextView.setText(productModel);
        priceTextView.setText(productPrice);
        quantityTextView.setText(String.valueOf(quantityProduct));
        photoImageView.setImageURI(productImageUri);

        buyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ItemsContract.ProductEntry.CONTENT_URI, productIdColumnIndex);
                adjustProductQuantity(context, productUri, quantityProduct);
            }
        });
    }
    private void adjustProductQuantity(Context context, Uri productUri, int currentQuantityInStock) {

        // Decrease 1 from present value if quantity of product >= 1
        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

        if (currentQuantityInStock == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemsContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Reflect error message in Logs with info about pass update.
            Log.i(TAG, context.getString(R.string.buy_msg_confirm));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();
            // Reflect error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.error_msg_stock_update));
        }


    }
}
