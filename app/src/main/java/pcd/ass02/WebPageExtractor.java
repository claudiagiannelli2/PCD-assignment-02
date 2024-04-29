package pcd.ass02;

import java.net.*;
import java.io.*;

public final class WebPageExtractor extends ExtractionTask<URL, Webpage> {
    public Webpage extract(URL u) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(u.openStream()));

            String body = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                body = body + inputLine + "\n";
            }
            in.close();
            return new Webpage(body, u);
        } catch (Exception e) {
            return new Webpage("", u);
        }
    }
}