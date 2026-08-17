import java.util.List;

public class AgentResult {

    private final Scenario scenario;
    private final boolean success;
    private final int finalTimestampGuess;
    private final CostTracker costTracker;
    private final List<Decision> decisions;

    public AgentResult(Scenario scenario,
                       boolean success,
                       int finalTimestampGuess,
                       CostTracker costTracker,
                       List<Decision> decisions) {
        this.scenario = scenario;
        this.success = success;
        this.finalTimestampGuess = finalTimestampGuess;
        this.costTracker = costTracker;
        this.decisions = decisions;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getFinalTimestampGuess() {
        return finalTimestampGuess;
    }

    public CostTracker getCostTracker() {
        return costTracker;
    }

    public List<Decision> getDecisions() {
        return decisions;
    }

    public void printReport() {
        SlowPrinter.println("=== " + scenario.getName() + " ===");
        SlowPrinter.println("Target: " + scenario.getTargetDescription());
        System.out.println();

        int stepNumber = 1;
        for (Decision decision : decisions) {
            decision.print(stepNumber);
            stepNumber++;
        }

        System.out.println();
        SlowPrinter.println(success
                ? "RESULT: Success - clip located at ~" + finalTimestampGuess + "s"
                : "RESULT: Gave up - best guess ~" + finalTimestampGuess + "s (see reasoning above)");

        System.out.println();
        costTracker.printBreakdown();
        System.out.println();
    }
}