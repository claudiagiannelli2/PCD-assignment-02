package pcd.ass02.eventLoop;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import pcd.ass02.*;

class Ass02MyVerticle extends AbstractVerticle {

    private final URL address;
    private final String word;
    private final int depth;
    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final Function<Void, Boolean> shouldRun;
    private Ass02Step1GUI guiInstance=null;
    private Ass02Step1CLI cliInstance=null;

    public Ass02MyVerticle(URL address, String word, int depth, Function<Pair<Integer, Integer>, Void> sendUpdates, Function<Void, Boolean> shouldRun, Ass02Step1GUI guiInstance) {
        this.address = address;
        this.word = word;
        this.depth = depth;
        this.sendUpdates = sendUpdates;
        this.shouldRun = shouldRun;
        this.guiInstance = guiInstance;
    }
    public Ass02MyVerticle(URL address, String word, int depth, Function<Pair<Integer, Integer>, Void> sendUpdates, Function<Void, Boolean> shouldRun, Ass02Step1CLI cliInstance) {
        this.address = address;
        this.word = word;
        this.depth = depth;
        this.sendUpdates = sendUpdates;
        this.shouldRun = shouldRun;
        this.cliInstance = cliInstance;
    }

    @Override
    public void start() {
        getWordOccurrences(address, word, depth)
                .onComplete(report -> {
                    if (report.succeeded()) {
                        //System.out.println("Report: " + report.result());
                        if(guiInstance!=null) {
                        guiInstance.displayTotalOccurrences(report.result());
                        }
                        else {
                        cliInstance.displayTotalOccurrences(report.result());  
                        }
                    } else {
                        System.out.println("Failed to get word occurrences: " + report.cause().getMessage());
                    }
                });

    }

    public Future<Integer> getWordOccurrences(URL indirizzo, String parola, int profondita) {
        ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
        ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
        ExtractionTask<String, Integer> occurrencesExtractor = new WordOccurrencesExtractor(parola.toLowerCase());

        int port = indirizzo.getPort();
        // Se la porta non è specificata nell'URL (-1), imposta la porta in base al protocollo (443 per HTTPS, 80 per HTTP).
        if(port == -1) {
            port = indirizzo.getProtocol().equals("https") ? 443 : 80;
        }
        //System.err.println(profondita);
        //System.err.println(indirizzo);
        //System.err.println("-----");
        HttpRequest<Buffer> request = WebClient.create(vertx)
                .get(port, indirizzo.getHost(), indirizzo.getPath())
                .ssl(port == 443)
                //.timeout(3500)
                // Imposta l'header "User-Agent" per identificare il bot che effettua la richiesta.
                .putHeader("User-Agent", "PCDBot/1.0 (eugenio.tampieri@studio.unibo.it)");
        //Invia la richiesta HTTP asincrona utilizzando il metodo .send().
        var response = request.send();
        return response.compose(asyncResult -> {
            // System.out.println(asyncResult.result().body());
            // System.out.println();
            // Controlla il tipo di contenuto della risposta attraverso l'header "Content-Type". Se non è di tipo testo o applicazione, restituisce subito una Future con valore 0.
            var contentType = asyncResult.getHeader("Content-Type");
            if(!contentType.contains("text") & !contentType.contains("application")){
                return Future.succeededFuture(0);
            }
            // estrae il corpo della risposta come stringa
            String body = asyncResult.bodyAsString();
            int occ = occurrencesExtractor.extract(body.toLowerCase());
            // Invia gli aggiornamenti sul numero di occorrenze trovate utilizzando la funzione sendUpdates.
            sendUpdates.apply(Pair.of(profondita, occ));
            // Estrae i link dalla pagina utilizzando LinkExtractor.extract()
            Webpage p = new Webpage(body, indirizzo);
            List<URL> nextLinks = absLinksExtractor.extract(p);
            nextLinks.addAll(relLinksExtractor.extract(p));
            //List<URL> nextLinks = LinkExtractor.extract(body, indirizzo);
            
            // System.out.println(nextLinks); // For debugging purposes
            // Se la profondità è maggiore di 0, chiama ricorsivamente getWordOccurrences() per ogni link trovato e aggrega i risultati.         

            if(!shouldRun.apply(null)) {
                System.out.println("Stopping early!");
            }
            
            if (profondita > 0 && shouldRun.apply(null)) {
                // La lista resultAtNextLevel conterrà le future risultanti da ogni chiamata ricorsiva.
                List<Future<Integer>> resultAtNextLevel = nextLinks.stream().map((x) -> this.getWordOccurrences(x, parola, profondita - 1)).toList();
                // Utilizza Future.all() per aspettare il completamento di tutte le future nella lista resultAtNextLevel.
                var atNextLevel = Future.all(resultAtNextLevel).map((x) -> {
                    List<Integer> l = x.list();
                    return l.stream().reduce(0, (acc, v) -> acc + v);
                });
                
                return atNextLevel.map((x) -> x + occ);
            } else {
                return Future.succeededFuture(occ);
            }
        }).otherwise(0);
    }

}
