package pl.poznan.put.sqldatagenerator.history;

import com.google.common.collect.HashMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.restriction.RestrictionsManager;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static final Logger logger = LoggerFactory.getLogger(HistoryManager.class);

    private List<History> positiveHistoryList;
    private List<History> negativeHistoryList;

    public HistoryManager() {
    }

    public void initialize(List<String> tablesAliasNames, RestrictionsManager restrictionsManager, List<AttributesPair> attributesPairs) {
        positiveHistoryList = initHistoryGroups(tablesAliasNames, restrictionsManager.getAll(true), attributesPairs);
        negativeHistoryList = initHistoryGroups(tablesAliasNames, restrictionsManager.getAll(false), attributesPairs);
    }

    private List<History> initHistoryGroups(List<String> tablesAliasNames, List<HashMultimap<Attribute, Restriction>> restrictionsByAttributeList, List<AttributesPair> attributesPairs) {
        int size = restrictionsByAttributeList.size();
        List<History> historyList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            logger.debug("Preparing historyGroup for index {}", i);
            historyList.add(new History(tablesAliasNames));
            historyList.get(i).addRestrictionsToGraph(restrictionsByAttributeList.get(i).values());
            historyList.get(i).addAttributesPairsToGraph(attributesPairs);
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
