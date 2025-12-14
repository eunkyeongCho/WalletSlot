package com.ssafy.b108.walletslot.backend.domain.account.service;

import com.ssafy.b108.walletslot.backend.common.dto.Header;
import com.ssafy.b108.walletslot.backend.common.util.AESUtil;
import com.ssafy.b108.walletslot.backend.common.util.LocalDateTimeFormatter;
import com.ssafy.b108.walletslot.backend.common.util.RandomNumberGenerator;
import com.ssafy.b108.walletslot.backend.domain.account.dto.*;
import com.ssafy.b108.walletslot.backend.domain.account.dto.external.*;
import com.ssafy.b108.walletslot.backend.domain.account.entity.Account;
import com.ssafy.b108.walletslot.backend.domain.account.repository.AccountRepository;
import com.ssafy.b108.walletslot.backend.domain.bank.entity.Bank;
import com.ssafy.b108.walletslot.backend.domain.bank.repository.BankRepository;
import com.ssafy.b108.walletslot.backend.domain.transaction.dto.external.SSAFYGetAccountBalanceResponseDto;
import com.ssafy.b108.walletslot.backend.domain.user.entity.Email;
import com.ssafy.b108.walletslot.backend.domain.user.entity.User;
import com.ssafy.b108.walletslot.backend.domain.user.repository.EmailRepository;
import com.ssafy.b108.walletslot.backend.domain.user.repository.UserRepository;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    // Field
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;
    private final EmailRepository emailRepository;

    @Value("${api.ssafy.finance.apiKey}")
    private String ssafyFinanceApiKey;

    private final SecretKey encryptionKey;
    private final RestTemplate restTemplate;

    // Method
    // 4-1-1
    public GetAccountsResponseDto getAccounts(Long userId, List<GetAccountsRequestDto.BankDto> bankDtos) {

        // user 조회 -> userKey 획득하기
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService]"));
        String userKey = user.getUserKey();

        // SSAFY 금융 API >>>>> 2.4.4 계좌 목록 조회
        // 요청보낼 url
        String url2 = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountList";
        
        // Header 만들기
        Map<String, String> formattedDateTime = LocalDateTimeFormatter.formatter();
        Header header = Header.builder()
                .apiName("inquireDemandDepositAccountList")
                .transmissionDate(formattedDateTime.get("date"))
                .transmissionTime(formattedDateTime.get("time"))
                .apiServiceCode("inquireDemandDepositAccountList")
                .institutionTransactionUniqueNo(formattedDateTime.get("date") + formattedDateTime.get("time") + RandomNumberGenerator.generateRandomNumber())
                .apiKey(ssafyFinanceApiKey)
                .userKey(userKey)
                .build();

        Map<String, Object> body2 = new HashMap<>();
        body2.put("Header", header);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity2 = new HttpEntity<>(body2);

        // 요청 보내기
        ResponseEntity<SSAFYGetAccountsResponseDto> httpResponse2 = restTemplate.exchange(
                url2,
                HttpMethod.POST,
                httpEntity2,
                SSAFYGetAccountsResponseDto.class
        );

        // 요청으로 들어온 bankUuid들과 매핑되는 bankCode의 Set 만들어두기
        List<String> bankUuids = new ArrayList<>();
        for(GetAccountsRequestDto.BankDto bankDto : bankDtos) {
            bankUuids.add(bankDto.getBankId());
        }

        Set<String> bankCodes = bankRepository.findCodesByUuids(bankUuids);

        for(String bankCode : bankCodes) {
            System.out.println(bankCode);
        }

        // 사용자가 선택한 은행의 계좌만 필터링
        List<AccountDto> filteredAccounts = new ArrayList<>();
        for(AccountDto accountDto : httpResponse2.getBody().getREC()) {
            if(bankCodes.contains(accountDto.getBankCode())) {
                filteredAccounts.add(accountDto);
            }
        }

        // SSAFY 금융망 API로부터 받은 응답 가지고 dto 조립
        // dto > data > accounts
        List<GetAccountsResponseDto.AccountResponseDto> accountResponseDtos = new ArrayList<>();
        for(AccountDto accountDto : filteredAccounts) {

            Bank bank = bankRepository.findByCode(accountDto.getBankCode()).orElseThrow(() -> new AppException(ErrorCode.BANK_NOT_FOUND, "AccountService - 000"));

            GetAccountsResponseDto.AccountResponseDto accountResponseDto = GetAccountsResponseDto.AccountResponseDto.builder()
                    .bankId(bank.getUuid())
                    .bankName(bank.getName())
                    .accountNo(accountDto.getAccountNo())
                    .accountBalance(accountDto.getAccountBalance())
                    .build();

            accountResponseDtos.add(accountResponseDto);
        }

        // dto
        GetAccountsResponseDto getAccountsResponseDto = GetAccountsResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 마이데이터 연동 성공")
                .data(GetAccountsResponseDto.Data.builder().accounts(accountResponseDtos).build())
                .build();

        // 응답
        return getAccountsResponseDto;
    }

    // 4-1-2
    public GetLinkedAccountListResponseDto getLinkedAccounts(Long userId) {

        // 현재 사용자의 userId != userId 이면 403
        // user테이블 돌았을 때, 존재하지 않는 userId(userRepository.findById(userId).size() == 0) 이면 404

        // 레포에서 이 사용자의 모든 계좌 가져오기
        List<Account> accountList = accountRepository.findByUser(userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]")));

        // 사용할 dto 변수 미리 선언
        GetLinkedAccountListResponseDto getLinkedAccountListResponse;

        // 조회된 게 없으면 바로 응답
        if(accountList.isEmpty()){
            getLinkedAccountListResponse = GetLinkedAccountListResponseDto.builder()
                    .success(true)
                    .message("[AccountService - 000] 연동계좌 조회 성공")
                    .build();

            return getLinkedAccountListResponse;
        }

        // dto 조립
        // dto > data > accounts
        List<AccountResponseDto> accountResponseDtoList = accountList.stream()
                .map(account -> {
                    try{
                        return AccountResponseDto.builder()
                                .accountId(account.getUuid())
                                .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                                .bankName(account.getBank().getName())
                                .bankId(account.getBank().getUuid())
                                .alias(account.getAlias())
                                .accountBalance(String.valueOf(account.getBalance()))
                                .build();
                    } catch(Exception e) {
                        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "AccountService - 000");
                    }
                })
                .toList();

        // dto
        getLinkedAccountListResponse = GetLinkedAccountListResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 연동계좌 조회 성공")
                .data(GetLinkedAccountListResponseDto.Data.builder().accounts(accountResponseDtoList).build())
                .build();

        // 응답
        return getLinkedAccountListResponse;
    }

    // 4-1-3
    public GetAccountResponseDto getAccount(long userId, String accountId) {

        // 존재하지 않는 accountId이면 404 응답
        Account account = accountRepository.findByUuid(accountId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]"));

        // 사용자의 userId != 조회한 계좌의 userId 이면 403

        // dto 조립하기
        // dto > data
        AccountResponseDto accountResponseDto;
        try{
            accountResponseDto = AccountResponseDto.builder()
                    .accountId(account.getUuid())
                    .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                    .bankName(account.getBank().getName())
                    .bankId(account.getBank().getUuid())
                    .alias(account.getAlias())
                    .accountBalance(String.valueOf(account.getBalance()))
                    .build();
        } catch(Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "[AccountService - 000]");
        }

        GetAccountResponseDto getAccountResponseDto = GetAccountResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 계좌 상세조회 성공")
                .data(accountResponseDto)
                .build();

        // 응답
        return getAccountResponseDto;
    }

    // 4-1-4
    public GetPrimaryAccountResponseDto getPrimaryAccount(long userId) {

        // account table에서 이 userId 이면서 is_primary==true인 계좌조회
        Account account = accountRepository.findByUserAndIsPrimaryTrue(
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]")))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]"));

        // account dto 말고, 그걸로 dto 말아서 응답
        // dto > data
        AccountResponseDto accountResponseDto;
        try {
            accountResponseDto = AccountResponseDto.builder()
                    .accountId(account.getUuid())
                    .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                    .bankName(account.getBank().getName())
                    .bankId(account.getBank().getUuid())
                    .alias(account.getAlias())
                    .accountBalance(String.valueOf(account.getBalance()))
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "[AccountService - 000]");
        }

        // dto
        GetPrimaryAccountResponseDto getPrimaryAccountResponseDto = GetPrimaryAccountResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 대표계좌 조회 성공")
                .data(accountResponseDto)
                .build();

        // 응답
        return getPrimaryAccountResponseDto;
    }

    /**
     * 4-1-5 계좌 잔액 조회
     */
    public GetAccountBalanceResponseDto getAccountBalance(Long userId, String accountUuid) {

        // user, account 조회 (없으면 404)
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));
        Account account = accountRepository.findByUuid(accountUuid).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));

        // userId != account userId 이면 403 응답
        if(userId != account.getUser().getId()) {
            throw new AppException(ErrorCode.FORBIDDEN, "AccountService - 000");
        }

        // SSAFY 금융 API >>>>> 2.4.6 예금주 조회
        // 사용할 userKey와 accountNo
        String userkey = user.getUserKey();
        String accountNo;
        try{
            accountNo = AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey);
        } catch(Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "AccountService - 000");
        }

        // 요청보낼 url
        String url = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountHolderName";

        // body 만들기
        Map<String, String> formattedDateTime = LocalDateTimeFormatter.formatter();
        Header header = Header.builder()
                .apiName("inquireDemandDepositAccountHolderName")
                .transmissionDate(formattedDateTime.get("date"))
                .transmissionTime(formattedDateTime.get("time"))
                .apiServiceCode("inquireDemandDepositAccountHolderName")
                .institutionTransactionUniqueNo(formattedDateTime.get("date") + formattedDateTime.get("time") + RandomNumberGenerator.generateRandomNumber())
                .apiKey(ssafyFinanceApiKey)
                .userKey(user.getUserKey())
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("Header", header);
        body.put("accountNo", accountNo);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body);

        // 요청 보내기
        ResponseEntity<SSAFYGetAccountHolderNameResponseDto> httpResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                SSAFYGetAccountHolderNameResponseDto.class
        );

        // 사용자 이름과 예금주 명이 불일치하면 403 응답
        Email email = emailRepository.findByUser(user).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));
        String emailStr = email.getEmail();
        int atIndex = emailStr.indexOf("@");
        String userName = emailStr.substring(0, atIndex);

        if(!userName.equals(httpResponse.getBody().getREC().getUserName())) {
            throw new AppException(ErrorCode.ACCOUNT_HOLDER_NAME_MISMATCH, "AccountService - 000");
        }

        // dto 조립하고 응답
        GetAccountBalanceResponseDto getAccountBalanceResponseDto = GetAccountBalanceResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 계좌 잔액 조회 성공")
                .data(GetAccountBalanceResponseDto.Data.builder().accountId(accountUuid).balance(account.getBalance()).build())
                .build();

        return getAccountBalanceResponseDto;
    }

    // 4-1-6
    public DeleteLinkedAccountResponseDto deleteLinkedAccount(long userId, String accountId) {

        // 조회 결과가 없으면 404
        Account account = accountRepository.findByUuid(accountId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]"));
        
        // 조회한 계좌가 이 userId꺼가 아니면 403
        
        // account 레포에서 삭제
        accountRepository.deleteByUuid(accountId);

        // dto 조립
        // dto > data
        AccountResponseDto accountResponseDto;
        try {
            accountResponseDto = AccountResponseDto.builder()
                    .accountId(account.getUuid())
                    .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                    .bankId(account.getBank().getUuid())
                    .bankName(account.getBank().getName())
                    .alias(account.getAlias())
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "[AccountService - 000]");
        }

        // dto
        DeleteLinkedAccountResponseDto deleteLinkedAccountResponseDto = DeleteLinkedAccountResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 계좌 삭제 성공")
                .data(accountResponseDto)
                .build();

        // 응답
        return deleteLinkedAccountResponseDto;
    }

    // 4-2-1
    public RequestVerificationResponseDto requestVerification(String userName, String bankUuid, String accountNo) {

        // user 조회 -> userKey 획득하기
        Email email = emailRepository.findByName(userName).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));

        // SSAFY 금융 API >>>>> 2.2.2 사용자 계정 조회
        // 요청보낼 url
        String url1 = "https://finopenapi.ssafy.io/ssafy/api/v1/member/search";

        Map<String, Object> body1 = new HashMap<>();
        body1.put("userId", email.getEmail());
        body1.put("apiKey", ssafyFinanceApiKey);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity1 = new HttpEntity<>(body1);

        // 요청 보내기
        ResponseEntity<SSAFYGetUserKeyResponseDto> httpResponse1 = restTemplate.exchange(
                url1,
                HttpMethod.POST,
                httpEntity1,
                SSAFYGetUserKeyResponseDto.class);

        String userKey = httpResponse1.getBody().getUserKey();

        // SSAFY 금융 API >>>>> 2.4.6 예금주 조회
        // 방금 조회한 사용자의 이름 != 예금주 명이면 403 보내야 함

        // 1원 송금받는 사용자 통장내역에 찍힐 기업명 만들기
        Bank bank = bankRepository.findByUuid(bankUuid).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 002"));
        String bankName = bank.getName();

        // SSAFY 금융 API >>>>> 2.9.1 1원 송금
        // 요청보낼 url
        String url = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/accountAuth/openAccountAuth";
        Map<String, String> formattedDateTime = LocalDateTimeFormatter.formatter();
        Header header = Header.builder()
                .apiName("openAccountAuth")
                .transmissionDate(formattedDateTime.get("date"))
                .transmissionTime(formattedDateTime.get("time"))
                .apiServiceCode("openAccountAuth")
                .institutionTransactionUniqueNo(formattedDateTime.get("date") + formattedDateTime.get("time") + RandomNumberGenerator.generateRandomNumber())
                .apiKey(ssafyFinanceApiKey)
                .userKey(userKey)
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("Header", header);
        body.put("accountNo", accountNo);
        body.put("authText", bankName);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body);

        // 요청 보내기
        ResponseEntity<SSAFYRequestVerificationResponseDto> httpResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                SSAFYRequestVerificationResponseDto.class
        );

        // 프론트한테 줄 인증번호 알아내기

        // SSAFY 금융 API >>>>> 2.4.13 계좌 거래 내역 조회(단건)
        // 요청보낼 url
        String url2 = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireTransactionHistory";
        Map<String, String> formattedDateTime2 = LocalDateTimeFormatter.formatter();
        Header header2 = Header.builder()
                .apiName("inquireTransactionHistory")
                .transmissionDate(formattedDateTime2.get("date"))
                .transmissionTime(formattedDateTime2.get("time"))
                .apiServiceCode("inquireTransactionHistory")
                .institutionTransactionUniqueNo(formattedDateTime2.get("date") + formattedDateTime2.get("time") + RandomNumberGenerator.generateRandomNumber())
                .apiKey(ssafyFinanceApiKey)
                .userKey(userKey)
                .build();

        Map<String, Object> body2 = new HashMap<>();
        body2.put("Header", header2);
        body2.put("accountNo", httpResponse.getBody().getREC().getAccountNo());
        body2.put("transactionUniqueNo", httpResponse.getBody().getREC().getTransactionUniqueNo());

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity2 = new HttpEntity<>(body2);

        // 요청 보내기
        ResponseEntity<SSAFYGetTransactionResponseDto> httpResponse2 = restTemplate.exchange(
                url2,
                HttpMethod.POST,
                httpEntity2,
                SSAFYGetTransactionResponseDto.class
        );

        // dto 조립
        RequestVerificationResponseDto requestVerificationResponseDto = RequestVerificationResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 1원인증 요청 성공")
                .data(RequestVerificationResponseDto.Data.builder().authIdentifier(httpResponse2.getBody().getREC().getTransactionSummary()).build())
                .build();

        return requestVerificationResponseDto;
    }

    // 4-2-2
    public VerifyAccountResponseDto verifyAccount(String userName, String accountNo, String authText, String authCode) {

        // user 조회 -> userKey 획득하기
        Email email = emailRepository.findByName(userName).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));

        // SSAFY 금융 API >>>>> 2.2.2 사용자 계정 조회
        // 요청보낼 url
        String url1 = "https://finopenapi.ssafy.io/ssafy/api/v1/member/search";

        Map<String, Object> body1 = new HashMap<>();
        body1.put("userId", email.getEmail());
        body1.put("apiKey", ssafyFinanceApiKey);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity1 = new HttpEntity<>(body1);

        // 요청 보내기
        ResponseEntity<SSAFYGetUserKeyResponseDto> httpResponse1 = restTemplate.exchange(
                url1,
                HttpMethod.POST,
                httpEntity1,
                SSAFYGetUserKeyResponseDto.class);

        String userKey = httpResponse1.getBody().getUserKey();

        // SSAFY 금융 API >>>>> 2.9.2 1원 송금 검증
        // 요청보낼 url
        String url = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/accountAuth/checkAuthCode";
        Map<String, String> formattedDateTime = LocalDateTimeFormatter.formatter();
        Header header = Header.builder()
                .apiName("checkAuthCode")
                .transmissionDate(formattedDateTime.get("date"))
                .transmissionTime(formattedDateTime.get("time"))
                .apiServiceCode("checkAuthCode")
                .institutionTransactionUniqueNo(formattedDateTime.get("date") + formattedDateTime.get("time") + RandomNumberGenerator.generateRandomNumber())
                .apiKey(ssafyFinanceApiKey)
                .userKey(userKey)
                .build();

        Map<String, Object> body2 = new HashMap<>();
        body2.put("Header", header);
        body2.put("accountNo", accountNo);
        body2.put("authText", authText);
        body2.put("authCode", authCode);

        // 요청보낼 http entity 만들기
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body2);

        // 요청 보내기
        ResponseEntity<SSAFYVerifyAccountResponseDto> httpResponse2 = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                SSAFYVerifyAccountResponseDto.class
        );

        // dto 만들기 (SUCCESS 키 값에 따라 분기처리)
        if(httpResponse2.getBody().getREC().getStatus().equals("SUCCESS")) {
            VerifyAccountResponseDto verifyAccountResponseDto = VerifyAccountResponseDto.builder()
                    .success(true)
                    .message("[AccountService - 000] 1원인증 검증 결과: 인증번호 일치")
                    .data(VerifyAccountResponseDto.Data.builder()
                            .accountNo(httpResponse2.getBody().getREC().getAccountNo())
                            .build())
                    .build();

            return verifyAccountResponseDto;
        } else if(httpResponse2.getBody().getREC().getStatus().equals("FAIL")) {
            VerifyAccountResponseDto verifyAccountResponseDto = VerifyAccountResponseDto.builder()
                    .success(true)
                    .message("[AccountService - 000] 1원인증 검증 결과: 인증번호 불일치")
                    .data(VerifyAccountResponseDto.Data.builder()
                            .accountNo(httpResponse2.getBody().getREC().getAccountNo())
                            .build())
                    .build();

            return verifyAccountResponseDto;
        } else {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "AccountService - 004");
        }
    }

    // 4-3-1
    public AddAccountResponseDto addAccount(long userId, List<AddAccountRequestDto.AccountDto> accounts) {

        // accounts 배열 돌면서 그때그때 Account 객체 만들고, save.
        // 그 와중에 userId의 이름 != 등록할 계좌의 예금주 이름인 account 있으면 403

        List<com.ssafy.b108.walletslot.backend.domain.account.dto.AccountDto> accountDtoList = new ArrayList<>();
        for(AddAccountRequestDto.AccountDto accountDto : accounts) {

            // user 조회 -> userKey 획득하기
            User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 005"));
            String userKey = user.getUserKey();

            // 요청보낼 url (예금주 조회)
            String url = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountHolderName";
            Map<String, String> formattedDateTime = LocalDateTimeFormatter.formatter();
            Header header = Header.builder()
                    .apiName("inquireDemandDepositAccountHolderName")
                    .transmissionDate(formattedDateTime.get("date"))
                    .transmissionTime(formattedDateTime.get("time"))
                    .apiServiceCode("inquireDemandDepositAccountHolderName")
                    .institutionTransactionUniqueNo(formattedDateTime.get("date") + formattedDateTime.get("time") + RandomNumberGenerator.generateRandomNumber())
                    .apiKey(ssafyFinanceApiKey)
                    .userKey(userKey)
                    .build();

            Map<String, Object> body = new HashMap<>();
            body.put("Header", header);
            body.put("accountNo", accountDto.getAccountNo());

            // 요청보낼 http entity 만들기
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body);

            // 요청 보내기
            ResponseEntity<SSAFYGetAccountHolderNameResponseDto> httpResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    SSAFYGetAccountHolderNameResponseDto.class
            );

            // 사용자 이름과 예금주 명이 불일치하면 403 응답
            Email email = emailRepository.findByUser(user).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 000"));
            String emailStr = email.getEmail();
            int atIndex = emailStr.indexOf("@");
            String userName = emailStr.substring(0, atIndex);

            if(!userName.equals(httpResponse.getBody().getREC().getUserName())) {
                throw new AppException(ErrorCode.ACCOUNT_HOLDER_NAME_MISMATCH, "AccountService - 000");
            }

            // SSAFY 금융망 API >>>>> 2.4.7 계좌 잔액 조회
            // 요청보낼 url
            String url2 = "https://finopenapi.ssafy.io/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountBalance";
            Map<String, String> formattedDateTime2 = LocalDateTimeFormatter.formatter();
            Header header2 = Header.builder()
                    .apiName("inquireDemandDepositAccountBalance")
                    .transmissionDate(formattedDateTime2.get("date"))
                    .transmissionTime(formattedDateTime2.get("time"))
                    .apiServiceCode("inquireDemandDepositAccountBalance")
                    .institutionTransactionUniqueNo(formattedDateTime2.get("date") + formattedDateTime2.get("time") + RandomNumberGenerator.generateRandomNumber())
                    .apiKey(ssafyFinanceApiKey)
                    .userKey(userKey)
                    .build();

            Map<String, Object> body2 = new HashMap<>();
            body2.put("Header", header2);
            body2.put("accountNo", accountDto.getAccountNo());

            // 요청보낼 http entity 만들기
            HttpEntity<Map<String, Object>> httpEntity2 = new HttpEntity<>(body2);

            // 요청 보내기
            ResponseEntity<SSAFYGetAccountBalanceResponseDto> httpResponse2 = restTemplate.exchange(
                    url2,
                    HttpMethod.POST,
                    httpEntity2,
                    SSAFYGetAccountBalanceResponseDto.class
            );

            // 잔액 데이터 확보
            Long balance = httpResponse2.getBody().getREC().getAccountBalance();

            // Bank 객체 조회하기 (없으면 404)
            Bank bank = bankRepository.findByUuid(accountDto.getBankId()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "AccountService - 002"));

            // 계좌번호 암호화
            String encryptedAccountNo;
            try {
                encryptedAccountNo = AESUtil.encrypt(accountDto.getAccountNo(), encryptionKey);
            } catch(Exception e) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "AccountService - 000");
            }

            // Account 객체 만들기
            Account account = Account.builder()
                    .user(user)
                    .bank(bank)
                    .encryptedAccountNo(encryptedAccountNo)
                    .balance(balance)
                    .lastSyncedAt(LocalDateTime.now())
                    .build();

            // Account 객체 저장
            accountRepository.save(account);

            // dto 조립하기
            // dto > data > accounts
            com.ssafy.b108.walletslot.backend.domain.account.dto.AccountDto accountDto2;
            try {
                accountDto2 = AccountDto.builder()
                        .accountId(account.getUuid())
                        .bankCode(account.getBank().getCode())
                        .bankName(account.getBank().getName())
                        .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                        .build();
            } catch(Exception e) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "AccountService - 000");
            }
            accountDtoList.add(accountDto2);
        } // 연동요청 받은 모든 계좌들에 대해 Account 객체로 만들어서 account 테이블에 저장 완료.

        // dto 조립하기
        AddAccountResponseDto addAccountResponseDto = AddAccountResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 계좌연동 성공")
                .data(AddAccountResponseDto.Data.builder().accounts(accountDtoList).build())
                .build();

        return addAccountResponseDto;
    }

    // 4-3-2
    public ModifyAccountResponseDto modifyAccount(Long userId, String accountId, ModifyAccountRequestDto request) {

        // account 객체 조회 (없으면 404)
        Account account = accountRepository.findByUuid(accountId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "[AccountService - 000]"));

        // userId != account의 userId이면 403 응답
        if(userId != account.getUser().getId()) {
            new AppException(ErrorCode.FORBIDDEN, "[AccountService - 000]");
        }

        // request 돌면서 null 아닌거 있으면 update 메서드 호출
        if(request.getAlias() != null) {
            account.updateAlias(request.getAlias());
        }

        if(request.getIsPrimary() != null) {
            account.updateIsPrimary(request.getIsPrimary());
        }

        // dto 조립
        // dto > data
        AccountResponseDto accountResponseDto;
        try {
            accountResponseDto = AccountResponseDto.builder()
                    .accountId(account.getUuid())
                    .bankId(account.getBank().getUuid())
                    .bankName(account.getBank().getName())
                    .accountNo(AESUtil.decrypt(account.getEncryptedAccountNo(), encryptionKey))
                    .build();
        } catch(Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "[AccountService - 000]");
        }

        // dto
        ModifyAccountResponseDto modifyAccountResponseDto = ModifyAccountResponseDto.builder()
                .success(true)
                .message("[AccountService - 000] 계좌정보 수정 성공")
                .data(accountResponseDto)
                .build();

        // 응답
        return modifyAccountResponseDto;
    }
}
