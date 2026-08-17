import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AgentGui gui = new AgentGui();
            gui.setVisible(true);
        });
    }
}