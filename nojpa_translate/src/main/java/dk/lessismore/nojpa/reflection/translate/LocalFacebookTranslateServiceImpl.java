package dk.lessismore.nojpa.reflection.translate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.lessismore.nojpa.utils.Pair;
import dk.lessismore.nojpa.utils.SuperIO;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class LocalFacebookTranslateServiceImpl implements TranslateService {

    private static final Logger log = LoggerFactory.getLogger(LocalFacebookTranslateServiceImpl.class);


    private static long totalCharactersTranslated = 0L;
    List<String> servers = new ArrayList<>();
    List<Pair<String, Integer>> hosts = new ArrayList<>();
    int current = 0;

    public LocalFacebookTranslateServiceImpl(Pair<String, Integer>... hosts) {

        if (hosts == null || hosts.length == 0) {
            servers.add("http://10.0.0.72:6060");
        } else {
            for(Pair<String, Integer> h : hosts) {
                this.hosts.add(h);
                for(int i = 0; i < h.getSecond(); i++) {
                    servers.add(h.getFirst());
                }
            }
        }
    }


    @Override
    public String translate(String uniqueID, String attributeNameForDebug, String srcLang2Char, String destLang2Char, String originalText) throws Exception {
        String url = servers.get(current++ % servers.size()) + "/translate";
        try {
            return doTranslate(uniqueID, attributeNameForDebug, srcLang2Char, destLang2Char, originalText, url);
        } catch (Exception e) {
            for (int i = 0; i < servers.size(); i++) {
                String newUrl = servers.get(current++ % servers.size()) + "/translate";
                if (!url.equals(newUrl)) {
                    return doTranslate(uniqueID, attributeNameForDebug, srcLang2Char, destLang2Char, originalText, newUrl);
                }
            }
        }
        return null;
    }


    private String doTranslate(String uniqueID, String attributeNameForDebug, String srcLang2Char, String destLang2Char, String originalText, String url) throws Exception {

        if(originalText == null || srcLang2Char == null || destLang2Char == null){
            if(originalText != null) {
                log.warn("We will return null - because we was called with null, in translate[uniqueID("+ uniqueID +")srcLang2Char(" + srcLang2Char + "), destLang2Char(" + destLang2Char + "), originalText(" + originalText + ")]");
            }
            return null;
        }

        Locale src = new Locale(srcLang2Char);
        Locale dst = new Locale(destLang2Char);


        StringBuilder cache = new StringBuilder((int) (originalText.length() * 1.4));

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("src_lang", src.getISO3Language() + "_Latn"));
        qparams.add(new BasicNameValuePair("tgt_lang", dst.getISO3Language() + "_Latn"));
        qparams.add(new BasicNameValuePair("source", originalText));


        InputStream getStream = Request.Post(url)
                .setHeader(new BasicHeader("X-HTTP-Method-Override", "GET"))
                .body(new UrlEncodedFormEntity(qparams, "UTF-8"))
                .execute()
                .returnContent()
                .asStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(getStream));
        String line;
        while ((line = rd.readLine()) != null) {
            cache.append(line);
        }
        log.debug("response from google: " + cache);
        JsonNode jsonNode = new ObjectMapper().reader().readTree(cache.toString());
        String translatedText = jsonNode.get("translation").get(0).textValue();

        SuperIO.writeTextToFile("/tmp/translation-"+ src.getISO3Language() + "-to-" + dst.getISO3Language() + ".txt", originalText + "\n-\n" + translatedText + "\n----------------------------\n", true);

        totalCharactersTranslated = totalCharactersTranslated + originalText.length();

        if (translatedText == null || translatedText.trim().isEmpty()) {
            return null;
        }

        return translatedText;
    }
}
