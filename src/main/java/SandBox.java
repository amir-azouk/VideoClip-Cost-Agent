public class SandBox {
    public static void main(String[] args) {
        Scenario scenario = new Scenario(
                "Test scenario",
                "the word 'launch' is spoken",
                120,
                45,
                40,
                5.00
        );

        VisionService vision = new VisionService(42);

        VisionResult result = vision.analyse(scenario, 45, 10);

        System.out.println("Found: " + result.isFound());
        System.out.println("Confidence: " + result.getConfidence());
        System.out.println("Frames checked: " + result.getFramesChecked());
        System.out.println("Cost: £" + result.getCost());
    }
}