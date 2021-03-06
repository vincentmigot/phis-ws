//******************************************************************************
//                          ImageMetadataMongoDAO.java
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: 11 Dec. 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.core.Response;
import opensilex.service.dao.exception.DAODataErrorAggregateException;
import opensilex.service.dao.exception.DAOPersistenceException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.PropertiesFileManager;
import opensilex.service.configuration.DateFormats;
import opensilex.service.dao.manager.MongoDAO;
import opensilex.service.documentation.StatusCodeMsg;
import opensilex.service.ontology.Contexts;
import opensilex.service.resource.dto.ConcernedItemDTO;
import opensilex.service.resource.dto.ImageMetadataDTO;
import opensilex.service.utils.POSTResultsReturn;
import opensilex.service.view.brapi.Status;
import opensilex.service.model.ConcernedItem;
import opensilex.service.model.ImageMetadata;
import org.opensilex.sparql.service.SPARQLService;

/**
 * Image metadata DAO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 * @update [Andréas Garcia] Jan. 2019: modify "concern(s)" occurences into 
 * "concernedItem(s)" in Java variables and MongoDB fields
 */
public class ImageMetadataMongoDAO extends MongoDAO<ImageMetadata> {

    private final SPARQLService sparql;

    public ImageMetadataMongoDAO(SPARQLService sparql) {
        this.sparql = sparql;
    }
    
    final static Logger LOGGER = LoggerFactory.getLogger(ImageMetadataMongoDAO.class);
    public String uri;
    public String rdfType;
    
    /**
     * Start date of the wanted images
     */
    public String startDate;
    
    /**
     * End date of the wanted images
     */
    public String endDate;
    
    /**
     * URI of the sensor for the wanted images
     */
    public String sensor;
    
    /**
     * List of the elements concerned by the image. 
     * The elements are represented by URIs.
     */
    public ArrayList<String> concernedItems = new ArrayList<>();
    
