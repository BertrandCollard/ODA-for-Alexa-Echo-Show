package ugbu.tinytown.alexa;

import java.util.LinkedHashMap;
import java.util.List;

public class MessageResponse {
    
    private String tenantId;
    private Object channelConversation;
    private Object messagePayload;
    
    public MessageResponse() {
        super();
    }
    
    public MessageResponse(Request request) {
        super();
        this.tenantId = request.getMessage().getTenantId();
        this.channelConversation = request.getMessage().getChannelConversation();
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
    
    public MessageResponse setTextConversationMessage(String text) {
        LinkedHashMap messagePayload = new LinkedHashMap();
        messagePayload.put("type", "text");
        messagePayload.put("text", text);
        this.setMessagePayload(messagePayload);
        return this;
    }
    
    public MessageResponse setTextConversationMessage(String text, List<Action> actions) {
        LinkedHashMap messagePayload = new LinkedHashMap();
        messagePayload.put("type", "text");
        messagePayload.put("text", text);
        messagePayload.put("actions", actions);
        this.setMessagePayload(messagePayload);
        return this;
    }
    
    public MessageResponse setCardConversationMessage(CardConversation cards) {
        LinkedHashMap messagePayload = new LinkedHashMap();
        messagePayload.put("type", cards.getType());
        messagePayload.put("layout", cards.getLayout());
        messagePayload.put("cards", cards.getCards());
        this.setMessagePayload(messagePayload);
        return this;
    }
    
    public MessageResponse setRawConversationMessage(Raw raw) {
        LinkedHashMap messagePayload = new LinkedHashMap();
        messagePayload.put("type", "raw");
        messagePayload.put("payload", raw.getPayloadAsJsonNode());
        this.setMessagePayload(messagePayload);
        return this;
    }
    
}
