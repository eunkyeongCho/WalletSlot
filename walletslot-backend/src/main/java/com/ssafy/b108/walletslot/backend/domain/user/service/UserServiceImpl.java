package com.ssafy.b108.walletslot.backend.domain.user.service;

import com.ssafy.b108.walletslot.backend.domain.user.dto.MeBaseDayResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MePatchRequestDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MeResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.UserDtoAssembler;
import com.ssafy.b108.walletslot.backend.domain.user.entity.Email;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.EmailRepository;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final EmailRepository emailRepo;
    private final VerificationService verificationService; // 연락처 변경 토큰 검증 훅

    // ===== 조회 =====
    @Override
    @Transactional(readOnly = true)
    public MeResponseDto getMe(long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "user"));

        String currentEmail = resolveCurrentEmail(u);
        return UserDtoAssembler.toMeResponse(u, currentEmail);
    }

    // ===== 수정(통합 PATCH) =====
    @Override
    public MeResponseDto patchMe(long userId, MePatchRequestDto r) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "user"));

        if (r.getName() != null)             u.changeName(r.getName());
        if (r.getBirthDate() != null)        u.changeBirthDate(r.getBirthDate().atStartOfDay());
        if (r.getGender() != null)           u.changeGender(User.Gender.valueOf(r.getGender()));
        if (r.getJob() != null)              u.changeJob(User.Job.valueOf(r.getJob()));
        if (r.getBaseDay() != null)          u.updateBaseDay(r.getBaseDay().shortValue());

        // 전화번호 변경: 토큰 필수
        if (r.getPhoneNumber() != null) {
            verificationService.assertPhoneToken(userId, r.getPhoneNumber(), r.getPhoneVerificationToken());
            u.changePhoneNumber(r.getPhoneNumber());
        }

        // 이메일 변경: 토큰 검증 → 기존 primary 해제 → 동일 주소 있으면 승격, 없으면 append(primary)
        if (r.getEmail() != null) {
            verificationService.assertEmailToken(userId, r.getEmail(), r.getEmailVerificationToken());

            // 1) 기존 기본 해제
            emailRepo.clearPrimary(u);

            // 2) 동일 주소 이력 있으면 재사용, 없으면 append
            Email target = emailRepo.findByUserAndEmail(u, r.getEmail())
                    .orElseGet(() -> emailRepo.save(Email.builder()
                            .user(u)
                            .name(u.getName())
                            .email(r.getEmail())
                            .build()));

            // 3) 기본 지정(+ 필요시 인증시각 갱신)
            target.markPrimary();
            target.verifyNow();
        }

        String currentEmail = resolveCurrentEmail(u);
        return UserDtoAssembler.toMeResponse(u, currentEmail, "수정 성공");
    }

    @Override
    @Transactional(readOnly = true)
    public MeBaseDayResponseDto getMeBaseDay(long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "user"));

        Integer baseDay = (u.getBaseDay() == null ? null : u.getBaseDay().intValue());

        return MeBaseDayResponseDto.builder()
                .success(true)
                .message("기준일 조회 성공")
                .data(MeBaseDayResponseDto.Data.builder()
                        .baseDay(baseDay)
                        .build())
                .build();
    }

    // ===== 내부 유틸 =====
    private String resolveCurrentEmail(User u) {
        return emailRepo.findFirstByUserAndPrimaryTrue(u)              // Optional<Email>
                .or(() -> emailRepo.findFirstByUserIdOrderByIdDesc(u.getId())) // Optional<Email>
                .map(Email::getEmail)                                  // Optional<String>
                .orElse(null);
    }
}
