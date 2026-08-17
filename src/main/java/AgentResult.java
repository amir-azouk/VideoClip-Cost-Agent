import java.util.ArrayList;
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

    public List<String> getReportLines() {
        List<String> lines = new ArrayList<>();

        lines.add("=== " + scenario.getName() + " ===");
        lines.add("Target: " + scenario.getTargetDescription());
        lines.add("");

        int stepNumber = 1;
        for (Decision decision : decisions) {
            lines.addAll(decision.getLines(stepNumber));
            stepNumber++;
        }

        lines.add("");
        lines.add(success
                ? "RESULT: Success - clip located at ~" + finalTimestampGuess + "s"
                : "RESULT: Gave up - best guess ~" + finalTimestampGuess + "s (see reasoning above)");

        lines.add("");
        lines.addAll(costTracker.getBreakdownLines());

        return lines;
    }
}