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
        System.out.println("Step " + stepNumber + ": " + stepDescription);
        System.out.println("  Outcome:   " + outcome);
        System.out.println("  Reasoning: " + reasoning);
        System.out.printf("  Cost so far: £%.2f%n", costSoFar);
    }
}