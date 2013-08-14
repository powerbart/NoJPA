package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;

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
