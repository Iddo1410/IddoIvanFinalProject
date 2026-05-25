// הגדרת החבילה (Package) שבה נמצא הקובץ בפרויקט שלך
package com.example.iddoivanfinalproject.adapter;

// ייבוא מחלקות וספריות נדרשות (מעברים בין מסכים, תצוגה, רשימות, תמונות וכו')
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.Itemdetails;
import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import org.jspecify.annotations.NonNull;

import java.util.List;

// הגדרת המחלקה של האדפטר (מתאם). יורשת מ-RecyclerView.Adapter ותפקידה להציג רשימה של מוצרים (Items)
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    // רשימה שתשמור את כל המוצרים שאנחנו רוצים להציג במסך
    private List<Item> items;

    // בנאי (Constructor) המקבל את רשימת המוצרים כאשר יוצרים את האדפטר
    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    // מתודה זו מופעלת כשהמערכת צריכה ליצור שורה חדשה ברשימה (ViewHolder חדש)
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב (XML) של שורת מוצר אחת (activity_itemrow) והפיכתו לאובייקט View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_itemrow, parent, false);
        // החזרת ה-ViewHolder שומר את הרכיבים של השורה הזו
        return new ItemViewHolder(view);
    }

    // מתודה זו מקשרת בין הנתונים של מוצר ספציפי לבין התצוגה שלו בשורה
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // שליפת המוצר (Item) הספציפי מתוך הרשימה לפי המיקום (position) שלו
        Item item = items.get(position);

        // הגדרת שם המוצר בתיבת הטקסט המתאימה
        holder.tvName.setText(item.getName());
        // הגדרת תיאור המוצר בתיבת הטקסט המתאימה
        holder.tvDesc.setText(item.getDetails());
        // הגדרת מחיר המוצר (המרה ממספר למחרוזת בעזרת String.valueOf)
        holder.tvPrice.setText(String.valueOf(item.getPrice()));

        // טעינת תמונה: בדיקה אם יש תמונה למוצר (הערך אינו null)
        if (item.getPic() != null) {
            // המרת המחרוזת של התמונה (Base64) לתמונה אמיתית (Bitmap) והצגתה
            holder.ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        }

        // מאזין לחיצה: הגדרת פעולה שתקרה כאשר לוחצים על השורה כולה
        holder.itemView.setOnClickListener(v -> {
            // יצירת 'כוונה' (Intent) לעבור מהמסך הנוכחי למסך פרטי המוצר (Itemdetails)
            Intent intent = new Intent(v.getContext(), Itemdetails.class);
            // העברת מזהה (ID) המוצר למסך הבא, כדי ש-Itemdetails ידע איזה מוצר לטעון ולהציג
            intent.putExtra("ITEM_ID", item.getId());
            // הפעלת המסך החדש
            v.getContext().startActivity(intent);
        });
    }

    // מתודה שמחזירה את מספר המוצרים הכולל ברשימה, כדי שהרשימה תדע כמה שורות להציג
    @Override
    public int getItemCount() {
        return items.size();
    }

    // מחלקה פנימית המשמשת לשמירת הרכיבים (Views) של כל שורה כדי לייעל את ביצועי האפליקציה
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        // הגדרת משתנים עבור כל רכיבי התצוגה (טקסטים ותמונה) שנמצאים בשורה
        TextView tvName, tvDesc, tvPrice;
        ImageView ivPic;

        // הבנאי של המחלקה - מופעל פעם אחת עבור כל שורה שנוצרת
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // מציאת הרכיבים בקובץ ה-XML לפי ה-ID שלהם וקישורם למשתנים שהגדרנו למעלה
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivPic = itemView.findViewById(R.id.ivitemPic);
        }
    }
}