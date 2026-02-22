package com.example.iddoivanfinalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.iddoivanfinalproject.R;
import com.example.iddoivanfinalproject.model.Item;
import com.example.iddoivanfinalproject.utils.ImageUtil;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    public interface OnItemClickListener {
        void onClick(Item item);
        void onLongClick(Item item);
    }

    private List<Item> items;

    private OnItemClickListener listener;

    public ItemAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_itemrow, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDesc.setText(item.getDetails());
        holder.tvPrice.setText(String.valueOf(item.getPrice()));
        if(item.getPic() != null) {
            holder.ivPic.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        }
        // Click
        holder.itemView.setOnClickListener(v ->
                listener.onClick(item)
        );

        // Long Click
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item);
            return true;
        });

        // --- הקוד שצריך להוסיף ---
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.iddoivanfinalproject.Itemdetails.class);
            intent.putExtra("ITEM_ID", item.getId()); // העברת ה-ID של הפריט למסך הבא
            v.getContext().startActivity(intent);
        });
        // -------------------------
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;

        ImageView ivPic;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivPic=itemView.findViewById(R.id.ivitemPic);
        }
    }

}

