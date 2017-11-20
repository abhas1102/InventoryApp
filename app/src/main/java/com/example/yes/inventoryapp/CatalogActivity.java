package com.example.yes.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yes.inventoryapp.ItemsContract.ProductEntry;

/**
 * Shows the list of products which was entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;
    ItemsCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Floating button to go on CatalogEditor
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, CatalogEditor.class);
                startActivity(intent);
            }
        });


        // Finding ListView which will be populated with the Items
        ListView ItemListView = (ListView) findViewById(R.id.list);

        //  setting empty view on the ListView.
        View emptyView = findViewById(R.id.empty_view);
        ItemListView.setEmptyView(emptyView);

        // Creating an Adapter to form a list item for each row of product data in the Cursor.
        // There is no Items data yet, so pass in null for theCursor
        mCursorAdapter = new ItemsCursorAdapter(this, null);
        ItemListView.setAdapter(mCursorAdapter);

        ItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to EditorActivity
                Intent intent = new Intent(CatalogActivity.this, CatalogEditor.class);

                // From the content URI that represents the specific product that was clicked on,
                // by appending "id" (passed as input to this method) onto the PerEntry#CONTENT_URI

                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                Log.i(LOG_TAG, "Current URI: " + currentProductUri);

                // Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                // Launch the EditorActivity to display the data for the current product
                startActivity(intent);
            }
        });



        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    private void insertProduct() {

        // Get Uri for example photo from drawable resource
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.example)
                + '/' + getResources().getResourceTypeName(R.drawable.example) + '/' + getResources().getResourceEntryName(R.drawable.example));

        Log.i(LOG_TAG, "Example photo uri: " + String.valueOf(imageUri));

        // Create a ContentValues object

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_data_product_name));
        values.put(ProductEntry.COLUMN_PRODUCT_MODEL, getString(R.string.dummy_data_product_model));
        values.put(ProductEntry.COLUMN_PRODUCT_GRADE, ProductEntry.GRADE_NEW);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 9);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, String.valueOf(imageUri));
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, getString(R.string.dummy_data_supplier_email));
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, getString(R.string.dummy_data_supplier_name));
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 97.00);


        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Defining a projection
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_PICTURE
        };

        // This loader will execute the ContentProvider's query method ona background thread
        return new CursorLoader(this,       // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductCursorAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback is called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from product database");
    }
}
