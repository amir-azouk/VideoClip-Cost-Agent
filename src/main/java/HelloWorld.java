public class HelloWorld {
    public static void main(String[] args) {
        Scenario test = new Scenario(
                "Test scenario",
                "the word 'launch' is spoken",
                120,
                45,
                40,
                5.00
        );

        System.out.println("Scenario name: " + test.getName());
        System.out.println("Budget: £" + test.getBudgetPounds());
    }
}