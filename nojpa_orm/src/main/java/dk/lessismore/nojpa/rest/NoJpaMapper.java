package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/25/14
 * Time: 9:29 AM
 */
public class NoJpaMapper extends ObjectMapper {
    public NoJpaMapper() {
        this.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        this.registerModule(new NoJpaModule());
    }
}
