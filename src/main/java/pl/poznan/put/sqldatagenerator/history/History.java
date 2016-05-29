package pl.poznan.put.sqldatagenerator.history;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.*;

public class History {
    private static final Logger logger = LoggerFactory.getLogger(History.class);

    private static final int HISTORY_SIZE = 10;

    private final Random random;
    private final List<TablesState> history;
    private final UndirectedGraph<String, DefaultEdge> graph;

    private int historyIndex = 0;

    public History(List<String> tablesAliasNames) {
        this.random = new Random();
        history = new ArrayList<>(HISTORY_SIZE);
        graph = new SimpleGraph<>(DefaultEdge.class);
        addVertices(tablesAliasNames);
    }

    public void addState(TablesState tablesState) {
        if (history.size() < HISTORY_SIZE) {
            history.add(tablesState);
        } else {
            history.set(getReplaceIndex(), tablesState);
        }
    }

    public TablesState getState(List<String> notNeededTableAliasList) {
        if (history.isEmpty()) {
            return null;
        }

        TablesState resultTableState = new TablesState();
        List<Set<String>> connectedComponentsList = getConnectedComponentsWithoutThatTables(notNeededTableAliasList);
        for (Set<String> connectedComponent : connectedComponentsList) {
            int randomIndex = random.nextInt(history.size());
            TablesState historyTableState = history.get(randomIndex);
            for (String tableAliasName : connectedComponent) {
                resultTableState.add(tableAliasName, historyTableState.get(tableAliasName));
            }
        }

        return resultTableState;
    }

    //TODO method not tested yet
    public void addRestrictionsToGraph(Collection<Restriction> restrictionList) {
        for (Restriction restriction : restrictionList) {
            List<String> tablesAliasNames = new ArrayList<>();
            restriction.getAttributes().stream().forEach(a -> tablesAliasNames.add(a.getTableAliasName()));
            addEdges(tablesAliasNames);
        }
    }

    public void addAttributesPairsToGraph(List<AttributesPair> attributesPairs) {
        for (AttributesPair pair : attributesPairs) {
            addEdges(Arrays.asList(pair.getAttribute1().getTableAliasName(), pair.getAttribute2().getTableAliasName()));
        }
    }

    private void addVertices(List<String> vertices) {
        vertices.stream().forEach(v -> graph.addVertex(v));
    }

    private void addEdges(List<String> vertices) {
        if (vertices.size() > 1) {
            logger.debug("Adding edges: {}", vertices);
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    graph.addEdge(vertices.get(i), vertices.get(j));
                }
            }
        }
    }

    private List<Set<String>> getConnectedComponentsWithoutThatTables(List<String> notNeededTableAliasList) {
        UndirectedGraph<String, DefaultEdge> tempGraph = (UndirectedGraph<String, DefaultEdge>) ((AbstractBaseGraph) graph).clone();
        notNeededTableAliasList.stream().forEach(v -> tempGraph.removeVertex(v));
        return (new ConnectivityInspector(tempGraph)).connectedSets();
    }

    //TODO implement better logic i.e. some distribution
    private int getReplaceIndex() {
        historyIndex = (++historyIndex) % HISTORY_SIZE;
        return historyIndex;
//        return random.nextInt(HISTORY_SIZE);
    }

}