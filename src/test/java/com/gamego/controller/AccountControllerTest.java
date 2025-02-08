package com.gamego.controller;

import com.gamego.domain.account.Account;
import com.gamego.domain.account.Gender;
import com.gamego.domain.account.form.SignUpForm;
import com.gamego.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("회원 가입 화면 확인")
    void signUp() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/signup"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }


    @Test
    @DisplayName("회원가입 실패 비밀번호 불일치")
    void createAccount_fail1() throws Exception {

        ResultActions resultAction = mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 🟢 폼 데이터 전송
                .param("nickname", "shark")
                .param("email", "hyuk2000s@naver.com")
                .param("password", "12341234")
                .param("confirmPassword", "1234134")
                .param("gender", "male")
                .with(csrf()));

        resultAction
                .andExpect(status().isOk())
                .andExpect(view().name("account/signup"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원가입 실패 이메일 형식 오류")
    void createAccount_fail2() throws Exception {

        ResultActions resultAction = mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 🟢 폼 데이터 전송
                .param("nickname", "shark")
                .param("email", "hyuk2000s.com")
                .param("password", "12341234")
                .param("confirmPassword", "1234134")
                .param("gender", "male")
                .with(csrf()));

        resultAction
                .andExpect(status().isOk())
                .andExpect(view().name("account/signup"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원가입 실패 닉네임 미입력")
    void createAccount_fail() throws Exception {

        ResultActions resultAction = mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 🟢 폼 데이터 전송
                .param("nickname", "")
                .param("email", "hyuk2000s@naver.com")
                .param("password", "12341234")
                .param("confirmPassword", "1234134")
                .param("gender", "male")
                .with(csrf()));

        resultAction
                .andExpect(status().isOk())
                .andExpect(view().name("account/signup"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원가입 성공")
    void createAccount_success() throws Exception {

        ResultActions resultAction = mockMvc.perform(post("/sign-up")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 🟢 폼 데이터 전송
                .param("nickname", "shark")
                .param("email", "hyuk2000s@naver.com")
                .param("password", "12341234")
                .param("confirmPassword", "12341234")
                .param("gender", "male")
                .with(csrf()));

        resultAction
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}