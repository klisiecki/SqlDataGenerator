package pl.poznan.put.sqldatagenerator.history;

import com.google.common.collect.HashMultimap;
import pl.poznan.put.sqldatagenerator.generator.Attribute;
import pl.poznan.put.sqldatagenerator.generator.TableInstance;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.ArrayList;
import java.util.List;

public class History {

    private List<HistoryGroup> historyGroups;

    public History() {
    }

    public void initialize(List<String> tablesAliasNames, List<HashMultimap<Attribute, Restriction>> restrictionsByAttributeList, List<AttributesPair> attributesPairs) {
        int size = restrictionsByAttributeList.size();
        historyGroups = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            historyGroups.add(new HistoryGroup(tablesAliasNames));
            historyGroups.get(i).addRestrictions(restrictionsByAttributeList.get(i).values());
            historyGroups.get(i).addAttributesPairs(attributesPairs);
        }

        historyGroups.stream().forEach(HistoryGroup::printGraph);
    }

    public void addState(int index, TablesState tablesState) {
        historyGroups.get(index).addState(tablesState);
    }

    public TablesState getState(int index, List<TableInstance> dontReturnThisTables) {
        return historyGroups.get(index).getState(dontReturnThisTables);
    }
}
