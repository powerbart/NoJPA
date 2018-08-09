package dk.lessismore.nojpa.reflection.db.model.nosql;

public interface NoSQLResponse {
    long getNumFound();

    int size();

    String getID(int i);

    Object getRaw(int i);
}
