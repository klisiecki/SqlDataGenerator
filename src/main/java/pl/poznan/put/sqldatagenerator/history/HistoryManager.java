package pl.poznan.put.sqldatagenerator.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HistoryManager {
    private static final Logger logger = LoggerFactory.getLogger(HistoryManager.class);

    private List<History> positiveHistoryList;
    private List<History> negativeHistoryList;

    public HistoryManager() {
    }

    public void initialize(List<String> tablesAliasNames, RestrictionsManager restrictionsManager,
                           List<AttributesPair> attributesPairs) {
        positiveHistoryList =
                initHistory(tablesAliasNames, restrictionsManager.getConnectedTablesAliases(true), attributesPairs);
        negativeHistoryList =
                initHistory(tablesAliasNames, restrictionsManager.getConnectedTablesAliases(false), attributesPairs);
    }

    private List<History> initHistory(List<String> tablesAliasNames, List<List<Set<String>>> connectedTablesAliases,
                                      List<AttributesPair> attributesPairs) {
        int size = connectedTablesAliases.size();
        List<History> historyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            logger.debug("Preparing historyGroup for index {}", i);
            History history = new History(tablesAliasNames);
            history.addSetsToGraph(connectedTablesAliases.get(i));
            history.addAttributesPairsToGraph(attributesPairs);
            historyList.add(i, history);
        }
        return historyList;
    }

    public void addState(boolean positive, int index, TablesState tablesState) {
        if (positive) {
            positiveHistoryList.get(index).addState(tablesState);
        } else {
            negativeHistoryList.get(index).addState(tablesState);
        }
    }

    public TablesState getState(boolean positive, int index, List<String> notNeededTableAliasList) {
        if (positive) {
            return positiveHistoryList.get(index).getState(notNeededTableAliasList);
        } else {
            return negativeHistoryList.get(index).getState(notNeededTableAliasList);
        }
    }
}
