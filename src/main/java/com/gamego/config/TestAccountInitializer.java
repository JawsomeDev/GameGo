package com.gamego.config;
import com.gamego.domain.account.Account;
import com.gamego.domain.account.Gender;
import com.gamego.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestAccountInitializer {

    @Bean
    public CommandLineRunner createTestAccount(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "test@email.com";
            if (!accountRepository.existsByEmail(email)) {  // 이미 계정이 있으면 생성 안 함
                Account account = new Account();  // TODO 실제 배포 시엔 더미 데이터를 삭제하고 Account 테이블의 기본생성사 protected 로 변환
                account.setEmail(email);
                account.setNickname("test");
                account.setPassword(passwordEncoder.encode("12341234"));
                account.setGender(Gender.male);
                account.setEmailVerified(false);


                accountRepository.save(account);
                System.out.println("✅ 테스트 계정 생성 완료! (이메일: " + email + ")");
            } else {
                System.out.println("⚠ 테스트 계정 이미 존재!");
            }
        };
    }
}
