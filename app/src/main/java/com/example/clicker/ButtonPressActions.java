package com.example.clicker;

public enum ButtonPressActions {

    CATCH(ContactType.CATCH),
    CONTACT(ContactType.CONTACT),
    FOLLOW(ContactType.FOLLOW),
    FOLLOW_ON_BLADES(ContactType.FOLLOW),
    FOLLOW_ON_RUBBER(ContactType.FOLLOW);

    private final ContactType type;

    ButtonPressActions(ContactType type) {
        this.type = type;
    }

    public final ContactType getType() { return type; }
}
