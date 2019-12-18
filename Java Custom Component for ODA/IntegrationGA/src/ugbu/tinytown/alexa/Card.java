package ugbu.tinytown.alexa;

import java.util.ArrayList;
import java.util.List;

public class Card {
    
    private String title;
    private String description;
    private String imageUrl;
    private String url;
    private List<Action> actions;
    
    public Card() {
        super();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
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
}
