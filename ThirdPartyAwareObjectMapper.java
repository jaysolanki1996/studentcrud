package my.airo.infra.web.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@SuppressWarnings("serial")
public class ThirdPartyAwareObjectMapper extends ObjectMapper {

    public ThirdPartyAwareObjectMapper() {
        ThirdPartyAwareObjectMapper.configureAndRegisterModules(this);
    }

    public static void configureAndRegisterModules(ObjectMapper om) {
        om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.registerModule(new JavaTimeModule());

        Hibernate5Module hibernate5Module = new Hibernate5Module();
        // Enables serialization of @Transient variable in Hibernate Entity
        hibernate5Module.configure(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        // Helps to disable the lazy loading in Hibernate
        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
        om.registerModule(hibernate5Module);
        
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(BigDecimal.class, new BigDecimalSerializer());
//        om.registerModule(simpleModule);
        
        om.registerModule(new ParameterNamesModule());
        om.registerModule(new JavaTimeModule());

        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

}