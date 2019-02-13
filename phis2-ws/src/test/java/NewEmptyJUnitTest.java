//******************************************************************************
//                                       NewEmptyJUnitTest.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 13 févr. 2019
// Contact: Expression userEmail is undefined on line 6, column 15 in file:///home/vincent/opensilex/opensilex-instance/phis-ws/phis2-ws/licenseheader.txt., anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import phis2ws.service.resources.brapi.BrapiCall;
import phis2ws.service.view.model.phis.Call;

/**
 *
 * @author vincent
 */
public class NewEmptyJUnitTest {
    
    public NewEmptyJUnitTest() {
        new BrapiCall() {
            @Override
            public ArrayList<Call> callInfo() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
