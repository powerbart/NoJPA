package dk.lessismore.nojpa.reflection.db.model.solr;

import dk.lessismore.nojpa.db.methodquery.NQL;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SpatialParams;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

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


    public void buildQueryRaw(){
        buildQueryInternal(true);
    }
    public void buildQuery(){
        buildQueryInternal(false);
    }

    private void buildQueryInternal(boolean raw){
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
                    } else if (expression.getGeoForm() == NQL.NoSQLExpression.GeoForm.POLYGON){
                        List<NQL.NLatLon> polygon = (List<NQL.NLatLon>) expression.getValue();
                        String s = polygon.stream().map( p -> p.toStringRev() ).collect(Collectors.joining(",")); //  StringUtils.join(polygon, ",");
                        solrQuery.set(SpatialParams.FIELD, solrAttributeName + "__LOC_RPT");
                        solrQuery.setParam(CommonParams.FQ, "{!field f="+ solrAttributeName +"__LOC_RPT}Intersects(POLYGON(("+ s +")))");
                    } else if (expression.getGeoForm() == NQL.NoSQLExpression.GeoForm.RECTANGLE){
                        solrQuery.set(SpatialParams.FIELD, solrAttributeName);
                        solrQuery.setParam(CommonParams.FQ, solrAttributeName +":" + expression.getValue());
                    }
                } else { // (0 0,100 0,100 100,0 100)

                    //WORKING
                    //q=*:*&q.op=OR&indent=true&spatial=true&spatial.sfield=_Company_latitude__LOC__LOC_RPT&fq=%7B!field%20f%3D_Company_latitude__LOC__LOC_RPT%7DIntersects(POLYGON((60.37855 16.42528,60.19522 16.70471,60.1951 16.70479,60.06922 16.29509,60.06818 16.28882,60.18054 16.02506,60.18215 16.02395,60.17692 15.80513,60.17748 15.80142,60.14588 15.73638,60.14491 15.72537,59.94054 15.6779,59.93792 15.68175,59.95678 15.53411,59.95716 15.53123,59.85858 15.42646,59.85415 15.42224,60.10434 14.8502,60.10449 14.85007,60.08219 14.73844,60.08175 14.73734,59.99893 14.75339,59.99718 14.75366,60.09521 14.21446,60.09574 14.2127,60.23037 14.14948,60.23066 14.1492,60.25107 13.95552,60.251 13.95107,60.18094 13.98204,60.1752 13.96552,60.99555 12.70368,60.99812 12.6987,61.13789 12.70546,61.14327 12.70702,61.34927 12.86818,61.35649 12.87086,61.54689 12.60047,61.56881 12.5691,61.56726 12.44659,61.56299 12.41968,61.71528 12.15181,61.72376 12.13772,62.25819 12.29673,62.2675 12.29941,62.21937 12.77545,62.22053 12.80597,62.06636 13.003,62.06576 13.00348,62.05481 13.2734,62.05808 13.28614,62.00646 13.34118,62.00081 13.39193,61.94201 13.23763,61.9291 13.20032,61.65552 13.54105,61.6435 13.55922,61.59132 14.67656,61.59132 14.6766,61.48613 14.67969,61.48612 14.67981,61.48562 15.06948,61.48412 15.09322,61.59535 15.15301,61.59576 15.15328,61.22547 15.64013,61.21562 15.65735,61.0528 15.75793,61.04964 15.76196,60.99264 16.12478,60.99214 16.13101,60.78939 16.38177,60.78679 16.38615,60.61164 16.15828,60.61031 16.13649,60.37855 16.42528)))
                    //q=*:*&sfield=_Company_latitude__LOC__LOC_RPT&fq={!field+f%3D_Company_latitude__LOC}Intersects(POLYGON((60.37855 16.42528,60.19522 16.70471,60.1951 16.70479,60.06922 16.29509,60.06818 16.28882,60.18054 16.02506,60.18215 16.02395,60.17692 15.80513,60.17748 15.80142,60.14588 15.73638,60.14491 15.72537,59.94054 15.6779,59.93792 15.68175,59.95678 15.53411,59.95716 15.53123,59.85858 15.42646,59.85415 15.42224,60.10434 14.8502,60.10449 14.85007,60.08219 14.73844,60.08175 14.73734,59.99893 14.75339,59.99718 14.75366,60.09521 14.21446,60.09574 14.2127,60.23037 14.14948,60.23066 14.1492,60.25107 13.95552,60.251 13.95107,60.18094 13.98204,60.1752 13.96552,60.99555 12.70368,60.99812 12.6987,61.13789 12.70546,61.14327 12.70702,61.34927 12.86818,61.35649 12.87086,61.54689 12.60047,61.56881 12.5691,61.56726 12.44659,61.56299 12.41968,61.71528 12.15181,61.72376 12.13772,62.25819 12.29673,62.2675 12.29941,62.21937 12.77545,62.22053 12.80597,62.06636 13.003,62.06576 13.00348,62.05481 13.2734,62.05808 13.28614,62.00646 13.34118,62.00081 13.39193,61.94201 13.23763,61.9291 13.20032,61.65552 13.54105,61.6435 13.55922,61.59132 14.67656,61.59132 14.6766,61.48613 14.67969,61.48612 14.67981,61.48562 15.06948,61.48412 15.09322,61.59535 15.15301,61.59576 15.15328,61.22547 15.64013,61.21562 15.65735,61.0528 15.75793,61.04964 15.76196,60.99264 16.12478,60.99214 16.13101,60.78939 16.38177,60.78679 16.38615,60.61164 16.15828,60.61031 16.13649,60.37855 16.42528)))&fl=objectID,score&start=0&rows=0}
                    //{!field f=_Company_latitude__LOC__LOC_RPT}Intersects(POLYGON((0 0,80 0,80 80,0 80,0 0)))
                    //q=*:*&sfield=_Company_latitude__LOC__LOC_RPT&{!field f=_Company_latitude__LOC__LOC_RPT}Intersects(POLYGON((0 0,80 0,80 80,0 80,0 0)))
                    //q=*:*&q.op=OR&indent=true&spatial=true&spatial.sfield=_Company_latitude__LOC__LOC_RPT&fq=%7B!field%20f%3D_Company_latitude__LOC__LOC_RPT%7DIntersects(POLYGON((0%200,80%200,80%2080,0%2080,0%200)))
                    //q=*:*&q.op=OR&indent=true&spatial=true&spatial.sfield=_Company_latitude__LOC__LOC_RPT&fq=%7B!field%20f%3D_Company_latitude__LOC__LOC_RPT%7DIntersects(POLYGON((0%200,80%200,80%2080,0%2080,0%200)))
                    //{q=*:*&sfield=_Company_latitude__LOC__LOC_RPT&fq={!field+f%3D_Company_latitude__LOC}Intersects(POLYGON((60.37855+16.42528,60.19522+16.70471,60.1951+16.70479,60.06922+16.29509,60.06818+16.28882,60.18054+16.02506,60.18215+16.02395,60.17692+15.80513,60.17748+15.80142,60.14588+15.73638,60.14491+15.72537,59.94054+15.6779,59.93792+15.68175,59.95678+15.53411,59.95716+15.53123,59.85858+15.42646,59.85415+15.42224,60.10434+14.8502,60.10449+14.85007,60.08219+14.73844,60.08175+14.73734,59.99893+14.75339,59.99718+14.75366,60.09521+14.21446,60.09574+14.2127,60.23037+14.14948,60.23066+14.1492,60.25107+13.95552,60.251+13.95107,60.18094+13.98204,60.1752+13.96552,60.99555+12.70368,60.99812+12.6987,61.13789+12.70546,61.14327+12.70702,61.34927+12.86818,61.35649+12.87086,61.54689+12.60047,61.56881+12.5691,61.56726+12.44659,61.56299+12.41968,61.71528+12.15181,61.72376+12.13772,62.25819+12.29673,62.2675+12.29941,62.21937+12.77545,62.22053+12.80597,62.06636+13.003,62.06576+13.00348,62.05481+13.2734,62.05808+13.28614,62.00646+13.34118,62.00081+13.39193,61.94201+13.23763,61.9291+13.20032,61.65552+13.54105,61.6435+13.55922,61.59132+14.67656,61.59132+14.6766,61.48613+14.67969,61.48612+14.67981,61.48562+15.06948,61.48412+15.09322,61.59535+15.15301,61.59576+15.15328,61.22547+15.64013,61.21562+15.65735,61.0528+15.75793,61.04964+15.76196,60.99264+16.12478,60.99214+16.13101,60.78939+16.38177,60.78679+16.38615,60.61164+16.15828,60.61031+16.13649,60.37855+16.42528)))&fl=objectID,score&start=0&rows=0}
                    //{q=*:*&sfield=_Company_latitude__LOC__LOC_RPT&fq={!field+f%3D_Company_latitude__LOC}Intersects(POLYGON((60.37855 16.42528,60.19522 16.70471,60.1951 16.70479,60.06922 16.29509,60.06818 16.28882,60.18054 16.02506,60.18215 16.02395,60.17692 15.80513,60.17748 15.80142,60.14588 15.73638,60.14491 15.72537,59.94054 15.6779,59.93792 15.68175,59.95678 15.53411,59.95716 15.53123,59.85858 15.42646,59.85415 15.42224,60.10434 14.8502,60.10449 14.85007,60.08219 14.73844,60.08175 14.73734,59.99893 14.75339,59.99718 14.75366,60.09521 14.21446,60.09574 14.2127,60.23037 14.14948,60.23066 14.1492,60.25107 13.95552,60.251 13.95107,60.18094 13.98204,60.1752 13.96552,60.99555 12.70368,60.99812 12.6987,61.13789 12.70546,61.14327 12.70702,61.34927 12.86818,61.35649 12.87086,61.54689 12.60047,61.56881 12.5691,61.56726 12.44659,61.56299 12.41968,61.71528 12.15181,61.72376 12.13772,62.25819 12.29673,62.2675 12.29941,62.21937 12.77545,62.22053 12.80597,62.06636 13.003,62.06576 13.00348,62.05481 13.2734,62.05808 13.28614,62.00646 13.34118,62.00081 13.39193,61.94201 13.23763,61.9291 13.20032,61.65552 13.54105,61.6435 13.55922,61.59132 14.67656,61.59132 14.6766,61.48613 14.67969,61.48612 14.67981,61.48562 15.06948,61.48412 15.09322,61.59535 15.15301,61.59576 15.15328,61.22547 15.64013,61.21562 15.65735,61.0528 15.75793,61.04964 15.76196,60.99264 16.12478,60.99214 16.13101,60.78939 16.38177,60.78679 16.38615,60.61164 16.15828,60.61031 16.13649,60.37855 16.42528)))&fl=objectID,score&start=0&rows=0}
                    //56.17194370230855,15.287987617163468
                    //55 15, 57 15, 57 16, 55 16, 55 15
                    //15 55, 15 57, 16 57, 16 55, 15 55

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
        } else if (!raw){
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
        return solrQuery.toString().replace('+', ' ') + " ON SHARD("+ getShard() +")";
    }


}
