package pcd.ass02.rx;

import pcd.ass02.Pair;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ass02rxCLI {
    private volatile boolean stopFlag = false;
    private int totalOccurrences;

    public Ass02rxCLI() {}



    private void search(String address, String word, int depth) {
        URL parsedURL;
        this.stopFlag = false;
        final Map<Integer, Integer> interimReport = new HashMap<>();
        
        Function<Pair<Integer, Integer>, Void> f = (x) -> {
            if (!interimReport.containsKey(x.getLeft())) {
                interimReport.put(x.getLeft(), 0);
            }
            interimReport.put(x.getLeft(), interimReport.get(x.getLeft()) + x.getRight());
            int total = interimReport.values().stream().reduce(0, Integer::sum);
            System.out.println("At level " + (depth - x.getLeft()) + ": found " + interimReport.get(x.getLeft())
                    + " occurrences (total: " + total + ")");
            return null;
        };

        try {
            parsedURL = new URI(address).toURL();
            totalOccurrences = 0;
            Ass02rx rx = new Ass02rx(f);
            rx.getWordOccurrences(parsedURL, word, depth)
                /*.subscribe(
                    occurrences -> {
                        totalOccurrences += occurrences;
                    },
                    error -> System.out.println("Error: " + error.getMessage()),
                    () -> {
                        if (this.stopFlag) {
                            System.out.println("Stopped! Found " + totalOccurrences + " occurrences");
                        } else {
                            System.out.println("Done! Found " + totalOccurrences + " occurrences");
                        }
                    }
                )*/;
        } catch (Exception e) {
            System.out.println("Invalid URL");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*if (args.length != 3) {
            System.out.println("Usage: java Ass02rxCLI <address> <word> <depth>");
            System.exit(1);
        }

        String address = args[0];
        String word = args[1];
        int depth = Integer.parseInt(args[2]);*/
        String address = "https://scuola.eutampieri.eu";
        String word = "il";
        int depth = 1;

        Ass02rxCLI cli = new Ass02rxCLI();
        cli.search(address, word, depth);
    }
}
