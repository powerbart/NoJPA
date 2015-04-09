package dk.lessismore.nojpa.net.geoip;

import static org.junit.Assert.assertEquals;

import dk.lessismore.nojpa.net.geo.GeoClient;
import dk.lessismore.nojpa.net.httpclient.HttpClient;
import org.junit.Test;

public class GeoIpServiceTest {


    @Test
    public void firstTest() throws Exception {
            System.out.println(GeoClient.lookup("86.58.206.93"));
    }

    @Test
    public void first0Test() throws Exception {
        System.out.println(GeoClient.lookup("78.56.108.121"));
    }

    @Test
    public void first2Test() throws Exception {
        String s = HttpClient.get("http://www.less-is-more.dk/1.txt");
        System.out.println("result("+ s +")");
    }

    @Test
    public void first3Test() throws Exception {
        String s = HttpClient.get("http://geo.less-is-more.dk/");
        System.out.println("result("+ s +")");
    }


    static long numberOfLookup = 0;
    static long start = 0;
    @Test
    public void lookupTest() throws Exception {


        start = System.currentTimeMillis();
        for(int t = 0; t < 50; t++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 100; i++) {
                        try {
                            testLookup("64.233.160.0", "US");
                            testLookup("www.helsinki.fi", "FI"); //returns EU
                            testLookup("www.wine.org", "FR");
                            testLookup("www.bhu.ac.in", "IN");
                            testLookup("www.tsinghua.edu.cn", "CN");
                            testLookup("www.keio.ac.jp", "JP");
                            testLookup("www.huji.ac.il", "IL");
                            testLookup("www.uu.se", "SE"); //returns EU
                            testLookup("www.uio.no", "NO"); //returns EU
                            testLookup("www.uni-heidelberg.de", "DE"); //return EU
                            testLookup("www.deutschland.de", "DE");
                            testLookup("www.uibk.ac.at", "AT"); //return eu
                            testLookup("wien.gv.at", "AT");
                            testLookup("www.sabanciuniv.edu.tr", "TR");
                            testLookup("www.unb.br", "BR");
                            testLookup("www.unam.mx", "MX");
                            testLookup("uwaterloo.ca", "CA");
                            testLookup("www.unibz.it", "IT");
                            testLookup("www.yonsei.ac.kr", "KR");


                            testLookup("lessismore.dk", "DK");
                            testLookup("90.184.14.47", "DK");
                            testLookup("china.org.cn", "CN");
                            testLookup("192.168.0.123", "EU");
                            testLookup("10.0.0.123", "EU");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            thread.start();

        }

        Thread.sleep(1000 * 60 * 10);

    }

    public static void testLookup(String address, String country) throws Exception {
        GeoClient.Geo lookup = GeoClient.lookup(address);
        long totalTime = System.currentTimeMillis() - start;
        System.out.println("["+ numberOfLookup++ +"] in ms("+ totalTime +") avg("+ (totalTime / numberOfLookup) +")Testing: address("+ address +"), country("+ country +") -> " + lookup);

        assertEquals(country, lookup.country);
    }
}