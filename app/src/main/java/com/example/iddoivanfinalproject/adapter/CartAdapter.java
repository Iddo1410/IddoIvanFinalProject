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

import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.Cart;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> cartList;
    private OnItemDeleteListener deleteListener;

    // ממשק לטיפול במחיקה מה-Activity
    public interface OnItemDeleteListener {
        void onDeleteClick(Cart cart);
    }

    public CartAdapter(List<Cart> cartList, OnItemDeleteListener deleteListener) {
        this.cartList = cartList;
        this.deleteListener = deleteListener;
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
        holder.tvPrice.setText(cart.getPrice() + " ₪");
        holder.tvQuantity.setText("כמות: " + cart.getQuantity());

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

        // הגדרת לחיצה על כפתור המחיקה
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(cart);
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

        public CartViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            ivCartItemPic = itemView.findViewById(R.id.ivCartItemPic);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
        }
    }
}