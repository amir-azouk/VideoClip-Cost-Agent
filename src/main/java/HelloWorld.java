public class HelloWorld {
    public static void main(String[] args) {
        CostTracker tracker = new CostTracker(1.00);

        tracker.addCharge("Speech-to-text", 0.05);
        tracker.addCharge("Speech-to-text", 0.05);
        tracker.addCharge("Vision (8 frames)", 0.16);

        tracker.printBreakdown();

        System.out.println("Can afford £0.50 more? " + tracker.canAfford(0.50));
        System.out.println("Can afford £1.00 more? " + tracker.canAfford(1.00));
    }
}