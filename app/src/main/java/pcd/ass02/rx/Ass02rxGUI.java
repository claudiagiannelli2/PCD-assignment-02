package pcd.ass02.rx;

import io.reactivex.rxjava3.subjects.PublishSubject;
import pcd.ass02.GenericGUI;

import javax.swing.*;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class Ass02rxGUI extends GenericGUI {
    private PublishSubject<Boolean> stopEvents = PublishSubject.create();

    public Ass02rxGUI() {
        super();
        setTitle("Search Tool - RX");
        stopButton.addActionListener((e) -> {
            stopEvents.onNext(true);
        });
    }

    @Override
    protected void startSearch(URL address, String word, int depth) {
        new Ass02rx((x) -> {
            this.updateStatus(x);
            return null;
        }, this.stopEvents) // Creare l'istanza del coordinatore
                .getWordOccurrences(address, word, depth)
                .subscribe(
                        this::displayTotalOccurrences,
                        error -> this.addToOutput("Error: " + error.getMessage() + "\n"),
                        () -> {}
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
