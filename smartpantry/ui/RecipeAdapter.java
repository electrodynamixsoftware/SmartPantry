package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.RecipeMatcher.MatchResult;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the Suggested Recipes lists (strict matches and the optional "almost there" list). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(long recipeId);
    }

    private final List<MatchResult> results = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setResults(List<MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MatchResult result = results.get(position);
        holder.txtName.setText(result.getRecipe().getName());

        if (result.isStrictMatch()) {
            holder.txtInfo.setText(result.getRecipe().getIngredients().size()
                    + " ingredients - you have everything you need");
        } else {
            StringBuilder missing = new StringBuilder("Missing: ");
            for (int i = 0; i < result.getMissing().size(); i++) {
                RecipeIngredient ingredient = result.getMissing().get(i);
                if (i > 0) {
                    missing.append(", ");
                }
                missing.append(ingredient.describe());
            }
            holder.txtInfo.setText(missing.toString());
        }
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(result.getRecipe().getId()));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtName;
        final TextView txtInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_recipe_name);
            txtInfo = itemView.findViewById(R.id.txt_recipe_info);
        }
    }
}
