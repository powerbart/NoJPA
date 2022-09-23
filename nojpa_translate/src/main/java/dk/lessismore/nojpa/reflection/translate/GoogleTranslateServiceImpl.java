package dk.lessismore.nojpa.reflection.translate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class GoogleTranslateServiceImpl implements TranslateService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTranslateServiceImpl.class);


    private static long totalCharactersTranslated = 0L;
    private String googleAPIkey = null;


    public GoogleTranslateServiceImpl(String googleAPIkey){
        this.googleAPIkey = googleAPIkey;
    }


    @Override
    public String translate(String uniqueID, String attributeNameForDebug, String srcLang2Char, String destLang2Char, String originalText) throws Exception {

        if(originalText == null || srcLang2Char == null || destLang2Char == null){
            if(originalText != null) {
                log.warn("We will return null - because we was called with null, in translate[uniqueID("+ uniqueID +")srcLang2Char(" + srcLang2Char + "), destLang2Char(" + destLang2Char + "), originalText(" + originalText + ")]");
            }
            return null;
        }


        StringBuilder cache = new StringBuilder((int) (originalText.length() * 1.4));


        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("key", googleAPIkey));
        qparams.add(new BasicNameValuePair("source", srcLang2Char));
        qparams.add(new BasicNameValuePair("target", destLang2Char));
        qparams.add(new BasicNameValuePair("q", originalText));


        InputStream getStream = Request.Post("https://www.googleapis.com/language/translate/v2")
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
        String translatedText = jsonNode.get("data").get("translations").get(0).get("translatedText").textValue();

        totalCharactersTranslated = totalCharactersTranslated + originalText.length();

        if (translatedText == null || translatedText.trim().isEmpty()) {
            return null;
        }

        return translatedText;
    }
}
