public class Scenario {

    private final String name;
    private final String targetDescription;
    private final int videoLengthSeconds;
    private final int groundTruthTimestamp;
    private final int initialGuessTimestamp;
    private final double budgetPounds;

    public Scenario(String name,
                    String targetDescription,
                    int videoLengthSeconds,
                    int groundTruthTimestamp,
                    int initialGuessTimestamp,
                    double budgetPounds) {
        this.name = name;
        this.targetDescription = targetDescription;
        this.videoLengthSeconds = videoLengthSeconds;
        this.groundTruthTimestamp = groundTruthTimestamp;
        this.initialGuessTimestamp = initialGuessTimestamp;
        this.budgetPounds = budgetPounds;
    }

    public String getName() {
        return name;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public int getVideoLengthSeconds() {
        return videoLengthSeconds;
    }

    public int getGroundTruthTimestamp() {
        return groundTruthTimestamp;
    }

    public int getInitialGuessTimestamp() {
        return initialGuessTimestamp;
    }

    public double getBudgetPounds() {
        return budgetPounds;
    }
}