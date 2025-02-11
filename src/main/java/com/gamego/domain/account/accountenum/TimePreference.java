package com.gamego.domain.account.accountenum;

public enum TimePreference {
    WORKER("출근러"), MORNING("아침형"),
    NIGHT("야행성"), TIME_RICH("시간부자");

    private final String value;

    TimePreference(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    /**
     * 전달받은 value와 일치하는 TimePreference를 반환 한다.
     * @param value
     * @return
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
