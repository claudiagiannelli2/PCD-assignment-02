package pcd.ass02.virtualThread;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import pcd.ass02.*;


public class Ass02MyVtCoordinator {
    private final Function<Pair<Integer, Integer>, Void> sendUpdates;
    private final Function<Void, Boolean> shouldRun;

    private class Job {
        private final ExtractionTask<Webpage, List<URL>> absLinksExtractor = new RegexExtractor();
        private final ExtractionTask<Webpage, List<URL>> relLinksExtractor = new RegexRelExtractor();
        private final ExtractionTask<String, Integer> occurrencesExtractor;
        private final WebPageExtractor e = new WebPageExtractor();
        private int result = 0;
        private BlockingQueue<Pair<URL, Integer>> linksQueue = new LinkedBlockingQueue<>();
        private final List<Thread> jobsList = new ArrayList<>();
        private boolean done = false;


        public Job(URL indirizzo, String parola, int profondita) {
            this.linksQueue.add(Pair.of(indirizzo, profondita));
            this.occurrencesExtractor = new WordOccurrencesExtractor(parola.toLowerCase());
        }

        public int run() throws InterruptedException {
            while (shouldRun.apply(null) && !this.done) {
                Pair<URL, Integer> item = linksQueue.take();
                if(item.getLeft() == null) {
                    continue;
                }
                Thread virtualThread = Thread.ofVirtual().start(() -> {
                    int profondita = item.getRight();
                    var r = doJob(item.getLeft(), profondita);
                    result += r.getLeft();
                    if (profondita > 0) {
                        linksQueue.addAll(r.getRight().stream().map(x -> Pair.of(x, profondita - 1)).toList());
                    } else if(this.linksQueue.isEmpty()) {
                        this.done = true;
                        this.linksQueue.add(Pair.of(null, null));
                    }
                });
                jobsList.add(virtualThread);
            }
            for (Thread t : jobsList) {
                t.join();
            }
            return result;
        }

        private Pair<Integer, List<URL>> doJob(URL indirizzo, int profondita) {
            Webpage page = e.extract(indirizzo);
            List<URL> nextLinks = absLinksExtractor.extract(page);
            nextLinks.addAll(relLinksExtractor.extract(page));
            int occ = occurrencesExtractor.extract(page.getBody().toLowerCase());
            sendUpdates.apply(Pair.of(profondita, occ));
            return Pair.of(occ, nextLinks);
        }

    }

    public Ass02MyVtCoordinator(
            Function<Pair<Integer, Integer>, Void> sendUpdates, Function<Void, Boolean> shouldRun) {
        this.sendUpdates = sendUpdates;
        this.shouldRun = shouldRun;
    }

    public int getWordOccurrences(URL indirizzo, String parola, int profondita) throws InterruptedException {
        Job j = new Job(indirizzo, parola, profondita);
        return j.run();
    }

}
