//******************************************************************************
//                                       helloWorldTest.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 26 nov. 2018
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package brapiv1calls;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.opensilex.core.OpensilexTest;
import org.opensilex.core.api.sample.HelloWorldService;
import phis2ws.service.resources.brapi.BrapiCall;
import phis2ws.service.resources.brapi.CallsResourceService;
import phis2ws.service.resources.brapi.StudyDetailsResourceService;
import phis2ws.service.resources.brapi.TokenResourceService;
import phis2ws.service.resources.brapi.VariableResourceService;
import phis2ws.service.resources.brapi.TraitsResourceService;

/**
 * Tests for the BrAPI Call service
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class CallsTest extends OpensilexTest {
    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = configure(CallsResourceService.class);
 
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                //Brapi services injection
                bind(CallsResourceService.class).to(BrapiCall.class);
                bind(TokenResourceService.class).to(BrapiCall.class);
                bind(StudyDetailsResourceService.class).to(BrapiCall.class);
                bind(TraitsResourceService.class).to(BrapiCall.class);
                bind(VariableResourceService.class).to(BrapiCall.class);
            }
        });

        return resourceConfig;
    }
    
    /**
     * Test if the returned code is 200 when the call is correct
     */
    @Test
    public void testCodeReturn() {
        final Response callResult = target("/brapi/v1/calls")
                                            .queryParam("datatype", "json")
                                            .queryParam("page", 0)
                                            .queryParam("pageSize", 1)
                                            .request().get();
        
        assertEquals(200, callResult.getStatus());
    }
}
