package dk.lessismore.nojpa.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.hateoas.hal.Jackson2HalModule;

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
}
