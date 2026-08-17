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

    public List<String> getBreakdownLines() {
        List<String> lines = new ArrayList<>();
        lines.add("--- Cost breakdown ---");
        for (Charge charge : charges) {
            lines.add(String.format("  %-25s £%.2f", charge.getServiceName(), charge.getAmount()));
        }
        lines.add(String.format("  %-25s £%.2f", "TOTAL", getTotalSpent()));
        lines.add(String.format("  %-25s £%.2f", "Budget was", budgetPounds));
        return lines;
    }
}