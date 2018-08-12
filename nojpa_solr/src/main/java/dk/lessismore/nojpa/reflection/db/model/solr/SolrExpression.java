package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class SolrExpression {

//    private static final Logger log = LoggerFactory.getLogger(NQL.class);
//
//    String statement = "*:*";
//    String attr = null;
//    String value = null;
//    private List<Pair<Class, String>> joints;
//    NQL.Comp comparator;
//    ArrayList<NQL.NoSQLFunction> noSQLFunctions = new ArrayList<NQL.NoSQLFunction>();
//
//
//    public void addFunction(NQL.NoSQLFunction noSQLFunction){
//        noSQLFunctions.add(noSQLFunction);
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, String value) {
//        this.statement = "("+ attributeName +":("+ value +"))";
//        this.attr = attributeName;
//        this.value = value;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, Enum value) {
//        this.statement = "("+ attributeName +":("+ value +"))";
//        this.attr = attributeName;
//        this.value = ""+ value;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, Calendar value) {
//        SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
//        xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        //solrObj.addField(solrAttributeName, xmlDateFormat.format(((Calendar) value).getTime()));
//
//        this.value = xmlDateFormat.format(value.getTime());
//        this.statement = "("+ attributeName +":("+ this.value +"))";
//        this.attr = attributeName;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, int value) {
//        log.trace("addConstrain:int("+ value +")");
//        this.statement = "("+ attributeName +":"+ value +")";
//        this.attr = attributeName;
//        this.value = "" + value;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, double value) {
//        log.trace("addConstrain:double("+ value +")");
//        this.statement = "("+ attributeName +":"+ value +")";
//        this.attr = attributeName;
//        this.value = "" + value;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, float value) {
//        log.trace("addConstrain:float("+ value +")");
//        this.statement = "("+ attributeName +":"+ value +")";
//        this.attr = attributeName;
//        this.value = "" + value;
//        this.comparator = comparator;
//        return this;
//    }
//
//    public SolrExpression addConstrain(String attributeName, NQL.Comp comparator, long value) {
//        log.trace("addConstrain:long("+ value +")");
//        this.statement = "("+ attributeName +":"+ value +")";
//        this.attr = attributeName;
//        this.value = "" + value;
//        this.comparator = comparator;
//        return this;
//    }
//    public SolrExpression addConstrain(String query) {
//        log.trace("addConstrain:("+ query+")");
//        this.statement = query;
//        return this;
//    }
//
//    public SolrExpression isNull(String attributeName, List<Pair<Class, String>> joints) {
////            log.warn("isNull ("+ attributeName +")");
//        attributeName = NQL.createFinalSolrAttributeName(joints, attributeName);
//        if (attributeName.endsWith("__DATE")) {
//            this.statement = "-(" + attributeName + ":[* TO *])";
//        } else {
//            this.statement = "-(" + attributeName + ":[\"\" TO *])";
//        }
//        this.attr = "-" + attributeName;
//        return this;
//    }
//
//    public SolrExpression isNotNull(String attributeName, List<Pair<Class, String>> joints) {
////            log.debug("isNotNull("+ attributeName +")");
//        attributeName = NQL.createFinalSolrAttributeName(joints, attributeName);
//        if (attributeName.endsWith("__DATE")) {
//            this.statement = "(" + attributeName + ":[* TO *])";
//        } else {
//            this.statement = "(" + attributeName + ":[\"\" TO *])";
//        }
//        this.attr = attributeName;
//        return this;
//    }
//
//    public SolrExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, String value) {
//        return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public SolrExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, Calendar value) {
//        return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//
//    public SolrExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, int value) {
//        return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public SolrExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, double value) {
//        return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    public SolrExpression addConstrain(Class sourceClass, String attributeName, NQL.Comp comparator, float value) {
//        return addConstrain(NQL.makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
//    }
//
//    @Override
//    public String updateQuery(SolrQuery solrQuery) {
//        String solrAttributeName = NQL.createFinalSolrAttributeName(joints, attr);
//        String boostQuery = "";
//        String otherFunctions = " ";
//        for(int i = 0; i < noSQLFunctions.size(); i++){
//            NQL.NoSQLFunction noSQLFunction = noSQLFunctions.get(i);
//            if(noSQLFunction instanceof NQL.Boost){
////                boostQuery = "^" + ((NQL.Boost)noSQLFunction).boost;
//            } else {
//                otherFunctions += " " + noSQLFunction;
//            }
//        }
//        if (this.value == null) {
//            return this.statement;
//        }
//        if(this.comparator == NQL.Comp.EQUAL_OR_LESS){
//            return " (" + solrAttributeName + ":[* TO " + value + "]"+ boostQuery +")" + otherFunctions;
//        } else if(this.comparator == NQL.Comp.EQUAL_OR_GREATER){
//            return " (" + solrAttributeName + ":[" + value + " TO *]"+ boostQuery +")" + otherFunctions;
//        } else if(this.comparator == NQL.Comp.NOT_EQUAL){
//            return
//                    (otherFunctions.equals(" ") ? "" : " (") +
//                            " -(" + solrAttributeName + ":(" + value + "))"+ boostQuery +" " +
//                            (otherFunctions.equals(" ") ? "" : " )" + otherFunctions);
//        }
//
////            return " (" + solrAttributeName + ":(" + removeFunnyChars(value) + ")"+ boostQuery +")" + otherFunctions;
//        return " (" + solrAttributeName + ":(" + (value) + ")"+ boostQuery +")" + otherFunctions;
//
//    }
//    //_Post_shareCounter__ID_Counter_count__TXT
//    //_Post_shareCounter__ID_Counter_count__TXT
//
//    public void addJoints(List<Pair<Class, String>> joints) {
//        this.joints = joints;
//    }
}
