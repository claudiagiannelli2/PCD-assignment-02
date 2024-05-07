package pcd.ass02.rx;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pcd.ass02.*;

import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Ass02rx {

    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
    private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
    private final WebPageExtractor e = new WebPageExtractor();

    public Ass02rx(Function<Pair<Integer, Integer>, Void> sendUpdates) {
        this.sendUpdates = sendUpdates;
    }

    private Maybe<Integer> getWordOccurrences(List<URL> addresses, String word, int depth) {
        if (depth < 0) {
            return Maybe.just(0);
        }
        ExtractionTask<String, Integer> counter = new WordOccurrencesExtractor(word.toLowerCase());
        return Flowable.fromArray(addresses.toArray()).observeOn(Schedulers.io())
                .map(x -> e.extract((URL) x))
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
                .reduce((a, b) -> Pair.of(
                        a.getLeft() + b.getLeft(),
                        (Stream.concat(a.getRight().stream(), b.getRight().stream()).toList()))
                )
                .flatMap(x -> this.getWordOccurrences(x.getRight(), word, depth - 1).map(y -> x.getLeft() + y));
    }

    public Maybe<Integer> getWordOccurrences(URL address, String word, int depth) {
        return this.getWordOccurrences(List.of(address), word, depth);
    }
}
