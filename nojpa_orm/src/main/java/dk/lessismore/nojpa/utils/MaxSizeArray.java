package dk.lessismore.nojpa.utils;

import java.util.Iterator;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 06-04-11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
public class MaxSizeArray<E> {


    private Object[] array;
    private int popCounter = 0;
    private int pullCounter = 0;
    private int maxSize = 0;

    public MaxSizeArray(int maxSize){
        this.maxSize = maxSize;
        this.array = new Object[maxSize];
    }


    public void add(E e){
        popCounter = popCounter % maxSize;
        if(array[popCounter] != null){
            pullCounter = ++pullCounter % maxSize;
        }
        array[popCounter++] = e;
    }

    public E pop(){
        if(popCounter == 0){
            popCounter = maxSize;
        }

        if(array[popCounter -1] != null){
            E toReturn = (E) array[--popCounter];
            array[popCounter] = null;
            return toReturn;
        } else {
            return null;
        }
    }

    public E pull(){
        if(array[pullCounter] != null){
            E toReturn = (E) array[pullCounter];
            array[pullCounter] = null;
            pullCounter = ++pullCounter % maxSize;
            return toReturn;
        } else {
            return null;
        }
    }

    public void debug(){
        System.out.print("DEBUG-ARRAY popCounter("+ popCounter +") pullCounter("+ pullCounter +") maxSize("+ maxSize +")[");
        for(int i = 0; i < array.length; i++){
            if(i > 0) {
                System.out.print(", ");
            }
            System.out.print(array[i]);
        }
        System.out.print("]");
        System.out.print("\n");
    }


    public Iterator<E> iterator(){

        Iterator<E> ite = new Iterator<E>() {
            int now = maxSize + popCounter -1;
            int counter = 0;

            public boolean hasNext() {

                for( ; counter++ < maxSize; now--){
                    if(array[now  % maxSize] != null){
                        break;
                    }
                }
                return counter < maxSize && array[now  % maxSize] != null;
            }

            public E next() {
                return (E) array[now-- % maxSize];
            }

            public void remove() {
                throw new RuntimeException("remove not implemented");
            }
        };

        return ite;
    }


