import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgentGui extends JFrame {

    private static final long STT_SEED = 6957025L;
    private static final long VISION_SEED = 8814175L;
    private static final int REVEAL_DELAY_MS = 400;

    private final JComboBox<String> scenarioSelector;
    private final JTextField videoLengthField;
    private final JTextField targetDescriptionField;
    private final JTextField guessTimestampField;
    private final JTextField budgetField;
    private final JButton runButton;
    private final JTextArea outputArea;

    private final List<Scenario> presetScenarios;

    public AgentGui() {
        super("Video Clip Cost Agent");

        presetScenarios = buildPresetScenarios();

        scenarioSelector = new JComboBox<>(new String[] {
                "1. Tight Budget - Easy Find",
                "2. Generous Budget - Retry Needed",
                "3. Ambiguous Target - Budget Runs Out",
                "4. Custom scenario"
        });

        videoLengthField = new JTextField();
        targetDescriptionField = new JTextField();
        guessTimestampField = new JTextField();
        budgetField = new JTextField();

        runButton = new JButton("Run Agent");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        setLayout(new BorderLayout(10, 10));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        scenarioSelector.addActionListener(e -> updateCustomFieldsEnabled());
        runButton.addActionListener(this::onRunClicked);

        updateCustomFieldsEnabled();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(labelled("Scenario:", scenarioSelector));
        panel.add(Box.createVerticalStrut(8));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                "Custom scenario details (used only if 'Custom scenario' is selected)"));
        formPanel.add(new JLabel("Video length (seconds):"));
        formPanel.add(videoLengthField);
        formPanel.add(new JLabel("Target description:"));
        formPanel.add(targetDescriptionField);
        formPanel.add(new JLabel("Your rough guess (seconds):"));
        formPanel.add(guessTimestampField);
        formPanel.add(new JLabel("Budget (£):"));
        formPanel.add(budgetField);

        panel.add(formPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(runButton);

        return panel;
    }

    private JPanel labelled(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void updateCustomFieldsEnabled() {
        boolean isCustom = scenarioSelector.getSelectedIndex() == 3;
        videoLengthField.setEnabled(isCustom);
        targetDescriptionField.setEnabled(isCustom);
        guessTimestampField.setEnabled(isCustom);
        budgetField.setEnabled(isCustom);
    }

    private void onRunClicked(ActionEvent event) {
        int selectedIndex = scenarioSelector.getSelectedIndex();

        Scenario scenario;
        SpeechToTextService speechToTextService;
        VisionService visionService;

        if (selectedIndex < presetScenarios.size()) {
            scenario = presetScenarios.get(selectedIndex);
            speechToTextService = new SpeechToTextService(STT_SEED);
            visionService = new VisionService(VISION_SEED);
        } else {
            Scenario customScenario = buildCustomScenario();
            if (customScenario == null) {
                return; // validation failed, error dialog already shown
            }
            scenario = customScenario;
            long randomSeed = System.nanoTime();
            speechToTextService = new SpeechToTextService(randomSeed);
            visionService = new VisionService(randomSeed + 1);
        }

        Agent agent = new Agent(speechToTextService, visionService);
        AgentResult result = agent.run(scenario);

        outputArea.setText("");
        runButton.setEnabled(false);
        revealLines(result.getReportLines(), 0);
    }

    private void revealLines(List<String> lines, int index) {
        if (index >= lines.size()) {
            runButton.setEnabled(true);
            return;
        }
        outputArea.append(lines.get(index) + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());

        Timer timer = new Timer(REVEAL_DELAY_MS, e -> revealLines(lines, index + 1));
        timer.setRepeats(false);
        timer.start();
    }

    private Scenario buildCustomScenario() {
        try {
            int videoLength = Integer.parseInt(videoLengthField.getText().trim());
            if (videoLength <= 0) {
                showError("Video length must be greater than 0.");
                return null;
            }

            String targetDescription = targetDescriptionField.getText().trim();
            if (targetDescription.isEmpty()) {
                showError("Please enter a target description.");
                return null;
            }

            int guess = Integer.parseInt(guessTimestampField.getText().trim());
            if (guess < 0 || guess > videoLength) {
                showError("Guess timestamp must be between 0 and the video length.");
                return null;
            }

            double budget = Double.parseDouble(budgetField.getText().trim());
            if (budget <= 0) {
                showError("Budget must be greater than 0.");
                return null;
            }

            Random groundTruthRandom = new Random();
            int groundTruth = groundTruthRandom.nextInt(videoLength + 1);
            // Ground truth is intentionally never shown - the agent has to find it
            // "blind", exactly like a real speech-to-text/vision service would have to.

            return new Scenario("Custom scenario", targetDescription, videoLength, groundTruth, guess, budget);

        } catch (NumberFormatException e) {
            showError("Please check your numeric fields (video length, guess, budget).");
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    private List<Scenario> buildPresetScenarios() {
        List<Scenario> scenarios = new ArrayList<>();

        scenarios.add(new Scenario(
                "Tight Budget - Easy Find",
                "the word 'launch' is spoken",
                300, 122, 120, 0.10
        ));

        scenarios.add(new Scenario(
                "Generous Budget - Retry Needed",
                "scene change to outdoor shot",
                600, 200, 175, 2.00
        ));

        scenarios.add(new Scenario(
                "Ambiguous Target - Budget Runs Out",
                "the word 'anomaly' spoken",
                400, 90, 40, 0.40
        ));

        return scenarios;
    }
}