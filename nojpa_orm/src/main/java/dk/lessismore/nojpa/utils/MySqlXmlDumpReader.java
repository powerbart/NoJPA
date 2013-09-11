package dk.lessismore.nojpa.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class MySqlXmlDumpReader {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySqlXmlDumpReader.class);


    private final FileReader fileReader;
    private StringBuilder readContent = new StringBuilder();
    char[] chars = new char[10 * 1024];
    int curLine = 0;
    int countOfRead = 0;
    long sizeInTotal;
    long sizeOfReadTotal = 0;

    public MySqlXmlDumpReader(File xmlFile) throws FileNotFoundException {
        fileReader = new FileReader(xmlFile);
        sizeInTotal = xmlFile.length();
    }


    public synchronized Row readRow() throws IOException {

        while (countOfRead != -1) {
            if(readContent.indexOf("</row>") == -1){
                countOfRead = fileReader.read(chars, 0, chars.length);
                if (countOfRead != -1) {
                    readContent.append(chars, 0, countOfRead);
                    sizeOfReadTotal = sizeOfReadTotal + countOfRead;
                    log.debug("readRow("+ sizeOfReadTotal +" / "+ sizeInTotal +").... ~" + (((double) sizeOfReadTotal) / ((double) sizeInTotal)));
                }
            } else {
                countOfRead = 0;
            }
            if (countOfRead != -1) {
                int endRow = readContent.indexOf("</row>");
                if(endRow != -1){
                    int startRow = readContent.indexOf("<row>");
                    String toReturn = readContent.substring(startRow, endRow + 6);
                    readContent.delete(0, endRow + 6);
                    return new Row(toReturn);
                }
            }
        }
        return null;
    }


    public static class Row {

        private final String row;
        public Row(String row){
            this.row = row;
        }

        public String getField(String name){

            String fieldStart = "<field name=\""+ name +"\">";
            String fieldEnd = "</field>";
            int start = row.indexOf(fieldStart);
            if(start != -1){
                String substring = row.substring(start + fieldStart.length(), row.indexOf(fieldEnd, start));
//                log.debug("getField("+ name +") -> " + substring);
                return substring;
            } else {
//                log.error("We cant find("+ name +") " + row);
            }
            return null;
        }

        @Override
        public String toString() {
            return row;
        }
    }





//    public static void main(String[] args) throws Exception {
//        MySqlXmlDumpReader r = new MySqlXmlDumpReader(new File("/Users/seb/tmp/luuux/users.xml"));
//        Row row = new Row("");
//        for(int i = 0; row != null && i < 50; i++){
//            row = r.readRow();
//            if(row != null){  // && i % 100 == 0
//                System.out.println("----------------------- START ROW --------------------------");
//                System.out.println("Mail:" + row.getField("mail"));
//                System.out.println("filepath:" + row.getField("filepath"));
//                System.out.println("------------------------ END ROW --------------------------");
//            }
//        }
//    }


    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1366315537L * 1000L);
        System.out.println(c.getTime());

    }




}
