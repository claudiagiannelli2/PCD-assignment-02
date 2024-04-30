package pcd.ass02;

import java.net.*;
import java.io.*;
import com.gitlab.pdftk_java.com.lowagie.text.pdf.PdfReader;

public final class WebPageExtractor extends ExtractionTask<URL, Webpage> {
    public Webpage extract(URL u) {
        try {
            String contentType = u.openConnection().getHeaderField("Content-Type");
            if(contentType.equals("application/pdf")) {
                PdfReader pdf = new PdfReader(u.openStream());

                return new Webpage("", u);
            } else {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(u.openStream()));

                String body = "";
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    body = body + inputLine + "\n";
                }
                in.close();
                return new Webpage(body, u);
            }
        } catch (Exception e) {
            return new Webpage("", u);
        }
    }
}