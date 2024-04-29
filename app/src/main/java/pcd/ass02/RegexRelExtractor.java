package pcd.ass02;

import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;

public final class RegexRelExtractor extends RegexExtractor {
    public RegexRelExtractor() {
        super("<a(\s?[a-zA-Z-](=\"?\\w*\"?)?)* href=\"?([-\\w/\\.]+)\"?");
    }

    protected String getMatch(Matcher matcher) {
        return matcher.group(matcher.groupCount());
    }

    @Override
    protected URL buildUrl(String url, URL base) throws java.net.URISyntaxException, java.net.MalformedURLException {
        String basePath = base.getPath();
        // Se il percorso di base non termina con "/", viene aggiunto il carattere "/"
        // alla fine del percorso.
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        if (url.startsWith("http")) {
            throw new IllegalArgumentException("Invalid URL");
        }

        String path = url.startsWith("/") ? url : basePath + url;
        
        System.out.println(new URI(base.getProtocol(), base.getHost(), path, "").toURL());
        return new URI(base.getProtocol(), base.getHost(), path, "").toURL();
    }
}