    private final MongoCollection<Document> imagesCollection = database.getCollection(PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "images"));
    
    //Represents the mongodb documents label for the image uri
    final static String DB_FIELDS_IMAGE_URI = "uri";
    //Represents the mongodb documents label for the concerned items uris
    final static String DB_FIELDS_CONCERNED_ITEM_URI = "uri";
    //Represents the mongodb documents label for the rdf types
    final static String DB_FIELDS_RDF_TYPE = "rdfType";
    //Represents the mongodb documents label for the concerned items list
    final static String DB_FIELDS_CONCERNED_ITEMS = "concernedItems";
    //Represents the mongodb documents label for the shooting configurations
    final static String DB_FIELDS_SHOOTING_CONFIGURATION = "shootingConfiguration";
        //Represents the mongodb documents label for the storage
    final static String DB_FIELDS_STORAGE = "storage";

    
    /**
     * Searches images metadata
     * @return the search query.
     *         Query example :
     *         { 
     *              "uri" : "http://www.phenome-fppn.fr/phis_field/2017/i170000000000" , 
     *              "rdfType" : "http://www.opensilex.org/vocabulary/oeso#HemisphericalImage" , 
     *              "$and" : 
     *                  [ 
     *                      { 
     *                          "concernedItems" : { 
     *                              "$elemMatch" : { 
     *                                  "uri" : "http://phenome-fppn.fr/phis_field/ao1"
     *                              }
     *                          }
     *                      },
     *                      { 
     *                          "concernedItems" : { 
     *                              "$elemMatch" : { 
     *                                  "uri" : "http://phenome-fppn.fr/phis_field/ao1"
     *                              }
     *                          }
     *                      }
     *                  ] , 
     *                  "shootingConfiguration.date" : { 
     *                      "$date" : "2017-06-15T08:51:00.000Z"
     *                  }
     *              }
     */
    @Override
    protected BasicDBObject prepareSearchQuery() {
       BasicDBObject query = new BasicDBObject();
       
       if (uri != null) {
           query.append(DB_FIELDS_IMAGE_URI, uri);
       }
       if (rdfType != null) {
           query.append(DB_FIELDS_RDF_TYPE, rdfType);
       }
       if (concernedItems != null && !concernedItems.isEmpty()) {
           BasicDBList and = new BasicDBList();
           for (String concernedItem : concernedItems) {
               BasicDBObject clause = new BasicDBObject(DB_FIELDS_CONCERNED_ITEMS, new BasicDBObject(MONGO_ELEM_MATCH, new BasicDBObject(DB_FIELDS_CONCERNED_ITEM_URI, concernedItem)));
               and.add(clause);
           }
           
           query.append(MONGO_AND, and);
       }
       if (startDate != null && endDate != null) {
           try {
               SimpleDateFormat df = new SimpleDateFormat(DateFormats.YMD_FORMAT);
               Date start = df.parse(startDate);
               Date end = df.parse(endDate);
               query.append(DB_FIELDS_SHOOTING_CONFIGURATION + "." + ShootingConfigurationDAO.DB_FIELDS_DATE, 
                       BasicDBObjectBuilder.start(MONGO_GTE, start).add(MONGO_LTE, end).get());
           } catch (ParseException ex) {
               java.util.logging.Logger.getLogger(ImageMetadataMongoDAO.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
       if (sensor != null) {
           query.append(DB_FIELDS_SHOOTING_CONFIGURATION + "." + ShootingConfigurationDAO.DB_FIELDS_SENSOR, sensor);
       }
       LOGGER.debug(getTraceabilityLogs() + " query : " + query.toString());
       
       return query;
    }
    
    /**
     * transform a documents list (which is supposed to contains concerned items)
     * into a concerned items list
     * @param concernedItemsDocuments
     * @return the concerned items extracted from the document list
     */
    private ArrayList<ConcernedItem> mongoDocumentListToConcernedItems(List<Document> concernedItemsDocuments) {
        ArrayList<ConcernedItem> concernedItemsToReturn = new ArrayList<>();
        for (Document concernedItemDocument : concernedItemsDocuments) {
            ConcernedItem concernedItem = new ConcernedItem();
            concernedItem.setUri(concernedItemDocument.getString(DB_FIELDS_CONCERNED_ITEM_URI));
            concernedItem.setRdfType(concernedItemDocument.getString(DB_FIELDS_RDF_TYPE));
            concernedItemsToReturn.add(concernedItem);
        }
        return concernedItemsToReturn;
    }

    public ArrayList<ImageMetadata> allPaginate() {
        BasicDBObject searchQuery = prepareSearchQuery();
       
        FindIterable<Document> imagesMetadataMongo = imagesCollection.find(searchQuery);
        //sort by date
        imagesMetadataMongo.sort(new BasicDBObject(DB_FIELDS_SHOOTING_CONFIGURATION + "." + ShootingConfigurationDAO.DB_FIELDS_DATE, 1));
        
        ArrayList<ImageMetadata> imagesMetadata = new ArrayList<>();
        
        try (MongoCursor<Document> imagesMetadataCursor = imagesMetadataMongo.iterator()) {
            //for each found image metadata, 
            //add the image in the ArrayList<ImageMetadata> to return
            while (imagesMetadataCursor.hasNext()) {
                Document imageMetadataDocument = imagesMetadataCursor.next();
                
                ImageMetadata imageMetadata = new ImageMetadata();
                imageMetadata.setUri(imageMetadataDocument.getString(DB_FIELDS_IMAGE_URI));
                imageMetadata.setRdfType(imageMetadataDocument.getString(DB_FIELDS_RDF_TYPE));
                
                //Add the elements concerned by the image in the ImageMetadata
                List<Document> concernedItemDocuments = (List<Document>) imageMetadataDocument.get(DB_FIELDS_CONCERNED_ITEMS);
                imageMetadata.setConcernedItems(mongoDocumentListToConcernedItems(concernedItemDocuments));
                
                //Add the shootingConfiguration
                Document shootingConfigurationDocument = (Document) imageMetadataDocument.get(DB_FIELDS_SHOOTING_CONFIGURATION);
                imageMetadata.setConfiguration(ShootingConfigurationDAO.mongoDocumentToShootingConfiguration(shootingConfigurationDocument));
                
                //add the file informations
                Document fileInformationsDocument = (Document) imageMetadataDocument.get(DB_FIELDS_STORAGE);
                imageMetadata.setFileInformations(FileInformationDAO.mongoDocumentToFileInformation(fileInformationsDocument));
                
                imagesMetadata.add(imageMetadata);
            }
        }
        
        return imagesMetadata;
    }
    
    /**
     * Checks if the images metadata are correct.
     * (rules, image type, concerned items)
     * @param imagesMetadata
     * @return the result of the check of the images metadata. 
     */
    public POSTResultsReturn check(List<ImageMetadataDTO> imagesMetadata) {
        List<Status> checkStatusList = new ArrayList<>(); //Status to be returned
        
        boolean dataOk = true;
        
        for (ImageMetadataDTO imageMetadata : imagesMetadata) {
            //1. Check if the image type exist
            ImageMetadataRdf4jDAO imageMetadataRdf4jDao = new opensilex.service.dao.ImageMetadataRdf4jDAO(sparql);
            if (!imageMetadataRdf4jDao.existUri(imageMetadata.getRdfType())) {
                dataOk = false;
                checkStatusList.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, "Wrong image type given : " + imageMetadata.getRdfType()));
            }

            //2. Check if the concerned items exist in the triplestore
            for (ConcernedItemDTO concernedItem : imageMetadata.getConcernedItems()) {
                if (!imageMetadataRdf4jDao.existUri(concernedItem.getUri())) {
                    dataOk = false;
                    checkStatusList.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, "Unknown concerned item given : " + concernedItem.getUri()));
                }
            }

            //3. Check if the sensor exist
            SensorDAO sensorDAO = new SensorDAO(sparql);
            if (!sensorDAO.existAndIsSensor(imageMetadata.getConfiguration().getSensor())) {
                dataOk = false;
                checkStatusList.add(new Status(StatusCodeMsg.WRONG_VALUE, StatusCodeMsg.ERR, "Unknown sensor given : " + imageMetadata.getConfiguration().getSensor()));
            }
        }
        
        POSTResultsReturn imagesMetadataCheck = new POSTResultsReturn(dataOk, null, dataOk);
        imagesMetadataCheck.statusList = checkStatusList;
        return imagesMetadataCheck;
    }
    
    /**
     * Prepares the query of getting the number of images by year.
     * @return query to get the number of images by year. 
     *         Query example : 
     *         {uri: {$regex: "http://www.phenome-fppn.fr/diaphen/2017*"}}    
     */
    private Document prepareGetLastId() {        
          Document regQuery = new Document();
          String regex = Contexts.PLATFORM.toString() + Year.now().toString() + "*";
          regQuery.append("$regex", regex);
          
          Document findQuery = new Document();
          findQuery.append(DB_FIELDS_IMAGE_URI, regQuery);
          
          LOGGER.debug(getTraceabilityLogs() + " query : " + findQuery.toString());
          
          return findQuery;
    }
    
    /**
     * Gets the number of images in the database for the current year.
     * @return the number of images in the database for the current year.
     */
    public long getImagesCountOfCurrentYear() {
        Document query = prepareGetLastId();
        
        FindIterable<Document> cursor = imagesCollection
                .find(query)
                .sort(new BasicDBObject(DB_FIELDS_IMAGE_URI, -1))
                .limit(1);
        
        String lastUri = "";
        MongoCursor<Document> imagesMetadataCursor = cursor.iterator();
        while (imagesMetadataCursor.hasNext()) {
            Document imageMetadataDocument = imagesMetadataCursor.next();
            lastUri = imageMetadataDocument.getString(DB_FIELDS_IMAGE_URI);
        }
        if (lastUri.equals("")) {
            return 0;
        } else {
            String[] splitString = lastUri.split("/i" + Year.now().toString().substring(2, 4));

            return Integer.parseInt(splitString[splitString.length - 1]);
        }
    }
    
    /**
     * Inserts the images metadata in MongoDB.
     * @param imagesMetadata
     * @return the result of the insert
     * @throws ParseException 
     */
    @SuppressWarnings("empty-statement")
    public POSTResultsReturn insert(List<ImageMetadata> imagesMetadata) throws ParseException {
        List<Status> insertStatus = new ArrayList<>();
        POSTResultsReturn result;
        List<String> createdResourcesUris = new ArrayList<>();
        
       //SILEX:todo
       // transactions
       //\SILEX:todo
       SimpleDateFormat df = new SimpleDateFormat(DateFormats.YMDHMSZ_FORMAT);
       for (ImageMetadata imageMetadata : imagesMetadata) {
           Document metadata = new Document();
           metadata.append(DB_FIELDS_IMAGE_URI, imageMetadata.getUri());
           metadata.append(DB_FIELDS_RDF_TYPE, imageMetadata.getRdfType());
           
           //Concerned Items
           ArrayList<Document> concernedItemsToSave = new ArrayList<>();
           for (ConcernedItem concernedItem : imageMetadata.getConcernedItems()) {
               Document concernedItemDocument = new Document();
               concernedItemDocument.append(DB_FIELDS_CONCERNED_ITEM_URI, concernedItem.getUri());
               concernedItemDocument.append(DB_FIELDS_RDF_TYPE, concernedItem.getRdfType());
               concernedItemsToSave.add(concernedItemDocument);
           }
           metadata.append(DB_FIELDS_CONCERNED_ITEMS, concernedItemsToSave);
           
           // Configuration
           Document configuration = new Document();
           Date dateImage = df.parse(imageMetadata.getConfiguration().getDate());
           configuration.append(ShootingConfigurationDAO.DB_FIELDS_DATE, dateImage);
           Timestamp timestamp = new Timestamp(new Date().getTime());
           configuration.append(ShootingConfigurationDAO.DB_FIELDS_TIMESTAMP, timestamp.getTime());
           configuration.append(ShootingConfigurationDAO.DB_FIELDS_SENSOR_POSITION, imageMetadata.getConfiguration().getPosition());
           configuration.append(ShootingConfigurationDAO.DB_FIELDS_SENSOR, imageMetadata.getConfiguration().getSensor());
           metadata.append(DB_FIELDS_SHOOTING_CONFIGURATION, configuration);
           
           // FileInformations (Storage)
           Document storage = new Document();
           storage.append(FileInformationDAO.DB_FIELDS_EXTENSION, imageMetadata.getFileInformations().getExtension());
           storage.append(FileInformationDAO.DB_FIELDS_MD5SUM, imageMetadata.getFileInformations().getChecksum());
           storage.append(FileInformationDAO.DB_FIELDS_SERVER_FILE_PATH, imageMetadata.getFileInformations().getServerFilePath());
           metadata.append(DB_FIELDS_STORAGE, storage);
           
           LOGGER.debug("MongoDB insert : " + metadata.toJson());
           imagesCollection.insertOne(metadata);
           createdResourcesUris.add(imageMetadata.getUri());
       }
       
       insertStatus.add(new Status(StatusCodeMsg.RESOURCES_CREATED, StatusCodeMsg.INFO, StatusCodeMsg.DATA_INSERTED));;
       result = new POSTResultsReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
       result.setHttpStatus(Response.Status.CREATED);
       result.statusList = insertStatus;
       result.createdResources = createdResourcesUris;
       
       return result;
    }

    @Override
    public List<ImageMetadata> create(List<ImageMetadata> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(List<ImageMetadata> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ImageMetadata> update(List<ImageMetadata> objects) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageMetadata find(ImageMetadata object) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ImageMetadata findById(String id) throws DAOPersistenceException, Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validate(List<ImageMetadata> objects) throws DAOPersistenceException, DAODataErrorAggregateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
