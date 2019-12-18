package ugbu.tinytown.alexa;

import java.util.LinkedHashMap;

import org.json.JSONObject;

public class MessageRequest {

    private String id;
    private String platformVersion;
    private String tenantId;
    private Object channelConversation;
    private Object messagePayload;
    private Object payload;
    private int retryCount;
    private String executionContext;
    private String createdOn;
    private String type;
    private int stateCount;

    public MessageRequest() {
        super();
    }

    public MessageRequest(JSONObject json) {
        super();
        id = (String) json.get("id");
        tenantId = (String) json.get("tenantId");
        channelConversation = ((JSONObject) json.get("channelConversation"));
        messagePayload = ((JSONObject) json.get("messagePayload"));
        payload = ((JSONObject) json.get("payload"));

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

    public void setChannelConversation(Object channelConversation) {
        this.channelConversation = channelConversation;
    }

    public Object getChannelConversation() {
        return channelConversation;
    }

    public void setMessagePayload(Object messagePayload) {
        this.messagePayload = messagePayload;
    }

    public Object getMessagePayload() {
        return messagePayload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setExecutionContext(String executionContext) {
        this.executionContext = executionContext;
    }

    public String getExecutionContext() {
        return executionContext;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setStateCount(int stateCount) {
        this.stateCount = stateCount;
    }

    public int getStateCount() {
        return stateCount;
    }
}
