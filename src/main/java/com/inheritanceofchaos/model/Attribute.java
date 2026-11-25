package com.inheritanceofchaos.model;

// Stats the player can improve with the points from leveling up.
public enum Attribute {
    VITALITY("Vitality", "+10 max HP"),
    STRENGTH("Strength", "+2 damage"),
    ENDURANCE("Endurance", "+1 defense"),
    SPIRIT("Spirit", "+8 max MP");

    private String title;
    private String effect;

    Attribute(String title, String effect) {
        this.title = title;
        this.effect = effect;
    }

    public String getTitle() {
        return title;
    }

    public String getEffect() {
        return effect;
    }
}
