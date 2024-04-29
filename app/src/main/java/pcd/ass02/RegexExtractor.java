package pcd.ass02;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

public class RegexExtractor extends ExtractionTask<Webpage, List<URL>> {
    // Una stringa di espressione regolare che corrisponde agli URL nel formato HTTP o HTTPS.
    private final String regex;
    // pattern: Un oggetto Pattern compilato basato sulla regex per l'estrazione degli URL.
    private final Pattern pattern;
    // Una stringa di espressione regolare che corrisponde agli elementi <a> (link) all'interno del markup HTML.
    
    public RegexExtractor() {
        this("https?:\\/\\/[a-zA-Z\\-.\\/0-9_:%]+");
    }

    protected RegexExtractor(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(this.regex, Pattern.MULTILINE);
    }

    protected URL buildUrl(String url, URL base) throws java.net.URISyntaxException, java.net.MalformedURLException {
        return new URI(url).toURL();
    }

    protected String getMatch(Matcher matcher) {
        return matcher.group(0);
    }

    // Accetta due parametri: la stringa page, che rappresenta il contenuto della pagina web, e l'URL base, che rappresenta l'URL di base relativo al quale vengono estratti i link relativi.
    public List<URL> extract(Webpage from) {
        //System.err.println(from.getBody());
        final Matcher matcher = pattern.matcher(from.getBody());
        List<URL> result = new ArrayList<>();

        while (matcher.find()) {
            // si ottiene il testo corrispondente all'intero pattern di ricerca, cioè l'URL trovato all'interno della pagina web che corrisponde alla regex definita.
            String match = this.getMatch(matcher);
            try {
                // trasformazione in url
                URL url = buildUrl(match, from.getUrl());
                // aggiunto lista degli url
                result.add(url);
            } catch (Exception e) {
                System.err.println("Skipping " + match + " because of " + e.toString());
            }
        }
        return result;
    }
}
