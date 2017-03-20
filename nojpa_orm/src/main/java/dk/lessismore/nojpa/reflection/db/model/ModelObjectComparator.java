package dk.lessismore.nojpa.reflection.db.model;

import java.util.*;

public class ModelObjectComparator implements Comparator {

    public int compare(Object o1, Object o2){
	return o1.toString().compareTo(o2.toString());
    }
    
    public boolean equals(Object o){ return o instanceof ModelObjectComparator; } 

}
