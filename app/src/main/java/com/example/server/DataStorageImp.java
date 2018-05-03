package com.example.server;
import com.example.server.IDataStore;

public class DataStorageImp implements IDataStore {
    public String state = null;
    public void saveState(String state) {
        this.state = state;
    }

    @Override
    public String getState() {
        return state;
    }
}
