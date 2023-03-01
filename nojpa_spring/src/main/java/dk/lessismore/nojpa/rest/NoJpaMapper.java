package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 * Date: 3/25/14
 * Time: 9:29 AM
 */
public class NoJpaMapper extends ObjectMapper {
    public NoJpaMapper() {

        this.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        this.registerModule(new NoJpaModule());
//        this.registerModule(new Jackson2HalModule());
    }


    @Override
    public ObjectMapper copy() {
        ObjectMapper copy = super.copy();
        copy.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        copy.registerModule(new NoJpaModule());
        return copy;
    }
}
