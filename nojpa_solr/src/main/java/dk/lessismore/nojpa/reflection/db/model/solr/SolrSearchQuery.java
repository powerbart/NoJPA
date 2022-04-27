package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SpatialParams;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class SolrSearchQuery extends NQL.SearchQuery{

    private static final Logger log = LoggerFactory.getLogger(SolrSearchQuery.class);

    SolrQuery solrQuery;

    private String shard = null;


    public SolrSearchQuery(Class selectClass) {
        super(selectClass);
        solrQuery = new SolrQuery();
    }


    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public String getShard() {
        return shard;
    }



    private String buildSubQuery(NQL.NoSQLExpression expression, NQL.Constraint constraint){
        if(expression instanceof NQL.NoSQLNegatingExpression){
            StringBuilder builder = new StringBuilder("(*:* -");
            String subQuery = buildSubQuery(((NQL.NoSQLNegatingExpression) expression).getExpression(), constraint);
            if(subQuery != null){
                builder.append(subQuery.trim());
            }
            builder.append(")");
            return builder.toString();
        } else if(expression instanceof NQL.NoSQLContainerExpression) {
            NQL.NoSQLContainerExpression root = (NQL.NoSQLContainerExpression) expression;
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < root.getExpressions().size(); i++) {
                NQL.NoSQLExpression exp = root.getExpressions().get(i);
                Integer condition = root.getConditions().get(i);
                String subQuery = buildSubQuery(exp, constraint);
//                log.debug("Will add subQuery("+ subQuery +") with " + NQL.NoSQLOperator.name(condition));
                boolean containsOrOrAnd = subQuery.contains(" OR ") || subQuery.contains(" AND ");
                if (containsOrOrAnd) {
                    subQuery = "(" + subQuery + ")";
                }
                builder.append(subQuery);
                if(root.getExpressions().size() > 1 && i + 1 < root.getExpressions().size()){
                    builder.append(" " + NQL.NoSQLOperator.name(condition) + " ");
                }
            }

            if(builder.length() > 2 && root.getConditions().size() > 1 && NQL.NoSQLOperator.name(root.getConditions().get(0)).equals("OR")){
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
                    boostQuery = "^" + ((NQL.Boost)noSQLFunction).getBoost();
                } else {
                    otherFunctions += " " + noSQLFunction;
                }
            }
            if (expression.getRaw() != null) {
//                return  "(" +  expression.getRaw() +")";
                return  expression.getRaw();
            }
            if (expression.getValue() == null) {
                return otherFunctions;
            }

            String statementValue = null;
            if(!expression.isNotNull() && !expression.isNull()){
                if(expression.isSharding() && expression.getValue() != null){
                    this.shard = "" + expression.getValue();
                }
                if(expression.getValueClazz().equals(String.class) || expression.isEnum()){
                    statementValue = "" + expression.getValue();
                } else if(expression.getValue() instanceof Calendar){
                    SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
                    xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    //solrObj.addField(solrAttributeName, xmlDateFormat.format(((Calendar) value).getTime()));
                    statementValue = "\"" + xmlDateFormat.format(((Calendar) expression.getValue()).getTime()) + "\"";
                } else if(expression.getValueClazz().equals(Integer.class) || expression.getValueClazz().equals(Float.class) || expression.getValueClazz().equals(Long.class) || expression.getValueClazz().equals(Double.class) || expression.getValueClazz().equals(Number.class) ){
                    statementValue = "" + expression.getValue();
                }

                if(expression.getComparator() == NQL.Comp.EQUAL_OR_LESS){
                    return " (" + solrAttributeName + ":[* TO " + statementValue + "]"+ boostQuery +")" + otherFunctions;
                } else if(expression.getComparator() == NQL.Comp.EQUAL_OR_GREATER){
                    return " (" + solrAttributeName + ":[" + statementValue + " TO *]"+ boostQuery +")" + otherFunctions;
                } else if(expression.getComparator() == NQL.Comp.NOT_EQUAL){
                    //return " (*:* -" + solrAttributeName + ":("+ statementValue +") "+ boostQuery +")" + otherFunctions;
                    if(expression.isEnum() || !statementValue.contains(" ")){
                        return " -" + solrAttributeName + ":"+ statementValue +" ";
                    } else {
                        return " (*:* -" + solrAttributeName + ":("+ statementValue +") "+ boostQuery +")" + otherFunctions;
                    }
                } else {
                    return " (" + solrAttributeName + ":("+ statementValue +")"+ boostQuery +")" + otherFunctions;
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


    public void buildQuery(){
        {
            StringBuilder builder = new StringBuilder();
            solrQuery.setQuery("*:*");
            for (int i = 0; i < rootConstraints.size(); i++) {
                NQL.Constraint constraint = (NQL.Constraint) rootConstraints.get(i);
                NQL.NoSQLExpression expression = constraint.getExpression();
                if (expression.getGeoForm() != null) {
                    String solrAttributeName = NQL.createFinalSolrAttributeName(expression.getJoints(), expression.getAttr());
                    if (expression.getGeoForm() == NQL.NoSQLExpression.GeoForm.CIRCLE) {
                        solrQuery.set(SpatialParams.FIELD, solrAttributeName);
                        solrQuery.set(SpatialParams.POINT, "" + expression.getValue());
                        solrQuery.set(SpatialParams.DISTANCE, "" + expression.getDistance());
                        solrQuery.setParam(CommonParams.FQ, "{!geofilt sfield="+ solrAttributeName +"}");
                    } else if (expression.getGeoForm() == NQL.NoSQLExpression.GeoForm.BOX){
                        solrQuery.set(SpatialParams.FIELD, solrAttributeName);
                        solrQuery.set(SpatialParams.POINT, "" + expression.getValue());
                        solrQuery.set(SpatialParams.DISTANCE, "" + expression.getDistance());
                        solrQuery.setParam(CommonParams.FQ, "{!bbox sfield="+ solrAttributeName +"}");
                    } else if (expression.getGeoForm() == NQL.NoSQLExpression.GeoForm.RECTANGLE){
                        solrQuery.set(SpatialParams.FIELD, solrAttributeName);
//                        solrQuery.set(SpatialParams.POINT, "" + expression.getValue());
//                        solrQuery.set(SpatialParams.DISTANCE, "" + expression.getDistance());
                        solrQuery.setParam(CommonParams.FQ, solrAttributeName +":" + expression.getValue());
                    }
                } else {
                    String subQuery = buildSubQuery(expression, constraint);
                    if (subQuery != null && subQuery.length() > 0) {
                        if (builder.length() > 1) {
                            builder.append(" AND ");
                        }
                        builder.append(subQuery);
                    }
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
            log.debug("We will query = " + query);
            solrQuery.setQuery(query);
        }
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < filterConstraints.size(); i++) {
                NQL.Constraint constraint = (NQL.Constraint) filterConstraints.get(i);
                NQL.NoSQLExpression expression = constraint.getExpression();
                String subQuery = buildSubQuery(expression, constraint);
                if (subQuery != null && subQuery.length() > 0) {
                    if (builder.length() > 1) {
                        builder.append(" AND ");
                    }
                    builder.append(subQuery);
                }
            }
            String query = builder.toString();
            if (query == null || query.length() < 2) {
                query = null;
            }
            if(query != null) {
                log.debug("We will add filters = " + query);
                solrQuery.setFilterQueries(query);
            }
        }

        if (enableHighlighting) {
            solrQuery.setHighlight(true);
            solrQuery.setHighlightSimplePre("__");
            solrQuery.setHighlightSimplePost("__");
            solrQuery.setParam(HighlightParams.FIELDS, "*");
            solrQuery.setParam(HighlightParams.USE_PHRASE_HIGHLIGHTER, true);
            solrQuery.setParam(HighlightParams.HIGHLIGHT_MULTI_TERM, true);

        }
        if(addStats){
            String[] statsFields = (String[]) statsAttributeIdentifier.toArray(new String[0]);
            solrQuery.addGetFieldStatistics(statsFields);
        } else if(addDateFacets){
            DateRangeFacet dateRangeFacet = (DateRangeFacet) dateFacetAttributeIdentifier.get(0);
            String[] facetFields = (String[]) facetAttributeIdentifier.toArray(new String[0]);
//            solrQuery.addFacetField(facetFields);
//            solrQuery.addGetFieldStatistics(facetFields);

//            Map<String, String> inner = new HashMap<>();
//            inner.put("myavg", "avg(_Article_analyzed__ID_ArticleAnalyzed_negative__DOUBLE)");
//            Map<String, Object> m = new HashMap<>();
//            m.put("type", "range");
//            m.put("field", "_Article_creationDate__DATE");
//            m.put("start", "NOW-28DAYS");
//            m.put("end", "NOW/DAY");
//            m.put("gap", "+1DAY");
//            m.put("facet", inner);

            String json = "" +
                    "{ nqlFacet:{\n" +
                    "    type : range,\n" +
                    "    field : \"_Article_creationDate__DATE\",\n" +
                    "    start :\""+ dateRangeFacet.getStart() +"\", \n" +
                    "    end : \""+ dateRangeFacet.getEnd() +"\", \n" +
                    "    gap : \""+ dateRangeFacet.getGap() +"\"\n" +
                    "    facet:{\n";
            for(int i = 0; i < facetFields.length; i++){
                DateRangeFacet.STATS[] stats = dateRangeFacet.getStats();
                for(int s = 0; s < stats.length; s++) {
                    json += "      " + stats[s] + facetFields[i] + " : \""+ stats[s].toString().toLowerCase() +"(" + facetFields[i] + ")\"" + (i + 1 < facetFields.length || s + 1 < stats.length  ? ", \n" : "\n");
                }
            }
            json +=   "    }\n" +
                    "  }" +
                    "}" +
                    "";
            System.out.println("");
            System.out.println(json);
            System.out.println("");

            solrQuery.add("json.facet", json);

//            solrQuery.addFacetQuery("avg("+ facetFields[0] +")");
            //solrQuery.addStatsFieldFacets(dateRangeFacet.getVariable(), facetFields);
            //solrQuery.addDateRangeFacet(dateRangeFacet.getVariable(), dateRangeFacet.getEnd().getTime(), dateRangeFacet.getStart().getTime(), dateRangeFacet.getGap());
        } else if(addFacets){
            String[] facetFields = (String[]) facetAttributeIdentifier.toArray(new String[0]);
            solrQuery.addFacetField(facetFields);
            if(facetLimit > 0) {
                solrQuery.setFacetLimit(facetLimit);
            }
        } else {
            solrQuery.setFields("objectID,score");
        }

//        solrQuery.setFilterQueries("_Article_downloaded__ID_ArticleDownloaded_domainEnding__ID:(\"dk\")");
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
