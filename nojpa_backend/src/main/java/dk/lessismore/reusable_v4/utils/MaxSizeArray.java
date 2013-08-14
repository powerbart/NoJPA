package dk.lessismore.reusable_v4.utils;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 06-04-11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
public class MaxSizeArray<E> {


    private Object[] array;
    private int current = 0;
    private int maxSize = 0;

    public MaxSizeArray(int maxSize){
        this.maxSize = maxSize;
        this.array = new Object[maxSize];
    }


    public void add(E e){
        current = current % maxSize;
        array[current++] = e;
    }

    public E pop(){
        if(current == 0){
            current = maxSize;
        }

        if(array[current-1] != null){
            E toReturn = (E) array[--current];
            array[current] = null;
            return toReturn;
        } else {
            return null;
        }
    }


    public Iterator<E> iterator(){

        Iterator<E> ite = new Iterator<E>() {
            int now = maxSize + current -1;
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



    }


}
