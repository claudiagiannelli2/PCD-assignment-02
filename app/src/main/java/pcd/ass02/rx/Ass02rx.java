package pcd.ass02.rx;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pcd.ass02.*;

import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Ass02rx {

    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
    private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
    private final WebPageExtractor e = new WebPageExtractor();
    private final PublishSubject<Boolean> stopEvents;
    private boolean done = false;

    public Ass02rx(Function<Pair<Integer, Integer>, Void> sendUpdates, PublishSubject<Boolean> stopEvents) {
        this.sendUpdates = sendUpdates;
        this.stopEvents = stopEvents;
    }


    private static void extractUrls(ExtractionTask<Webpage, List<URL>> linkExtractor, Pair<Webpage, Integer> x, PublishSubject<Pair<URL, Integer>> urls) {
        int newDepth = x.getRight() - 1;
        System.err.println(newDepth);
        if (newDepth < 0) {
            return;
        }
        for (URL extracted : linkExtractor.extract(x.getLeft())) {
            urls.onNext(Pair.of(extracted, newDepth));
        }
    }

    /*private static boolean hasDone(List<PublishSubject<Object>> ps) {
        return ps.stream().reduce(true, (x, acc) -> acc);
    }*/

    public Observable<Integer> getWordOccurrences(URL address, String word, int depth) {
        PublishSubject<Pair<URL, Integer>> urls = PublishSubject.create();
        PublishSubject<Pair<Webpage, Integer>> bodies = PublishSubject.create();
        PublishSubject<Pair<Integer, Integer>> stats = PublishSubject.create();
        AtomicInteger total = new AtomicInteger();

        ExtractionTask<String, Integer> getOccurrences = new WordOccurrencesExtractor(word.toLowerCase());

        urls.observeOn(Schedulers.io()).subscribe((x) -> {
            bodies.onNext(Pair.of(e.extract(x.getLeft()), x.getRight()));
        });

        bodies.observeOn(Schedulers.computation()).subscribe((x) -> extractUrls(absLinksExtractor, x, urls));

        bodies.observeOn(Schedulers.computation()).subscribe((x) -> extractUrls(relLinksExtractor, x, urls)
        );

        bodies.observeOn(Schedulers.computation()).subscribe((x) -> {
            int occ = getOccurrences.extract(x.getLeft().getBody());
            total.addAndGet(occ);
            stats.onNext(Pair.of(x.getRight(), occ));
        });

        stats.observeOn(Schedulers.computation()).subscribe(this.sendUpdates::apply);

        //urls.isEmpty().blockingGet();

        stopEvents.observeOn(Schedulers.computation()).subscribe((x) -> {
            System.out.println("Stopping!");
            urls.onComplete();
            bodies.onComplete();
            stats.onComplete();
        });

        urls.onNext(Pair.of(address, depth));

        Observable.zip(
                urls.isEmpty(),
                bodies.isEmpty(),
                (isEmpty1, isEmpty2) -> {return (Boolean)isEmpty1 && (Boolean)isEmpty2;}
        );
        return Observable.just(1);
    }
}
