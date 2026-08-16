public class VisionResult {

    private final boolean found;
    private final double confidence;
    private final double cost;
    private final int framesChecked;

    public VisionResult(boolean found, double confidence, double cost, int framesChecked) {
        this.found = found;
        this.confidence = confidence;
        this.cost = cost;
        this.framesChecked = framesChecked;
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

    public int getFramesChecked() {
        return framesChecked;
    }
}