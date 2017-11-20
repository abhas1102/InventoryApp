package com.example.yes.inventoryapp;

/**
 * Created by yes on 11/19/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yes.inventoryapp.ItemsContract.ProductEntry;

public class CatalogEditor extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;


    private Uri mImageUri;

    private int mQuantity;

    private Uri mCurrentItemUri;

    private EditText mNameEditText;

    private EditText mModelEditText;

    private Spinner mGradeSpinner;

    private ImageView mPhoto;

    private EditText mPrice;

    private EditText mSupplierName;

    private EditText mSupplierEmail;

    private EditText mQuantityEditText;

    private TextView mPhotoHintText;

    private Button mAddItemButton;

    private Button mRejectItemButton;

    private int mGrade = ProductEntry.GRADE_UNKNOWN;

    private boolean mItemHasChanged = false;

    private int mCurrentQuantity = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    boolean hasAllRequiredValues = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_catalog);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Find all relevant views that that suits according to the user input
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mModelEditText = (EditText) findViewById(R.id.edit_item_model);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mGradeSpinner = (Spinner) findViewById(R.id.spinner_grade);
        mPhoto = (ImageView) findViewById(R.id.edit_item_photo);
        mPrice = (EditText) findViewById(R.id.edit_item_price);
        mSupplierName = (EditText) findViewById(R.id.edit_item_supplier_name);
        mSupplierEmail = (EditText) findViewById(R.id.edit_item_supplier_email);
        mAddItemButton = (Button) findViewById(R.id.addItemButton);
        mRejectItemButton = (Button) findViewById(R.id.rejectItemButton);
        mPhotoHintText = (TextView) findViewById(R.id.add_or_edit_photo_hint);


        // creating a new Item.
        if (mCurrentItemUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            mPhotoHintText.setText(getText(R.string.add_photo_hint_text));
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mPhoto.setImageResource(R.drawable.icon_empty_storehouse);
            mAddItemButton.setVisibility(View.GONE);
            mRejectItemButton.setVisibility(View.GONE);

            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit_product));
            mPhotoHintText.setText(getText(R.string.edit_photo_hint_text));
            mSupplierName.setEnabled(false);
            mSupplierEmail.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            mAddItemButton.setVisibility(View.VISIBLE);
            mRejectItemButton.setVisibility(View.VISIBLE);

            // Initialize a loader to read the Item data from the database
            // and display the current values
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // We are using touchListener to determine the user activity of unsaved data
        mNameEditText.setOnTouchListener(mTouchListener);
        mModelEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mGradeSpinner.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);
        mAddItemButton.setOnTouchListener(mTouchListener);
        mRejectItemButton.setOnTouchListener(mTouchListener);

        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemButton(v);
            }
        });

        mRejectItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectItemButton(v);
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mItemHasChanged = true;
            }
        });

        setupSpinner();


    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mPhoto.setImageURI(mImageUri);
                mPhoto.invalidate();
            }
        }
    }

    public void orderMore() {
        Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + mSupplierEmail.getText().toString().trim()));
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order: " +
                mNameEditText.getText().toString().trim() +
                " " + mModelEditText.getText().toString().trim());
        String message = "We are going to make a new order of: " +
                mNameEditText.getText().toString().trim() +
                " " +
                mModelEditText.getText().toString().trim() + "." +
                "\n" +
                "Please confirm that I can receive ___ pcs." +
                "\n" +
                "\n" +
                "With regards," + "\n" +
                "_________________";
        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(intent);
    }


    /**
     * Spinner for allowing the user to select the grade of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner.
        ArrayAdapter gradeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_grade_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        gradeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGradeSpinner.setAdapter(gradeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.grade_new))) {
                        mGrade = ProductEntry.GRADE_NEW;
                    } else if (selection.equals(getString(R.string.grade_used))) {
                        mGrade = ProductEntry.GRADE_USED;
                    } else {
                        mGrade = ProductEntry.GRADE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGrade = ProductEntry.GRADE_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save product into database.
     */
    private boolean saveProduct() {

        int quantity;

        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String modelString = mModelEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();


        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(modelString) &&
                TextUtils.isEmpty(quantityString) &&
                mGrade == ProductEntry.GRADE_UNKNOWN &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                mImageUri == null) {
            // Because of no fields are modified, we can return early without creating a new product.
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }

        // Create a ContentValues object
        ContentValues values = new ContentValues();


        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_quantity), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            quantity = Integer.parseInt(quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_price), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.validation_msg_product_image), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, mImageUri.toString());
        }

        // OPTIONAL VALUES
        values.put(ProductEntry.COLUMN_PRODUCT_MODEL, modelString);             // optional, nullable
        values.put(ProductEntry.COLUMN_PRODUCT_GRADE, mGrade);                  // always have a value
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);      // optional, nullable
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);    // optional, nullable


        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

        hasAllRequiredValues = true;
        return hasAllRequiredValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                if (hasAllRequiredValues == true) {
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order more" menu option
            case R.id.action_add_more:
                orderMore();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(CatalogEditor.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, go to parent activity.
                                NavUtils.navigateUpFromSameTask(CatalogEditor.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show message that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
       //defining a projection with all required attributes
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_GRADE,
                ProductEntry.COLUMN_PRODUCT_PICTURE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current product
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_MODEL);
            int gradeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_GRADE);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String model = cursor.getString(modelColumnIndex);
            int grade = cursor.getInt(gradeColumnIndex);
            String imageUriString = cursor.getString(pictureColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            mQuantity = quantity;
            mImageUri = Uri.parse(imageUriString);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mModelEditText.setText(model);
            mPhoto.setImageURI(mImageUri);
            mPrice.setText(price);
            mSupplierName.setText(supplierName);
            mSupplierEmail.setText(supplierEmail);
            mQuantityEditText.setText(Integer.toString(quantity));

           //Mapping the value to the spinner grade.
            switch (grade) {
                case ProductEntry.GRADE_NEW:
                    mGradeSpinner.setSelection(1);
                    break;
                case ProductEntry.GRADE_USED:
                    mGradeSpinner.setSelection(2);
                    break;
                default:
                    mGradeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mModelEditText.setText("");
        mPhoto.setImageResource(R.drawable.icon_empty_storehouse);
        mPrice.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mQuantityEditText.setText("");
        mGradeSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // An AlertDialog.Builder and setting the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm for deleting the product .
     */
    private void showDeleteConfirmationDialog() {
        // An AlertDialog.Builder and setting the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    public void addItemButton(View view) {
        mQuantity++;
        displayQuantity();
    }

    public void rejectItemButton(View view) {
        if (mQuantity == 0) {
            Toast.makeText(this, "Not Allowed!!", Toast.LENGTH_SHORT).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityEditText.setText(String.valueOf(mQuantity));
    }
}
