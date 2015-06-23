package dk.lessismore.nojpa.rest;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by niakoi on 12/3/15.
 */
public class NoJpaConverter implements GenericConverter {

    protected Log log = LogFactory.getLog(getClass());

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
            return formatter.parse((String) source, null);
        } else {
            return formatter.print((ModelObjectInterface) source, null);
        }
        return null;
    }
}
