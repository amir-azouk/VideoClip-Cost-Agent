import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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

    private static final Color ACCENT_COLOR = new Color(0x2F, 0x6F, 0xED);
    private static final Color SUCCESS_COLOR = new Color(0x1B, 0x8A, 0x3A);
    private static final Color GIVE_UP_COLOR = new Color(0xC9, 0x7A, 0x00);
    private static final Color BACKGROUND_COLOR = new Color(0xFA, 0xFA, 0xFA);

    private final JComboBox<String> scenarioSelector;
    private final JTextField videoLengthField;
    private final JButton selectFileButton;
    private final JLabel selectedFileLabel;
    private final JTextField targetDescriptionField;
    private final JTextField guessTimestampField;
    private final JTextField budgetField;
    private final JButton runButton;
    private final JTextPane outputArea;
    private final StyledDocument outputDocument;

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
        scenarioSelector.setFont(scenarioSelector.getFont().deriveFont(14f));

        videoLengthField = new JTextField();
        selectFileButton = new JButton("Select video file...");
        selectedFileLabel = new JLabel("No file selected (you can also type a length manually)");
        selectedFileLabel.setFont(selectedFileLabel.getFont().deriveFont(Font.ITALIC, 11f));
        selectedFileLabel.setForeground(Color.DARK_GRAY);

        targetDescriptionField = new JTextField();
        guessTimestampField = new JTextField();
        budgetField = new JTextField();

        runButton = createAccentButton("Run Agent");

        outputArea = new JTextPane();
        outputArea.setEditable(false);
        outputArea.setBackground(Color.WHITE);
        outputDocument = outputArea.getStyledDocument();
        defineTextStyles();

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        add(buildTopPanel(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        add(scrollPane, BorderLayout.CENTER);

        scenarioSelector.addActionListener(e -> updateCustomFieldsEnabled());
        selectFileButton.addActionListener(this::onSelectFileClicked);
        runButton.addActionListener(this::onRunClicked);

        updateCustomFieldsEnabled();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 700);
        setLocationRelativeTo(null);
    }

    private JButton createAccentButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT_COLOR.darker() : ACCENT_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setContentAreaFilled(false);
        button.setForeground(Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 15f));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void defineTextStyles() {
        SimpleAttributeSet normal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(normal, Font.MONOSPACED);
        StyleConstants.setFontSize(normal, 13);
        outputArea.addStyle("normal", null);
        StyleConstants.setFontFamily(getStyle("normal"), Font.MONOSPACED);
        StyleConstants.setFontSize(getStyle("normal"), 13);

        var header = outputArea.addStyle("header", getStyle("normal"));
        StyleConstants.setBold(header, true);
        StyleConstants.setForeground(header, ACCENT_COLOR);
        StyleConstants.setFontSize(header, 14);

        var success = outputArea.addStyle("success", getStyle("normal"));
        StyleConstants.setBold(success, true);
        StyleConstants.setForeground(success, SUCCESS_COLOR);

        var giveUp = outputArea.addStyle("giveUp", getStyle("normal"));
        StyleConstants.setBold(giveUp, true);
        StyleConstants.setForeground(giveUp, GIVE_UP_COLOR);

        var step = outputArea.addStyle("step", getStyle("normal"));
        StyleConstants.setBold(step, true);
    }

    private javax.swing.text.Style getStyle(String name) {
        return outputArea.getStyle(name);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        panel.setBackground(BACKGROUND_COLOR);

        JLabel title = new JLabel("Video Clip Cost Agent");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);

        JLabel subtitle = new JLabel("Decides the cheapest reliable way to locate a target moment within your budget");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(Color.DARK_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(14));

        JPanel scenarioRow = labelled("Scenario:", scenarioSelector);
        scenarioRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scenarioRow);
        panel.add(Box.createVerticalStrut(10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Custom scenario details (used only if 'Custom scenario' is selected)"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(new JLabel("Video length (seconds):"));
        JPanel videoLengthRow = new JPanel(new BorderLayout(6, 0));
        videoLengthRow.setBackground(BACKGROUND_COLOR);
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
        panel.add(Box.createVerticalStrut(14));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setBackground(BACKGROUND_COLOR);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(runButton);
        panel.add(buttonRow);

        return panel;
    }

    private JPanel labelled(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BACKGROUND_COLOR);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 13f));
        row.add(labelComponent, BorderLayout.WEST);
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
                return;
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

        appendStyledLine(lines.get(index));

        Timer timer = new Timer(REVEAL_DELAY_MS, e -> revealLines(lines, index + 1));
        timer.setRepeats(false);
        timer.start();
    }

    private void appendStyledLine(String line) {
        String styleName;
        if (line.startsWith("===")) {
            styleName = "header";
        } else if (line.startsWith("RESULT: Success")) {
            styleName = "success";
        } else if (line.startsWith("RESULT: Gave up")) {
            styleName = "giveUp";
        } else if (line.startsWith("Step ")) {
            styleName = "step";
        } else {
            styleName = "normal";
        }

        try {
            outputDocument.insertString(outputDocument.getLength(), line + "\n", getStyle(styleName));
        } catch (javax.swing.text.BadLocationException e) {
            // Should not happen since we always insert at the current end of the document.
        }

        outputArea.setCaretPosition(outputDocument.getLength());
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