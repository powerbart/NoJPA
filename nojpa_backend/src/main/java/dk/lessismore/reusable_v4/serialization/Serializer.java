package dk.lessismore.reusable_v4.serialization;

import java.io.IOException;
import java.io.File;

public interface Serializer {

    public String serialize(Object object);

    public void store(Object object, File file) throws IOException;

    public <T> T unserialize(String serializedString);

    public Object restore(File file) throws IOException;
}