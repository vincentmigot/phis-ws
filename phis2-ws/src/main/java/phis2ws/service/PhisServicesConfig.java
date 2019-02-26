//******************************************************************************
//                       PhisServicesConfig.java
// OpenSILEX
// Copyright © INRA 2019
// Creation date: 01 jan. 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service;

/**
 * Phis service configuration
 */
public interface PhisServicesConfig {
    String ontologyBaseURI();
    String infrastructure();
    String vocabulary();
    
    String sessionTime();
    String waitingFileTime();
    
    String uploadFileServerIP();
    String uploadFileServerUsername();
    String uploadFileServerPassword();
    String uploadFileServerDirectory();
    
    String layerFileServerDirectory();
    String layerFileServerAddress();
    
    String uploadImageServerDirectory();
    String imageFileServerDirectory();
    
    String webAppApiDocsName();
    
    String gnpisPublicKeyFileName();
    String phisPublicKeyFileName();
    
    String documentsCollection();
    String provenanceCollection();
    String dataCollection();
    String imagesCollection();

}
