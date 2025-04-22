package pvlov;

public sealed interface Scan permits Scan.Clean, Scan.Infected, Scan.SizeExceeded {

    static Scan clean() {
        return Clean.INSTANCE;
    }

    static Scan infected(final String response) {
        return new Infected(response);
    }

    static Scan sizeExceeded(final String response) {
        return new SizeExceeded(response);
    }

    record Clean() implements Scan {
        public static final Clean INSTANCE = new Clean();
    }

    record Infected(String response) implements Scan {}

    record SizeExceeded(String response) implements Scan {}
}

