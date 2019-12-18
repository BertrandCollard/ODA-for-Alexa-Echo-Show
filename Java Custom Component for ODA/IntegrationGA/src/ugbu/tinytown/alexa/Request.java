package ugbu.tinytown.alexa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Request {

    private String botId;
    private String platformVersion;
    private Object context;
    private Object properties;
    private MessageRequest message;
    private String state;
    private String previousState;
    private Object executedStates;
    private boolean modifyContext = false;


    public Request() {
        super();
    }

    public JsonNode getVariable(String name) {
        return getVariable(getContextRoot(), name);
    }

    public JsonNode getVariable(JsonNode contextNode, String name) {
        JsonNode value = getVariableRef(contextNode, name);
        if (value != null) {
            value = value.findValue("value");
        }
        return value;
    }

    public JsonNode getVariableRef(JsonNode contextNode, String name) {
        JsonNode value = null;
        String nameToUse = name;
        int index = name.indexOf(".");
        if (index > 0) {
            // with scope
            String scope = name.substring(0, index);
            while (contextNode != null) {
                try {
                    JsonNode scopeNode = contextNode.findValue("scope");
                    System.out.println("scopeF: " + scopeNode.asText());
                    if (scope.equals(scopeNode.asText())) {
                        nameToUse = name.substring(index + 1, name.length());
                        break;
                    } else {
                        contextNode = contextNode.findValue("parent");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (contextNode != null && contextNode.findValue("variables") != null &&
            contextNode.findValue("variables").findValue(nameToUse) != null) {
            value = contextNode.findValue("variables").findPath(nameToUse);
        }
        return value;
    }

    private JsonNode getVariableRoot(JsonNode contextNode, String name) {
        JsonNode value = null;
        String nameToUse = name;
        int index = name.indexOf(".");
        if (index > 0) {
            // with scope
            String scope = name.substring(0, index);
            while (contextNode != null) {
                try {
                    JsonNode scopeNode = contextNode.findValue("scope");
                    System.out.println("scopeF: " + scopeNode.asText());
                    if (scope.equals(scopeNode.asText())) {
                        nameToUse = name.substring(index + 1, name.length());
                        break;
                    } else {
                        contextNode = contextNode.findValue("parent");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (contextNode != null && contextNode.findValue("variables") != null) {
            value = contextNode.findPath("variables");
        }
        return value;
    }

    private JsonNode getContextRoot() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode contextNode = mapper.valueToTree(this.getContext());
        return contextNode;
    }

    private void updateContext(JsonNode contextNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.setContext(mapper.treeToValue(contextNode, Object.class));
            this.modifyContext = true;
        } catch (JsonProcessingException e) {
        }
    }

    private JsonNode createVariable(Object value) {
        JsonNode actualObj = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            actualObj = mapper.readTree("{\"type\":\"string\", \"entity\":false, \"value\":" + value + "}");
        } catch (Exception e) {
        }
        return actualObj;
    }

    public void setVariable(String name, String value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).put("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                ((ObjectNode) variable).put(name, createVariable(value));
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public void setVariable(String name, boolean value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).put("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                System.out.println(contextNode);
                JsonNode v = createVariable(value);
                System.out.println(name + ":" + v);
                ((ObjectNode) variable).replace(name, v);
                System.out.println(contextNode);
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public void setVariable(String name, int value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).put("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                ((ObjectNode) variable).replace(name, createVariable(value));
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public void setVariable(String name, float value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).put("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                ((ObjectNode) variable).replace(name, createVariable(value));
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public void setVariable(String name, double value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).put("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                ((ObjectNode) variable).replace(name, createVariable(value));
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public void setVariable(String name, JsonNode value) {
        JsonNode contextNode = getContextRoot();
        JsonNode variable = getVariableRef(contextNode, name);
        if (variable != null && !(variable instanceof MissingNode))
            ((ObjectNode) variable).replace("value", value);
        else {
            variable = getVariableRoot(contextNode, name);
            if (variable != null && !(variable instanceof MissingNode)) {
                ((ObjectNode) variable).replace(name, createVariable(value));
            }
        }
        if (variable != null)
            updateContext(contextNode);
    }

    public Object getMessagePayload() {
        Object payload = this.getMessage().getMessagePayload();
        return payload;
    }

    public String getText() {
        String text = null;
        Object messagePayload = getMessagePayload();
        if (messagePayload != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode messagePayloadNode = mapper.valueToTree(messagePayload);
            if (messagePayloadNode != null && messagePayloadNode.findValue("text") != null) {
                text = messagePayloadNode.findValue("text").asText();
            } else {
                JsonNode postback = getPostback();
                if (postback.isTextual()) {
                    text = postback.textValue();
                }
            }
        }
        return text;
    }

    public boolean asText() {
        return (getText() != null);
    }

    public JsonNode getPostback() {
        JsonNode postback = null;
        Object messagePayload = getMessagePayload();
        if (messagePayload != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode messagePayloadNode = mapper.valueToTree(messagePayload);
            if (messagePayloadNode != null && messagePayloadNode.findValue("postback") != null) {
                postback = messagePayloadNode.findValue("postback");
            }
        }
        return postback;
    }

    public boolean asPostback() {
        return (getPostback() != null);
    }

    public JsonNode getPostback(String key) {
        JsonNode value = null;
        JsonNode postback = getPostback();
        if (postback != null) {
            value = postback.findValue(key);
        }
        return value;
    }

    public boolean asPostback(String key) {
        return (getPostback(key) != null);
    }

    public Object getProperties(String key) {
        Object value = null;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode propertiesNode = mapper.valueToTree(this.getProperties());
        if (propertiesNode != null) {
            JsonNode valueNode = propertiesNode.findValue(key);
            if (valueNode.isInt())
                value = valueNode.asInt();
            if (valueNode.isLong())
                value = valueNode.asLong();
            if (valueNode.isBoolean())
                value = valueNode.asBoolean();
            if (valueNode.isTextual())
                value = valueNode.asText();
            if (valueNode.isFloat())
                value = valueNode.asDouble();
            if (valueNode.isDouble())
                value = valueNode.asDouble();
        }
        return value;
    }

    public static Request loadRequest(String jsonString) throws JsonProcessingException, JsonMappingException {
        //JSONParser parser = new JSONParser();
        //JSONObject json = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        Request request = mapper.readValue(jsonString, Request.class);

        //json = new JSONObject(jsonString);
        //botId = (String) json.get("botId");
        //platformVersion = (String) json.get("platformVersion");
        //context = json.get("context");
        //properties = json.get("properties");
        //message = new Message((JSONObject) json.get("message"));
        return request;
    }

    public String toString() {
        String result = "";
        result += "botId:" + botId + System.lineSeparator();
        result += "platformVersion:" + platformVersion + System.lineSeparator();
        result += "context:" + context.toString() + System.lineSeparator();
        result += "properties:" + properties.toString() + System.lineSeparator();
        result += "message:" + message;
        return (result);
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getBotId() {
        return botId;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public Object getContext() {
        return context;
    }

    public void setProperties(Object properties) {
        this.properties = properties;
    }

    public Object getProperties() {
        return properties;
    }

    public void setMessage(MessageRequest message) {
        this.message = message;
    }

    public MessageRequest getMessage() {
        return message;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setExecutedStates(Object executedStates) {
        this.executedStates = executedStates;
    }

    public Object getExecutedStates() {
        return executedStates;
    }

    public boolean isModifyContext() {
        return modifyContext;
    }
}
