import java.util.Random;

public class SpeechToTextService {

    private static final double COST_PER_CALL = 0.05;
    private static final int SIMULATED_LATENCY_MS = 200;

    private final Random random;

    public SpeechToTextService(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    public SpeechToTextResult search(Scenario scenario, int windowStart, int windowEnd) {
        simulateLatency();

        int groundTruth = scenario.getGroundTruthTimestamp();
        boolean targetInWindow = groundTruth >= windowStart && groundTruth <= windowEnd;

        boolean found;
        double confidence;

        if (targetInWindow) {
            double roll = random.nextDouble();
            if (roll < 0.75) {
                found = true;
                confidence = 0.85 + (random.nextDouble() * 0.15);
            } else {
                found = false;
                confidence = 0.2 + (random.nextDouble() * 0.2);
            }
        } else {
            double roll = random.nextDouble();
            if (roll < 0.10) {
                found = true;
                confidence = 0.3 + (random.nextDouble() * 0.2);
            } else {
                found = false;
                confidence = 0.05 + (random.nextDouble() * 0.1);
            }
        }

        return new SpeechToTextResult(found, confidence, COST_PER_CALL);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}