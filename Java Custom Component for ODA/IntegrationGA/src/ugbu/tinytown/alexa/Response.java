package ugbu.tinytown.alexa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import ugbu.tinytown.alexa.AlexaGenericResource.SupportedAction;

public class Response {
    
    private String platformVersion;
    private Object context;
    private Object action;
    private boolean keepTurn;
    private boolean transition;
    private boolean error;
    private boolean modifyContext;
    private List<MessageResponse> messages;
    
    public Response() {
        super();
    }
    
    public Response(Request request) {
        super();
        this.platformVersion = request.getPlatformVersion();
        this.context = request.getContext();
        this.action = null;
        this.keepTurn = true;
        this.transition = false;
        this.error = false;
        this.modifyContext = request.isModifyContext();
        this.messages = new ArrayList<MessageResponse>();
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

    public void setAction(Object action) {
        this.action = action;
    }

    public Object getAction() {
        return action;
    }

    public void setKeepTurn(boolean keepTurn) {
        this.keepTurn = keepTurn;
    }

    public boolean isKeepTurn() {
        return keepTurn;
    }

    public void setTransition(boolean transition) {
        this.transition = transition;
    }

    public boolean isTransition() {
        return transition;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    public void setModifyContext(boolean modifyContext) {
        this.modifyContext = modifyContext;
    }

    public boolean isModifyContext() {
        return modifyContext;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }
    
    private void addMesssage(MessageResponse message) {
        this.messages.add(message);
    }
    
    public Response reply(Request request, String text){
        MessageResponse message = new MessageResponse(request);
        message.setTextConversationMessage(text);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    }
    
    public Response reply(Request request, String text, List<Action> actions){
        MessageResponse message = new MessageResponse(request);
        message.setTextConversationMessage(text, actions);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    }
    
    public Response reply(Request request, String text, String[] suggestions){
        MessageResponse message = new MessageResponse(request);
        List<Action> actions = new ArrayList<Action>();
        for (String suggestion : suggestions) {
            PostbackAction actionObj = new PostbackAction();
            Map<String, Object> postback2 = new HashMap<String, Object>();
            postback2.put(AlexaGenericResource.ACTION_NAME, SupportedAction.NONE);
            actionObj.setPostback(postback2);
            actionObj.setLabel(suggestion);
            actions.add(actionObj);    
        }
        message.setTextConversationMessage(text, actions);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    } 
    
    public Response reply(Request request, String text, Map<String,String> suggestions){
        MessageResponse message = new MessageResponse(request);
        List<Action> actions = new ArrayList<Action>();
        for (Map.Entry<String,String> entry : suggestions.entrySet()) {  
            PostbackAction actionObj = new PostbackAction();
            Map<String, Object> postback2 = new HashMap<String, Object>();
            postback2.put(AlexaGenericResource.ACTION_NAME, entry.getValue());
            actionObj.setPostback(postback2);
            actionObj.setLabel(entry.getKey());
            actions.add(actionObj);   
        }
        message.setTextConversationMessage(text, actions);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    } 
    
    public Response reply(Request request, CardConversation card){
        MessageResponse message = new MessageResponse(request);
        message.setCardConversationMessage(card);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    }
    
    public Response reply(Request request, Raw raw){
        MessageResponse message = new MessageResponse(request);
        message.setRawConversationMessage(raw);
        this.addMesssage(message);
        this.setKeepTurn(false);
        return this;
    }
    
    public Response transition(){
        this.transition = true;
        return this;
    }
    
    public Response transition(String transition){
        this.transition = true;
        this.setAction(transition);
        return this;
    }
    
    public Response transition(Enum transition){
        this.transition = true;
        this.setAction(transition.toString());
        return this;
    }
}
