package com.pixplicity.cryptogram.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Database {

    private static final Database sInstance = new Database();

    private final CollectionReference mDbTopics, mDbSuggestions;

    public static Database getInstance() {
        return sInstance;
    }

    private Database() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        mDbTopics = database.collection("topics");
        mDbSuggestions = database.collection("suggestions");
    }

    public CollectionReference getPuzzles() {
        return mDbTopics;
    }

    public CollectionReference getSuggestions() {
        return mDbSuggestions;
    }

}
