import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                0.40
        ));

        int choice = promptForScenarioChoice(scenarios);
        Scenario selectedScenario = scenarios.get(choice - 1);

        SpeechToTextService speechToTextService = new SpeechToTextService(STT_SEED);
        VisionService visionService = new VisionService(VISION_SEED);
        Agent agent = new Agent(speechToTextService, visionService);

        AgentResult result = agent.run(selectedScenario);
        result.printReport();
    }

    private static int promptForScenarioChoice(List<Scenario> scenarios) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose a scenario to run:");
        for (int i = 0; i < scenarios.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + scenarios.get(i).getName());
        }

        int choice = -1;
        while (choice < 1 || choice > scenarios.size()) {
            System.out.print("Enter 1-" + scenarios.size() + ": ");
            String input = scanner.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                choice = -1;
            }
            if (choice < 1 || choice > scenarios.size()) {
                System.out.println("Invalid choice, try again.");
            }
        }

        return choice;
    }
}