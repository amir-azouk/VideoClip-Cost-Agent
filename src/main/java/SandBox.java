public class SandBox {
    public static void main(String[] args) {
        Decision d1 = new Decision(
                "Narrow speech-to-text search (window 35-55)",
                "No confident match (confidence 0.31)",
                "Confidence too low, budget allows retry with vision",
                0.05
        );

        Decision d2 = new Decision(
                "Vision analysis (10 frames around timestamp 45)",
                "Confident match (confidence 0.94)",
                "Vision confirmed the target, stopping here",
                0.25
        );

        d1.print(1);
        d2.print(2);
    }
}