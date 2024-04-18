package grafo.model;

import org.graphstream.graph.Graph;

import java.util.List;
@FunctionalInterface
public interface GraphCreator {
    Graph createGraph(List<List<String>> grams);
}
