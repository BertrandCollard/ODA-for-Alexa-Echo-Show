package ugbu.tinytown.alexa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;


public class PostbackAction extends Action {

    private Object postback;
    public final static String TYPE = "postback";

    public PostbackAction() {
        super();
        this.setType(TYPE);
    }

    public void setPostback(Object postback) {
        this.postback = postback;
    }

    public void setPostback(Map<String, Object> map) {
        String postback = "{";
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pair = (Map.Entry<String, Object>) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            postback += "\"" + pair.getKey() + "\":" + "\"" + pair.getValue().toString() + "\",";
        }
        if (postback.length() > 1)
            postback = postback.substring(0,postback.length() - 1);
        postback += "}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.postback = mapper.readTree(postback);
        } catch (JsonMappingException e) {
        } catch (JsonProcessingException e) {
        }
    }

    public Object getPostback() {
        return postback;
    }
}
