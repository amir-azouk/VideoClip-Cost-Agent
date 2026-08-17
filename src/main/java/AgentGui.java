import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgentGui extends JFrame {

    private static final long STT_SEED = 6957025L;
    private static final long VISION_SEED = 8814175L;
    private static final int REVEAL_DELAY_MS = 400;

    private final JComboBox<String> scenarioSelector;
    private final JTextField videoLengthField;
    private final JButton selectFileButton;
    private final JLabel selectedFileLabel;
    private final JTextField targetDescriptionField;
    private final JTextField guessTimestampField;
    private final JTextField budgetField;
    private final JButton runButton;
    private final JTextArea outputArea;

    private final List<Scenario> presetScenarios;
    private String selectedFileName = null;

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
        selectFileButton = new JButton("Select video file...");
        selectedFileLabel = new JLabel("No file selected (you can also type a length manually)");
        selectedFileLabel.setFont(selectedFileLabel.getFont().deriveFont(Font.ITALIC, 11f));

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
        selectFileButton.addActionListener(this::onSelectFileClicked);
        runButton.addActionListener(this::onRunClicked);

        updateCustomFieldsEnabled();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(labelled("Scenario:", scenarioSelector));
        panel.add(Box.createVerticalStrut(8));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                "Custom scenario details (used only if 'Custom scenario' is selected)"));

        formPanel.add(new JLabel("Video length (seconds):"));
        JPanel videoLengthRow = new JPanel(new BorderLayout(6, 0));
        videoLengthField.setEditable(true);
        videoLengthRow.add(videoLengthField, BorderLayout.CENTER);
        videoLengthRow.add(selectFileButton, BorderLayout.EAST);
        formPanel.add(videoLengthRow);

        formPanel.add(new JLabel(""));
        formPanel.add(selectedFileLabel);

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
        selectFileButton.setEnabled(isCustom);
        targetDescriptionField.setEnabled(isCustom);
        guessTimestampField.setEnabled(isCustom);
        budgetField.setEnabled(isCustom);
    }

    private void onSelectFileClicked(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Video files (.mp4, .mov)", "mp4", "mov"));

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        try {
            int durationSeconds = VideoMetadataReader.readDurationSeconds(file);
            videoLengthField.setText(String.valueOf(durationSeconds));
            videoLengthField.setEditable(false);
            selectedFileName = file.getName();
            selectedFileLabel.setText("Using real duration from: " + selectedFileName + " (" + durationSeconds + "s)");
        } catch (Exception e) {
            selectedFileName = null;
            videoLengthField.setEditable(true);
            showError("Could not read duration from this file (" + e.getMessage() + "). "
                    + "This can happen with some MP4/MOV files depending on how they were encoded. "
                    + "You can still type the video length manually below.");
        }
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

            String name = selectedFileName != null
                    ? "Custom scenario (" + selectedFileName + ")"
                    : "Custom scenario";

            return new Scenario(name, targetDescription, videoLength, groundTruth, guess, budget);

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