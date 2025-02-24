package com.gamego.validator;

import com.gamego.domain.event.Event;
import com.gamego.domain.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;


@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }


    /**
     *  1. 마감날짜가 현재시간 이후여야 함. -> isNotValidEndEnrolledAt
     *  2. 시작날짜가 등록 날짜보다 뒤여야 함. -> isNotValidStartedAt
     *  3. 시작날짜가 종료 날짜보다 앞이어야 하고 마감날짜가 종료날보다 뒤어야함. -> isNotValidEndedAt
     */
    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if(isNotValidEndEnrolledAt(eventForm)){
            errors.rejectValue("endEnrolledAt", "wrong.endEnrolledTime", "파티원 모집 마감 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndedAt(eventForm)){
            errors.rejectValue("endedAt", "wrong.endTime", "파티원 모집 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidStartedAt(eventForm)){
            errors.rejectValue("startedAt", "wrong.starTime", "파티원 모집 시작 일시를 정확히 입력하세요.");
        }
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfNumbers() < event.getNumbersOfAcceptedEnrollments()){
            errors.rejectValue("limitOfNumbers", "wrong.value", "확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }



    private boolean isNotValidStartedAt(EventForm eventForm){
        return eventForm.getStartedAt().isBefore(eventForm.getEndEnrolledAt());
    }

    private boolean isNotValidEndEnrolledAt(EventForm eventForm){
        return eventForm.getEndEnrolledAt().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndedAt(EventForm eventForm){
        return eventForm.getEndedAt().isBefore(eventForm.getStartedAt()) || eventForm.getEndedAt().isBefore(eventForm.getEndEnrolledAt());
    }
}
