package com.smartpantry.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.smartpantry.data.*;
import java.util.List;

public class SmartPantryViewModel extends AndroidViewModel {
    private SmartPantryRepository mRepository;
    private LiveData<List<PantryItem>> mAllPantryItems;
    private LiveData<List<ShoppingItem>> mAllShoppingItems;
    private LiveData<List<Recipe>> mAllRecipes;

    public SmartPantryViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SmartPantryRepository(application);
        mAllPantryItems = mRepository.getAllPantryItems();
        mAllShoppingItems = mRepository.getAllShoppingItems();
        mAllRecipes = mRepository.getAllRecipes();
    }

    public LiveData<List<PantryItem>> getAllPantryItems() { return mAllPantryItems; }
    public LiveData<List<ShoppingItem>> getAllShoppingItems() { return mAllShoppingItems; }
    public LiveData<List<Recipe>> getAllRecipes() { return mAllRecipes; }

    public void insertPantryItem(PantryItem item) { mRepository.insertPantryItem(item); }
    public void updatePantryItem(PantryItem item) { mRepository.updatePantryItem(item); }
    public void deletePantryItem(PantryItem item) { mRepository.deletePantryItem(item); }

    public void insertShoppingItem(ShoppingItem item) { mRepository.insertShoppingItem(item); }
    public void updateShoppingItem(ShoppingItem item) { mRepository.updateShoppingItem(item); }
    public void deleteShoppingItem(ShoppingItem item) { mRepository.deleteShoppingItem(item); }
    public void clearCheckedShoppingItems() { mRepository.clearCheckedShoppingItems(); }

    public void insertRecipe(Recipe recipe) { mRepository.insertRecipe(recipe); }
}
