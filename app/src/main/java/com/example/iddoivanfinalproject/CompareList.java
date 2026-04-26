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

import java.util.ArrayList;
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

    // רכיבי פריט 3
    private TextView tvName3, tvBrand3, tvYear3, tvPrice3, tvDetails3;
    private ImageView imgItem3;

    private DataBaseService.DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_list);

        initViews();
        setupSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (spCompareCategory != null && spCompareCategory.getSelectedItem() != null) {
            loadComparisonData(spCompareCategory.getSelectedItem().toString());
        }
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

        tvName3 = findViewById(R.id.tvName3);
        tvBrand3 = findViewById(R.id.tvBrand3);
        tvYear3 = findViewById(R.id.tvYear3);
        tvPrice3 = findViewById(R.id.tvPrice3);
        tvDetails3 = findViewById(R.id.tvDetails3);
        imgItem3 = findViewById(R.id.imgItem3);

        databaseService = DataBaseService.DatabaseService.getInstance();
    }

    private void setupSpinner() {
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
        databaseService.getAllItems(new DataBaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> allStoreItems) {
                if (allStoreItems == null) return;

                databaseService.getCompareByType(type, new DataBaseService.DatabaseCallback<Compareitem>() {
                    @Override
                    public void onCompleted(Compareitem dbCompare) {
                        if (dbCompare != null && dbCompare.getItemArrayList() != null && !dbCompare.getItemArrayList().isEmpty()) {

                            List<Item> validItems = new ArrayList<>();
                            for (Item compareItem : dbCompare.getItemArrayList()) {
                                boolean existsInStore = false;
                                for (Item storeItem : allStoreItems) {
                                    if (compareItem.getId() != null && storeItem.getId() != null && compareItem.getId().equals(storeItem.getId())) {
                                        existsInStore = true;
                                        break;
                                    }
                                }
                                if (existsInStore) {
                                    validItems.add(compareItem);
                                }
                            }

                            // --- מנגנון הריפוי האוטומטי (Auto-Heal) ---
                            // אם מצאנו שיש ב-Firebase פריטים שכבר לא קיימים בחנות ("רוחות רפאים"), אנחנו מנקים אותם לתמיד!
                            if (validItems.size() != dbCompare.getItemArrayList().size()) {
                                dbCompare.getItemArrayList().clear();
                                dbCompare.getItemArrayList().addAll(validItems);

                                databaseService.updateCompareList(dbCompare, new DataBaseService.DatabaseCallback<Void>() {
                                    @Override public void onCompleted(Void o) {}
                                    @Override public void onFailed(Exception e) {}
                                });
                            }
                            // ------------------------------------------------

                            if (!validItems.isEmpty()) {
                                cardTable.setVisibility(View.VISIBLE);
                                tvEmptyMessage.setVisibility(View.GONE);

                                populateItem1(validItems.get(0));

                                if (validItems.size() > 1) {
                                    populateItem2(validItems.get(1));
                                } else {
                                    clearItem2();
                                }

                                if (validItems.size() > 2) {
                                    populateItem3(validItems.get(2));
                                } else {
                                    clearItem3();
                                }

                                applyHighlighting(validItems);

                            } else {
                                cardTable.setVisibility(View.GONE);
                                tvEmptyMessage.setVisibility(View.VISIBLE);
                                tvEmptyMessage.setText("אין מוצרים זמינים להשוואה בקטגוריית " + type);
                            }

                        } else {
                            cardTable.setVisibility(View.GONE);
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                            tvEmptyMessage.setText("אין מוצרים להשוואה בקטגוריית " + type);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(CompareList.this, "שגיאה בטעינת ההשוואה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(CompareList.this, "שגיאה בחיבור לנתוני החנות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyHighlighting(List<Item> items) {
        resetColors();

        if (items.size() < 2) return;

        double minPrice = items.get(0).getPrice();
        for (Item item : items) {
            if (item.getPrice() < minPrice) minPrice = item.getPrice();
        }

        int maxYear = 0;
        for (Item item : items) {
            try {
                int year = Integer.parseInt(item.getYear());
                if (year > maxYear) maxYear = year;
            } catch (Exception ignored) {}
        }

        if (items.size() >= 1) {
            if (items.get(0).getPrice() == minPrice) tvPrice1.setTextColor(Color.parseColor("#27AE60"));
            try { if (Integer.parseInt(items.get(0).getYear()) == maxYear) tvYear1.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }

        if (items.size() >= 2) {
            if (items.get(1).getPrice() == minPrice) tvPrice2.setTextColor(Color.parseColor("#27AE60"));
            try { if (Integer.parseInt(items.get(1).getYear()) == maxYear) tvYear2.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }

        if (items.size() >= 3 && tvPrice3 != null && tvYear3 != null) {
            if (items.get(2).getPrice() == minPrice) tvPrice3.setTextColor(Color.parseColor("#27AE60"));
            try { if (Integer.parseInt(items.get(2).getYear()) == maxYear) tvYear3.setTextColor(Color.parseColor("#27AE60")); } catch(Exception ignored){}
        }
    }

    private void resetColors() {
        tvPrice1.setTextColor(Color.BLACK);
        tvYear1.setTextColor(Color.BLACK);
        tvPrice2.setTextColor(Color.BLACK);
        tvYear2.setTextColor(Color.BLACK);

        if (tvPrice3 != null) tvPrice3.setTextColor(Color.BLACK);
        if (tvYear3 != null) tvYear3.setTextColor(Color.BLACK);
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

    private void populateItem3(Item item) {
        if (tvName3 != null) tvName3.setText(item.getName());
        if (tvBrand3 != null) tvBrand3.setText(item.getBrand());
        if (tvYear3 != null) tvYear3.setText(item.getYear());
        if (tvPrice3 != null) tvPrice3.setText("₪" + item.getPrice());
        if (tvDetails3 != null) tvDetails3.setText(item.getDetails());
        if (imgItem3 != null && item.getPic() != null) imgItem3.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
    }

    private void clearItem2() {
        tvName2.setText("טרם נבחר");
        tvBrand2.setText("-");
        tvYear2.setText("-");
        tvPrice2.setText("-");
        tvDetails2.setText("-");
        imgItem2.setImageDrawable(null);
    }

    private void clearItem3() {
        if (tvName3 != null) tvName3.setText("טרם נבחר");
        if (tvBrand3 != null) tvBrand3.setText("-");
        if (tvYear3 != null) tvYear3.setText("-");
        if (tvPrice3 != null) tvPrice3.setText("-");
        if (tvDetails3 != null) tvDetails3.setText("-");
        if (imgItem3 != null) imgItem3.setImageDrawable(null);
    }
}