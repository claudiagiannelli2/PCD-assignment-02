package pcd.ass02;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

public class LinkExtractor {
    // Una stringa di espressione regolare che corrisponde agli URL nel formato HTTP o HTTPS.
    private static final String regex = "https?:\\/\\/[a-zA-Z\\-.\\/0-9_:%]+";
    // pattern: Un oggetto Pattern compilato basato sulla regex per l'estrazione degli URL.
    private static final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    // Una stringa di espressione regolare che corrisponde agli elementi <a> (link) all'interno del markup HTML.
    private static final String regexLinks = "<a(\s?[a-zA-Z-](=\"?\\w*\"?)?)* href=\"?([-\\w/\\.]+)\"?";
    // Un oggetto Pattern compilato basato sulla regex per l'estrazione dei link.
    private static final Pattern patternLinks = Pattern.compile(regexLinks, Pattern.MULTILINE);
    // Accetta due parametri: la stringa page, che rappresenta il contenuto della pagina web, e l'URL base, che rappresenta l'URL di base relativo al quale vengono estratti i link relativi.
    public static List<URL> extract(String page, URL base) {
        final Matcher matcher = pattern.matcher(page);
        List<URL> result = new ArrayList<>();

        while (matcher.find()) {
            // si ottiene il testo corrispondente all'intero pattern di ricerca, cioè l'URL trovato all'interno della pagina web che corrisponde alla regex definita.
            String match = matcher.group(0);
            try {
                // trasformazione in url
                URL url = new URI(match).toURL();
                // aggiunto lista degli url
                result.add(url);
            } catch (Exception e) {
                System.err.println("Skipping " + match + " because of " + e.toString());
            }
        }
        // Viene creato un oggetto Matcher utilizzando il pattern definito in patternLinks per cercare i link all'interno della stringa page, che rappresenta il contenuto della pagina web.
        final Matcher linkMatcher = patternLinks.matcher(page);
        //Viene estratto il percorso di base dall'URL fornito come parametro base che è l'indirizzo
        String basePath = base.getPath();
        // Se il percorso di base non termina con "/", viene aggiunto il carattere "/" alla fine del percorso.
        if(!basePath.endsWith("/")) {
            basePath += "/";
        }

        while (linkMatcher.find()) {
            String match = linkMatcher.group(linkMatcher.groupCount());
            // Se il link inizia con "http", significa che è già un URL assoluto. In questo caso, si salta l'iterazione del ciclo while utilizzando continue per evitare di aggiungere due volte lo stesso URL.
            if(match.startsWith("http")) {
                continue;
            }
            try {
                // Si costruisce il percorso completo del link. Se il link inizia con "/", significa che è un link relativo alla radice del sito, quindi viene utilizzato direttamente. Altrimenti, viene aggiunto al percorso di base ottenuto sopra.
                // /a/b/c  + d -> /a/b/d
                String path = match.startsWith("/") ? match : basePath + match;
                URL url = new URI(base.getProtocol(), base.getHost(), path, "").toURL();
                result.add(url);
            } catch (Exception e) {
                System.err.println("Skipping " + match + " because of " + e.toString());
            }
        }

        return result;
    }
}
