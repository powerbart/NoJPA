package dk.lessismore.reusable_v4.reflection.db.model;

import java.util.*;

public class ModelObjectComparator implements Comparator {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelObjectComparator.class);

    public int compare(Object o1, Object o2){
	return o1.toString().compareTo(o2.toString());
    }
    
    public boolean equals(Object o){ return o instanceof ModelObjectComparator; } 

}
