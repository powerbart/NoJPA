package dk.lessismore.nojpa.net.geo;

import dk.lessismore.nojpa.net.httpclient.HttpClient;
import dk.lessismore.nojpa.utils.MaxSizeWeakMap;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class GeoClient {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GeoClient.class);


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



    private final static MaxSizeWeakMap<String, Geo> lookupCache = new MaxSizeWeakMap<String, Geo>(1000);
    public static Geo lookup(String ip) throws Exception {
        synchronized (lookupCache){
            Geo geo = lookupCache.get(ip);
            if(geo != null){
                return geo;
            }
        }

        String s = null;
        try {
            s = HttpClient.get("http://geo.less-is-more.dk/lookup?ip=" + ip);
        } catch (Exception e) {
        	try {
            	s = HttpClient.get("http://geo.less-is-more.dk/lookup?ip=" + ip);
        	} catch (Exception e) {
	            log.error("Some error: " + e, e);
	            throw e;
        	}
        }
        JSONObject json = new JSONObject(s);
        Geo geo = new Geo(json.getString("city"), json.getString("country"), json.getString("continent"));
        synchronized (lookupCache){
            lookupCache.put(ip, geo);
        }
        return geo;
    }



}
