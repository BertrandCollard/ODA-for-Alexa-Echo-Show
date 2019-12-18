package ugbu.tinytown.alexa;

import org.json.simple.JSONObject;

public class Message {
    
    private String id;
    private String platformVersion;
    private String tenantId;
    private Object channelConversation;
    private JSONObject messagePayload;
    private JSONObject payload;
    
    public Message() {
        super();
    }
    
    public Message(Request request) {
        super();
        this.id = request.getMessage().getId();
        this.platformVersion = request.getPlatformVersion();
        this.tenantId = request.getMessage().getTenantId();
        this.channelConversation = request.getMessage().getChannelConversation();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setChannelConversation(JSONObject channelConversation) {
        this.channelConversation = channelConversation;
    }

    public Object getChannelConversation() {
        return channelConversation;
    }

    public void setMessagePayload(JSONObject messagePayload) {
        this.messagePayload = messagePayload;
    }

    public JSONObject getMessagePayload() {
        return messagePayload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return payload;
    }
    
    public void setTextConversationMessage(String text){
        JSONObject messagePayload = new JSONObject();
        messagePayload.put("type", "text");
        messagePayload.put("text", text);
        this.setMessagePayload(messagePayload);
    }
}
