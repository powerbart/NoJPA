package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.utils.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static dk.lessismore.nojpa.db.methodquery.NQL.makeAttributeIdentifier;

public class SolrSearchQuery extends NQL.SearchQuery{

    private static final Logger log = LoggerFactory.getLogger(SolrSearchQuery.class);

    SolrQuery solrQuery;

    public SolrSearchQuery(Class selectClass) {
        super(selectClass);
        solrQuery = new SolrQuery();
    }


    public SolrQuery getSolrQuery() {
        return solrQuery;
    }


    private String buildSubQuery(NQL.NoSQLExpression expression){
        if(expression instanceof NQL.NoSQLNegatingExpression){
            StringBuilder builder = new StringBuilder("-");
            String subQuery = buildSubQuery(((NQL.NoSQLNegatingExpression) expression).getExpression());
            if(subQuery != null){
                builder.append(subQuery);
            }
            return builder.toString();
        } else if(expression instanceof NQL.NoSQLContainerExpression) {
            NQL.NoSQLContainerExpression root = (NQL.NoSQLContainerExpression) expression;
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < root.getExpressions().size(); i++) {
                NQL.NoSQLExpression exp = root.getExpressions().get(i);
                Integer condition = root.getConditions().get(i);
                String subQuery = buildSubQuery(exp);
//                log.debug("Will add subQuery("+ subQuery +") with " + NQL.NoSQLOperator.name(condition));
                builder.append(subQuery);
                if(root.getExpressions().size() > 1 && i + 1 < root.getExpressions().size()){
                    builder.append(" " + NQL.NoSQLOperator.name(condition) + " ");
                }
            }

            if(builder.length() > 2){
                builder.insert(0, "(");
                builder.append(")");
            }
            return builder.toString();

        } else if(expression.getClass().equals(NQL.NoSQLExpression.class)){
            String solrAttributeName = NQL.createFinalSolrAttributeName(expression.getJoints(), expression.getAttr());
            String boostQuery = "";
            String otherFunctions = " ";
            for(int i = 0; i < expression.getNoSQLFunctions().size(); i++){
                NQL.NoSQLFunction noSQLFunction = expression.getNoSQLFunctions().get(i);
                if(noSQLFunction instanceof NQL.Boost){
    //                boostQuery = "^" + ((NQL.Boost)noSQLFunction).boost;
                } else {
                    otherFunctions += " " + noSQLFunction;
                }
            }
            if (expression.getRaw() != null) {
                return  "(" +  expression.getRaw() +")";
            }
            if (expression.getValue() == null) {
                return otherFunctions;
            }

            String statementValue = null;
            if(!expression.isNotNull() && !expression.isNull()){
                if(expression.getValueClazz().equals(String.class) || expression.isEnum()){
                    statementValue = "" + NQL.removeFunnyChars("" + expression.getValue());
                } else if(expression.getValue() instanceof Calendar){
                    SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
                    xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    //solrObj.addField(solrAttributeName, xmlDateFormat.format(((Calendar) value).getTime()));
                    statementValue = xmlDateFormat.format(((Calendar) expression.getValue()).getTime());
                } else if(expression.getValueClazz().equals(Integer.class) || expression.getValueClazz().equals(Float.class) || expression.getValueClazz().equals(Long.class) || expression.getValueClazz().equals(Double.class) || expression.getValueClazz().equals(Number.class) ){
                    statementValue = "" + expression.getValue();
                }

                if(expression.getComparator() == NQL.Comp.EQUAL_OR_LESS){
                    return " (" + solrAttributeName + ":[* TO " + statementValue + "]"+ boostQuery +")" + otherFunctions;
                } else if(expression.getComparator() == NQL.Comp.EQUAL_OR_GREATER){
                    return " (" + solrAttributeName + ":[" + statementValue + " TO *]"+ boostQuery +")" + otherFunctions;
                } else if(expression.getComparator() == NQL.Comp.NOT_EQUAL){
                    return " -(" + solrAttributeName + ":("+ statementValue +") "+ boostQuery +")" + otherFunctions;
                } else {
                    return " (" + solrAttributeName + ":("+ statementValue +") "+ boostQuery +")" + otherFunctions;
                }
            } else if(expression.isNull()){
                if (solrAttributeName.endsWith("__DATE")) {
                    return "-(" + solrAttributeName + ":[* TO *])";
                } else {
                    return "-(" + solrAttributeName + ":[\"\" TO *])";
                }
            } else if(expression.isNotNull()){
                if (solrAttributeName.endsWith("__DATE")) {
                    return "(" + solrAttributeName + ":[* TO *])";
                } else {
                    return "(" + solrAttributeName + ":[\"\" TO *])";
                }
            }
        }
        String s = "Some error in the logic ... ";
        log.error(s);
        throw new RuntimeException(s);
    }


    protected void buildQuery(){
        StringBuilder builder = new StringBuilder();
        solrQuery.setQuery("*:*");
        for (int i = 0; i < rootConstraints.size(); i++) {
            NQL.Constraint constraint = (NQL.Constraint) rootConstraints.get(i);
            NQL.NoSQLExpression expression = constraint.getExpression();
            String subQuery = buildSubQuery(expression);
            if (subQuery != null) {
                if(builder.length() > 1){
                    builder.append(" AND ");
                }
                builder.append(subQuery);
            }
        }
        String query = builder.toString();
        if (query == null || query.length() < 2) {
            query = "*:*";
        } else {
            if (preBoost != null) {
                query = preBoost + " " + query;
            }
        }
        if(addStats){
            String[] statsFields = (String[]) statsAttributeIdentifier.toArray(new String[0]);
            solrQuery.addGetFieldStatistics(statsFields);
        }
        if(addFacets){
            String[] facetFields = (String[]) facetAttributeIdentifier.toArray(new String[0]);
            solrQuery.addFacetField(facetFields);
            if(facetLimit > 0) {
                solrQuery.setFacetLimit(facetLimit);
            }
        }

        log.debug("We will query = " + query);
        solrQuery.setQuery(query);
        if (startLimit != -1) {
            solrQuery.setStart(startLimit);
            solrQuery.setRows(endLimit - startLimit);
        }
        if (this.orderByAttribute != null) {
            log.debug("Will sort by " + orderByAttribute + " with " + this.orderByORDER);
            solrQuery.addSort(this.orderByAttribute, this.orderByORDER == NQL.Order.ASC ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
        }
    }




    public void addAggregation(AggregationBuilder aggregation){
        throw new RuntimeException("NOT-IMPLEMENTED... addAggregation@Solr");
    }







    @Override
    protected String toStringDebugQuery() {
        return solrQuery.toString().replace('+', ' ');
    }


}
