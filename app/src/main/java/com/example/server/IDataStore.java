package com.example.server;

public interface IDataStore {
    void saveState(String state);
    String getState();
}
