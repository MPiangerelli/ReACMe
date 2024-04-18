package grafo.config;

import grafo.comparators.DictionaryComparator;
import grafo.filters.LowHighFilter;

import static grafo.config.DefaultProperties.*;

/**
 * Questa classe rappresenta un contenitore dei parametri utilizzati generalmente nell'algoritmo
 * di process mining. Infatti in essa si possono trovare i seguenti parametri:
 * <ul>
 *     <li><code>gramsEqualScore</code> - di default, il valore sta a <code>1</code></li>
 *     <li><code>gramsSemiEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>gramsNotEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>activityEqualScore</code> - di default, il valore sta a <code>1</code></li>
 *     <li><code>activitySemiEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>activityNotEqualsScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>gamma</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>grams</code> - di default, il valore sta a <code>3</code></li>
 * </ul>
 *
 * @author Bogdan Donici
 * @see DefaultProperties
 */
public class RunProperties {
    private final double gramsEqualScore;
    private final double gramsSemiEqualScore;
    private final double gramsNotEqualScore;
    private final double activityEqualScore;
    private final double activitySemiEqualScore;
    private final double activityNotEqualScore;
    private double gamma;
    private double grams;


    private DictionaryComparator comparator;

    private final LowHighFilter lowHighFilter;


    public RunProperties(double activityEqualScore, double activityNotEqualScore, double activitySemiEqualScore, double gramsEqualScore, double gramsNotEqualScore, double gramsSemiEqualScore, double gamma, double grams) {
        this.activityEqualScore = activityEqualScore;
        this.activityNotEqualScore = activityNotEqualScore;
        this.activitySemiEqualScore = activitySemiEqualScore;
        this.gramsEqualScore = gramsEqualScore;
        this.gramsNotEqualScore = gramsNotEqualScore;
        this.gramsSemiEqualScore = gramsSemiEqualScore;
        this.gamma = gamma;
        this.grams = grams;
        this.lowHighFilter = new LowHighFilter(0, Integer.MAX_VALUE);
    }

    public RunProperties() {
        this(DEFAULT_ACTIVITY_EQUAL_SCORE.getValue(), DEFAULT_ACTIVITY_NOT_EQUAL_SCORE.getValue(), DEFAULT_ACTIVITY_SEMI_EQUAL_SCORE.getValue(), DEFAULT_GRAMS_EQUAL_SCORE.getValue(), DEFAULT_GRAMS_NOT_EQUAL_SCORE.getValue(), DEFAULT_GRAMS_SEMI_EQUAL_SCORE.getValue(), DEFAULT_GAMMA.getValue(), DEFAULT_GRAMS.getValue());
    }

    public void setComparator(DictionaryComparator comparator) {
        this.comparator = comparator;
    }
    public DictionaryComparator getComparator() {
        return comparator;
    }


    public double getGramEqualScore() {
        return gramsEqualScore;
    }

    public double getGramSemiEqualScore() {
        return gramsSemiEqualScore;
    }

    public double getGramNotEqualScore() {
        return gramsNotEqualScore;
    }

    public double getActivityEqualScore() {
        return activityEqualScore;
    }

    public double getActivitySemiEqualScore() {
        return activitySemiEqualScore;
    }

    public double getActivityNotEqualScore() {
        return activityNotEqualScore;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(int grams) {
        this.grams = grams;
    }

    @Override
    public String toString() {
        return "RunProperties{" + "gramsEqualScore=" + gramsEqualScore + ", gramsSemiEqualScore=" + gramsSemiEqualScore + ", gramsNotEqualScore=" + gramsNotEqualScore + ", activityEqualScore=" + activityEqualScore + ", activitySemiEqualScore=" + activitySemiEqualScore + ", activityNotEqualScore=" + activityNotEqualScore + ", gamma=" + gamma + ", grams=" + grams + '}';
    }

    public LowHighFilter getLowHighFilter() {
        return lowHighFilter;
    }
}
