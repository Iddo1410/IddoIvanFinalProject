package com.example.iddoivanfinalproject;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

public class Itemdetails extends AppCompatActivity {

    private TextView tvName, tvDescription, tvPrice;
    private ImageView ivPic; // הגדרת משתנה התמונה
    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetails);

        // חיבור המשתנים לרכיבים במסך (XML)
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        ivPic = findViewById(R.id.ivPic); // חיבור התמונה

        databaseService = DataBaseService.DatabaseService.getInstance();

        // קבלת ה-ID של הפריט מהמסך הקודם
        String itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId != null) {
            databaseService.getItemById(itemId, new DataBaseService.DatabaseCallback<Item>() {
                @Override
                public void onCompleted(Item item) {
                    // עדכון הטקסטים
                    tvName.setText(item.getName());
                    tvDescription.setText(item.getDetails());
                    tvPrice.setText(item.getPrice() + " ₪"); // הוספת סימן שקלים

                    // עדכון התמונה (אם קיימת)
                    if(item.getPic() != null) {
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
}
