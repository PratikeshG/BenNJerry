package migrations.stripe;

import com.google.gson.annotations.SerializedName;

public class StripeCustomer {
    private StripeCard[] cards;
    private String description;
    private String email;
    private String id;
    private String name;
    @SerializedName("default_source")
    private String defaultSource;
    private String type;

    public StripeCustomer() {
    }

    public StripeCard[] getCards() {
        return cards;
    }

    public void setCards(StripeCard[] cards) {
        this.cards = cards;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public void setDefaultSource(String defaultSource) {
        this.defaultSource = defaultSource;
    }
}