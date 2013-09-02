package dk.lessismore.nojpa.utils;

import java.util.*;
import java.lang.reflect.*;

import org.apache.log4j.Logger;


public class GenericComparator<T> implements Comparator<T> {

    private final static org.apache.log4j.Logger log = Logger.getLogger(GenericComparator.class);

    private Method getMethod = null;
    //private Class someClass = null;
    //private String someAttribute = null;
    private boolean invertDirection = false;

    public GenericComparator(Class someClass, String someAttribute) {
        this(someClass, someAttribute, false);
    }

    public GenericComparator(Class someClass, String someAttribute, boolean invertDirection) {

        try {
            String someAttributeGet = "get" + ("" + someAttribute.charAt(0)).toUpperCase() + someAttribute.substring(1);
            //this.someClass = someClass;
            try {
                this.getMethod = someClass.getMethod(someAttributeGet, (Class[]) null);
            } catch (Exception e) {
                log.error("Some error in GenericComparator("+ someClass.getSimpleName() +"."+ someAttribute +")");
                this.getMethod = someClass.getMethod(someAttribute, (Class[]) null);
            }
            this.invertDirection = invertDirection;
            log.debug("GenericComparator :: someAttribute : " + someAttribute);
            log.debug("GenericComparator :: someClass : " + someClass);
            log.debug("GenericComparator :: getMethod : " + getMethod);
            log.debug("GenericComparator :: this : " + this);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Some error in GenericComparator::construtor " + someAttribute);
            log.error("Some error in GenericComparator::construtor " + e);
        }
    }


    public int compare(Object o1, Object o2) {
        try {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
//	    log.debug("GenericComparator :: compareWithNullLast : getMethod : " + getMethod);
//	    log.debug("GenericComparator :: compareWithNullLast : someClass : " + someClass);
//	    log.debug("GenericComparator :: compareWithNullLast : someAttribute : " + someAttribute);
//	    log.debug("GenericComparator :: this : " + this);
//	    log.debug("GenericComparator :: compareWithNullLast : o1 : " + o1);
//	    log.debug("GenericComparator :: compareWithNullLast : o2 : " + o2);
            Object v1 = getMethod.invoke(o1, (Object[]) null);
            Object v2 = getMethod.invoke(o2, (Object[]) null);
            if (v1 == null && v2 == null) {
                return 0;
            }
            if (v1 == null) {
                return -1;
            }
            if (v2 == null) {
                return 1;
            }
            if (v1 instanceof Float) {
                return (invertDirection ? -1 : 1) * (((Float) v1).intValue() < ((Float) v2).intValue() ? -1 : ((Float) v1).intValue() > ((Float) v2).intValue() ? 1 : 0);
            } else if (v1 instanceof Integer) {
                return (invertDirection ? -1 : 1) * (((Integer) v1).intValue() < ((Integer) v2).intValue() ? -1 : ((Integer) v1).intValue() > ((Integer) v2).intValue() ? 1 : 0);
            } else if (v1 instanceof Calendar) {
                int i = (invertDirection ? -1 : 1) * (((Calendar) v1).before((Calendar) v2) ? -1 : ((Calendar) v1).after((Calendar) v2) ? 1 : 0);
//		log.debug("-------------");
//		log.debug("i= " + i);
//		log.debug("v1.getTimeInMillis() " + ((Calendar) v1).getTimeInMillis());
//		log.debug("v2.getTimeInMillis()= " + ((Calendar) v2).getTimeInMillis());
//		log.debug("-------------");
                return i;

            } else {
                return (invertDirection ? -1 : 1) * ("" + v1).compareTo("" + v2);
            }
        } catch (Exception exp) {
            log.error("Some error in compareWithNullLast ... GenericComparator("+ getMethod.getClass() +"."+ getMethod.getName() +"): " + exp);
            exp.printStackTrace();
            return -1;
        }
    }


    public static void main(String[] args) throws Exception {
        log.fatal("Hej med dig");
        log.debug("deeededed hej med dig");

//        class Tester {
//            Calendar c = Calendar.getInstance();
//            String id = null;
//            long time = System.currentTimeMillis();
//
//            float f;
//
//            public Tester(int c, String id) {
//                //this.c.setTimeInMillis( this.c.getTimeInMillis()  + c);
//                try {
//                    (new Thread()).sleep(2);
//                } catch (InterruptedException e) {
//                    System.out.println(e);
//                }
//                this.id = id;
//                f = Float.parseFloat(id);
//            }
//
//            public Calendar getC() {
//                return c;
//            }
//
//            public float getF() {
//                return f;
//            }
//
//            public String getID() {
//                return id;
//            }
//
//            public long getSomeTime() { return time; }
//
//            public String toString() {
//                return "id=" + id + " \t " + time;
//            }
//        }
//
//        Tester ts[] = new Tester[10];
//        for (int i = 0; i < ts.length; i++) {
//            ts[i] = new Tester((-i * i), "" + i);
//
//        }
//        for (int i = 0; i < ts.length; i++) {
//            System.out.println("before " + ts[i]);
//        }
//        GenericComparator c = new GenericComparator(Tester.class, "f", true);
//        Arrays.sort(ts, c);
//        for (int i = 0; i < ts.length; i++) {
//            System.out.println("after " + ts[i]);
//        }

    }
}
