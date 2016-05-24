package pl.poznan.put.sqldatagenerator.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HistoryManager {
    private static final int HISTORY_SIZE = 10;

    private final Random random;
    private List<List<TablesState>> history;

    public HistoryManager() {
        this.random = new Random();
    }

    public void initialize(int size) {
        history = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            history.add(new ArrayList<>(HISTORY_SIZE));
        }
    }

    public void add(int index, TablesState tablesState) {
        List<TablesState> tablesStates = history.get(index);
        if (tablesStates.size() < HISTORY_SIZE) {
            tablesStates.add(tablesState);
        } else {
            tablesStates.add(getReplaceIndex(), tablesState);
        }
    }

    public TablesState get(int index) {
        List<TablesState> tablesStates = history.get(index);
        if (tablesStates.isEmpty()) {
            return null;
        }
        return tablesStates.get(random.nextInt(tablesStates.size()));
    }

    //TODO implement better logic i.e. some distribution
    private int getReplaceIndex() {
        return random.nextInt(HISTORY_SIZE);
    }

}
