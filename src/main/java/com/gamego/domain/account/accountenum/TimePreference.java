package com.gamego.domain.account.accountenum;

public enum TimePreference {
    MORNING("오전"), NIGHT("오후"), DAWN("새벽");

    private final String value;

    TimePreference(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    /**
     * 전달받은 value와 일치하는 TimePreference를 반환
     */
    public static TimePreference fromValue(String value) {
        if(value == null || value.trim().isEmpty()){
            return null;
        }
        for (TimePreference t : TimePreference.values()) {
            if(t.getValue().equals(value)){
                return t;
            }
        }
        return null;
    }
}
