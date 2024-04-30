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

    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final Function<Void, Boolean> shouldRun;
    private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
    private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
    private final WebPageExtractor e = new WebPageExtractor();
    private boolean done = false;

    public Ass02rx(Function<Pair<Integer, Integer>, Void> sendUpdates, Function<Void, Boolean> shouldRun) {
        this.sendUpdates = sendUpdates;
        this.shouldRun = shouldRun;
    }


    public Observable<Integer> getWordOccurrences(URL address, String word, int depth) {
        final ExtractionTask<String, Integer> occurrencesExtractor = new WordOccurrencesExtractor(word.toLowerCase());
        final BlockingQueue<Pair<URL, Integer>> linksQueue = new LinkedBlockingQueue<>();

        return Observable.create(emitter -> {
            linksQueue.add(Pair.of(address, depth));
            while (shouldRun.apply(null) && !this.done) {
                Pair<URL, Integer> pair = linksQueue.take();
                if (pair.getLeft() == null) {
                    continue;
                }
                doJob(pair.getLeft(), pair.getRight(), occurrencesExtractor)
                        .flatMap(resultPair -> {
                            int leftValue = resultPair.getLeft();
                            List<URL> rightValue = resultPair.getRight();
                            if (pair.getRight() > 0) {
                                linksQueue.addAll(rightValue.stream().map(x -> Pair.of(x, depth - 1)).toList());
                            } else if (linksQueue.isEmpty()) {
                                this.done = true;
                                linksQueue.add(Pair.of(null, null));
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


    private Observable<Pair<Webpage, List<URL>>> createWebpageObservable(URL indirizzo) {
        return Observable.create(emitter -> {
            Webpage page = e.extract(indirizzo);
            List<URL> nextLinks = absLinksExtractor.extract(page);
            nextLinks.addAll(relLinksExtractor.extract(page));
            emitter.onNext(Pair.of(page, nextLinks));
            emitter.onComplete();
        });
    }

    private Observable<Pair<Integer, List<URL>>> doJob(URL indirizzo, int profondita, ExtractionTask<String, Integer> occurrencesExtractor) {
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
