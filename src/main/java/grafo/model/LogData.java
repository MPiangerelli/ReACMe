package grafo.model;


import grafo.filters.LowHighFilter;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.images.Resolutions;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contenitore per i dati analizzati di un file log. Usiamo questa classe per generare il dizionario
 * che è utile nella generazione dell distance matrix
 */
public class LogData {
    private final String logName;

    public List<Trace> getTraces() {
        return traces;
    }

    private List<Trace> traces;
    private final Map<List<String>, Integer> dictionaryOfGrams;
    private final Map<String, Integer> dictionaryOfActivities;
    private Map<List<String>, Integer> dictionaryOfTraces;
    private final List<List<String>> listOfGrams;
    private List<String> nodeSet = new LinkedList<>();
    private Graph graphLog;


    public LogData(String logName, List<Trace> traces) {
        this.logName = logName;
        this.traces = traces;
        this.dictionaryOfTraces = generateDictionaryOfTraces();
        this.dictionaryOfActivities = new HashMap<>();
        this.dictionaryOfGrams = new HashMap<>();
        this.listOfGrams = new LinkedList<>();
        this.graphLog = new MultiGraph("Graph" + this.logName);
    }

    public void updateTraces(List<Trace> traces) {
        this.traces = traces;
    }


    public Map<List<String>, Integer> getGramsDictionary() {
        return dictionaryOfGrams;
    }

    public Map<String, Integer> getDictionaryOfActivities() {
        return dictionaryOfActivities;
    }

    public Map<List<String>, Integer> getDictionaryOfTraces() {
        return dictionaryOfTraces;
    }

    public void generateGraph() {
        GraphLog graphLog = new GraphLog(logName);
        this.graphLog = graphLog.createGraph(listOfGrams);
    }

    public Map<List<String>, Integer> generateDictionaryOfTraces() {
        Map<List<String>, Integer> mapOfTraces = new HashMap<>();

        for (Trace trace : traces) {
            List<String> currentActivitySequence = trace.getActivitySequence();
            if (mapOfTraces.containsKey(currentActivitySequence)) {
                mapOfTraces.put(currentActivitySequence, mapOfTraces.get(currentActivitySequence) + 1);
            } else {
                mapOfTraces.put(currentActivitySequence, 1);
            }
        }

        dictionaryOfTraces = mapOfTraces;
        //applyTracesFilter(RunProperties.getInstance().getLowHighFilter());
        return mapOfTraces;
    }

    public void applyTracesFilter(final LowHighFilter filter) {
        Map<List<String>, Integer> toRemove = new HashMap<>();
        dictionaryOfTraces.forEach((key, value) -> {
            if (value < filter.getLow() || value > filter.getHigh()) {
                toRemove.put(key, value);
            }
        });
        toRemove.forEach((key, value) -> dictionaryOfTraces.remove(key));
    }

    public void generateDictionaryOfGramsByValue(int gram) {
        dictionaryOfTraces.forEach((k, v) -> valutateSubsequenceAndAddToDictionary(k ,gram));
    }

    private void valutateSubsequenceAndAddToDictionary(List<String> activitySequence, int grams) {
        Map<List<String>, Integer> auxiliaryDictionary = new HashMap<>();
        for (int i = 0; i < activitySequence.size() - grams + 1; i++) {
            List<String> subsequence = activitySequence.subList(i, i + grams);
            listOfGrams.add(subsequence);
            if (auxiliaryDictionary.containsKey(subsequence)) {
                auxiliaryDictionary.put(subsequence, auxiliaryDictionary.get(subsequence) + 1);
            } else {
                auxiliaryDictionary.put(subsequence, 1);
            }
        }
        auxiliaryDictionary.forEach(this::updateDictionaryOfSubsequences);
    }

    private void updateDictionaryOfSubsequences(List<String> key, Integer value) {
        if (dictionaryOfGrams.containsKey(key)) {
            if (dictionaryOfGrams.get(key) < value) {
                dictionaryOfGrams.put(key, value);
            }
        } else {
            dictionaryOfGrams.put(key, value);
        }
    }

    public void generateDictionaryOfActivities() {
        dictionaryOfTraces.forEach((k, v) -> updateDictionaryOfActivities(k));
    }


    private void updateDictionaryOfActivities(List<String> activities) {
        Map<String, Integer> auxiliaryDictionary = new HashMap<>();
        for(String activity : activities) {
            if (auxiliaryDictionary.containsKey(activity)) {
                auxiliaryDictionary.put(activity, auxiliaryDictionary.get(activity) + 1);
            } else {
                auxiliaryDictionary.put(activity, 1);
            }
        }
        auxiliaryDictionary.forEach(this::modifyDioctionaryOfActivities);
    }

    private void modifyDioctionaryOfActivities(String key, Integer value) {
        if (dictionaryOfActivities.containsKey(key)) {
            if (dictionaryOfActivities.get(key) < value) {
                dictionaryOfActivities.put(key, value);
            }
        } else {
            dictionaryOfActivities.put(key, value);
        }
    }


    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        dictionaryOfGrams.forEach((key, value) -> toReturn.append(key).append(" : ").append(value).append("\n"));
        return toReturn.toString();
    }

    public void printGraph() {
        System.setProperty("org.graphstream.ui", "swing");
        graphLog.setAutoCreate(true);
        graphLog.setStrict(false);

        graphLog.setAttribute("ui.stylesheet", "graph { fill-color: white; } node { size: 30px, 30px; shape: box; fill-color: yellow; stroke-mode: plain; stroke-color: black; }  node:clicked { fill-color: red;} edge { shape: line; text-alignment: above;  fill-mode: dyn-plain; fill-color: #222, #555, green, yellow; arrow-size: 8px, 3px;}");

        graphLog.setAttribute("ui.quality");
        graphLog.setAttribute("ui.antialias");
        graphLog.display();


        FileSinkImages pic = FileSinkImages.createDefault();
        pic.setOutputType(FileSinkImages.OutputType.PNG);
        pic.setResolution(Resolutions.HD1080);

        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

        try {
            pic.writeAll(graphLog, logName + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getLogName() {
        return this.logName;
    }

}