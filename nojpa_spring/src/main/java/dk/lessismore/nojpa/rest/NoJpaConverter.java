package dk.lessismore.nojpa.rest;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by niakoi on 12/3/15.
 */
public class NoJpaConverter implements GenericConverter {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private NoJpaFormatter formatter;

    public NoJpaConverter(NoJpaFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> pairs = new LinkedHashSet<ConvertiblePair>(2);
        pairs.add(new ConvertiblePair(formatter.getClazz(), String.class));
        pairs.add(new ConvertiblePair(String.class, formatter.getClazz()));
        return pairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType().equals(String.class)) {
            try {
                log.debug("convert(source("+ source +"), sourceType("+ sourceType +"), targetType("+ targetType +"))");
                return formatter.parse((String) source, null);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (RuntimeException re) {
                // if @RequestParam(required=false)
                if (targetType.hasAnnotation(RequestParam.class) && !targetType.getAnnotation(RequestParam.class).required()) {
                    return null;
                }
                throw re;
            }

        } else {
            return formatter.print((ModelObjectInterface) source, null);
        }
    }
}
