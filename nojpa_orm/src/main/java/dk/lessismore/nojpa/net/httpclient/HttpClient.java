package dk.lessismore.nojpa.net.httpclient;

import dk.lessismore.nojpa.net.dns.DNSServerLookup;
import dk.lessismore.nojpa.utils.TimerWithPrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class HttpClient {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HttpClient.class);

    private static boolean logAll = false;

    public static String get(String url) throws Exception {
        String host = getHostname(url);
        boolean isSSL = url.startsWith("https");
        int port = isSSL ? 443 : 80;

        int colonIndex = url.indexOf(":", 10);
        int slashIndex = url.indexOf("/", 10);
        if(colonIndex > 10 && slashIndex > colonIndex){
            port = new Integer(url.substring(colonIndex + 1, slashIndex));
        }


        List<String> ips = DNSServerLookup.lookupIp(host);

        Exception expToReturn = null;
        boolean rev = Math.random() > 0.5;
        for(int p = 0; p < ips.size(); p++){
            String ip = ips.get(rev ? ips.size() - 1 - p : p);
            try{
                // open connection
                if(logAll)  log.debug("Opening socket to " + host);
                Socket socket = new Socket(ip, port);

                // send request
                String request = createRequest(url, host);
                OutputStream os = socket.getOutputStream();
                if(logAll) log.debug("Sending request:\n----------\n" + request + "\n----------\n");
                os.write(request.getBytes());
                os.flush();

                // recieve response
                InputStream is = socket.getInputStream();

                boolean debug = false;

                if(debug) {
                    byte[] bs = new byte[32];
                    int s = 0;
                    while ((s = is.read(bs, 0, bs.length)) > 0) {
                        for(int i = 0; i < s; i++){
                            if(bs[i] == '\r') {
                                System.out.println(" - r - ");
                            } else if(bs[i] == '\n' || bs[i] == '\r'){
                                System.out.println(" - n - ");
                            } else {
                                System.out.print("["+((int) bs[i])+"|"+ ((char) bs[i]) +"] ");
                            }

                        }
                    }
                    return "";
                } else {
                    HttpParser parser = new HttpParser(is);

                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    parser.writeTo(false, true, response);
                    return new String(response.toByteArray());
                }
            } catch (Exception e){
                log.error("Got error when trying for hostname("+ host +")/ip("+ ip +") out of "+ p +"/"+ ips.size() +" ");
                expToReturn = e;
            }
        }
        if(expToReturn != null){
            throw expToReturn;
        }
        throw new Exception("No result from: " + host);
    }


    public static String createRequest(String url, String host) throws MalformedURLException {
        StringBuilder request = new StringBuilder();
        String path = url.substring(url.indexOf("/", 10));
        request.append("GET " + path + " HTTP/1.1");
        request.append("\r\n");
        request.append("User-Agent: curl/7.37.1");
        request.append("\r\n");
        request.append("Host: " + host);
        request.append("\r\n");
        request.append("Accept: */*");
        request.append("\r\n");
        request.append("\r\n");
        return request.toString();
    }

    public static String getHostname(String url) throws MalformedURLException {
        URL realUrl = new URL(url);
        String host = realUrl.getHost();
        return host;
    }







    static protected class HttpParser {
        private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HttpParser.class);

        private final static Pattern HTTP_RESPONSE_PATTERN = Pattern.compile("^HTTP/[\\d\\.]+ \\d+ \\w+$");
        private final static Pattern SESSION_PATTERN =
                Pattern.compile("\\bJSESSIONID=([0-9A-F]+)\\b", Pattern.MULTILINE);
        private final static Pattern STATUS_PATTERN = Pattern.compile("HTTP/1\\.[0-9] ([0-9]+) .*");


        public final static String HTTP_CHARSET = "UTF-8";
        private final static int BUFFER_SIZE = 1024 * 4;
        private final InputStream inputStream;
        private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private HashMap<String, String> headers = new HashMap<String, String>();
        private String firstLine = null;
        private String sessionId = null;
        private Integer contentLength = null;
        private boolean chunked = false;
        private boolean isResponse = false;
        private boolean partlyWritten = false;


        /**
         * Creates a parser and reads in the HTTP header from the InputStream.
         */
        public HttpParser(InputStream inputStream) throws IOException {
//        log.debug("Parsing header ------------------------------ START ");
            this.inputStream = inputStream;

            byte[] firstLineData = readLine();
            firstLine = new String(firstLineData, HTTP_CHARSET);
            bytes.write(firstLineData);
            bytes.write('\r');
            bytes.write('\n');
            int debugTotalHeaderLength = 0;
            byte[] data;
            while((data = readLine()).length > 0) {
                String line = new String(data, HTTP_CHARSET);
                if(logAll) log.debug("Reading line("+ line +")");
                debugTotalHeaderLength += (line != null ? line.length() : -1);
                String[] header = line.split(": ", 2);
                String key = header[0];
                String value = null;
                value = header[1];
                if(logAll) log.debug("headers.put("+ key +", "+ value +");");
                headers.put(key, value);
                bytes.write(new String(key + ": " + value).getBytes(HTTP_CHARSET));
                bytes.write('\r');
                bytes.write('\n');
            }
            if(logAll) log.debug("Reads " +debugTotalHeaderLength);
            bytes.write('\r');
            bytes.write('\n');

            isResponse = HTTP_RESPONSE_PATTERN.matcher(firstLine).find();

            int inLineSessionID = firstLine.indexOf(";jsessionid=");
            if(inLineSessionID != -1){
                int start = inLineSessionID + ";jsessionid=".length();
                sessionId = firstLine.substring(start, start + 32);
            } else {
                String cookie = headers.get("Cookie");
                if(cookie == null) cookie = headers.get("Set-Cookie");
                if(cookie != null) {
                    Matcher matcher = SESSION_PATTERN.matcher(cookie);
                    if(matcher.find()) {
                        sessionId = matcher.group(1);
                    }
                }
            }

            if(headers.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            } else {
                if(headers.containsKey("Transfer-Encoding") && headers.get("Transfer-Encoding").equals("chunked")) {
                    chunked = true;
                }
            }
            if(logAll) log.debug("Parsing header ------------------------------ END ");
        }

        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(headers);
        }

        public String getFirstLine() {
            return firstLine;
        }

        public String getUserAgent() {
            return headers.get("User-Agent");
        }

        public String getReferer() {
            return headers.get("Referer");
        }

        public String getSessionId() {
            return sessionId;
        }

        public boolean getCloseConnection() {
            return !firstLine.contains(" HTTP/1.1") ||  headers.containsKey("Connection") && headers.get("Connection").equals("close");
        }

        public int getStatusCode() {
            if(firstLine == null) return -1;
            Matcher matcher = STATUS_PATTERN.matcher(firstLine);
            if(matcher.matches()) return Integer.parseInt(matcher.group(1));
            else return -1;
        }

        /**
         * Replaces the current session ID with a new one.
         */
        public void replaceSessionId(String newSessionId) {
            if(bytes == null) throw new IllegalStateException("writeTo has already been called.");
            try {
                byte[] newBytes = bytes.toString(HTTP_CHARSET).replace(
                        "JSESSIONID=" + sessionId, "JSESSIONID=" + newSessionId).getBytes(HTTP_CHARSET);
                bytes.reset();
                bytes.write(newBytes);
                sessionId = newSessionId;
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Writes the entire request to one or more streams. This can only be called once.
         */
        public void writeTo(boolean onlyWriteHeader, boolean skipHeader, OutputStream... streams) throws IOException {
            if(!skipHeader) {
                if(bytes == null) throw new IllegalStateException("writeTo has already been called.");
                byte[] headerBytes = bytes.toByteArray();
//              log.debug("WWWWWWWWWWWWWWWWWWWWWWWWWWWW1: wrinting headers:\n" + (new String(headerBytes)) + "\n-------------------" );
                for(OutputStream stream: streams) {
                    stream.write(headerBytes);
                }
                bytes = null;
                partlyWritten = true;
            }
            if(onlyWriteHeader) return;
            if(chunked) {
                while(true) {
                    byte[] line = readLine();
                    String size = new String(line, HTTP_CHARSET);
                    if(size.contains(";")) size = size.substring(0, size.indexOf(";"));
                    int chunkLength = Integer.parseInt(size, 16);
                    if(chunkLength == 0) break;
                    line = readLine(chunkLength);
                    for(OutputStream stream: streams) {
                        writeLine(stream, line);
                    }
                }
            } else if(firstLine == null || !firstLine.equals("HTTP/1.1 100 Continue")){
                // We dont need content-length header if closing connection
                byte[] buffer = new byte[BUFFER_SIZE];
//            log.debug("contentLength: " + contentLength + " ... onlyWriteHeader("+ onlyWriteHeader +") .... this.isResponse("+ this.isResponse +")");
                if (this.isResponse && contentLength == null) {
                    int readSize;
                    while ((readSize = inputStream.read(buffer, 0, buffer.length)) != -1) {
                        for(OutputStream stream: streams) {
                            //log.debug("X1: " + new String(buffer));
                            stream.write(buffer, 0, readSize);
                        }
                    }
                } else if (contentLength != null) {
                    int remainingBytes = contentLength;
                    while(remainingBytes != 0) {
                        int readSize = this.bufferRead(buffer, remainingBytes);
                        if(readSize == -1) throw new IOException("Unexpected end of file.");
                        for(OutputStream stream: streams) {
                            //log.debug("X2: " + new String(buffer));
                            stream.write(buffer, 0, readSize);
                        }
                        remainingBytes -= readSize;
                    }
                }
            }
        }

        private byte[] readLine() throws IOException {
            return readLine(-1);
        }

        private byte[] readLine(int minimumBytesBeforeBreak) throws IOException {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if(minimumBytesBeforeBreak > 0) {
                byte[] buffer = new byte[BUFFER_SIZE];
                while(minimumBytesBeforeBreak > 0) {
                    int size = inputStream.read(buffer, 0, Math.min(minimumBytesBeforeBreak, BUFFER_SIZE));
                    if(size == -1) throw new IOException("Unexpected end of file.");
                    stream.write(buffer, 0, size);
                    minimumBytesBeforeBreak -= size;
                }
            }
            int last = -1;
            int current;
            while((current = inputStream.read()) != -1) {
                if(last == '\r' && current == '\n') {
                    return stream.toByteArray();
                }
                if(last != -1) stream.write(last);
                last = current;
            }
            if(last != -1) stream.write(last);
            return stream.toByteArray();
        }

        private void writeLine(OutputStream stream, byte[] line) throws IOException {
            stream.write(line);
        }

        private int bufferRead(byte[] buffer, int length) throws IOException {
            if(length <= 0) return -1;
            else if(length >= BUFFER_SIZE) return inputStream.read(buffer, 0, BUFFER_SIZE);
            else return inputStream.read(buffer, 0, length);
        }


    }


}
