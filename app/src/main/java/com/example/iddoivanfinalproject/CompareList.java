package com.example.iddoivanfinalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.iddoivanfinalproject.model.Compareitem;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.services.DataBaseService;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import java.util.List;

public class CompareList extends AppCompatActivity {

    private TextView tvEmptyMessage, tvTitle;
    private CardView cardTable;
    private Spinner spCompareCategory;

    // רכיבי פריט 1
    private TextView tvName1, tvBrand1, tvYear1, tvPrice1, tvDetails1;
    private ImageView imgItem1;

    // רכיבי פריט 2
    private TextView tvName2, tvBrand2, tvYear2, tvPrice2, tvDetails2;
    private ImageView imgItem2;

    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_list);

        initViews();
        setupSpinner();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        cardTable = findViewById(R.id.cardTable);
        spCompareCategory = findViewById(R.id.spCompareCategory);

        tvName1 = findViewById(R.id.tvName1);
        tvBrand1 = findViewById(R.id.tvBrand1);
        tvYear1 = findViewById(R.id.tvYear1);
        tvPrice1 = findViewById(R.id.tvPrice1);
        tvDetails1 = findViewById(R.id.tvDetails1);
        imgItem1 = findViewById(R.id.imgItem1);

        tvName2 = findViewById(R.id.tvName2);
        tvBrand2 = findViewById(R.id.tvBrand2);
        tvYear2 = findViewById(R.id.tvYear2);
        tvPrice2 = findViewById(R.id.tvPrice2);
        tvDetails2 = findViewById(R.id.tvDetails2);
        imgItem2 = findViewById(R.id.imgItem2);

        databaseService = DataBaseService.DatabaseService.getInstance();
    }

    private void setupSpinner() {
        // משיכת הקטגוריות מהקובץ arrs.xml
        String[] categoriesArray = getResources().getStringArray(R.array.typeArr);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCompareCategory.setAdapter(adapter);

        String initialType = getIntent().getStringExtra("COMPARE_TYPE");
        if (initialType != null) {
            int pos = adapter.getPosition(initialType);
            if (pos >= 0) spCompareCategory.setSelection(pos);
        }

        spCompareCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadComparisonData(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadComparisonData(String type) {
        databaseService.getCompareByType(type, new DataBaseService.DatabaseCallback<Compareitem>() {
            @Override
            public void onCompleted(Compareitem dbCompare) {
                if (dbCompare != null && dbCompare.getItemArrayList() != null && !dbCompare.getItemArrayList().isEmpty()) {
                    List<Item> items = dbCompare.getItemArrayList();
                    cardTable.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setVisibility(View.GONE);

                    populateItem1(items.get(0));

                    if (items.size() > 1) {
                        populateItem2(items.get(1));
                        // ביצוע ההשוואה והדגשת הערכים הטובים יותר
                        applyHighlighting(items.get(0), items.get(1));
                    } else {
                        clearItem2();
                        resetColors(); // איפוס צבעים אם יש רק פריט אחד
                    }
                } else {
                    cardTable.setVisibility(View.GONE);
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    tvEmptyMessage.setText("אין מוצרים להשוואה בקטגוריית " + type);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CompareList.this, "שגיאה בטעינה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // לוגיקת ההדגשה (Highlighting)
    private void applyHighlighting(Item item1, Item item2) {
        // --- השוואת מחיר (נמוך יותר טוב יותר) ---
        if (item1.getPrice() < item2.getPrice()) {
            tvPrice1.setTextColor(Color.parseColor("#27AE60")); // ירוק
            tvPrice2.setTextColor(Color.BLACK);
        } else if (item2.getPrice() < item1.getPrice()) {
            tvPrice2.setTextColor(Color.parseColor("#27AE60"));
            tvPrice1.setTextColor(Color.BLACK);
        } else {
            resetPriceColors(); // שווים
        }

        // --- השוואת שנה (גבוהה/חדשה יותר טוב יותר) ---
        try {
            int year1 = Integer.parseInt(item1.getYear());
            int year2 = Integer.parseInt(item2.getYear());

            if (year1 > year2) {
                tvYear1.setTextColor(Color.parseColor("#27AE60"));
                tvYear2.setTextColor(Color.BLACK);
            } else if (year2 > year1) {
                tvYear2.setTextColor(Color.parseColor("#27AE60"));
                tvYear1.setTextColor(Color.BLACK);
            } else {
                tvYear1.setTextColor(Color.BLACK);
                tvYear2.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
            // במקרה של שגיאה בפורמט השנה
            tvYear1.setTextColor(Color.BLACK);
            tvYear2.setTextColor(Color.BLACK);
        }
    }

    private void resetColors() {
        tvPrice1.setTextColor(Color.BLACK);
        tvPrice2.setTextColor(Color.BLACK);
        tvYear1.setTextColor(Color.BLACK);
        tvYear2.setTextColor(Color.BLACK);
    }

    private void resetPriceColors() {
        tvPrice1.setTextColor(Color.BLACK);
        tvPrice2.setTextColor(Color.BLACK);
    }

    private void populateItem1(Item item) {
        tvName1.setText(item.getName());
        tvBrand1.setText(item.getBrand());
        tvYear1.setText(item.getYear());
        tvPrice1.setText("₪" + item.getPrice());
        tvDetails1.setText(item.getDetails());
        if (item.getPic() != null) imgItem1.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    private void populateItem2(Item item) {
        tvName2.setText(item.getName());
        tvBrand2.setText(item.getBrand());
        tvYear2.setText(item.getYear());
        tvPrice2.setText("₪" + item.getPrice());
        tvDetails2.setText(item.getDetails());
        if (item.getPic() != null) imgItem2.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    private void clearItem2() {
        tvName2.setText("טרם נבחר");
        tvBrand2.setText("-");
        tvYear2.setText("-");
        tvPrice2.setText("-");
        tvDetails2.setText("-");
        imgItem2.setImageDrawable(null);
    }
}