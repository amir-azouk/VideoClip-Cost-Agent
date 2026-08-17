public class SlowPrinter {

    private static final long DEFAULT_DELAY_MS = 500;

    public static void println(String text) {
        println(text, DEFAULT_DELAY_MS);
    }

    public static void println(String text, long delayMs) {
        System.out.println(text);
        pause(delayMs);
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
        pause(DEFAULT_DELAY_MS);
    }

    private static void pause(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}