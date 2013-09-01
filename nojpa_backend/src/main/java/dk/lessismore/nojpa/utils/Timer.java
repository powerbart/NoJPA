package dk.lessismore.nojpa.utils;

/**
 * User: seb
 */
public class Timer {

    long start = System.currentTimeMillis();
    long end = System.currentTimeMillis();

    String prefix = null;
    String fileName = null;

    public Timer(String prefix, String fileName){
        this.prefix = prefix;
        this.fileName = fileName;
        start();
        //System.out.println("file = " + (new File(fileName).getAbsolutePath()));
    }

    public void tmpTime(String comment){
        String toPrint = prefix + ":" + comment + " " + (start - System.currentTimeMillis()) + "\n";
        System.out.print(toPrint);
        SuperIO.writeTextToFile(fileName, toPrint, true);
    }

    public void getTime(){
        String toPrint = prefix + " " + (end- start) + "\n";
        System.out.print(toPrint);
        SuperIO.writeTextToFile(fileName, toPrint, true);
    }


    public void stop(){
        end = System.currentTimeMillis();
        getTime();
    }

    public void start(){
        start = System.currentTimeMillis();
    }


}
