package julietgroupproject;


import java.util.ArrayDeque;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author George
 */
public class JulietLogger<T> {
    
    public static final int DEFAULT_LIMIT = 1000;
    private int limit;
    private ArrayList<T> entries;
    
    public JulietLogger(int limit) {
        this.limit = limit;
        entries = new ArrayList<>(DEFAULT_LIMIT);
    }

    public JulietLogger() {
        this(DEFAULT_LIMIT);
    }
    
    public synchronized void push(T entry) {
        entries.add(entry);
        
        while (entries.size() > limit) {
            entries.remove(0);
        }
    }
    
    public int getSize() {
        return entries.size();
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public synchronized ArrayList<T> getEntries() {
        return new ArrayList(entries);
    }
    
    public synchronized ArrayList<T> getLastEntries(int n) {
        return new ArrayList<>(entries.subList(Math.max(0, entries.size()-n), entries.size()));
    }
    
    public void clear() {
        entries.clear();
    }
}
