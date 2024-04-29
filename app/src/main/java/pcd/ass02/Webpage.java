package pcd.ass02;

import java.net.URL;

public class Webpage {
    private final String page;
    private final URL base;
    public Webpage(String page, URL base) {
        this.page = page;
        this.base = base;
    }

    public URL getUrl() {
        return this.base;
    }

    public String getBody() {
        return this.page;
    }
}
