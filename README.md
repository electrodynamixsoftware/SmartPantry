# Smart Pantry Manager

[![Android SDK](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/dashboards)
[![Language](https://img.shields.io/badge/Language-Java_8-orange.svg)](https://www.oracle.com/java/)
[![Database](https://img.shields.io/badge/Database-SQLite-blue.svg)](https://www.sqlite.org/)

**Smart Pantry Manager** is an offline-first Android application designed to help users track home ingredients and eliminate food waste. By matching user-owned pantry items against a pre-loaded collection of recipes using robust normalization and unit conversion algorithms, the app suggests recipes that can be cooked **immediately**—without requiring extra grocery trips.

---

## Table of Contents

- [Key Features](#key-features)
- [How It Works & Business Logic](#how-it-works--business-logic)
  - [1. Strict Recipe Matching](#1-strict-recipe-matching)
  - [2. Name Normalization & Synonyms](#2-name-normalization--synonyms)
  - [3. Unit Conversion & Quantity Aggregation](#3-unit-conversion--quantity-aggregation)
- [Application Architecture & Project Structure](#application-architecture--project-structure)
- [Database Schema](#database-schema)
- [Screens & Navigation](#screens--navigation)
- [Tech Stack & Dependencies](#tech-stack--dependencies)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building and Running](#building-and-running)
  - [Running Unit Tests](#running-unit-tests)
- [Privacy & Security](#privacy--security)

---

## Key Features

- 🧺 **Pantry Inventory Management**: Full CRUD support to add, edit, view, and delete pantry items (name, quantity, unit, and optional expiry date).
- ⏰ **Expiring-Soon Alerts**: Automatic visual indicators highlight ingredients nearing their expiration date.
- 🍳 **Smart Recipe Suggestions**: Real-time matching algorithm guarantees that suggested recipes require **zero missing ingredients** in the required amounts.
- 💡 **"Almost There" Suggestions**: Option to view recipes where only a single ingredient is missing.
- 📖 **21 Pre-loaded Recipes**: Embedded SQLite recipe collection available on initial run.
- 🔍 **Detailed Recipe Guides**: Visual check/cross indicators for ingredient requirements alongside step-by-step preparation instructions.
- ⚙️ **Customizable Settings**: Configurable alert thresholds, option to toggle missing ingredient suggestions, and database maintenance tools.
- ✈️ **100% Offline & Private**: Zero network calls, no location services, and no external server requirements.

---

## How It Works & Business Logic

The engine located in `com.example.smartpantry.logic` handles raw input reconciliation to ensure accurate matching.

### 1. Strict Recipe Matching (`RecipeMatcher.java`)
A recipe is classified as a **Strict Match** if and only if **every** required ingredient exists in the pantry in a quantity equal to or greater than the required amount.
- **To-taste ingredients**: Items required with quantity `0` (e.g., salt to taste) only require the item to be present in the pantry.
- **Duplicate items**: Multiple pantry entries for the same ingredient (e.g., two half-full bags of flour) are summed together automatically.

### 2. Name Normalization & Synonyms (`IngredientNameNormalizer.java`)
Ingredient names are normalized before evaluation:
- Strips punctuation and converts text to lowercase.
- Handles singular vs. plural forms (e.g., `"Tomatoes"`, `"tomato "`, and `"TOMATO"` all normalize to `"tomato"`).
- Resolves common ingredient synonyms (e.g., `"aubergine"` $\rightarrow$ `"eggplant"`, `"courgette"` $\rightarrow$ `"zucchini"`, `"capsicum"` $\rightarrow$ `"bell pepper"`).

### 3. Unit Conversion & Quantity Aggregation (`UnitConverter.java`)
Quantities are converted into base units depending on their physical dimension:
- **Mass** (Base unit: `grams`): Supports `g` and `kg` (e.g., 1 kg stock covers 200 g required).
- **Volume** (Base unit: `milliliters`): Supports `ml`, `l`, `cup` (240ml), `tbsp` (15ml), `tsp` (5ml).
- **Count** (Base unit: `pieces`): Supports `pcs`, `units`, `whole`.

---

## Application Architecture & Project Structure

The project follows standard Android architectural separation of concerns:

```
app/src/main/java/com/example/smartpantry/
├── data/
│   └── DatabaseHelper.java          # SQLite helper managing CRUD operations & seeding
├── logic/
│   ├── IngredientNameNormalizer.java# Text normalization & singularization logic
│   ├── RecipeMatcher.java           # Core strict matching engine
│   └── UnitConverter.java          # Multi-unit conversions & dimension matching
├── model/
│   ├── PantryItem.java              # Data model for user pantry entries
│   ├── Recipe.java                  # Data model for recipe metadata
│   └── RecipeIngredient.java        # Data model for recipe ingredients
├── ui/
│   ├── AddEditItemActivity.java     # Screen for adding or editing pantry items
│   ├── BaseActivity.java            # Abstract activity with common navigation toolbar
│   ├── PantryAdapter.java           # RecyclerView adapter for pantry items
│   ├── PantryListActivity.java      # Main inventory screen
│   ├── RecipeAdapter.java           # RecyclerView adapter for recipe lists
│   ├── RecipeDetailActivity.java    # Screen displaying cooking instructions
│   ├── SettingsActivity.java        # Preferences & management screen
│   └── SuggestedRecipesActivity.java# Matches display screen
└── util/
    ├── AppPrefs.java                # Shared Preferences wrapper for app settings
    └── FormatUtils.java             # Date and quantity formatting utilities
```

---

## Database Schema

Local data persistence uses native SQLite via `SQLiteOpenHelper` (`smart_pantry.db`).

```
 +----------------------------------+        +----------------------------------+
 |             pantry               |        |             recipes              |
 +----------------------------------+        +----------------------------------+
 | _id      INTEGER PRIMARY KEY AUTO|        | _id      INTEGER PRIMARY KEY AUTO|
 | name     TEXT NOT NULL           |        | name     TEXT NOT NULL           |
 | quantity REAL NOT NULL           |        | steps    TEXT NOT NULL           |
 | unit     TEXT NOT NULL           |        +----------------------------------+
 | expiry   TEXT                    |                         | 1
 +----------------------------------+                         |
                                                              | N
                                             +----------------------------------+
                                             |        recipe_ingredients        |
                                             +----------------------------------+
                                             | _id       INTEGER PRIMARY KEY AUTO|
                                             | recipe_id INTEGER NOT NULL (FK)  |
                                             | name      TEXT NOT NULL          |
                                             | quantity  REAL NOT NULL          |
                                             | unit      TEXT NOT NULL          |
                                             +----------------------------------+
```

---

## Screens & Navigation

1. **Pantry Inventory (`PantryListActivity`)**: View existing pantry stock sorted alphabetically with color-coded expiry highlights. Access options to add, edit, or delete items.
2. **Add/Edit Item (`AddEditItemActivity`)**: Form to enter item name, quantity, measurement unit, and optional expiration date with date picker integration.
3. **Recipe Suggestions (`SuggestedRecipesActivity`)**: Displays recipes matched against current pantry contents. Includes tabbed or sectioned views for "Can Cook Now" and optional "Almost There" recipes.
4. **Recipe Detail (`RecipeDetailActivity`)**: Full recipe instructions along with visual status icons showing available versus required amounts for each ingredient.
5. **Settings (`SettingsActivity`)**: Toggle expiring-soon highlighting, adjust missing ingredient options, or clear user pantry data.

---

## Tech Stack & Dependencies

- **Language**: Java 8 (`sourceCompatibility 1.8`)
- **Min SDK**: API 24 (Android 7.0 Nougat)
- **Target / Compile SDK**: API 34 (Android 14)
- **UI Components**:
  - AndroidX AppCompat
  - Google Material Components
  - ConstraintLayout
  - RecyclerView
- **Local Database**: Native SQLite (`SQLiteOpenHelper`)
- **Testing**: JUnit 4

---

## Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Hedgehog 2023.1.1 or newer recommended)
- Android SDK with API Level 34 installed
- Java Development Kit (JDK) 17 (bundled with Android Studio)

### Building and Running
1. Clone or download the repository to your local machine.
2. Open Android Studio and select **File > Open**, navigating to the project root directory.
3. Allow Gradle to sync project files.
4. Set up an Android Virtual Device (AVD) running API 24 or higher, or connect a physical device via USB debugging.
5. Click **Run** (`Shift + F10` or green play button) to build and deploy the app.

### Running Unit Tests
The project includes unit test coverage for matching algorithms, name normalization, and unit conversion rules:
- **In Android Studio**: Right-click on `app/src/test/java` in the project tree and select **Run 'Tests in 'smartpantry''**.
- **Via Command Line**:
  ```bash
  ./gradlew test
  ```

---

## Privacy & Security

- **No Network Access**: The app requests zero internet or network permissions (`AndroidManifest.xml`).
- **No Analytics / Tracking**: Personal pantry data remains exclusively on the user's local device.
- **No External Services**: Operates without third-party backend servers, SDKs, or cloud storage requirements.
