package com.example.smartpantry.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter that binds pantry rows from the database to item_pantry.xml. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public static final int EXPIRING_SOON_DAYS = 3;

    public interface Listener {
        void onItemClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean highlightExpiring;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems, boolean highlightExpiring) {
        items.clear();
        items.addAll(newItems);
        this.highlightExpiring = highlightExpiring;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        holder.txtName.setText(item.getName());
        holder.txtQuantity.setText(FormatUtils.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        holder.txtExpiry.setTextColor(holder.defaultExpiryColor);
        if (item.getExpiry() == null) {
            holder.txtExpiry.setText("No expiry date");
        } else {
            String text = "Expires: " + item.getExpiry();
            int days = FormatUtils.daysUntil(item.getExpiry());
            if (highlightExpiring && days < 0) {
                text = "EXPIRED (" + item.getExpiry() + ")";
                holder.txtExpiry.setTextColor(Color.parseColor("#C62828"));
            } else if (highlightExpiring && days <= EXPIRING_SOON_DAYS) {
                text = "Expires in " + days + (days == 1 ? " day" : " days") + " (" + item.getExpiry() + ")";
                holder.txtExpiry.setTextColor(Color.parseColor("#E65100"));
            }
            holder.txtExpiry.setText(text);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtQuantity;
        final TextView txtExpiry;
        final ImageButton btnDelete;
        final int defaultExpiryColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtQuantity = itemView.findViewById(R.id.txt_quantity);
            txtExpiry = itemView.findViewById(R.id.txt_expiry);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            defaultExpiryColor = txtExpiry.getCurrentTextColor();
        }
    }
}
