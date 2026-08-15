import java.util.ArrayList;
import java.util.List;

public class CostTracker {

    public static class Charge {
        private final String serviceName;
        private final double amount;

        public Charge(String serviceName, double amount) {
            this.serviceName = serviceName;
            this.amount = amount;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getAmount() {
            return amount;
        }
    }

    private final double budgetPounds;
    private final List<Charge> charges = new ArrayList<>();

    public CostTracker(double budgetPounds) {
        this.budgetPounds = budgetPounds;
    }

    public void addCharge(String serviceName, double amount) {
        charges.add(new Charge(serviceName, amount));
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Charge charge : charges) {
            total += charge.getAmount();
        }
        return total;
    }

    public double getRemainingBudget() {
        return budgetPounds - getTotalSpent();
    }

    public boolean canAfford(double amount) {
        return getTotalSpent() + amount <= budgetPounds;
    }

    public List<Charge> getCharges() {
        return charges;
    }

    public void printBreakdown() {
        System.out.println("--- Cost Breakdown ---");
        for (Charge charge : charges) {
            System.out.printf("  %-20s £%.2f%n", charge.getServiceName(), charge.getAmount());
        }
        System.out.printf("  %-20s £%.2f%n", "TOTAL", getTotalSpent());
        System.out.printf("  %-20s £%.2f%n", "Budget was", budgetPounds);
    }
}