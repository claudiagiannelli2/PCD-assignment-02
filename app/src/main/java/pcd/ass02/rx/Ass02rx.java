package pcd.ass02.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import pcd.ass02.*;

import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Ass02rx {

    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
    private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
    private final WebPageExtractor e = new WebPageExtractor();

    public Ass02rx(Function<Pair<Integer, Integer>, Void> sendUpdates) {
        this.sendUpdates = sendUpdates;
    }


    private static void extractUrls(ExtractionTask<Webpage, List<URL>> linkExtractor, Pair<Webpage, Integer> x, PublishSubject<Pair<URL, Integer>> urls, AtomicInteger linksAtLastLevel) {
        int newDepth = x.getRight() - 1;
        System.err.println(newDepth);
        if (newDepth < 0) {
            return;
        }
        if (newDepth == 0) {
            linksAtLastLevel.addAndGet(1);
        }
        for (URL extracted : linkExtractor.extract(x.getLeft())) {
            urls.onNext(Pair.of(extracted, newDepth));
        }
    }

    /*private static boolean hasDone(List<PublishSubject<Object>> ps) {
        return ps.stream().reduce(true, (x, acc) -> acc);
    }*/

    private Flowable<Integer> getWordOccurrences(List<URL> addresses, String word, int depth) {
        if(depth < 0) {
            return Flowable.just(0);
        }
        ExtractionTask<String, Integer> counter = new WordOccurrencesExtractor(word.toLowerCase());
        return Flowable.fromArray(addresses.toArray()).observeOn(Schedulers.io())
                .map(x -> e.extract((URL)x))
                .map(x -> {
                    List<URL> links = absLinksExtractor.extract(x);
                    links.addAll(relLinksExtractor.extract(x));
                    return Pair.of(x.getBody(), links);
                })
                .map(x -> Pair.of(counter.extract(x.getLeft().toLowerCase()), x.getRight()))
                .map(x -> {
                    this.sendUpdates.apply(Pair.of(depth, x.getLeft()));
                    return x;
                })
                .flatMap(x -> this.getWordOccurrences(x.getRight(), word, depth - 1)
                        .flatMap(y -> Flowable.just(x.getLeft(), y)))
                .map(x -> {
                    System.out.println(" - " + x);
                    return x;
                });
    }

    public Maybe<Integer> getWordOccurrences(URL address, String word, int depth) {
        return this.getWordOccurrences(List.of(address), word, depth).reduce(Integer::sum);
    }
}
