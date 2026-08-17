import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        Scanner scanner = new Scanner(System.in);
        int choice = promptForScenarioChoice(scanner, scenarios.size());

        Scenario selectedScenario;
        SpeechToTextService speechToTextService;
        VisionService visionService;

        if (choice <= scenarios.size()) {
            selectedScenario = scenarios.get(choice - 1);
            speechToTextService = new SpeechToTextService(STT_SEED);
            visionService = new VisionService(VISION_SEED);
        } else {
            selectedScenario = buildCustomScenario(scanner);
            long randomSeed = System.nanoTime();
            speechToTextService = new SpeechToTextService(randomSeed);
            visionService = new VisionService(randomSeed + 1);
        }

        Agent agent = new Agent(speechToTextService, visionService);
        AgentResult result = agent.run(selectedScenario);
        result.printReport();
    }

    private static int promptForScenarioChoice(Scanner scanner, int presetCount) {
        System.out.println("Choose a scenario to run:");
        System.out.println("  1. Tight Budget - Easy Find");
        System.out.println("  2. Generous Budget - Retry Needed");
        System.out.println("  3. Ambiguous Target - Budget Runs Out");
        System.out.println("  4. Custom scenario (enter your own target and budget)");

        int totalOptions = presetCount + 1;
        int choice = -1;

        while (choice < 1 || choice > totalOptions) {
            System.out.print("Enter 1-" + totalOptions + ": ");
            String input = scanner.nextLine().trim();
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                choice = -1;
            }
            if (choice < 1 || choice > totalOptions) {
                System.out.println("Invalid choice, try again.");
            }
        }

        return choice;
    }

    private static Scenario buildCustomScenario(Scanner scanner) {
        System.out.println();
        System.out.println("--- Custom scenario setup ---");

        int videoLength = promptForPositiveInt(scanner, "Video length in seconds: ");
        String targetDescription = promptForNonEmptyString(scanner, "What are you searching for (e.g. \"the word 'launch' is spoken\"): ");
        int initialGuess = promptForBoundedInt(scanner, "Your rough guess of the timestamp (seconds, 0-" + videoLength + "): ", 0, videoLength);
        double budget = promptForPositiveDouble(scanner, "Budget in pounds (e.g. 0.50): ");

        Random groundTruthRandom = new Random();
        int groundTruth = groundTruthRandom.nextInt(videoLength + 1);
        // Ground truth is intentionally never shown - the agent has to find it "blind",
        // exactly like a real speech-to-text/vision service would have to.

        return new Scenario(
                "Custom scenario",
                targetDescription,
                videoLength,
                groundTruth,
                initialGuess,
                budget
        );
    }

    private static int promptForPositiveInt(Scanner scanner, String prompt) {
        int value = -1;
        while (value <= 0) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                value = -1;
            }
            if (value <= 0) {
                System.out.println("Please enter a whole number greater than 0.");
            }
        }
        return value;
    }

    private static int promptForBoundedInt(Scanner scanner, String prompt, int min, int max) {
        int value = min - 1;
        while (value < min || value > max) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                value = min - 1;
            }
            if (value < min || value > max) {
                System.out.println("Please enter a whole number between " + min + " and " + max + ".");
            }
        }
        return value;
    }

    private static double promptForPositiveDouble(Scanner scanner, String prompt) {
        double value = -1;
        while (value <= 0) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                value = -1;
            }
            if (value <= 0) {
                System.out.println("Please enter a number greater than 0.");
            }
        }
        return value;
    }

    private static String promptForNonEmptyString(Scanner scanner, String prompt) {
        String value = "";
        while (value.isEmpty()) {
            System.out.print(prompt);
            value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                System.out.println("Please enter something.");
            }
        }
        return value;
    }
}