package grafo.model;

import java.util.List;
import java.util.Objects;

/**
 * Support Class to store relevant infos about the traces
 *
 * @author Donici Ionut Bogdan, Riccardo Ceccarani, Roberta Nerla
 */
public class Trace implements Comparable<Trace> {

    // Contenitore per i Grams
    private final List<String> activitySequence;
    private final String traceLine;
    private final String traceId;

    public Trace(List<String> activitySequence, String traceLine, String traceId) {
        this.activitySequence = activitySequence;
        this.traceLine = traceLine;
        this.traceId = traceId;
    }

    public List<String> getActivitySequence() {
        return this.activitySequence;
    }


    public String getTraceLine() {
        return traceLine;
    }


    public String getTraceId() {
        return traceId;
    }




    @Override
    public int compareTo(Trace o) {
        if (this.traceLine.equals(o.traceLine)) return 0;
        else return this.traceId.compareTo(o.getTraceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash( getActivitySequence(), getTraceLine(), getTraceId());
    }

}
