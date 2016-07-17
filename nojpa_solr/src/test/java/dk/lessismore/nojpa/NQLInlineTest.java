package dk.lessismore.nojpa;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.testmodel.Address;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;

/**
 * Created by niakoi on 3/5/16.
 */
public class NQLInlineTest {

    @Test
    public void test() throws Exception {
        //inline query: q=+(_Address_a__ID_Phone_funnyD__DOUBLE:(0.1))+
//        inline query: q=+(_Address_a__ID_Phone_funnyD__DOUBLE:(0.1))+

        Address mock = NQL.mock(Address.class);
        System.out.println("inline query: " + NQL.search(mock).search(mock.getA().getFunnyD(), NQL.Comp.EQUAL, 0.1d).buildQuery());
        System.out.println("normal query: " + NQL.search(mock).search(mock.getArea(), NQL.Comp.EQUAL, "area").buildQuery());
    }
}
