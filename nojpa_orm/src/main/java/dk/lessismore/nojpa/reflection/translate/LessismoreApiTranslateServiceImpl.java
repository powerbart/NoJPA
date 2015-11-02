package dk.lessismore.nojpa.reflection.translate;

import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class LessismoreApiTranslateServiceImpl  implements TranslateService {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LessismoreTranslateServiceImpl.class);



    private String googleAPIkey = null;
    private String lessismoreAPIkey = null;
    private String gglTransUrl = null;


    public LessismoreApiTranslateServiceImpl(String lessismoreAPIkey, String googleAPIkey){
        this.lessismoreAPIkey = lessismoreAPIkey;
        this.googleAPIkey = googleAPIkey;
    }

    public LessismoreApiTranslateServiceImpl(String lessismoreAPIkey, String googleAPIkey, String gglTransUrl){
        this.lessismoreAPIkey = lessismoreAPIkey;
        this.googleAPIkey = googleAPIkey;
        this.gglTransUrl = gglTransUrl;
    }




    @Override
    public String translate(String uniqueID, String attributeNameForDebug, String srcLang2Char, String destLang2Char, String originalText) throws Exception{

        long start = System.currentTimeMillis();
        if(originalText == null || srcLang2Char == null || destLang2Char == null){
            if(originalText != null) {
                log.warn("We will return null - because we was called with null, in translate[uniqueID("+ uniqueID +")srcLang2Char(" + srcLang2Char + "), destLang2Char(" + destLang2Char + "), originalText(" + originalText + ")]");
            }
            return null;
        }


        StringBuilder cache = new StringBuilder((int) (originalText.length() * 1.4));



        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("key", lessismoreAPIkey));
        qparams.add(new BasicNameValuePair("source", srcLang2Char));
        qparams.add(new BasicNameValuePair("target", destLang2Char));
        qparams.add(new BasicNameValuePair("q", originalText));

        HttpPost httpPost = null;
        if(gglTransUrl == null) {
            httpPost = new HttpPost("http://vsrvj71.less-is-more.dk/translate");
        } else {
            httpPost = new HttpPost(gglTransUrl);
        }
        httpPost.setHeader(new BasicHeader("X-HTTP-Method-Override", "GET"));

        httpPost.setEntity(new UrlEncodedFormEntity(qparams, "UTF-8"));

        HttpClient client = new ContentEncodingHttpClient();
        HttpResponse httpResponse = client.execute(httpPost);
        BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        String line;
        while ((line = rd.readLine()) != null) {
            cache.append(line);
        }
        JSONParser parser = new JSONParser();
        JSONObject responseJSON = (JSONObject) parser.parse(cache.toString());

        long end = System.currentTimeMillis();

        String code = (String) responseJSON.get("code");
        String translatedText = (String) responseJSON.get("translatedText");
        log.debug("Running translate: uniqueID("+ uniqueID +") TIME["+ (end - start) +"] src("+ srcLang2Char +") dest("+ destLang2Char +") originalText("+ originalText +") translated_text(" + translatedText +")");
        return translatedText;
    }


}
