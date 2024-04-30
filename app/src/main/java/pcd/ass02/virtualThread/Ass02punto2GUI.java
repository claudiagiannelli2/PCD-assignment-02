package pcd.ass02.virtualThread;

import pcd.ass02.GenericGUI;

import javax.swing.*;
import java.net.URL;

public class Ass02punto2GUI extends GenericGUI {

    private final Ass02MyVtCoordinator coordinator = new Ass02MyVtCoordinator((x) -> {
        this.updateStatus(x);
        return null;
    }, (x) -> !this.getStopFlag());

    public Ass02punto2GUI() {
        super();
        this.setTitle("Search Tool - VT");
    }

    @Override
    protected void startSearch(URL address, String word, int depth) {
        Thread.ofVirtual().start(() -> {
            try {
                int occurrences = coordinator.getWordOccurrences(address, word, depth);
                this.displayTotalOccurrences(occurrences);
            } catch (InterruptedException e) {
                this.addToOutput("Error: " + e.toString() + "\n");
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Ass02punto2GUI().setVisible(true);
            }

        });
    }
}
