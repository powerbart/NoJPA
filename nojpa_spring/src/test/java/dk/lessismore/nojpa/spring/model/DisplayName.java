package dk.lessismore.nojpa.spring.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created by niakoi on 9/3/14.
 */
@JsonFilter("getDisplayType")
public interface DisplayName extends ModelObjectInterface {

    String getName();
    void setName(String name);

    DisplayType getDisplayType();
    void setDisplayType(DisplayType displayType);

}
