package com.example.iddoivanfinalproject.adapter;

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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    // רשימת ההזמנות שתוצג ב-RecyclerView
    private List<Order> ordersList;

    // בנאי (Constructor) שמקבל את רשימת ההזמנות
    public OrderAdapter(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // חיבור לעיצוב של השורה הבודדת שיצרנו (order_row.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderrow, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // שליפת ההזמנה הנוכחית לפי המיקום שלה ברשימה
        Order currentOrder = ordersList.get(position);

        // הגדרת האימייל של הקונה
        holder.tvEmail.setText("אימייל קונה: " + currentOrder.getUserEmail());

        // הגדרת סך הכל לתשלום
        holder.tvTotal.setText("סה\"כ שולם: ₪" + currentOrder.getTotalPrice());

        // המרת הזמן (שנשמר כ-long במילישניות) לתאריך קריא
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("he", "IL"));
        Date date = new Date(currentOrder.getTimestamp());
        holder.tvDate.setText("תאריך רכישה: " + sdf.format(date));

        // בניית מחרוזת המציגה את כל הפריטים שנקנו באותה הזמנה
        StringBuilder itemsDetails = new StringBuilder("פריטים:\n");
        if (currentOrder.getItems() != null) {
            for (Cart item : currentOrder.getItems()) {
                // מציג כמות + שם המוצר (לדוגמה: 2 x רכב מאזדה)
                itemsDetails.append("- ").append(item.getQuantity())
                        .append(" x ").append(item.getName()).append("\n");
            }
        } else {
            itemsDetails.append("לא נמצאו פריטים.");
        }

        holder.tvItems.setText(itemsDetails.toString().trim());
    }

    @Override
    public int getItemCount() {
        if (ordersList == null) {
            return 0;
        }
        return ordersList.size();
    }

    // מחלקה פנימית שמחזיקה את הרכיבים של כל שורה (ViewHolder)
    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvEmail, tvDate, tvItems, tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // מציאת הרכיבים מתוך ה-XML של השורה (order_row.xml)
            tvEmail = itemView.findViewById(R.id.tvOrderEmail);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}