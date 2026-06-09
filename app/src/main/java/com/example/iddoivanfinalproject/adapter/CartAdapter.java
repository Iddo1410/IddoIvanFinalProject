// הגדרת החבילה (Package) שבה נמצא הקובץ בפרויקט שלך
package com.example.iddoivanfinalproject.adapter;

// ייבוא מחלקות וספריות נדרשות (לתמונות, תצוגה, רשימות וכו')
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.CartActivity;
import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import java.util.List;

// הגדרת המחלקה של האדפטר (מתאם). הוא יורש מ-RecyclerView.Adapter ותפקידו לקשר בין נתוני העגלה לתצוגת הרשימה
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // רשימה שתשמור את כל המוצרים שנמצאים בעגלת הקניות
    private List<Cart> cartList;
    // משתנה מסוג ממשק (Interface) שמאזין לפעולות המשתמש (מחיקה, הוספה, הפחתה, ולחיצה)
    private CartActivity.CartActionListener actionListener;

    // בנאי (Constructor) המקבל את רשימת המוצרים ואת המאזין (המסך שמשתמש באדפטר)
    public CartAdapter(List<Cart> cartList, CartActivity.CartActionListener actionListener) {
        this.cartList = cartList;
        this.actionListener = actionListener;
    }

    // מתודה זו נקראת כשהמערכת צריכה ליצור שורה חדשה ברשימה (ViewHolder חדש)
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // טעינת קובץ העיצוב (XML) של שורה אחת בעגלה (activity_cartrow) והפיכתו לאובייקט View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_cartrow, parent, false);
        // החזרת ה-ViewHolder ששומר את הרכיבים של השורה
        return new CartViewHolder(view);
    }

    // מתודה זו מקשרת בין הנתונים של פריט ספציפי לבין התצוגה שלו בשורה
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // שליפת הפריט (מוצר) הספציפי מתוך הרשימה לפי המיקום שלו
        Cart cart = cartList.get(position);

        // הגדרת שם המוצר בתיבת הטקסט המתאימה בשורה
        holder.tvName.setText(cart.getName());

        // --- חישוב המחיר: מחיר המוצר כפול הכמות ---
        double totalPrice = cart.getPrice() * cart.getQuantity();
        // הגדרת המחיר המחושב בתיבת הטקסט בתוספת סמל השקל
        holder.tvPrice.setText(totalPrice + " ₪");
        // ------------------------------------------

        // הגדרת הכמות בתיבת הטקסט (ממירים את המספר לטקסט בעזרת String.valueOf)
        holder.tvQuantity.setText(String.valueOf(cart.getQuantity()));

        // טעינת תמונה: בדיקה האם קיימת תמונה לפריט והיא אינה ריקה
        if (cart.getPic() != null && !cart.getPic().isEmpty()) {
            try {
                // המרת מחרוזת התמונה מפורמט Base64 חזרה לאובייקט תמונה (Bitmap)
                Bitmap bitmap = ImageUtil.convertFrom64base(cart.getPic());
                // הצגת התמונה ברכיב ה-ImageView
                holder.ivCartItemPic.setImageBitmap(bitmap);
            } catch (Exception e) {
                // במקרה של שגיאה בהמרה, תוצג תמונת ברירת מחדל
                holder.ivCartItemPic.setImageResource(R.drawable.images__1_);
            }
        } else {
            // אם אין תמונה למוצר, תוצג תמונת ברירת מחדל
            holder.ivCartItemPic.setImageResource(R.drawable.images__1_);
        }

        // --- תוספת חדשה: הגדרת לחיצה על כל מסגרת השורה (כדי לעבור לעמוד המוצר) ---
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                // מדווח למסך העגלה שלחצו על השורה, ומעביר את הפריט שנלחץ
                actionListener.onItemClicked(cart);
            }
        });
        // -------------------------------------------------------------------------

        // הגדרת פעולה בעת לחיצה על כפתור המחיקה (פח אשפה)
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                // קריאה למתודת המחיקה דרך המאזין, שמעבירה את הפריט שנבחר
                actionListener.onDelete(cart);
            }
        });

        // הגדרת פעולה בעת לחיצה על כפתור הפלוס (הגדלת כמות)
        holder.btnPlus.setOnClickListener(v -> {
            if (actionListener != null) {
                // קריאה למתודת שינוי הכמות דרך המאזין, ושליחת הכמות הנוכחית + 1
                actionListener.onQuantityChanged(cart, cart.getQuantity() + 1);
            }
        });

        // הגדרת פעולה בעת לחיצה על כפתור המינוס (הקטנת כמות)
        holder.btnMinus.setOnClickListener(v -> {
            if (actionListener != null) {
                // קריאה למתודת שינוי הכמות דרך המאזין, ושליחת הכמות הנוכחית - 1
                actionListener.onQuantityChanged(cart, cart.getQuantity() - 1);
            }
        });
    }

    // מתודה שמחזירה את מספר הפריטים הכולל ברשימה כדי שהרשימה תדע כמה שורות לייצר
    @Override
    public int getItemCount() {
        return cartList.size();
    }

    // מחלקה פנימית המשמשת לשמירת הרכיבים (Views) של כל שורה כדי לא לחפש אותם בכל פעם מחדש (לשיפור ביצועים)
    static class CartViewHolder extends RecyclerView.ViewHolder {
        // הגדרת משתנים עבור כל רכיבי התצוגה שנמצאים בשורה
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivCartItemPic;
        ImageButton btnDelete;
        TextView btnPlus, btnMinus;

        // הבנאי של המחלקה - מופעל פעם אחת עבור כל שורה שנוצרת
        public CartViewHolder(View itemView) {
            super(itemView);
            // מציאת הרכיבים בקובץ ה-XML לפי ה-ID שלהם וקישורם למשתנים
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            ivCartItemPic = itemView.findViewById(R.id.ivCartItemPic);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);

            // קישור כפתורי הפלוס והמינוס לתצוגה
            btnPlus = itemView.findViewById(R.id.btnPlusQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinusQuantity);
        }
    }
}