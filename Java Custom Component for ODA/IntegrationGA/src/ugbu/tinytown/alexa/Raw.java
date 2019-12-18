package ugbu.tinytown.alexa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Raw {

    private String payload;

    public Raw() {
        super();
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public Object getPayloadAsJsonNode() {
        Object value = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            value = mapper.readTree(payload);
        } catch (JsonMappingException e) {
        } catch (JsonProcessingException e) {
        }
        return (value);
    }
    
    public void setAlexaAPL(String ui, String data){
        String apl = "{\"document\":"+ui+", \"datasources\":"+data+"}";
        this.setPayload(apl);
    }
}
