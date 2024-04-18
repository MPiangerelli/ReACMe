package grafo.config;

import grafo.comparators.JaccardSimilarity;

/**
 * Questa enumerazione rappresenta i parametri di default per
 * l'esecuzione del programma
 */
public enum DefaultProperties {
    DEFAULT_GRAMS_EQUAL_SCORE(1.0),
    DEFAULT_GRAMS_SEMI_EQUAL_SCORE(0.0),
    DEFAULT_GRAMS_NOT_EQUAL_SCORE(0.0),

    DEFAULT_ACTIVITY_EQUAL_SCORE(1.0),
    DEFAULT_ACTIVITY_SEMI_EQUAL_SCORE(0.0),
    DEFAULT_ACTIVITY_NOT_EQUAL_SCORE(0.0),

    DEFAULT_GAMMA(0.0 ),
    DEFAULT_GRAMS(2.0);


    private final Double value;

    DefaultProperties(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

}
