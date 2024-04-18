package grafo.comparators;


import grafo.config.RunProperties;
import grafo.model.LogData;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class JaccardSimilarity implements DictionaryComparator {

    private double activityScore;
    private double gramsScore;

    private final RunProperties runProperties;


    public JaccardSimilarity(RunProperties runProperties) {
        this.runProperties = runProperties;
    }


    private void calculateActivityScore(Map<String, Integer> firstDictionaryOfActivities, Map<String, Integer> secondDictionaryOfActivities) {
       double activity;
       if(runProperties.getActivitySemiEqualScore()==0.0){
           activity = activityEqual(firstDictionaryOfActivities,secondDictionaryOfActivities);
       }else{
           activity = activitySemiEqual(firstDictionaryOfActivities,secondDictionaryOfActivities);
       }
       this.activityScore = activity * runProperties.getGamma();
    }

    private void calculateGramsScore(Map<List<String>, Integer> firstSubsequenceMap, Map<List<String>, Integer> secondSubsequenceMap) {
        double grams;
        if(runProperties.getGramSemiEqualScore()==0.0){
            grams = gramsEquals(firstSubsequenceMap,secondSubsequenceMap);
        }else{
            grams = gramsSemiEquals(firstSubsequenceMap,secondSubsequenceMap);
        }
        this.gramsScore = grams * (1 - runProperties.getGamma());
    }



    private double activityEqual(Map<String, Integer> firstDictionaryOfActivities, Map<String, Integer> secondDictionaryOfActivities) {
        Set<String> allActivities = new HashSet<>(firstDictionaryOfActivities.keySet());
        allActivities.addAll(secondDictionaryOfActivities.keySet());
        double intersection = 0.0;
        double union = 0.0;
        for(String activity : allActivities){
            if(firstDictionaryOfActivities.containsKey(activity) && secondDictionaryOfActivities.containsKey(activity)){
                intersection += Math.min(firstDictionaryOfActivities.get(activity),secondDictionaryOfActivities.get(activity));
                union += Math.max(firstDictionaryOfActivities.get(activity),secondDictionaryOfActivities.get(activity));
            }else if(firstDictionaryOfActivities.containsKey(activity)){
                union += firstDictionaryOfActivities.get(activity);
            }else if(secondDictionaryOfActivities.containsKey(activity)){
                union += secondDictionaryOfActivities.get(activity);
            }
        }

        return intersection / union;
    }

    private double activitySemiEqual(Map<String, Integer> firstDictionaryOfActivities, Map<String, Integer> secondDictionaryOfActivities) {
        Set<String> allActivities = new HashSet<>(firstDictionaryOfActivities.keySet());
        allActivities.addAll(secondDictionaryOfActivities.keySet());
        double intersection = 0.0;
        double union = 0.0;
        for(String activity : allActivities){
            if(firstDictionaryOfActivities.containsKey(activity) && secondDictionaryOfActivities.containsKey(activity)){
                intersection += Math.max(firstDictionaryOfActivities.get(activity),secondDictionaryOfActivities.get(activity));
                union += Math.max(firstDictionaryOfActivities.get(activity),secondDictionaryOfActivities.get(activity));
            }else if(firstDictionaryOfActivities.containsKey(activity)){
                union += firstDictionaryOfActivities.get(activity);
            }else if(secondDictionaryOfActivities.containsKey(activity)){
                union += secondDictionaryOfActivities.get(activity);
            }
        }
        return intersection / union;
    }

    private double gramsEquals(Map<List<String>, Integer> firstDictionaryOfGrams, Map<List<String>, Integer> secondDictionaryOfGrams) {
        Set<List<String>> allActivities = new HashSet<>(firstDictionaryOfGrams.keySet());
        allActivities.addAll(secondDictionaryOfGrams.keySet());
        double intersection = 0.0;
        double union = 0.0;
        for(List<String> gram : allActivities){
            if(firstDictionaryOfGrams.containsKey(gram) && secondDictionaryOfGrams.containsKey(gram)){
                intersection += Math.min(firstDictionaryOfGrams.get(gram),secondDictionaryOfGrams.get(gram));
                union += Math.max(firstDictionaryOfGrams.get(gram),secondDictionaryOfGrams.get(gram));
            }else if(firstDictionaryOfGrams.containsKey(gram)){
                union += firstDictionaryOfGrams.get(gram);
            }else if(secondDictionaryOfGrams.containsKey(gram)){
                union += secondDictionaryOfGrams.get(gram);
            }
        }
        return intersection / union;
    }

    private double gramsSemiEquals(Map<List<String>, Integer> firstDictionaryOfGrams, Map<List<String>, Integer> secondDictionaryOfGrams) {
        Set<List<String>> allActivities = new HashSet<>(firstDictionaryOfGrams.keySet());
        allActivities.addAll(secondDictionaryOfGrams.keySet());
        double intersection = 0.0;
        double union = 0.0;
        for(List<String> gram : allActivities){
            if(firstDictionaryOfGrams.containsKey(gram) && secondDictionaryOfGrams.containsKey(gram)){
                intersection += Math.max(firstDictionaryOfGrams.get(gram),secondDictionaryOfGrams.get(gram));
                union += Math.max(firstDictionaryOfGrams.get(gram),secondDictionaryOfGrams.get(gram));
            }else if(firstDictionaryOfGrams.containsKey(gram)){
                union += firstDictionaryOfGrams.get(gram);
            }else if(secondDictionaryOfGrams.containsKey(gram)){
                union += secondDictionaryOfGrams.get(gram);
            }
        }
        return intersection / union;
    }


    private double getActivityScore() {
        return activityScore;
    }

    private double getGramsScore() {
        return gramsScore;
    }


    @Override
    public synchronized double compare(LogData s, LogData d) {
        reset();
        calculateActivityScore(s.getDictionaryOfActivities(), d.getDictionaryOfActivities());
        calculateGramsScore(s.getGramsDictionary(), d.getGramsDictionary());
        return 1 - (getActivityScore() + getGramsScore());
    }

    private void reset() {
        this.activityScore = 0;
        this.gramsScore = 0;
    }


}