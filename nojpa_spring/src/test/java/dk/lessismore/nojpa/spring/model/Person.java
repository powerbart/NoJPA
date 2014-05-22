package dk.lessismore.nojpa.spring.model;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
//@Searchable(impl = Person1Searcher.Indexer.class)
//@Searchable(impl = Person2Searcher.Indexer.class)
//@DbAlias(dbName = "CCRDB")
public interface Person extends ModelObjectInterface {

    String getName();
    void setName(String name);

    Car getCar();
    void setCar(Car car);

    Car[] getCars();
    void setCars(Car[] car);

}
