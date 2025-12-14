package com.ssafy.b108.walletslot.backend.domain.user.service;

import com.ssafy.b108.walletslot.backend.domain.user.dto.MeBaseDayResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MePatchRequestDto;
import com.ssafy.b108.walletslot.backend.domain.user.dto.MeResponseDto;

public interface UserService {
    MeResponseDto getMe(long userId);
    MeResponseDto patchMe(long userId, MePatchRequestDto req);
    MeBaseDayResponseDto getMeBaseDay(long userId);
}
