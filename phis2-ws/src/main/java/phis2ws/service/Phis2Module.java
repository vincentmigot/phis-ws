//******************************************************************************
//                           Phis2Module.java
// OpenSILEX
// Copyright © INRA 2019
// Creation date: 01 jan. 2019
// Contact: vincent.migot@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service;

import javax.inject.Singleton;
import org.opensilex.core.Application;
import org.opensilex.core.OpenSilexModule;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import phis2ws.service.authentication.Session;
import phis2ws.service.injection.SessionFactory;
import phis2ws.service.injection.SessionInject;
import phis2ws.service.injection.SessionInjectResolver;
import phis2ws.service.resources.brapi.BrapiCall;
import phis2ws.service.resources.brapi.CallsResourceService;
import phis2ws.service.resources.brapi.StudyDetailsResourceService;
import phis2ws.service.resources.brapi.TokenResourceService;
import phis2ws.service.resources.brapi.TraitsResourceService;
import phis2ws.service.resources.brapi.VariableResourceService;

/**
 *
 * @author vincent
 */
public class Phis2Module implements OpenSilexModule {

    @Override
    public void init(Application app) {
        app.register(new AbstractBinder() {
            @Override
            protected void configure() {
                // cree la session a partir du sessionId reçu
                bindFactory(SessionFactory.class).to(Session.class);
                // Injection de la session grace au type definit dans SessionInjectResolver
                bind(SessionInjectResolver.class)
                        .to(new TypeLiteral<InjectionResolver<SessionInject>>() {
                        })
                        .in(Singleton.class); 
                //Brapi services injection
                bind(CallsResourceService.class).to(BrapiCall.class);
                bind(TokenResourceService.class).to(BrapiCall.class);
                bind(StudyDetailsResourceService.class).to(BrapiCall.class);
                bind(TraitsResourceService.class).to(BrapiCall.class);
                bind(VariableResourceService.class).to(BrapiCall.class);
            }
        });
    }

}
