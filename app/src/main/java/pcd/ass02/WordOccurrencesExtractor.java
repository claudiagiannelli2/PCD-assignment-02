package pcd.ass02;

public final class WordOccurrencesExtractor extends ExtractionTask<String, Integer> {

    private final String word;
    
    public WordOccurrencesExtractor(String word) {
        this.word = word;
    }

    public Integer extract(String content) {
        // Simple occurrence counting logic
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}