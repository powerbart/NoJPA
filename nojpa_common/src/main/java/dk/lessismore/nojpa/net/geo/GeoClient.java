package dk.lessismore.nojpa.net.geo;

import dk.lessismore.nojpa.utils.MaxSizeWeakMap;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class GeoClient {

    private static final Logger log = LoggerFactory.getLogger(GeoClient.class);


    public static class Geo {
        public final String city;
        public final String country;
        public final String continent;

        public Geo(String city, String country, String continent) {
            this.city = city;
            this.country = country;
            this.continent = continent;
        }

        @Override
        public String toString() {
            return "Geo(city(" + city + "),country(" + country + "),continent(" + continent + "))";
        }
    }


    private final static MaxSizeWeakMap<String, Geo> lookupCache = new MaxSizeWeakMap<String, Geo>(1000);

    public static Geo lookup(String ip) throws Exception {
        synchronized (lookupCache) {
            Geo geo = lookupCache.get(ip);
            if (geo != null) {
                return geo;
            }
        }

        String s = null;
        try {

            s = Request.Get("http://geo.less-is-more.dk/lookup?ip=" + ip)
                    .execute()
                    .returnContent()
                    .asString();
        } catch (Exception e) {
            try {
                s = Request.Get("http://geo.less-is-more.dk/lookup?ip=" + ip)
                        .execute()
                        .returnContent()
                        .asString();
            } catch (Exception ee) {
                log.error("Some error: " + ee, ee);
                throw ee;
            }
        }
        JSONObject json = new JSONObject(s);
        Geo geo = new Geo(json.getString("city"), json.getString("country"), json.getString("continent"));
        synchronized (lookupCache) {
            lookupCache.put(ip, geo);
        }
        return geo;
    }


}
