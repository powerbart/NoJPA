package dk.lessismore.nojpa.reflection.db.model;

import dk.lessismore.nojpa.db.DbDataType;
import dk.lessismore.nojpa.reflection.attributeconverters.AttributeConverter;
import dk.lessismore.nojpa.reflection.attributeconverters.AttributeConverterFactory;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
public class ModelObjectSearchService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelObjectSearchService.class);


    public static int AUTO_COMMIT_MS = 300;
    public static int INPUTS_BETWEEN_COMMITS_VISITOR = 1000;

    private static HashMap<String, SolrServer> servers = new HashMap<String, SolrServer>();

    //TODO: Should be StreamingUpdateSolrServer
    public static void addSolrServer(Class className, SolrServer solrServer){
        log.info("Adding solrServer("+ solrServer +") for class("+ className.getSimpleName() +")");
        servers.put(className.getSimpleName(), solrServer);
    }

    public static <T extends ModelObjectInterface> void deleteAll(T object) {
        ModelObject modelObject = (ModelObject) object;
        deleteAll(modelObject.getInterface());

    }

    public static void deleteAll(Class aClass) {
        try {
            SolrServer solrServer = servers.get(aClass.getSimpleName());
            solrServer.deleteByQuery("*:*");
        } catch (SolrServerException e) {
            log.error("Some ERROR-1 when deleting all: " + e, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Some ERROR-2 when deleting all: " + e, e);
            throw new RuntimeException(e);
        }
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
        try{
//            log.debug("put_:1:start");
            ModelObject modelObject = (ModelObject) object;
            SolrServer solrServer = servers.get(modelObject.getInterface().getSimpleName());
//            log.debug("put_:2");
            if(solrServer == null){
                log.fatal("Cant find a solrServer for class("+ modelObject.getInterface().getSimpleName() +")");
            }
//            log.debug("put_:3");
            SolrInputDocument solrObj = new SolrInputDocument();
            put(object, "", new HashMap<String, String>(), solrServer, solrObj);
//            log.debug("put_:4:end");
        } catch (Exception e){
            log.error("put:_ Some error in put-1 " + e, e);
            throw new RuntimeException(e);

        }

    }

    public static <T extends ModelObjectInterface> void put(T object, String prefix, HashMap<String, String> storedObjects, SolrServer solrServer, SolrInputDocument solrObj) {
        ModelObject modelObject = (ModelObject) object;
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(modelObject.getInterface());
        String objectIDInSolr = (prefix.length() == 0 ? "" : prefix + "_") + "objectID" + (prefix.length() == 0 ? "" : "__ID");
        solrObj.addField(objectIDInSolr, object.getObjectID());
        for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            SearchField searchField = dbAttribute.getAttribute().getAnnotation(SearchField.class);
            if(searchField != null) {
                if(!dbAttribute.isAssociation()) {
                    Object value = null;
                    value = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    addAttributeValueToStatement(dbAttribute, solrObj, value, prefix);
                } else if (!dbAttribute.isMultiAssociation()) {
                    ModelObjectInterface value = (ModelObjectInterface) dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    addAttributeValueToStatement(dbAttribute, solrObj, value, prefix);
                    if(value != null && !storedObjects.containsKey(value.getObjectID())){
                        storedObjects.put(value.getObjectID(), value.getObjectID());
                        put(value, dbAttribute.getSolrAttributeName(prefix), storedObjects, solrServer, solrObj);
                    }
//                    solrObj.addField(attributeName, modelObject.getSingleAssociationID(attributeName));
                } else { //isMultiAssociation
                    if(dbAttribute.getAttributeClass().isEnum() || dbAttribute.getAttributeClass().isPrimitive()){
                        Object objects = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
//                        String attributeName = dbAttribute.getAttributeName();
                        String solrAttributeName = dbAttribute.getSolrAttributeName(prefix);
                        solrObj.addField(solrAttributeName, objects);

                    } else {
                        ModelObjectInterface[] vs = (ModelObjectInterface[]) dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                        HashMap<String, ArrayList<Object>> values = new HashMap<String, ArrayList<Object>>();
                        for(int i = 0; vs != null && i < vs.length; i++){
                            ModelObjectInterface value = vs[i];
                            if(value != null && !storedObjects.containsKey(value.getObjectID())){
                                storedObjects.put(value.getObjectID(), value.getObjectID());
                                getSearchValues(value, dbAttribute.getSolrAttributeName(prefix), storedObjects, values);
                            }
                        }
                        Iterator<String> nameIterator = values.keySet().iterator();
                        for(int i = 0; nameIterator.hasNext(); i++){
                            String name = nameIterator.next();
                            ArrayList<Object> objects = values.get(name);
                            String solrArrayName = name + "_ARRAY";
                            log.debug("Adding " + solrArrayName + "("+ (objects != null ? objects.size() : -1) +")");
                            solrObj.addField(solrArrayName, objects);
                        }
                    }
                }
            }
        }
        try {
            solrServer.add(solrObj, AUTO_COMMIT_MS);
        } catch (SolrServerException e) {
            log.error("Some error when adding document ... " + e, e);
        } catch (IOException e) {
            log.error("Some error when adding document ... " + e, e);
        }
    }

    private static  <T extends ModelObjectInterface> void getSearchValues(T object, String prefix, HashMap<String, String> storedObjects, HashMap<String, ArrayList<Object>> values){
        ModelObject modelObject = (ModelObject) object;
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(modelObject.getInterface());
        String objectIDInSolr = (prefix.length() == 0 ? "" : prefix + "_") + "objectID" + (prefix.length() == 0 ? "" : "__ID");
        for (Iterator iterator = dbAttributeContainer.getDbAttributes().values().iterator(); iterator.hasNext();) {
            DbAttribute dbAttribute = (DbAttribute) iterator.next();
            String attributeName = dbAttribute.getAttributeName();
            SearchField searchField = dbAttribute.getAttribute().getAnnotation(SearchField.class);
            if(searchField != null) {
                if(!dbAttribute.isAssociation()) {
                    Object value = null;
                    value = dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    if(value != null){
                        addAttributeValueToMap(dbAttribute, value, prefix, values);
                    }
                } else if (!dbAttribute.isMultiAssociation()) {
                    ModelObjectInterface value = (ModelObjectInterface) dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    if(value != null && !storedObjects.containsKey(value.getObjectID())){
                        storedObjects.put(value.getObjectID(), value.getObjectID());
                        getSearchValues(value, dbAttribute.getSolrAttributeName(prefix), storedObjects, values);
                    }
                } else {
                    ModelObjectInterface[] vs = (ModelObjectInterface[]) dbAttributeContainer.getAttributeValue(modelObject, dbAttribute);
                    for(int i = 0; vs != null && i < vs.length; i++){
                        ModelObjectInterface value = vs[i];
                        if(value != null && !storedObjects.containsKey(value.getObjectID())){
                            storedObjects.put(value.getObjectID(), value.getObjectID());
                            getSearchValues(value, dbAttribute.getSolrAttributeName(prefix), storedObjects, values);
                        }
                    }
                }
            }
        }

    }

    private static  <T extends ModelObjectInterface> void addAttributeValueToMap(DbAttribute dbAttribute, Object value, String prefix, HashMap<String, ArrayList<Object>> values){
        String solrAttributeName = dbAttribute.getSolrAttributeName(prefix);
//        log.debug("Will add solrAttributeName("+ solrAttributeName +") with value("+ value +") to objectMap");
        if (value != null) {
            ArrayList<Object> objects = values.get(solrAttributeName);
            if(objects == null){
                objects = new ArrayList<Object>();
                values.put(solrAttributeName, objects);
            }
            if(dbAttribute.getDataType().getType() == DbDataType.DB_DATE){
                //log.debug("***TimeWrite: " + attributeName + " " + (value != null ? ((Calendar) value).getTime() : "null"));
                SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
                xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                value = xmlDateFormat.format(((Calendar) value).getTime());
            }

            objects.add(value);
        }
    }





    public static DbObjectVisitor putAll(){
        log.debug("************* putAll() *****************");
        return putAll(INPUTS_BETWEEN_COMMITS_VISITOR);
    }

    public static DbObjectVisitor putAll(final int inputsBetweenCommits){
        DbObjectVisitor dbObjectVisitor = new DbObjectVisitor() {
            int counter = 0;


            //TODO:SEB This can be optimized !!
            @Override
            public void visit(ModelObjectInterface m) {
                put(m);
                if(counter++ % inputsBetweenCommits == 0){
                    ModelObjectSearchService.commit(m);
                }
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





    private static void addAttributeValueToStatement(DbAttribute dbAttribute, SolrInputDocument solrObj, Object value, String prefix) {
        String attributeName = dbAttribute.getAttributeName();
        String solrAttributeName = dbAttribute.getSolrAttributeName(prefix);
        log.debug("Will add solrAttributeName("+ solrAttributeName +") with value("+ value +")");
        if (value != null) {
            //Convert the value to the equivalent data type.

            int type = dbAttribute.getDataType().getType();
            switch (type) {
                case DbDataType.DB_LONG:
                    solrObj.addField(solrAttributeName, ((Long) value).longValue());
                    break;
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
                    solrObj.addField(solrAttributeName, valueStr);
                    log.debug("solrObj.addField(" + solrAttributeName +", "+ valueStr +");");
                    break;
                case DbDataType.DB_INT:
                    solrObj.addField(solrAttributeName, ((Integer) value).intValue());
                    break;
                case DbDataType.DB_DOUBLE:
                    solrObj.addField(solrAttributeName, ((Double) value).doubleValue());
                    break;
                case DbDataType.DB_FLOAT:
                    solrObj.addField(solrAttributeName, ((Float) value).floatValue());
                    break;
                case DbDataType.DB_BOOLEAN:
                    solrObj.addField(solrAttributeName, ((Boolean) value).booleanValue() );
                    break;
                case DbDataType.DB_DATE:
                    //log.debug("***TimeWrite: " + attributeName + " " + (value != null ? ((Calendar) value).getTime() : "null"));
                    SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2011-11-28T18:30:30Z
                    xmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    solrObj.addField(solrAttributeName, xmlDateFormat.format(((Calendar) value).getTime()));
                    break;
            }
        } else {
            if (dbAttribute.getDataType().getType() == DbDataType.DB_DATE) {
                solrObj.addField(solrAttributeName, ((Calendar) value));
            } else {
                solrObj.addField(solrAttributeName, (String) null);
            }
        }

    }








}
