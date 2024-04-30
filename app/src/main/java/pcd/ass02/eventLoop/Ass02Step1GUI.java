package pcd.ass02.eventLoop;

import io.vertx.core.Vertx;
import pcd.ass02.GenericGUI;

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Ass02Step1GUI extends GenericGUI {

    public Ass02Step1GUI() {
        super();
        setTitle("Search Tool - Vert.x");
    }


    @Override
    protected void startSearch(URL address, String word, int depth) {
        final Map<Integer, Integer> interimReport = new HashMap<>();
        // accetta una coppia di valori <Integer, Integer> (presumibilmente rappresentanti il livello di profondità e il numero di occorrenze trovate a quel livello) e restituisce Void
        // la x è la pair
        // Chiamata al tuo metodo per eseguire la ricerca
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Ass02MyVerticle(address, word, depth, (x) -> {
            this.updateStatus(x);
            return null;
        }, (x) -> {
            return !this.getStopFlag();
        }, this));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Ass02Step1GUI().setVisible(true);
            }
        });
    }
}
