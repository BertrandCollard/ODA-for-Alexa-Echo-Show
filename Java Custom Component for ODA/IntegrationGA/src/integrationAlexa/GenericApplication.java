package integrationAlexa;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import oracle.wsm.metadata.annotation.PolicyReference;
import oracle.wsm.metadata.annotation.PolicySet;

import ugbu.tinytown.alexa.AlexaGenericResource;

@ApplicationPath("rest")
//@PolicySet(references = { @PolicyReference(value = "oracle/http_basic_auth_over_ssl_service_policy") })
public class GenericApplication extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();

        // Register root resources.
        classes.add(AlexaGenericResource.class);
        System.out.println("GenericApplication Alexa Rest started!");

        // Register provider classes.

        return classes;
    }
}
