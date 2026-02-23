package com.example.iddoivanfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice, tvBrand, tvType, tvYear;
    private ImageView ivPic;
    private Button btnBack, btnAddToCart;
    private CheckBox cbCompare;
    private DataBaseService.DatabaseService databaseService;

    Compareitem compareitem= new Compareitem();

    Item currentItem;
    DateTimeFormatter formatter;
    LocalDate date;
    String formattedDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);



        // קבלת תאריך בלבד

         date = LocalDate.now();
        System.out.println(date); // פלט לדוגמה: 2024-05-20

// עיצוב התאריך לפורמט ישראלי (יום/חודש/שנה)
       formatter  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
         formattedDate = date.format(formatter);
        initViews();
        // חיבור רכיבי ה-UI


        // קבלת ה-ID מה-Intent (חשוב להגדיר כ-final לשימוש בתוך ה-Listener)
        final String itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId != null) {
            databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
                @Override
                public void onCompleted(Item item) {
                    if (item == null) {
                        Toast.makeText(Itemdetails.this, "הפריט לא נמצא", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentItem=item;
                    // עדכון התצוגה בנתוני הפריט
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(String.valueOf(item.getPrice()));
                    tvBrand.setText("Brand: " + item.getBrand());
                    tvType.setText("Type: "+item.getType());
                    tvYear.setText("Year: " +String.valueOf((item.getYear())));
                    if (item.getPic() != null && !item.getPic().isEmpty()) {
                        ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
                    }
                }





                    @Override
                    public void onFailed(Exception e) {
                        tvName.setText("Error loading item");
                    }
                });
            }

        }

    private void initViews() {

        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvBrand=findViewById(R.id.tvBrand);
        tvType=findViewById(R.id.tvType);
        tvYear=findViewById(R.id.tvYear);
        btnAddToCart=findViewById(R.id.btnAddToCart);
        btnBack=findViewById(R.id.btnBack);
        cbCompare=findViewById(R.id.cbCompare);
        ivPic = findViewById(R.id.ivPic);

        cbCompare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                if(b){


                    String id=databaseService.generateCompareId();
                    compareitem.setId(currentItem.getType());
                  compareitem.setDate(formattedDate);
                  compareitem.setType(currentItem.getType());
                  if(compareitem.getItemArrayList()==null){

                      compareitem.setItemArrayList(new ArrayList<>());
                  }
                  compareitem.getItemArrayList().add(currentItem);
                  databaseService.createNewCompareList(compareitem, new DataBaseService.DatabaseCallback<Void>() {
                      @Override
                      public void onCompleted(Void object) {

                      }

                      @Override
                      public void onFailed(Exception e) {

                      }
                  });

                }

            }
        });


        databaseService = DataBaseService.DatabaseService.getInstance();
    }
}