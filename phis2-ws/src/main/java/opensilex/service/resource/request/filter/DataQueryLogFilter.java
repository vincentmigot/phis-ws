//******************************************************************************
//                     DataLogAccessFilter.java
// SILEX-PHIS
// Copyright © INRAE 2020
// Creation date: February 2020
// Contact: arnaud.charleroy@inrae.fr, anne.tireau@inrae.fr, pascal.neveu@inrae.fr
//******************************************************************************
package opensilex.service.resource.request.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import opensilex.service.PropertiesFileManager;
import opensilex.service.authentication.Session;
import opensilex.service.configuration.GlobalWebserviceValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import opensilex.service.dao.DataQueryLogDAO;
import opensilex.service.resource.DataResourceService;
import opensilex.service.resource.EnvironmentResourceService;
import org.opensilex.rest.authentication.AuthenticationService;
import org.opensilex.rest.user.dal.UserModel;
import org.opensilex.sparql.service.SPARQLService;

/**
 * Filters web service requests to log query done on data.
 *
 * @author Arnaud Charleroy
 */
@Provider
public class DataQueryLogFilter implements ContainerRequestFilter {

    final static Logger LOGGER = LoggerFactory.getLogger(DataQueryLogFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpServletRequest servletRequest;

    @Inject
    AuthenticationService authentication;

    final static String MAP_FIELD_QUERY_PARAMETERS = "queryParameters";
    final static String MAP_FIELD_RESSOURCE_PATH = "ressourcePath";
    final static String MAP_FIELD_WS_VERSION = "wsVersion";

    /**
     * Filters the session token.
     *
     * @param requestContext
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        //1 . get ressource path
        final UriInfo uriInfo = requestContext.getUriInfo();
        final String resourcePath = uriInfo.getPath();
        String httpMethod = servletRequest.getMethod();

        //2 . check access log configuration
        String logDataQuery = PropertiesFileManager.getConfigFileProperty("mongodb_nosql_config", "logDataQuery");

        if (Boolean.valueOf(logDataQuery)) {
            //3 . check if the path equals to data service and sub services
            if (resourcePath != null
                    && httpMethod.equals("GET")
                    && !resourcePath.contains("querylog")
                    && (resourcePath.contains("data")
                    || resourceInfo.getResourceClass() == DataResourceService.class
                    || resourcePath.contains("environment")
                    || resourceInfo.getResourceClass() == EnvironmentResourceService.class)) {
                MultivaluedMap<String, String> queryPathParameters = uriInfo.getQueryParameters();

                // 4. retreive user informations
                String authorization = requestContext.getHeaderString(GlobalWebserviceValues.AUTHORIZATION_PROPERTY);
                String userToken = authorization.replace(GlobalWebserviceValues.AUTHENTICATION_SCHEME + " ", "");

                URI userURI;
                try {
                    userURI = authentication.decodeTokenUserURI(userToken);
                    // 6. save data search query
                    DataQueryLogDAO dataAccessLogDao = new DataQueryLogDAO();
                    dataAccessLogDao.remoteUserAdress = servletRequest.getRemoteAddr();
                    Map<String, Object> queryParmeters = new HashMap<>();
                    queryParmeters.put(MAP_FIELD_QUERY_PARAMETERS, queryPathParameters);
                    queryParmeters.put(MAP_FIELD_RESSOURCE_PATH, resourcePath);
                    queryParmeters.put(MAP_FIELD_WS_VERSION, getClass().getPackage().getImplementationVersion());
                    Date currentDate = new Date();
                    dataAccessLogDao.insert(userURI.toString(), servletRequest.getRemoteAddr(), currentDate, queryParmeters);
                } catch (JWTVerificationException ex) {
                    java.util.logging.Logger.getLogger(DataQueryLogFilter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (URISyntaxException ex) {
                    java.util.logging.Logger.getLogger(DataQueryLogFilter.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(DataQueryLogFilter.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
    
    
}
