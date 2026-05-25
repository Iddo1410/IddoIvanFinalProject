// הגדרת החבילה (Package) שבה נמצא הקובץ בפרויקט
package com.example.iddoivanfinalproject.adapter;

// ייבוא מחלקות וספריות נדרשות (לעיצוב, תצוגה, רשימות, וטיפול בתאריכים)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// הגדרת המחלקה של האדפטר (מתאם) המציג את היסטוריית ההזמנות. יורשת מ-RecyclerView.Adapter
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    // רשימה שתשמור את כל ההזמנות שאנחנו רוצים להציג במסך
    private List<Order> ordersList;

    // בנאי (Constructor) המקבל את רשימת ההזמנות בזמן יצירת האדפטר
    public OrderAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    // מתודה שמופעלת כשהמערכת צריכה ליצור שורה חדשה ברשימה (ViewHolder חדש)
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב (XML) של שורת הזמנה אחת (orderrow) והפיכתו לאובייקט View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderrow, parent, false);
        // החזרת ה-ViewHolder ששומר את הרכיבים של השורה שנוצרה
        return new OrderViewHolder(view);
    }

    // מתודה שמקשרת בין הנתונים של הזמנה ספציפית לבין התצוגה שלה בשורה
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // שליפת ההזמנה הנוכחית מתוך הרשימה לפי המיקום (position) שלה
        Order currentOrder = ordersList.get(position);

        // הגדרת אימייל הקונה בתיבת הטקסט המתאימה, בתוספת כיתוב מקדים
        holder.tvEmail.setText("אימייל קונה: " + currentOrder.getUserEmail());

        // הגדרת סך הכל לתשלום בתיבת הטקסט, בתוספת סמל השקל (₪)
        holder.tvTotal.setText("סה\"כ שולם: ₪" + currentOrder.getTotalPrice());

        // --- טיפול בתאריך ההזמנה ---
        // יצירת תבנית (פורמט) להצגת תאריך ושעה (יום/חודש/שנה שעות:דקות), מותאמת לישראל
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("he", "IL"));
        // המרת חותמת הזמן (Timestamp שנשמר כ-long במילישניות) לאובייקט של תאריך (Date)
        Date date = new Date(currentOrder.getTimestamp());
        // הגדרת התאריך המעוצב בתיבת הטקסט
        holder.tvDate.setText("תאריך רכישה: " + sdf.format(date));
        // -----------------------------

        // --- בניית רשימת הפריטים בהזמנה ---
        // שימוש ב-StringBuilder כדי לחבר טקסטים בצורה יעילה לתוך מחרוזת אחת
        StringBuilder itemsDetails = new StringBuilder("פריטים:\n");

        // בדיקה האם קיימת רשימת פריטים להזמנה והיא אינה ריקה (null)
        if (currentOrder.getItems() != null) {
            // לולאה שעוברת על כל הפריטים (Cart) שנמצאים בהזמנה
            for (Cart item : currentOrder.getItems()) {
                // הוספת שורה לכל פריט: כמות + שם המוצר (לדוגמה: "- 2 x רכב מאזדה"), וירידת שורה
                itemsDetails.append("- ").append(item.getQuantity())
                        .append(" x ").append(item.getName()).append("\n");
            }
        } else {
            // אם במקרה אין פריטים בהזמנה, נוסיף הודעה מתאימה
            itemsDetails.append("לא נמצאו פריטים.");
        }

        // הגדרת הטקסט המלא של כל הפריטים לתיבת הטקסט. trim() חותך רווחים או ירידות שורה מיותרות בסוף
        holder.tvItems.setText(itemsDetails.toString().trim());
        // ----------------------------------
    }

    // מתודה שמחזירה את כמות ההזמנות הכוללת ברשימה כדי שה-RecyclerView ידע כמה שורות לייצר
    @Override
    public int getItemCount() {
        // בדיקת בטיחות: אם הרשימה ריקה לגמרי, נחזיר 0 כדי לא לקרוס
        if (ordersList == null) {
            return 0;
        }
        // אחרת, החזר את מספר ההזמנות הקיים ברשימה
        return ordersList.size();
    }

    // מחלקה פנימית המשמשת לשמירת הרכיבים (Views) של כל שורה כדי לייעל ביצועים
    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        // הגדרת משתנים עבור תיבות הטקסט (אימייל, תאריך, רשימת פריטים וסה"כ לתשלום)
        TextView tvEmail, tvDate, tvItems, tvTotal;

        // הבנאי של המחלקה - מופעל פעם אחת עבור כל שורה שנוצרת
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView); // קריאה לבנאי של מחלקת האב

            // מציאת הרכיבים מתוך קובץ ה-XML של השורה (orderrow) לפי ה-ID שלהם וקישורם למשתנים
            tvEmail = itemView.findViewById(R.id.tvOrderEmail);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}