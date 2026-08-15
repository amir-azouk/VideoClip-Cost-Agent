public class SpeechToTextResult {

    private final boolean found;
    private final double confidence;
    private final double cost;

    public SpeechToTextResult(boolean found, double confidence, double cost) {
        this.found = found;
        this.confidence = confidence;
        this.cost = cost;
    }

    public boolean isFound() {
        return found;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getCost() {
        return cost;
    }
}