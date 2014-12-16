package dk.lessismore.nojpa.net.dns;

import dk.lessismore.nojpa.pool.threadpool.RuntimeCallbackCalcInterface;
import dk.lessismore.nojpa.pool.threadpool.RuntimeCallbackThreadPool;
import dk.lessismore.nojpa.utils.MaxSizeWeakMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class DNSServerLookup {


//    private final static Logger log = LoggerFactory.getInstance(DNSServerLookup.class);
    protected final static Log log = LogFactory.getLog(DNSServerLookup.class);



    private static final DNSServerLookup ME = new DNSServerLookup();
    private RuntimeCallbackThreadPool mxPool;
    private RuntimeCallbackThreadPool revIPPool;
    private final static MaxSizeWeakMap<String, ArrayList<String>> mxRecords = new MaxSizeWeakMap<String, ArrayList<String>>(200);

    private DNSServerLookup(){
        try{
            mxPool = new RuntimeCallbackThreadPool(3, MXCalculator.class);
            revIPPool = new RuntimeCallbackThreadPool(2, RevIPCalculator.class);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    public ArrayList<String> lookupMXForEmail(String email) throws Exception {
        return lookupMXForDomain(email.substring(email.indexOf('@') + 1));
    }


    public ArrayList<String> lookupMXForDomain(String dom) throws Exception {
        ArrayList<String> mxs = (ArrayList<String>) mxPool.doCalc(dom);
        return mxs;
    }

    public String lookupRevIP(String ip) throws Exception {
        try{
            ArrayList<String> mxs = (ArrayList<String>) revIPPool.doCalc(ip);
            return mxs != null && mxs.size() > 0 ? mxs.get(0) : null;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static DNSServerLookup getInstance() {
        return ME;
    }



    public static class MXCalculator implements RuntimeCallbackCalcInterface {

        private DirContext ictx = null;

        public MXCalculator(){
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            try {
                ictx = new InitialDirContext(env);
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }


        public Object doCalc(Object input) throws Exception {
            String hostName = (String) input;
            ArrayList<String> res = null;
            synchronized(mxRecords){
                res = mxRecords.get(hostName);
            }
            if (res != null) {
                return res;
            } else {
                res = new ArrayList<String>();
            }

            //DirContext ictx = null;
            try {
                //Hashtable env = new Hashtable();
                //env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                //ictx = new InitialDirContext(env);
                Attributes attrs = ictx.getAttributes(hostName, new String[]{"MX"});
                Attribute attr = attrs.get("MX");

                // if we don't have an MX record, try the machine itself
                if ((attr == null) || (attr.size() == 0)) {
                    attrs = ictx.getAttributes(hostName, new String[]{"A"});
                    attr = attrs.get("A");
                    if (attr == null) {
                        throw new NamingException("No match for name '" + hostName + "'");
                    }
                }

                NamingEnumeration en = attr.getAll();

                ArrayList records = new ArrayList();
                while (en.hasMore()) {
                    records.add((String) en.next());
                }
                Collections.sort(records);


                for (int i = 0; i < records.size(); i++) {
                    String x = (String) records.get(i);
                    String f[] = x.split(" ");
                    if (f != null && f.length > 1 && f[1].endsWith(".")) f[1] = f[1].substring(0, (f[1].length() - 1));
                    if (f != null && f.length > 1 && f[1].indexOf(".") != -1) {
                        res.add(f[1]);
                    } else if (f != null && f.length > 0 && f[0].indexOf(".") != -1) {
                        res.add(f[0]);
                    }
                }
                synchronized(mxRecords){
                    mxRecords.put(hostName, res);
                }
                return res;

            } catch(NamingException e){
                e.printStackTrace();
                throw e;
            } catch(Exception e){
                throw e;
            } finally {
                //if (ictx != null) ictx.close();
            }
        }
    }

    public static class RevIPCalculator implements RuntimeCallbackCalcInterface {

        private DirContext ictx = null;

        public RevIPCalculator(){
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            try {
                ictx = new InitialDirContext(env);
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }


        public Object doCalc(Object input) throws Exception {
            String ip = (String) input;
            String revIP = "";
            StringTokenizer toks = new StringTokenizer(ip, ".");
            while(toks.hasMoreTokens()){
                revIP = toks.nextToken() + "." + revIP;
            }

            String hostName = revIP + "in-addr.arpa";
            ArrayList<String> res = null;
            synchronized(mxRecords){
                res = mxRecords.get(hostName);
            }
            if (res != null) {
                return res;
            } else {
                res = new ArrayList<String>();
            }

            //DirContext ictx = null;
            try {
                //Hashtable env = new Hashtable();
                //env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                //ictx = new InitialDirContext(env);
                Attributes attrs = ictx.getAttributes(hostName, new String[]{"PTR"});
                Attribute attr = attrs.get("PTR");

                // if we don't have an MX record, try the machine itself
                if ((attr == null) || (attr.size() == 0)) {
                    attrs = ictx.getAttributes(hostName, new String[]{"PTR"});
                    attr = attrs.get("A");
                    if (attr == null) {
                        throw new NamingException("No match for name '" + hostName + "'");
                    }
                }

                NamingEnumeration en = attr.getAll();

                ArrayList records = new ArrayList();
                while (en.hasMore()) {
                    records.add((String) en.next());
                }
                Collections.sort(records);


                for (int i = 0; i < records.size(); i++) {
                    String x = (String) records.get(i);
                    String f[] = x.split(" ");
                    if (f != null && f.length > 1 && f[1].endsWith(".")) f[1] = f[1].substring(0, (f[1].length() - 1));
                    if (f != null && f.length > 1 && f[1].indexOf(".") != -1) {
                        res.add(f[1]);
                    } else if (f != null && f.length > 0 && f[0].indexOf(".") != -1) {
                        res.add(f[0]);
                    }
                }
                synchronized(mxRecords){
                    mxRecords.put(hostName, res);
                }
                return res;
            } catch(javax.naming.NameNotFoundException e){
                log.info("Cant find host("+ input +") ... " + e);
                return null;
            } catch(NamingException e){
                throw e;
            } catch(Exception e){
                throw e;
            } finally {
                //if (ictx != null) ictx.close();
            }
        }
    }



    public static void listAllDNS(String hostName) throws Exception {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext(env);

        Attributes attrs = ictx.getAttributes(hostName);

        NamingEnumeration<? extends Attribute> namingEnumeration = attrs.getAll();

        for(int i = 0; namingEnumeration.hasMore() ; i++){
            Attribute attribute = namingEnumeration.next();
            NamingEnumeration en = attribute.getAll();

            ArrayList records = new ArrayList();
            while (en.hasMore()) {
                System.out.println(hostName + "->" + en.next());
            }


        }

    }


    public static void main(String[] args) throws Exception {
//        System.out.println("----------------------------------------");
//        listAllDNS("hotmail.com");
//        System.out.println("----------------------------------------");
//        listAllDNS("webpartner.dk");
//        System.out.println("----------------------------------------");
//        listAllDNS("less-is-more.dk");
//        System.out.println("----------------------------------------");
//        listAllDNS("webpartner.dk");
//        System.out.println("----------------------------------------");
//        listAllDNS("smtp.webpartner.dk");
//        System.out.println("----------------------------------------");
//        listAllDNS("yahoo.com");
//        System.out.println("----------------------------------------");
//        listAllDNS("dating.dk");
//        System.out.println("----------------------------------------");
//        listAllDNS("spf-b.hotmail.com");
//        System.out.println("----------------------------------------");
//        listAllDNS("topchancen.dk");
//        System.out.println("----------------------------------------");


        Calendar now = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0; i < 400; i++){
            now.add(Calendar.DAY_OF_YEAR, -1);
            System.out.println("" + format.format(now.getTime()));
        }




    }




}
