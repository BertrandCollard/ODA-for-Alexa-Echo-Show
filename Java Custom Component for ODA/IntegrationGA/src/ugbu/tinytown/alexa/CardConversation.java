package ugbu.tinytown.alexa;

import java.util.ArrayList;
import java.util.List;

public class CardConversation {
    
    private String type = "card";
    private String layout; //'horizontal', 'vertical'
    private List<Card> cards;
    private List<Action> actions;
    private List<Action> globalActions;
    
    public CardConversation() {
        super();
    }

    public String getType() {
        return type;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
    }
    
    public void addCard(Card card){
        if (cards == null) cards = new ArrayList<Card>();
        cards.add(card);
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Action> getActions() {
        return actions;
    }
    
    public void addAction(Action action){
        if (actions == null) actions = new ArrayList<Action>();
        actions.add(action);
    }

    public void setGlobalActions(List<Action> globalActions) {
        this.globalActions = globalActions;
    }

    public List<Action> getGlobalActions() {
        return globalActions;
    }
    
    public void addGlobalActions(Action action){
        if (globalActions == null) globalActions = new ArrayList<Action>();
        globalActions.add(action);
    }

}
