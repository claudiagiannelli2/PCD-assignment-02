package pcd.ass02.virtualThread;

import pcd.ass02.Pair;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Ass02punto2CLI {
    private volatile boolean stopFlag = false;
    private int totalOccurrences;
    private Ass02MyVtCoordinator coordinator;

    public Ass02punto2CLI() {}


    private void search(String indirizzo, String parola, int profondita) {
        URL parsedURL;
        this.stopFlag = false;
        final Map<Integer, Integer> interimReport = new HashMap<>();

        Function<Pair<Integer, Integer>, Void> f = (x) -> {
            if (!interimReport.containsKey(x.getLeft())) {
                interimReport.put(x.getLeft(), 0);
            }
            interimReport.put(x.getLeft(), interimReport.get(x.getLeft()) + x.getRight());
            int total = interimReport.values().stream().reduce(0, Integer::sum);
            System.out.println("At level " + (profondita - x.getLeft()) + ": found " + interimReport.get(x.getLeft())
                    + " occurrences (total: " + total + ")");
            return null;
        };

        try {
            parsedURL = new URI(indirizzo).toURL();
            coordinator = new Ass02MyVtCoordinator(f, (x) -> !this.stopFlag);
            totalOccurrences = coordinator.getWordOccurrences(parsedURL, parola, profondita);
            if(this.stopFlag) {
                System.out.println("Stopped! Found " + totalOccurrences + " occurrences");
            } else {
                System.out.println("Done! Found " + totalOccurrences + " occurrences");
            }
            

        } catch (Exception e) {
            System.out.println("Invalid URL");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*if (args.length != 3) {
            System.out.println("Usage: java Ass02punto2CLI <indirizzo> <parola> <profondita>");
            System.exit(1);
        }*/

        //String indirizzo = args[0];
        //String parola = args[1];
        //int profondita = Integer.parseInt(args[2]);
        String indirizzo = "https://scuola.eutampieri.eu";
        String parola = "il";
        int profondita = 1;

        Ass02punto2CLI cli = new Ass02punto2CLI();
        cli.search(indirizzo, parola, profondita);
    }
}
