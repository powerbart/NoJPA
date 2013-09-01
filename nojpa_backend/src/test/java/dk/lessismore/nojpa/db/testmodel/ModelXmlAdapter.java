package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ModelXmlAdapter<T extends ModelObjectInterface> extends XmlAdapter<T, T> {

    @Override
    public T unmarshal(T t) throws Exception {
        return t;
    }

    @Override
    public T marshal(T t) throws Exception {
        return null;
    }
}
