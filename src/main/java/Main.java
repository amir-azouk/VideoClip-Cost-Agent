import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final long STT_SEED = 6957025L;
    private static final long VISION_SEED = 8814175L;

    public static void main(String[] args) {
        List<Scenario> scenarios = new ArrayList<>();

        scenarios.add(new Scenario(
                "Tight Budget - Easy Find",
                "the word 'launch' is spoken",
                300,
                122,
                120,
                0.10
        ));

        scenarios.add(new Scenario(
                "Generous Budget - Retry Needed",
                "scene change to outdoor shot",
                600,
                200,
                175,
                2.00
        ));

        scenarios.add(new Scenario(
                "Ambiguous Target - Budget Runs Out",
                "the word 'anomaly' spoken",
                400,
                90,
                40,
                0.35
        ));

        for (Scenario scenario : scenarios) {
            SpeechToTextService speechToTextService = new SpeechToTextService(STT_SEED);
            VisionService visionService = new VisionService(VISION_SEED);
            Agent agent = new Agent(speechToTextService, visionService);

            AgentResult result = agent.run(scenario);
            result.printReport();
        }
    }
}