package com.brainstation.ib.gateway.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpecialChars {

    SPACE(' '),
    COLON(':'),
    UNDERSCORE('_'),
    HYPHEN('-'),
    FORWARD_SLASH('/'),
    BACKSLASH('\''),
    STAR('*');

    private final char character;

    public String getText() {
        return String.valueOf(character);
    }
}
