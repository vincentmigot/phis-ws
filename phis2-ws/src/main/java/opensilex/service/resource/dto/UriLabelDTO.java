//******************************************************************************
//                                UriLabelDTO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 23 mai 2019
// Contact: Expression userEmail is undefined on line 6, column 15 in file:///home/vincent/opensilex/phis-ws/phis2-ws/licenseheader.txt., anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.dto;

/**
 *
 * @author vincent
 */
public class UriLabelDTO {
    
    String uri;
    
    String label;

    public UriLabelDTO(String uri, String label) {
        this.uri = uri;
        this.label = label;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    
}
