import java.util.ArrayList;
import java.util.List;

public class Agent {

    private final SpeechToTextService speechToTextService;
    private final VisionService visionService;

    public Agent(SpeechToTextService speechToTextService, VisionService visionService) {
        this.speechToTextService = speechToTextService;
        this.visionService = visionService;
    }

    public AgentResult run(Scenario scenario) {
        CostTracker costTracker = new CostTracker(scenario.getBudgetPounds());
        List<Decision> decisions = new ArrayList<>();

        int guess = scenario.getInitialGuessTimestamp();

        // Step 1: narrow speech-to-text search
        int narrowStart = guess - 10;
        int narrowEnd = guess + 10;
        SpeechToTextResult sttResult = speechToTextService.search(scenario, narrowStart, narrowEnd);
        costTracker.addCharge("Speech-to-text (narrow)", sttResult.getCost());

        decisions.add(new Decision(
                "Narrow speech-to-text search (window " + narrowStart + "-" + narrowEnd + ")",
                describeOutcome(sttResult.isFound(), sttResult.getConfidence()),
                "Cheapest possible first attempt, centred on initial guess",
                costTracker.getTotalSpent()
        ));

        if (sttResult.isFound() && sttResult.getConfidence() >= 0.8) {
            decisions.add(new Decision(
                    "Stop - accept narrow search result",
                    "Confident match, no further spend needed",
                    "Confidence " + roundedPercent(sttResult.getConfidence()) + " is high enough to trust",
                    costTracker.getTotalSpent()
            ));
            return new AgentResult(scenario, true, guess, costTracker, decisions);
        }

        // Decide: retry wider STT, or escalate to vision?
        boolean veryLowConfidence = sttResult.getConfidence() < 0.3;
        double widerSttCost = 0.05;
        double visionCost = 0.02 * 10;

        if (veryLowConfidence) {
            if (costTracker.canAfford(visionCost)) {
                return escalateToVision(scenario, guess, costTracker, decisions,
                        "Initial confidence very low (" + roundedPercent(sttResult.getConfidence())
                                + ") - speech-to-text unlikely to help further, escalating straight to vision");
            } else {
                return giveUp(scenario, guess, costTracker, decisions,
                        "Confidence very low and budget does not allow vision escalation");
            }
        } else {
            if (costTracker.canAfford(widerSttCost)) {
                return retryWiderSpeechToText(scenario, guess, costTracker, decisions);
            } else if (costTracker.canAfford(visionCost)) {
                return escalateToVision(scenario, guess, costTracker, decisions,
                        "Cannot afford a wider speech-to-text retry, but vision is affordable");
            } else {
                return giveUp(scenario, guess, costTracker, decisions,
                        "Moderate confidence but budget does not allow retry or escalation");
            }
        }
    }

    private AgentResult retryWiderSpeechToText(Scenario scenario, int guess,
                                               CostTracker costTracker,
                                               List<Decision> decisions) {
        int wideStart = guess - 30;
        int wideEnd = guess + 30;
        SpeechToTextResult retryResult = speechToTextService.search(scenario, wideStart, wideEnd);
        costTracker.addCharge("Speech-to-text (wide retry)", retryResult.getCost());

        decisions.add(new Decision(
                "Retry speech-to-text with wider window (" + wideStart + "-" + wideEnd + ")",
                describeOutcome(retryResult.isFound(), retryResult.getConfidence()),
                "Moderate initial confidence suggested a wider window might catch it, cheaper than vision",
                costTracker.getTotalSpent()
        ));

        if (retryResult.isFound() && retryResult.getConfidence() >= 0.7) {
            decisions.add(new Decision(
                    "Stop - accept wider search result",
                    "Confident match after retry",
                    "Confidence " + roundedPercent(retryResult.getConfidence()) + " is high enough to trust",
                    costTracker.getTotalSpent()
            ));
            return new AgentResult(scenario, true, guess, costTracker, decisions);
        }

        double visionCost = 0.02 * 10;
        if (costTracker.canAfford(visionCost)) {
            return escalateToVision(scenario, guess, costTracker, decisions,
                    "Wider retry still inconclusive, escalating to vision as last resort");
        } else {
            return giveUp(scenario, guess, costTracker, decisions,
                    "Wider retry still inconclusive and budget does not allow vision");
        }
    }

    private AgentResult escalateToVision(Scenario scenario, int guess,
                                         CostTracker costTracker,
                                         List<Decision> decisions,
                                         String reasoning) {
        int frameCount = 10;
        VisionResult visionResult = visionService.analyse(scenario, guess, frameCount);
        costTracker.addCharge("Vision (" + frameCount + " frames)", visionResult.getCost());

        decisions.add(new Decision(
                "Vision analysis (" + frameCount + " frames around timestamp " + guess + ")",
                describeOutcome(visionResult.isFound(), visionResult.getConfidence()),
                reasoning,
                costTracker.getTotalSpent()
        ));

        boolean success = visionResult.isFound() && visionResult.getConfidence() >= 0.7;
        decisions.add(new Decision(
                success ? "Stop - accept vision result" : "Stop - out of good options",
                success ? "Confident match via vision" : "Vision inconclusive, no budget for further attempts",
                success
                        ? "Confidence " + roundedPercent(visionResult.getConfidence()) + " is high enough to trust"
                        : "Reporting best available guess rather than overspending on further attempts",
                costTracker.getTotalSpent()
        ));

        return new AgentResult(scenario, success, guess, costTracker, decisions);
    }

    private AgentResult giveUp(Scenario scenario, int guess,
                               CostTracker costTracker,
                               List<Decision> decisions,
                               String reasoning) {
        decisions.add(new Decision(
                "Stop - budget exhausted",
                "No confident match found",
                reasoning,
                costTracker.getTotalSpent()
        ));
        return new AgentResult(scenario, false, guess, costTracker, decisions);
    }

    private String describeOutcome(boolean found, double confidence) {
        return (found ? "Match reported" : "No match reported")
                + ", confidence " + roundedPercent(confidence);
    }

    private String roundedPercent(double confidence) {
        return Math.round(confidence * 100) + "%";
    }
}