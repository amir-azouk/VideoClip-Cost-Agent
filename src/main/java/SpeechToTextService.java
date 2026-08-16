import java.util.Random;

public class SpeechToTextService {

    private static final double BASE_COST_PER_CALL = 0.01;
    private static final double COST_PER_SECOND = 0.002;

    private final Random random;

    public SpeechToTextService(long seed) {
        this.random = new Random(seed);
    }

    public SpeechToTextResult search(Scenario scenario, int windowStart, int windowEnd) {
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

        int windowSizeSeconds = windowEnd - windowStart;
        double cost = BASE_COST_PER_CALL + (COST_PER_SECOND * windowSizeSeconds);

        return new SpeechToTextResult(found, confidence, cost);
    }
}