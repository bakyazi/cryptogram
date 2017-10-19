package com.pixplicity.cryptogram.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Database {

    private static final Database sInstance = new Database();

    private final DatabaseReference mDbPuzzles;

    public static Database getInstance() {
        return sInstance;
    }

    private Database() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDbPuzzles = database.getReference("puzzles");
    }

    private DatabaseReference getPuzzles() {
        return mDbPuzzles;
    }

    public DatabaseReference getSuggestions() {
        return getPuzzles().child("suggestions");
    }

}
