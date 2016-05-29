package pl.poznan.put.sqldatagenerator.history;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.TableInstance;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.*;

public class HistoryGroup {
    private static final Logger logger = LoggerFactory.getLogger(HistoryGroup.class);

    private static final int HISTORY_SIZE = 10;

    private final Random random;
    private final List<TablesState> history;
    private final UndirectedGraph<String, DefaultEdge> graph;

    public HistoryGroup(List<String> tablesNames) {
        this.random = new Random();
        history = new ArrayList<>(HISTORY_SIZE);
        graph = new SimpleGraph<>(DefaultEdge.class);
        addVertices(tablesNames);
    }

    public void addState(TablesState tablesState) {
        if (history.size() < HISTORY_SIZE) {
            history.add(tablesState);
        } else {
            history.add(getReplaceIndex(), tablesState);
        }
    }

    public TablesState getState(List<TableInstance> dontReturnThisTables) {
        if (history.isEmpty()) {
            return null;
        }

        TablesState resultTableState = new TablesState();
        List<Set<String>> connectedComponentsList = getConnectedComponentsWithoutThatTables(dontReturnThisTables);
        for (Set<String> connectedComponent : connectedComponentsList) {
            TablesState historyEntry = history.get(random.nextInt(history.size()));
            for (String tableName : connectedComponent) {
                //TODO TU SKOŃCZYŁEM Tu trzeba pobrać aliasy dla tego tableName i je przekazać poniżej
                resultTableState.add(tableName, historyEntry.get(tableName));
            }
        }

        return resultTableState;
//        return history.get(random.nextInt(history.size()));
    }

    public void addRestrictions(Collection<Restriction> restrictionList) {
        for (Restriction restriction : restrictionList) {
            List<String> tablesNames = new ArrayList<>();
            restriction.getAttributes().stream().forEach(a -> tablesNames.add(a.getBaseTableName()));
            addEdges(tablesNames);
        }
    }

    public void addAttributesPairs(List<AttributesPair> attributesPairs) {
        for (AttributesPair pair : attributesPairs) {
            List<String> tablesNames = new ArrayList<>();
            tablesNames.add(pair.getAttribute1().getBaseTableName());
            tablesNames.add(pair.getAttribute2().getBaseTableName());
            addEdges(tablesNames);
        }
    }

    private void addVertices(List<String> vertices) {
        vertices.stream().forEach(v -> graph.addVertex(v));
    }

    private void addEdges(List<String> vertices) {
        if (vertices.size() > 1) {
            logger.info("Adding edges in history: {}", vertices);
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    graph.addEdge(vertices.get(i), vertices.get(j));
                }
            }
        }
    }

    private List<Set<String>> getConnectedComponentsWithoutThatTables(List<TableInstance> dontReturnThisTables) {
        UndirectedGraph<String, DefaultEdge> tempGraph = (UndirectedGraph<String, DefaultEdge>) ((AbstractBaseGraph) graph).clone();
        dontReturnThisTables.stream().forEach(v -> tempGraph.removeVertex(v.getBase().getName()));
        return (new ConnectivityInspector(tempGraph)).connectedSets();
    }

    // TODO to remove
    public void printGraph() {
        UndirectedGraph<String, DefaultEdge> tempGraph = (UndirectedGraph<String, DefaultEdge>) ((AbstractBaseGraph) graph).clone();
        logger.info("Before: {}", tempGraph.edgeSet());
//        graph.removeVertex("CLIENTS");
        tempGraph.removeVertex("ORDERS");
        logger.info("After: {}", tempGraph.edgeSet());
        List<Set<String>> dupa = (new ConnectivityInspector(tempGraph)).connectedSets();
        logger.info("Connected components:");
        dupa.stream().forEach(a -> logger.info("Component: {}", a));

        logger.info("Original graph: {}", graph.edgeSet());
    }

    //TODO implement better logic i.e. some distribution
    private int getReplaceIndex() {
        return random.nextInt(HISTORY_SIZE);
    }

}
