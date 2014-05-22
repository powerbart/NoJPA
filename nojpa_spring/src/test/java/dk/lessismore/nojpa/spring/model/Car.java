package dk.lessismore.nojpa.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
//@Searchable2
    @JsonInclude
public interface Car extends ModelObjectInterface {
    String getName();
    void setName(String name);
}
