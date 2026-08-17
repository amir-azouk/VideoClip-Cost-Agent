import java.util.ArrayList;
import java.util.List;

public class Decision {

    private final String stepDescription;
    private final String outcome;
    private final String reasoning;
    private final double costSoFar;

    public Decision(String stepDescription, String outcome, String reasoning, double costSoFar) {
        this.stepDescription = stepDescription;
        this.outcome = outcome;
        this.reasoning = reasoning;
        this.costSoFar = costSoFar;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getReasoning() {
        return reasoning;
    }

    public double getCostSoFar() {
        return costSoFar;
    }

    public List<String> getLines(int stepNumber) {
        List<String> lines = new ArrayList<>();
        lines.add("Step " + stepNumber + ": " + stepDescription);
        lines.add("  Outcome:   " + outcome);
        lines.add("  Reasoning: " + reasoning);
        lines.add(String.format("  Cost so far: £%.2f", costSoFar));
        return lines;
    }
}