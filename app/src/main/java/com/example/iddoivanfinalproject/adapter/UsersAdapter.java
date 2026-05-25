// הגדרת החבילה (Package) שבה נמצא הקובץ בתוך הפרויקט שלך
package com.example.iddoivanfinalproject.adapter;

// ייבוא מחלקות וספריות נדרשות של אנדרואיד (לתצוגה, רשימות, ורכיבי טקסט)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ייבוא מחלקות ספציפיות לפרויקט שלך (קובץ ה-R לזיהוי עיצובים, ומודל המשתמש)
import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.User;

import java.util.List;

// הגדרת מחלקת האדפטר (מתאם) עבור הצגת רשימת המשתמשים. יורשת מ-RecyclerView.Adapter
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    // רשימה שתשמור את כל המשתמשים (אובייקטים מסוג User) שאנחנו רוצים להציג
    private List<User> users;

    // בנאי (Constructor) - מופעל כאשר יוצרים את האדפטר ומקבל את רשימת המשתמשים
    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    // מתודה שמופעלת כשהמערכת צריכה ליצור שורה חדשה ברשימה (ViewHolder חדש)
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב (XML) של שורת משתמש בודד (activity_allusers) והפיכתו לאובייקט View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_allusers, parent, false);
        // החזרת אובייקט ה-ViewHolder ששומר את הרכיבים של השורה שיצרנו
        return new UserViewHolder(view);
    }

    // מתודה שמקשרת בין הנתונים של משתמש ספציפי לבין התצוגה שלו בשורה עצמה
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // שליפת המשתמש (User) הספציפי מתוך הרשימה לפי המיקום (position) שלו
        User user = users.get(position);

        // חיבור השם הפרטי והשם משפחה של המשתמש (עם רווח באמצע) והצגתו בתיבת הטקסט של השם
        holder.tvName.setText(user.getFname() + " " + user.getLname());

        // הגדרת כתובת האימייל של המשתמש בתיבת הטקסט המתאימה
        holder.tvEmail.setText(user.getEmail());
    }

    // מתודה שמחזירה את כמות המשתמשים הכוללת ברשימה, כדי שהרשימה תדע כמה שורות להציג
    @Override
    public int getItemCount() {
        return users.size();
    }

    // מחלקה פנימית המשמשת לשמירת הרכיבים (Views) של כל שורה כדי לא לחפש אותם שוב ושוב (לייעול הביצועים)
    static class UserViewHolder extends RecyclerView.ViewHolder {

        // משתנים עבור תיבות הטקסט שיוצגו בשורה (שם ואימייל)
        TextView tvName, tvEmail;

        // הבנאי של המחלקה - מופעל פעם אחת עבור כל שורה שנוצרת
        public UserViewHolder(@NonNull View itemView) {
            super(itemView); // קריאה לבנאי של מחלקת האב

            // מציאת הרכיבים מתוך קובץ ה-XML לפי ה-ID שלהם וקישורם למשתנים שהגדרנו למעלה
            // שים לב: לפי ה-ID נראה שהשתמשת בשמות כמו etFname ו-etEmailLogin מקובץ ה-XML של העיצוב
            tvName = itemView.findViewById(R.id.etFname);
            tvEmail = itemView.findViewById(R.id.etEmailLogin);
        }
    }
}