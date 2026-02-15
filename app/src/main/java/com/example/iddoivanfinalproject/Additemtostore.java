package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

public class Additemtostore extends AppCompatActivity {

    private EditText etItemName, etItemPrice, etItemType, etItemBrand, etItemYear, etItemDetails;
    private Spinner spType, spBrand, spYear;
    private Button btnGallery, btnTakePic, btnAddItem;
    private ImageView imageView;

    private ImageButton btnBack;


    private DataBaseService.DatabaseService databaseService;


    /// Activity result launcher for capturing image from camera
    private ActivityResultLauncher<Intent> captureImageLauncher;



    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additemtostore);



        InitViews();

        /// request permission for the camera and storage
        ImageUtil.requestPermission(this);

        /// get the instance of the database service
        databaseService= DataBaseService.DatabaseService.getInstance();

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.typeArr,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.yearArr,
                android.R.layout.simple_spinner_item
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        ArrayAdapter<CharSequence> brandAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.brandArr,
                android.R.layout.simple_spinner_item
        );
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBrand.setAdapter(brandAdapter);









        /// register the activity result launcher for capturing image from camera
        captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                    }
                });







        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();


            }
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageFromCamera();

            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = etItemName.getText().toString();
                String itemType=spType.getSelectedItem().toString();
                String itemBrand = spBrand.getSelectedItem().toString();
                String itemPrice = etItemPrice.getText().toString();
                String itemYear=spYear.getSelectedItem().toString();
                String itemDetails=etItemDetails.getText().toString();


                String imageBase64 = ImageUtil.convertTo64Base(imageView);
                double price = Double.parseDouble(itemPrice);

                if (itemName.isEmpty() || itemType.isEmpty() || itemBrand.isEmpty() ||
                        itemPrice.isEmpty() || itemYear.isEmpty()||itemDetails.isEmpty() ) {
                    Toast.makeText(Additemtostore.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Additemtostore.this, "המוצר נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
                }

                /// generate a new id for the item
                String id = DataBaseService.DatabaseService.getInstance().generateUserId();


                Item newItem = new Item(id, itemName, itemType, itemBrand, price, itemYear, itemDetails, imageBase64);

                /// save the item to the database and get the result in the callback
                databaseService.createNewItem(newItem, new DataBaseService.DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(Void object) {
                        Log.d("TAG", "Item added successfully");
                        Toast.makeText(Additemtostore.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        /// clear the input fields after adding the item for the next item
                        Log.d("TAG", "Clearing input fields");

                        Intent intent = new Intent(Additemtostore.this, AdminPage.class);
                        startActivity(intent);


                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e("TAG", "Failed to add item", e);
                        Toast.makeText(Additemtostore.this, "Failed to add food", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });
    }

    private void InitViews() {
        etItemName = findViewById(R.id.etName);
        etItemPrice = findViewById(R.id.etPrice);
        etItemDetails=findViewById(R.id.etDetails);
        spType = findViewById(R.id.spType);
        spBrand = findViewById(R.id.spBrand);
        spYear=findViewById(R.id.spYear);
        btnGallery = findViewById(R.id.btnGallery);
        btnTakePic = findViewById(R.id.btnCamera);
        btnAddItem = findViewById(R.id.btnAdd);
        imageView = findViewById(R.id.myImg);
    }


    /// select image from gallery
    private void selectImageFromGallery() {
        //   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //  selectImageLauncher.launch(intent);

        imageChooser();
    }

    /// capture image from camera
    private void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageLauncher.launch(takePictureIntent);
    }





    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }

}

