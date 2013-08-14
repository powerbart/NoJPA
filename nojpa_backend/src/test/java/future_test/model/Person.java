package future_test.model;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
//@Searchable(impl = Person1Searcher.Indexer.class)
//@Searchable(impl = Person2Searcher.Indexer.class)
//@DbAlias(dbName = "CCRDB")
public interface Person {

    String getName();
    void setName();


}
