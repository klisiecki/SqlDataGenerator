package pl.poznan.put.sqldatagenerator.history;

import com.google.common.collect.Lists;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;
import pl.poznan.put.sqldatagenerator.generator.TablesState;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.*;

//TODO duplicated entries in history. Save if at least 2 tables were generated?
public class History {

    private static final Logger logger = LoggerFactory.getLogger(History.class);
    private static final Configuration configuration = Configuration.getInstance();

    private static final int HISTORY_SIZE = configuration.getIntegerProperty(ConfigurationKeys.HISTORY_SIZE, 10000);

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
        List<Set<String>> connectedComponentsList = getConnectedComponentsWithoutTables(notNeededTableAliasList);
        for (Set<String> connectedComponent : connectedComponentsList) {
            int randomIndex = random.nextInt(history.size());
            TablesState historyTableState = history.get(randomIndex);
            for (String tableAliasName : connectedComponent) {
                resultTableState.add(tableAliasName, historyTableState.get(tableAliasName));
            }
        }

        return resultTableState;
    }

    public void addSetsToGraph(List<Set<String>> connectedTablesAliases) {
        connectedTablesAliases.forEach(this::addEdges);
    }

    public void addAttributesPairsToGraph(List<AttributesPair> attributesPairs) {
        for (AttributesPair pair : attributesPairs) {
            addEdges(Arrays.asList(pair.getAttribute1().getTableAliasName(), pair.getAttribute2().getTableAliasName()));
        }
    }

    private void addVertices(List<String> vertices) {
        vertices.forEach(graph::addVertex);
    }

    private void addEdges(Collection<String> vertices) {
        List<String> verticesList = Lists.newArrayList(vertices);
        if (vertices.size() > 1) {
            logger.debug("Adding edges: {}", vertices);
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    graph.addEdge(verticesList.get(i), verticesList.get(j));
                }
            }
        }
    }

    private List<Set<String>> getConnectedComponentsWithoutTables(List<String> notNeededTableAliasList) {
        UndirectedGraph<String, DefaultEdge> tempGraph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addGraph(tempGraph, graph);
        notNeededTableAliasList.forEach(tempGraph::removeVertex);
        return new ConnectivityInspector<>(tempGraph).connectedSets();
    }

    //TODO implement better logic i.e. some distribution
    private int getReplaceIndex() {
        historyIndex = ++historyIndex % HISTORY_SIZE;
        return historyIndex;
//        return random.nextInt(HISTORY_SIZE);
    }

}