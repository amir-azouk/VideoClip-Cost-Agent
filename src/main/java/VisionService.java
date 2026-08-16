import java.util.Random;

public class VisionService {

    private static final double COST_PER_FRAME = 0.02;
    private static final int SIMULATED_LATENCY_MS = 400;

    private final Random random;

    public VisionService(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    public VisionResult analyse(Scenario scenario, int centerTimestamp, int frameCount) {
        simulateLatency();

        int halfRange = frameCount / 2;
        int rangeStart = centerTimestamp - halfRange;
        int rangeEnd = centerTimestamp + halfRange;

        int groundTruth = scenario.getGroundTruthTimestamp();
        boolean targetInRange = groundTruth >= rangeStart && groundTruth <= rangeEnd;

        boolean found;
        double confidence;

        if (targetInRange) {
            double roll = random.nextDouble();
            if (roll < 0.95) {
                found = true;
                confidence = 0.90 + (random.nextDouble() * 0.10);
            } else {
                found = false;
                confidence = 0.3 + (random.nextDouble() * 0.2);
            }
        } else {
            double roll = random.nextDouble();
            if (roll < 0.03) {
                found = true;
                confidence = 0.4 + (random.nextDouble() * 0.2);
            } else {
                found = false;
                confidence = 0.02 + (random.nextDouble() * 0.08);
            }
        }

        double cost = COST_PER_FRAME * frameCount;

        return new VisionResult(found, confidence, cost, frameCount);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}