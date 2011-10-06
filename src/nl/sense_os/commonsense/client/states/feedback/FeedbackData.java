package nl.sense_os.commonsense.client.states.feedback;

public class FeedbackData {

    static final int TYPE_ADD = 1;
    static final int TYPE_REMOVE = -1;
    private long start;
    private long end;
    private String label;
    private int type;

    /**
     * @param start
     *            Start time of feedback period.
     * @param end
     *            End time of feedback period.
     * @param type
     *            Type of feedback change.
     * @param label
     *            (Optional) label of the feedback state.
     */
    public FeedbackData(long start, long end, int type, String label) {
        setStart(start);
        setEnd(end);
        setType(type);
        setLabel(label);
    }

    long getEnd() {
        return end;
    }

    String getLabel() {
        return label;
    }

    long getStart() {
        return start;
    }

    int getType() {
        return type;
    }
    void setEnd(long end) {
        this.end = end;
    }
    void setLabel(String label) {
        this.label = label;
    }
    void setStart(long start) {
        this.start = start;
    }

    void setType(int type) {
        this.type = type;
    }
}
