package com.example.iddoivanfinalproject.adapter;

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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> cartList;
    private CartActivity.CartActionListener actionListener;

    public CartAdapter(List<Cart> cartList, CartActivity.CartActionListener actionListener) {
        this.cartList = cartList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_cartrow, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cart = cartList.get(position);

        holder.tvName.setText(cart.getName());

        // --- חישוב המחיר: מחיר המוצר כפול הכמות ---
        double totalPrice = cart.getPrice() * cart.getQuantity();
        holder.tvPrice.setText(totalPrice + " ₪");
        // ------------------------------------------

        holder.tvQuantity.setText(String.valueOf(cart.getQuantity()));

        // טעינת תמונה
        if (cart.getPic() != null && !cart.getPic().isEmpty()) {
            try {
                Bitmap bitmap = ImageUtil.convertFrom64base(cart.getPic());
                holder.ivCartItemPic.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivCartItemPic.setImageResource(R.drawable.images__1_);
            }
        } else {
            holder.ivCartItemPic.setImageResource(R.drawable.images__1_);
        }

        // מחיקה
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(cart);
            }
        });

        // פלוס (הגדלת כמות)
        holder.btnPlus.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onQuantityChanged(cart, cart.getQuantity() + 1);
            }
        });

        // מינוס (הקטנת כמות)
        holder.btnMinus.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onQuantityChanged(cart, cart.getQuantity() - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity;
        ImageView ivCartItemPic;
        ImageButton btnDelete;
        TextView btnPlus, btnMinus;

        public CartViewHolder(View itemView) {
            super(itemView);
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