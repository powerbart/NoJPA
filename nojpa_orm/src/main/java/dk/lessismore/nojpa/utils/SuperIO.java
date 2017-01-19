package dk.lessismore.nojpa.utils;

import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;


/**
 * @author sebastian
 *         <p/>
 *         To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation..
 */
public class SuperIO {

    private final static org.apache.log4j.Logger log = Logger.getLogger(SuperIO.class);

    public static boolean writeTextToFile(String fileName, StringBuffer content) {
        return writeTextToFile(fileName, content.toString(), false);
    }

    public static boolean writeTextToFile(String fileName, String content) {
        return writeTextToFile(fileName, content, false);
    }

    public static boolean writeTextToFile(String fileName, String content, boolean append) {
        return writeTextToFile(new File(fileName), content, append);
    }

    public static boolean writeTextToFile(File file, String s) {
        return writeTextToFile(file, s, false);
    }

    public static boolean writeTextToFile(File file, String content, boolean append) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, append);
            return true;
        } catch (Exception ioe) {
            log.error("Method >> writeTextToFile << caused exception: " + ioe, ioe);
            return false;
        } finally{
            try {
                if (fileWriter != null){
                    fileWriter.write(content);
                    fileWriter.flush();
                    fileWriter.close();
                    fileWriter = null;
                }
            } catch (Exception exp) {}
        }
    }


    public static String readTextFromFile(File fileName) {
        FileReader fileReader = null;
        StringBuilder toReturn = new StringBuilder();
        try {
            fileReader = new FileReader(fileName);
            char[] chars = new char[10 * 1024];
            String curLine = "";
            int countOfRead = 0;
            while (countOfRead != -1) {
                countOfRead = fileReader.read(chars, 0, 10 * 1024);
                if (countOfRead != -1) {
                    curLine = new String(chars, 0, countOfRead);
                    toReturn.append(curLine);
                }
            }
            fileReader.close();
            fileReader = null;
        } catch (Exception ioe) {
            try {
                if (fileReader != null) fileReader.close();
                fileReader = null;
            } catch (Exception exp) {}
            log.error("Method >> readTextFromFile<< caused exception: " + ioe, ioe);
        }
        return toReturn.toString();
    }

    public static String readTextFromFile(String fileName) {
        return readTextFromFile(new File(fileName));
    }

    public static List<String> textFileToStringList(String fileName) {
        return textFileToStringList(new File(fileName));
    }


    public static String readFirstLine(String fileName) {
        return readFirstLine(new File(fileName));
    }

    public static String readFirstLine(File fileName) {
        FileReader fileReader = null;
        BufferedReader br = null;
        try {
            fileReader = new FileReader(fileName);
            br = new BufferedReader(fileReader);
            if (br.ready()) {
                String newLine = br.readLine();
                if (newLine != null) {
                    return newLine;
                }
            }
            return null;
        } catch (Exception ioe) {
            log.error("Method >> readTextFromFile<< caused exception: " + ioe, ioe);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                fileReader = null;
            } catch (Exception exp) {}
            try {
                if (br != null) br.close();
                br = null;
            } catch (Exception exp) {}

        }
        return null;
    }

    // Loads all lines in a file into a String List
    public static List<String> textFileToStringList(File fileName) {
        FileReader fileReader = null;
        BufferedReader br = null;
        List<String> dataLinesList = new ArrayList<String>();
        try {
            fileReader = new FileReader(fileName);
            br = new BufferedReader(fileReader);
            while (br.ready()) {
                String newLine = br.readLine();
                if (newLine != null) {
                    dataLinesList.add(newLine);
                }
            }
        } catch (Exception ioe) {
            log.error("Method >> readTextFromFile<< caused exception: " + ioe, ioe);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                fileReader = null;
            } catch (Exception exp) {}
            try {
                if (br != null) br.close();
                br = null;
            } catch (Exception exp) {}

        }
        return dataLinesList;
    }

    public static int countLines(File fileName) {
        FileInputStream fr = null;
        int lines = 0;
        try {
            fr = new FileInputStream(fileName);
            int readLength = 1;
            byte[] bytes = new byte[2 * 1024];
            while(readLength > 0){
                readLength = fr.read(bytes, 0, bytes.length);
                for(int j = 0; j < readLength; j++){
                    if(bytes[j] == '\n'){
                        lines++;
                    }
                }
            }
        } catch (Exception ioe) {
            log.error("Method >> countLines<< caused exception: " + ioe, ioe);
        } finally {
            try {
                if (fr != null) fr.close();
                fr = null;
            } catch (Exception exp) {}
        }
        return lines;
    }

    public static String readStreamWithClose(InputStream in){
        String s = null;
        try{
            s = readStreamWithClose(in);
        } catch(Exception e){
        } finally{
            try{
                if(in != null) in.close();
            } catch(Exception e){ }
            return s;
        }
    }

    public static String readStreamWithOutClose(InputStream in){
        StringBuilder b = new StringBuilder();
        try{
            byte[] bytes = new byte[2048];
            int rLen = 0;
            while(rLen != -1){
                rLen = in.read(bytes, 0, 2028);
                if(rLen != -1){
                    b.append(new String(bytes, 0, rLen));
                }
            }
        } catch(Exception e){
            log.error("Some error in readStream: " + e);
        } finally{
            return b.toString();
        }
    }


    // Loads all lines in a file into a String List
    public static boolean stringList2TextFile(List list, String fileName) {
        return stringList2TextFile(list, new File(fileName));
    }

    public static boolean stringList2TextFile(List list, File file) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            for (int i = 0; i < list.size(); i++) {
                fileWriter.write(list.get(i).toString() + "\n");
            }
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
        } catch (Exception ioe) {
            try {
                if (fileWriter != null) fileWriter.close();
                fileWriter = null;
            } catch (Exception exp) {}
            log.error("Method >> stringList2TextFile<< caused exception: " + ioe, ioe);
            return false;
        }
        return true;
    }


    public static String readTopOfFile(String fileName, int maxCharsToRead) {
        FileReader fileReader = null;
        String toReturn = "";
        try {
            fileReader = new FileReader(fileName);
            char[] chars = new char[maxCharsToRead];
            String curLine = "";
            int countOfRead = fileReader.read(chars, 0, maxCharsToRead);
            if (countOfRead != -1) {
                toReturn = new String(chars, 0, countOfRead);
            }
            fileReader.close();
            fileReader = null;
        } catch (Exception ioe) {
            try {
                if (fileReader != null) fileReader.close();
                fileReader = null;
            } catch (Exception exp) {}
            log.error("Method >> readTextFromFile<< caused exception: " + ioe, ioe);
        }
        return toReturn;
    }

    public static String readTopOfFile(String fileName, int maxCharsToRead, String encoding) {
        if(fileName == null) return null;
        FileInputStream fileInput = null;
        InputStreamReader inputStream = null;
        BufferedReader in = null;
        try {
            fileInput = new FileInputStream(fileName);
            inputStream = new InputStreamReader(fileInput, encoding);
            in = new BufferedReader(inputStream);
            StringBuilder b = new StringBuilder();
            String s1 = "";
            int totalRead = 0;
            while (s1 != null && totalRead < maxCharsToRead) {
                s1 = in.readLine();
                if (s1 != null) {
                    totalRead = totalRead + s1.length();
                    b.append(s1);
                    b.append('\n');
                }
            }
            return b.toString();
        } catch (IOException e) {
            log.error("Some error in readTopOfFile ... " + e, e);
            throw new RuntimeException(e);
        } finally {
            try{ if(fileInput != null) fileInput.close(); fileInput = null; }
            catch(Exception e){}
            try{ if(inputStream != null) inputStream.close(); inputStream = null; }
            catch(Exception e){}
            try{ if(in != null) in.close(); in = null; }
            catch(Exception e){}
        }
    }

    public static String convertFromUTF8(String input) throws IOException {
        if(input == null) return null;
        FileInputStream fileInput = null;
        InputStreamReader inputStream = null;
        BufferedReader in = null;
        try {
            File f = File.createTempFile("convertFromUTF8", ".txt");
            SuperIO.writeTextToFile(f, input);
            fileInput = new FileInputStream(f);
            inputStream = new InputStreamReader(fileInput, "UTF8");
            in = new BufferedReader(inputStream);
            StringBuilder b = new StringBuilder();
            String s1 = "";
            while (s1 != null) {
                s1 = in.readLine();
                if (s1 != null) {
                    b.append(s1);
                    b.append('\n');
                }
            }
            //removes last \n append'ed
            final int i = b.length();
            if(i > 0){ b.deleteCharAt(i - 1); }
            f.delete();
            return b.toString();
        } catch (IOException e) {
            log.error("Some error in convertFromUTF8 ... " + e, e);
            throw e;
        } finally {
            try{ if(fileInput != null) fileInput.close(); fileInput = null; }
            catch(Exception e){}
            try{ if(inputStream != null) inputStream.close(); inputStream = null; }
            catch(Exception e){}
            try{ if(in != null) in.close(); in = null; }
            catch(Exception e){}
        }
    }

    public static String convertToUTF8(String input) throws IOException {
        FileOutputStream stream = null;
        OutputStreamWriter outputStreamWriter = null;
        Writer out = null;
        try{
            File f = File.createTempFile("convertToUTF8", ".txt");
            stream = new FileOutputStream(f);
            outputStreamWriter = new OutputStreamWriter(stream, "UTF8");
            out = new BufferedWriter(outputStreamWriter);
            out.write(input);
            out.flush();
            out.close();
            String b = readTextFromFile(f);
            f.delete();
            return b;
        } catch(IOException e){
            log.error("Some error in convertFromUTF8 ... " + e, e);
            throw e;
        } finally {
            try{ if(stream != null) stream.close(); stream = null; }
            catch(Exception e){}
            try{ if(outputStreamWriter != null) outputStreamWriter.close(); outputStreamWriter = null; }
            catch(Exception e){}
            try{ if(out != null) out.close(); out = null; }
            catch(Exception e){}
        }
    }


    public static void main(String[] args) throws IOException {
        String infileName = "/Users/seb/Downloads/LarsRonnedal.pdf";
        String outfileName = "/Users/seb/Downloads/LarsRonnedal-2.pdf";
        FileInputStream fr = new FileInputStream(infileName);
        FileOutputStream out = new FileOutputStream(outfileName);
        int readLength = 1;
        byte[] bytes = new byte[64];
        int counter = 0;
        while(readLength != -1){
            readLength = fr.read(bytes, 0, bytes.length);
            if(readLength != -1) {
                String s = new String(bytes, 0, readLength);
                if(s.contains("(Lars R\\370nnedal)")){
                    System.out.println(counter++ + ":" + s);
                    s.replace("Lars", "Sebastian");
                } else {
                    //System.out.println(counter++ + ":NONE");
                }
                //out.write(s.getBytes());
                out.write(bytes, 0, readLength);
            }
        }
        out.flush();
        out.close();
        fr.close();



    }

}
