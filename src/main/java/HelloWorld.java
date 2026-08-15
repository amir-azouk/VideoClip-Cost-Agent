public class HelloWorld {
    public static void main(String[] args) {
        Scenario scenario = new Scenario(
                "Test scenario",
                "the word 'launch' is spoken",
                120,
                45,
                40,
                5.00
        );

        SpeechToTextService stt = new SpeechToTextService(42);

        SpeechToTextResult result = stt.search(scenario, 30, 50);

        System.out.println("Found: " + result.isFound());
        System.out.println("Confidence: " + result.getConfidence());
        System.out.println("Cost: £" + result.getCost());
    }
}