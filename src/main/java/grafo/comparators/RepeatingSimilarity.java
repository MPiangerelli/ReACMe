package grafo.comparators;

import grafo.config.RunProperties;
import grafo.model.LogData;


import java.util.*;

public class RepeatingSimilarity implements DictionaryComparator {

    private  double gramsScore=0.0;
    private  double activityScore=0.0;
    private int totalGramsSize;
    private int totalActivitySize;
    private final RunProperties runProperties;

    public RepeatingSimilarity(RunProperties runProperties) {
        this.runProperties = runProperties;
    }


    private void setTotalGramsSize(Map<List<String>, Integer> firstDictionary, Map<List<String>, Integer> secondDictionary){
        Set<List<String>> union = new HashSet<>(firstDictionary.keySet());
        union.addAll(secondDictionary.keySet());
        totalGramsSize = union.size();
    }

    private void setTotalActivitySize(Map<String,Integer> firstDictionary,Map<String,Integer> secondDictionary){
        Set<String> union = new HashSet<>(firstDictionary.keySet());
        union.addAll(secondDictionary.keySet());
        totalActivitySize = union.size();
    }



    private void calculateActivityScore(Map<String,Integer> firstDictionary,Map<String,Integer> secondDictionary) {
        double equal = getActivityEqualScore(firstDictionary,secondDictionary);
        double semiEqual = getActivitySemiEqualScore(firstDictionary,secondDictionary);
        this.activityScore = equal+semiEqual;
    }

    private void calculateGramsScore(Map<List<String>, Integer> firstDictionary, Map<List<String>, Integer> secondDictionary) {
        double equal = getGramsEqualScore(firstDictionary,secondDictionary);
        double semiEqual = getGramsSemiEqualScore(firstDictionary,secondDictionary);
        this.gramsScore = equal+semiEqual;
    }

    private double getActivityEqualScore(Map<String,Integer> firstDictionary,Map<String,Integer> secondDictionary){
        Set<String> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        double value = 0.0;
        for(String activity : intersection){
            if(firstDictionary.get(activity)==1 && secondDictionary.get(activity)==1){
                value = value + runProperties.getActivityEqualScore();
            }else if(firstDictionary.get(activity)>1 && secondDictionary.get(activity)>1){
                value = value + runProperties.getActivityEqualScore();
            }
        }
        return value;
    }

    private double getActivitySemiEqualScore(Map<String,Integer> firstDictionary,Map<String,Integer> secondDictionary){
        Set<String> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        double value = 0.0;
        for(String activity : intersection){
            if(firstDictionary.get(activity) == 1 && secondDictionary.get(activity)>1){
                value = value + runProperties.getActivitySemiEqualScore();
            }else if(secondDictionary.get(activity)==1 && firstDictionary.get(activity)>1 ){
                value = value + runProperties.getActivitySemiEqualScore();
            }
        }
        return value;
    }

    private double getActivityNotEqualScore(Map<String,Integer> firstDictionary,Map<String,Integer> secondDictionary){
        Set<String> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        Set<String> union = new HashSet<>(firstDictionary.keySet());
        union.addAll(secondDictionary.keySet());
        union.removeAll(intersection);
        double value = 0.0;
        for(String activity : union) {
            value += runProperties.getActivityNotEqualScore();
        }
        return value;

    }

    private double getGramsEqualScore(Map<List<String>, Integer> firstDictionary, Map<List<String>, Integer> secondDictionary){
        Set<List<String>> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        double value = 0.0;
        for(List<String> gram : intersection){
            if(firstDictionary.get(gram)==1 && secondDictionary.get(gram)==1){
                value = value + runProperties.getGramEqualScore();
            }else if(firstDictionary.get(gram)>1 && secondDictionary.get(gram)>1){
                value = value + runProperties.getGramEqualScore();
            }
        }
        return value;

    }

    private double getGramsSemiEqualScore(Map<List<String>, Integer> firstDictionary, Map<List<String>, Integer> secondDictionary){
        Set<List<String>> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        double value = 0.0;
        for(List<String> gram : intersection){
            if(firstDictionary.get(gram) == 1 && secondDictionary.get(gram)>1){
                value = value + runProperties.getGramSemiEqualScore();
            }else if(secondDictionary.get(gram)==1 && firstDictionary.get(gram)>1 ){
                value = value + runProperties.getGramSemiEqualScore();
            }
        }
        return value;
    }

    private double gramsNotEqualScore(Map<List<String>, Integer> firstDictionary, Map<List<String>, Integer> secondDictionary){
        Set<List<String>> intersection = new HashSet<>(firstDictionary.keySet());
        intersection.retainAll(secondDictionary.keySet());
        Set<List<String>> union = new HashSet<>(firstDictionary.keySet());
        union.addAll(secondDictionary.keySet());
        union.removeAll(intersection);
        double value = 0.0;
        for(List<String> activity : union) {
            value += runProperties.getGramNotEqualScore();
        }
        return value;
    }

    private double getTotalGramsScore(){
        double totalGramsScore;
        if (totalGramsSize == 0) totalGramsScore = 0.0;
        else totalGramsScore = ((1 - runProperties.getGamma()) * this.gramsScore) / totalGramsSize;
        return totalGramsScore;
    }

    private double getTotalActivityScore(){
        double totalActivityScore;
        if (totalActivitySize == 0) totalActivityScore = 0.0;
        else totalActivityScore = (runProperties.getGamma() * this.activityScore) / totalActivitySize;
        return totalActivityScore;
    }


    @Override
    public synchronized double compare(LogData s, LogData d) {
        reset();

        calculateGramsScore(s.getGramsDictionary(),d.getGramsDictionary());
        setTotalGramsSize(s.getGramsDictionary(),d.getGramsDictionary());

        calculateActivityScore(s.getDictionaryOfActivities(),d.getDictionaryOfActivities());
        setTotalActivitySize(s.getDictionaryOfActivities(),d.getDictionaryOfActivities());

        return 1 - (getTotalGramsScore() + getTotalActivityScore());
    }

    private void reset() {
        this.activityScore = 0;
        this.gramsScore = 0;

    }


}