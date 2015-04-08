package dk.lessismore.nojpa.net.geo;

import dk.lessismore.nojpa.net.httpclient.HttpClient;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class GeoClient {


    public static class Geo {
        public final String city;
        public final String country;
        public final String continent;

        public Geo(String city, String country, String continent){
            this.city = city;
            this.country = country;
            this.continent = continent;
        }

        @Override
        public String toString() {
            return "Geo(city("+ city +"),country("+ country +"),continent("+ continent +"))";
        }
    }



    public static Geo lookup(String ip) throws Exception {
//        String s = HttpClient.get("http://geo.less-is-more.dk/lookup?ip=" + ip);
        String s = HttpClient.get("http://localhost:8080/lookup?ip=" + ip);
        JSONObject json = new JSONObject(s);
        return new Geo(json.getString("city"), json.getString("country"), json.getString("continent"));
    }



}
