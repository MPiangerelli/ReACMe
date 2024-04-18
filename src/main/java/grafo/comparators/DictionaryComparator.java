package grafo.comparators;

import grafo.model.LogData;

@FunctionalInterface
public interface DictionaryComparator {

    double compare(LogData s, LogData d);

}
