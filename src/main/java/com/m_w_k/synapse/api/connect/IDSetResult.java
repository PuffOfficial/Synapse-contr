package com.m_w_k.synapse.api.connect;

public enum IDSetResult {
    SUCCESS, SUCCESS_UNCHANGED, FAIL_CHILD_CONFLICT, FAIL_SPECIAL_CODE, FAIL;

    public boolean success() {
        return this == SUCCESS || this == SUCCESS_UNCHANGED;
    }

    public String failTranslation() {
        return switch (this) {
            case FAIL_CHILD_CONFLICT -> "synapse.menu.id.fail_conflict";
            case FAIL_SPECIAL_CODE -> "synapse.menu.id.fail_code";
            case FAIL -> "synapse.menu.id.fail";
            default -> "";
        };
    }
}