    public static void main(String[] args) throws InterruptedException {

        int n = (0 + 10) % 10;
        System.out.println("n = " + n);
        {
            MaxSizeArray<String> maxSizeList = new MaxSizeArray<String>(10);
            for(Iterator<String> iterator = maxSizeList.iterator(); iterator.hasNext(); ){
                System.out.println("iterator.next(1) = " + iterator.next());
            }
            maxSizeList.add("0");
            maxSizeList.add("1");
            maxSizeList.add("2");
            maxSizeList.add("3");
            maxSizeList.add("4");
            maxSizeList.add("5");
            maxSizeList.add("6");
            for(Iterator<String> iterator = maxSizeList.iterator(); iterator.hasNext(); ){
                System.out.println("iterator.next(2) = " + iterator.next());
            }
            System.out.println("--------------------------------- = ");
            Thread.sleep(200);
            maxSizeList.add("7");
            maxSizeList.add("8");
            maxSizeList.add("9");
            maxSizeList.add("10");
            maxSizeList.add("11");
            maxSizeList.add("12");
            maxSizeList.add("13");
            maxSizeList.add("14");
            maxSizeList.add("15");
            maxSizeList.add("16");
            maxSizeList.add("17");
            maxSizeList.add("18");
            maxSizeList.add("19");
            maxSizeList.add("20");
            maxSizeList.add("21");
            maxSizeList.add("22");
            maxSizeList.add("23");
            maxSizeList.add("24");
            maxSizeList.add("25");
            maxSizeList.add("26");
            maxSizeList.add("27");
            maxSizeList.add("28");
            maxSizeList.add("29");
            maxSizeList.add("30");
            maxSizeList.add("31");
            for(Iterator<String> iterator = maxSizeList.iterator(); iterator.hasNext(); ){
                System.out.println("iterator.next(3) = " + iterator.next());
            }
        }
        System.out.println("---------------KKKKKKKKKKKKKKKK------------------------------------");
        {
            MaxSizeArray<String> mx = new MaxSizeArray<String>(5);
            mx.add("1");
            System.out.println("1="+ mx.pop());
            System.out.println("null="+ mx.pop());
            System.out.println("null="+ mx.pop());
            mx.add("2");
            mx.add("3");
            mx.add("4");
            mx.add("5");
            mx.add("6");
            System.out.println("6="+ mx.pop());
            mx.add("7");
            System.out.println("7="+ mx.pop());
            System.out.println("5="+ mx.pop());
            System.out.println("4="+ mx.pop());
            System.out.println("3="+ mx.pop());
            System.out.println("2="+ mx.pop());
            System.out.println("null="+ mx.pop());
            System.out.println("null="+ mx.pop());
            mx.add("8");
            mx.add("9");
            mx.add("10");
            mx.add("11");
            mx.add("12");
            mx.add("13");
            mx.add("14");
            mx.add("15");
            System.out.println("15="+ mx.pop());
            System.out.println("14="+ mx.pop());
            for(int i = 0; i < 10; i++){
                System.out.println("x*null="+ mx.pop());
            }
        }

        System.out.println("---------------KKKKKKKKKKKKKKKK------------------------------------");
        {
            MaxSizeArray<String> mx = new MaxSizeArray<String>(5);
            mx.add("1");
            System.out.println("1="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            mx.add("2");
            mx.add("3");
            mx.add("4");
            mx.add("5");
            mx.add("6");
            System.out.println("2="+ mx.pull());
            mx.add("7");
            System.out.println("3="+ mx.pull());
            System.out.println("4="+ mx.pull());
            System.out.println("5="+ mx.pull());
            System.out.println("6="+ mx.pull());
            System.out.println("7="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            mx.add("8");
            mx.add("9");
            mx.add("10");
            mx.add("11");
            mx.add("12");
            mx.add("13");
            mx.add("14");
            mx.add("15");
            System.out.println("13="+ mx.pull());
            System.out.println("14="+ mx.pull());
            for(int i = 0; i < 10; i++){
                System.out.println("x*null="+ mx.pull());
            }
        }


        System.out.println("---------------K2K2K2K2K2K2K2K2K2K2K2K2K2K2K2K------------------------------------");
        {
            MaxSizeArray<String> mx = new MaxSizeArray<String>(5);
            mx.add("1");
            mx.add("2");
            mx.debug();
            mx.add("3");
            mx.add("4");
            mx.add("5");
            mx.debug();
            mx.add("6");
            mx.debug();
            mx.add("7");
            mx.debug();
            System.out.println("3=" + mx.pull());
            mx.debug();
            System.out.println("4=" + mx.pull());
            mx.debug();
            System.out.println("5=" + mx.pull());
            mx.debug();
            System.out.println("6=" + mx.pull());
            mx.debug();
            System.out.println("7=" + mx.pull());
            mx.debug();
            System.out.println("null=" + mx.pull());
            mx.add("8");
            mx.add("9");
            mx.add("0");
            mx.add("1");
            mx.add("2");
            mx.add("3");
            System.out.println("9=" + mx.pull());
            System.out.println("0=" + mx.pull());
            System.out.println("1=" + mx.pull());
            System.out.println("2=" + mx.pull());
            mx.add("0");
            mx.add("1");
            mx.add("2");
            mx.add("3");
            System.out.println("3=" + mx.pull());
            System.out.println("0=" + mx.pull());
            System.out.println("1=" + mx.pull());
            System.out.println("2=" + mx.pull());
            System.out.println("3=" + mx.pull());
            System.out.println("null=" + mx.pull());
            mx.add("7");
            System.out.println("7=" + mx.pull());
            mx.add("7");
            System.out.println("7=" + mx.pull());
            mx.add("7");
            System.out.println("7=" + mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
            System.out.println("null="+ mx.pull());
        }



    }


}
