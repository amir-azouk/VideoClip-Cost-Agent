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

    public void print(int stepNumber) {
        SlowPrinter.println("Step " + stepNumber + ": " + stepDescription);
        SlowPrinter.println("  Outcome:   " + outcome);
        SlowPrinter.println("  Reasoning: " + reasoning);
        SlowPrinter.printf("  Cost so far: £%.2f%n", costSoFar);
    }
}