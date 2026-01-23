package com.ahd.backend.carcontracts.template.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TemplateItemKind {
    FIELD, TEXT, IMAGE , SHAPE;

    @JsonCreator
    public static TemplateItemKind fromString(String v) {
        if (v == null) return null;
        return TemplateItemKind.valueOf(v.trim().toUpperCase());
    }
}
