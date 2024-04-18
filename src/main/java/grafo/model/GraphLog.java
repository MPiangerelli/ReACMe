package grafo.model;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GraphLog implements GraphCreator{

    private final Set<String> nodeSet;
    private final Set<String> edgeSet;

    private final Graph graphLog;

    public GraphLog(String logName) {
        this.nodeSet = new TreeSet<>();
        this.edgeSet = new TreeSet<>();
        this.graphLog = new MultiGraph("Graph"+logName);
    }


    private String getGramStringID(List<String> gram){
        String gramID= "";
        for(String activity : gram){
            gramID = gramID.concat(activity);
        }
        return gramID;
    }

    private boolean checkIfNodeIsPresent(String gramID){
        return nodeSet.contains(gramID);
    }

    private void addNodeIfNotPresent(String gramID){
        if(!checkIfNodeIsPresent(gramID)){
            Node n = this.graphLog.addNode(gramID);
            n.setAttribute("ui.label", n.getId());
            nodeSet.add(n.getId());
        }
    }

    private void addEdgeIfNotPresent(String edgeLabel, String firstGramID, String secondGramID){
        if(!checkIfEdgeIsPresent(edgeLabel)){
            this.graphLog.addEdge(edgeLabel,firstGramID,secondGramID,true);
            edgeSet.add(edgeLabel);
        }
    }

    private boolean checkIfIsASuccessor(List<String> firstGram, List<String> secondGram){
        return firstGram.get(firstGram.size()-2).equals(secondGram.get(0));
    }

    private boolean checkIfEdgeIsPresent(String edgeLabel){
        return edgeSet.contains(edgeLabel);
    }

    private boolean checkIfHasAPredecessor(List<String> firstGram, List<String> secondGram){
        return firstGram.get(firstGram.size()-2).equals(secondGram.get(0));
    }

    @Override
    public Graph createGraph(List<List<String>> grams) {
        List<String> lastNode = null;
        String lastNodeID = "";
        for (List<String> currentGram : grams) {
            String currentGramID = getGramStringID(currentGram);
            addNodeIfNotPresent(currentGramID);
            if (lastNode != null) {
                if (checkIfHasAPredecessor(lastNode, currentGram)) {
                    String edgeLabel = lastNodeID + currentGramID;
                    addEdgeIfNotPresent(edgeLabel, lastNodeID, currentGramID);
                }
            }
            lastNode = currentGram;
            lastNodeID = currentGramID;
        }
        return graphLog;
    }
}
