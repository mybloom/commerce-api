package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private static final String PATTERN_USER_ID = "^[a-zA-Z0-9]{1,10}$";
    public static final String PATTERN_EMAIL = "^(?!.*\\.\\.)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PATTER_BIRTHDATE_STRING = "^\\d{4}-\\d{2}-\\d{2}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String memberId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public User(String memberId, String email, String birthDate, Gender gender) {
        validateMemberId(memberId);
        validateEmail(email);
        validateBirthDate(birthDate);
        validateGender(gender);

        this.memberId = memberId;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    private void validateGender(Gender gender) {
        if (gender == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입니다.");
        }
    }

    private void validateMemberId(String memberId) {
        if (memberId == null || !memberId.matches(PATTERN_USER_ID)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 영문 및 숫자 10자 이내여야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches(PATTERN_EMAIL)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validateBirthDate(String birthDate) {
        if (birthDate == null || !birthDate.matches(PATTER_BIRTHDATE_STRING)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일 형식은 YYYY-MM-DD 이어야 합니다.");
        }

        try {
            LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 날짜입니다.");
        }
    }
}
