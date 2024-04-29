package pcd.ass02.rx;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import pcd.ass02.*;

public class Ass02rx {

    private final URL indirizzo;
    private final String parola;
    private final int profondita;
    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final Function<Void, Boolean> shouldRun;
    private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
    private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
    private final ExtractionTask<String, Integer> occurrencesExtractor;
    private final WebPageExtractor e = new WebPageExtractor();
    private BlockingQueue<Pair<URL, Integer>> linksQueue = new LinkedBlockingQueue<>();
    private boolean done = false;
    
    public Ass02rx(URL indirizzo, String parola, int profondita, Function<Pair<Integer, Integer>, Void> sendUpdates, Function<Void, Boolean> shouldRun) {
            this.indirizzo = indirizzo;
            this.parola = parola;
            this.profondita = profondita;
            this.sendUpdates = sendUpdates;
            this.shouldRun = shouldRun;
            occurrencesExtractor = new WordOccurrencesExtractor(parola.toLowerCase());
    }

       

    public Observable<Integer> getWordOccurrences(URL indirizzo, int profondita) {
        return Observable.create(emitter -> {
            linksQueue.add(Pair.of(indirizzo, profondita));    
            while (shouldRun.apply(null) && !this.done) {
                Pair<URL, Integer> pair = linksQueue.take();
                if(pair.getLeft() == null) {
                    continue;
                }
                doJob(pair.getLeft(), pair.getRight())
                    .flatMap(resultPair -> {
                        int leftValue = resultPair.getLeft();
                        List<URL> rightValue = resultPair.getRight();
                        if (pair.getRight() > 0) {
                            linksQueue.addAll(rightValue.stream().map(x -> Pair.of(x, profondita - 1)).toList());
                        }
                        else if(this.linksQueue.isEmpty()) {
                        this.done = true;
                        this.linksQueue.add(Pair.of(null, null));
                        }
                        return Observable.just(leftValue); // Emesso il valore sinistro
                    })
                    // Accumula i valori emessi fino ad ora
                .reduce(0, (accumulator, newValue) -> accumulator + newValue)
                .subscribe(emitter::onNext, emitter::onError);
            }
            
            emitter.onComplete(); // Emette un segnale di completamento
        });
    }
    
    
        public Observable<Pair<Webpage, List<URL>>> createWebpageObservable(URL indirizzo) {
            return Observable.create(emitter -> {
                Webpage page = e.extract(indirizzo);
                List<URL> nextLinks = absLinksExtractor.extract(page);
                nextLinks.addAll(relLinksExtractor.extract(page));
                emitter.onNext(Pair.of(page, nextLinks));
                emitter.onComplete();
            });
        }
    
        public Observable<Pair<Integer, List<URL>>> doJob(URL indirizzo, int profondita) {
            return createWebpageObservable(indirizzo)
            .flatMap(pair -> {
                Webpage page = pair.getLeft();
                List<URL> nextLinks = pair.getRight();
                int occ = occurrencesExtractor.extract(page.getBody().toLowerCase());
                sendUpdates.apply(Pair.of(profondita, occ));
                return Single.just(Pair.of(occ, nextLinks)).toObservable();
            });
        }
    }
