package dk.lessismore.reusable_v4.reflection.db.model;

import dk.lessismore.reusable_v4.db.DbDataType;
import dk.lessismore.reusable_v4.db.statements.InsertSQLStatement;
import dk.lessismore.reusable_v4.reflection.attributeconverters.AttributeConverter;
import dk.lessismore.reusable_v4.reflection.attributeconverters.AttributeConverterFactory;
import dk.lessismore.reusable_v4.reflection.db.DbClassReflector;
import dk.lessismore.reusable_v4.reflection.db.DbObjectVisitor;
import dk.lessismore.reusable_v4.reflection.db.annotations.ModelObjectLifeCycleListener;
import dk.lessismore.reusable_v4.reflection.db.annotations.SearchField;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttribute;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttributeContainer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class ModelObjectSearchService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelObjectSearchService.class);


    public static int AUTO_COMMIT_MS = 300;
    public static int INPUTS_BETWEEN_COMMITS_VISITOR = 1000;

    private static HashMap<String, SolrServer> servers = new HashMap<String, SolrServer>();

    public static void addSolrServer(Class className, SolrServer solrServer){
        servers.put(className.getSimpleName(), solrServer);
    }

    public static <T extends ModelObjectInterface> void delete(T object) {
        ModelObject modelObject = (ModelObject) object;
        SolrServer solrServer = servers.get(modelObject.getInterface().getSimpleName());
        try {
            solrServer.deleteById(object.getObjectID(), AUTO_COMMIT_MS);
        } catch (SolrServerException e) {
            log.error("Some error when adding document ... " + e, e);
        } catch (IOException e) {
            log.error("Some error when adding document ... " + e, e);
        }
    }


    public static <T extends ModelObjectInterface> SolrServer solrServer(T object) {
        ModelObject modelObject = (ModelObject) object;
        SolrServer solrServer = servers.get(modelObject.getInterface().getSimpleName());
        return solrServer;
    }

    public static <T extends ModelObjectInterface> SolrServer solrServer(Class<T> aClass) {
        SolrServer solrServer = servers.get(aClass.getSimpleName());
        return solrServer;
    }




    public static <T extends ModelObjectInterface> void put(T object) {
        ModelObject modelObject = (ModelObject) object;
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(modelObject.getInterface());
        SolrInputDocument solrObj = new SolrInputDocument();
        for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            String attributeName = dbAttribute.getAttributeName();
            SearchField searchField = dbAttribute.getAttribute().getAnnotation(SearchField.class);
            if(searchField != null) {
                if(!dbAttribute.isAssociation()) {
                    Object value = null;
                    value = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    addAttributeValueToStatement(dbAttribute, solrObj, value);
                } else if (!dbAttribute.isMultiAssociation()) {
                    solrObj.addField(attributeName, modelObject.getSingleAssociationID(attributeName));
                }
            }
        }
        SolrServer solrServer = servers.get(modelObject.getInterface().getSimpleName());
        try {
            solrServer.add(solrObj, AUTO_COMMIT_MS);
        } catch (SolrServerException e) {
            log.error("Some error when adding document ... " + e, e);
        } catch (IOException e) {
            log.error("Some error when adding document ... " + e, e);
        }
    }

    public DbObjectVisitor putAll(){
        return putAll(INPUTS_BETWEEN_COMMITS_VISITOR);
    }

    public DbObjectVisitor putAll(int inputsBetweenCommits){
        DbObjectVisitor dbObjectVisitor = new DbObjectVisitor() {
            int counter = 0;


            //TODO:SEB This can be optimized !!
            @Override
            public void visit(ModelObjectInterface m) {
                put(m);
            }

            @Override
            public void setDone(boolean b) {

            }

            @Override
            public boolean getDone() {
                return false;
            }
        };
        return dbObjectVisitor;
    }



    public static <T extends ModelObjectInterface> void commit(T object) {
        ModelObject modelObject = (ModelObject) object;
        SolrServer solrServer = servers.get(modelObject.getInterface().getSimpleName());
        try {
            solrServer.commit();
        } catch (SolrServerException e) {
            log.error("Some error when commit document ... " + e, e);
        } catch (IOException e) {
            log.error("Some error when commit document ... " + e, e);
        }
    }

    public static <T extends ModelObjectInterface> void commit(Class<T> modelObjectClass) {
        SolrServer solrServer = servers.get(modelObjectClass.getSimpleName());
        try {
            solrServer.commit();
        } catch (SolrServerException e) {
            log.error("Some error when commit document ... " + e, e);
        } catch (IOException e) {
            log.error("Some error when commit document ... " + e, e);
        }
    }





    private static void addAttributeValueToStatement(DbAttribute dbAttribute, SolrInputDocument solrObj, Object value) {
        String attributeName = dbAttribute.getAttributeName();
        if (value != null) {
            //Convert the value to the equivalent data type.
            int type = dbAttribute.getDataType().getType();
            switch (type) {
                case DbDataType.DB_CHAR:
                case DbDataType.DB_VARCHAR:
                    String valueStr = null;
                    if (value instanceof String) {
                        valueStr = (String) value;
                    } else {
                        //This attribute is not a string; but we have to save it as one in the
                        //database. We must convert the object to a string!
                        AttributeConverter converter = AttributeConverterFactory.getInstance().getConverter(dbAttribute.getAttributeClass());
                        if (converter != null) {
                            //We have a converter for it.
                            if (!dbAttribute.getAttribute().isArray()) {
                                valueStr = converter.objectToString(value);
                            } else {
                                valueStr = converter.arrayToString((Object[]) value);
                            }
                        } else {
                            valueStr = value.toString();
                        }
                    }
                    solrObj.addField(attributeName, valueStr);
                    break;
                case DbDataType.DB_INT:
                    solrObj.addField(attributeName, ((Integer) value).intValue());
                    break;
                case DbDataType.DB_DOUBLE:
                    solrObj.addField(attributeName, ((Double) value).doubleValue());
                    break;
                case DbDataType.DB_BOOLEAN:
                    solrObj.addField(attributeName, ((Boolean) value).booleanValue() );
                    break;
                case DbDataType.DB_DATE:
                    //log.debug("***TimeWrite: " + attributeName + " " + (value != null ? ((Calendar) value).getTime() : "null"));
                    solrObj.addField(attributeName, ((Calendar) value).getTime());
                    break;
            }
        } else {
            if (dbAttribute.getDataType().getType() == DbDataType.DB_DATE) {
                solrObj.addField(attributeName, ((Calendar) value));
            } else {
                solrObj.addField(attributeName, (String) null);
            }
        }

    }








}
