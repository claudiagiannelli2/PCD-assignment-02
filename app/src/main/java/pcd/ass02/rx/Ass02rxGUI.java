package pcd.ass02.rx;

import pcd.ass02.GenericGUI;

import javax.swing.*;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Ass02rxGUI extends GenericGUI {

    public Ass02rxGUI() {
        super();
        setTitle("Search Tool - RX");
    }

    @Override
    protected void startSearch(URL address, String word, int depth) {
        AtomicInteger totalOccurrences = new AtomicInteger();
        //outputArea.append("Done! Found " + occurrences + " occurrences\n");
        // Aggiorna il valore di totalOccurrences
        new Ass02rx((x) -> {
            this.updateStatus(x);
            return null;
        }, (x) -> !this.getStopFlag()) // Creare l'istanza del coordinatore
                .getWordOccurrences(address, word, depth)
                .subscribe(
                        totalOccurrences::addAndGet,
                        error -> this.addToOutput("Error: " + error.getMessage() + "\n"),
                        () -> this.displayTotalOccurrences(totalOccurrences.get())
                );
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Ass02rxGUI().setVisible(true);
            }

        });
    }
}
