package gdsc.skhu.drugescape.service;

import com.google.gson.Gson;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Role;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.domain.dto.MemberDTO;
import gdsc.skhu.drugescape.domain.dto.TokenDTO;
import gdsc.skhu.drugescape.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Type;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

@Service
public class MemberService {
    private final String GOOGLE_TOKEN_URL;
    private final String GOOGLE_CLIENT_ID;
    private final String GOOGLE_CLIENT_SECRET;
    private final String GOOGLE_REDIRECT_URI;

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final TokenBlackListService tokenBlackListService;

    public MemberService(@Value("${GOOGLE_TOKEN_URL}") String googleTokenUrl,
                         @Value("${GOOGLE_CLIENT_ID}") String googleClientId,
                         @Value("${GOOGLE_CLIENT_SECRET}") String googleClientSecret,
                         @Value("${GOOGLE_REDIRECT_URI}") String googleRedirectUri,
                         MemberRepository memberRepository,
                         TokenProvider tokenProvider,
                         TokenBlackListService tokenBlackListService) {
        this.GOOGLE_TOKEN_URL = googleTokenUrl;
        this.GOOGLE_CLIENT_ID = googleClientId;
        this.GOOGLE_CLIENT_SECRET = googleClientSecret;
        this.GOOGLE_REDIRECT_URI = googleRedirectUri;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.tokenBlackListService = tokenBlackListService;
    }

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", GOOGLE_CLIENT_ID);
        params.add("client_secret", GOOGLE_CLIENT_SECRET);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI);
        params.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String json = response.getBody();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> responseMap = gson.fromJson(json, type);
            if (responseMap != null && responseMap.containsKey("access_token")) {
                return responseMap.get("access_token");
            } else {
                throw new RuntimeException("응답에 액세스 토큰이 포함되어 있지 않습니다.");
            }
        } else {
            throw new RuntimeException("Google 액세스 토큰을 검색하지 못했습니다: " + response.getStatusCode());
        }
    }

    public MemberDTO getMemberDTO(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, MemberDTO.class);
        }
        throw new RuntimeException("사용자 정보를 검색하지 못했습니다.");
    }

    public TokenDTO googleLoginSignup(String googleAccessToken) {
        MemberDTO memberDTO = getMemberDTO(googleAccessToken);
        Member member = memberRepository.findByEmail(memberDTO.getEmail()).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(memberDTO.getEmail())
                        .name(memberDTO.getName())
                        .picture(memberDTO.getPicture())
                        .role(Role.ROLE_USER)
                        .build())
        );
        return tokenProvider.createToken(member);
    }

    public void deactivateTokens(String accessToken, String refreshToken) {
        tokenBlackListService.addToBlackList(accessToken);
        tokenBlackListService.addToBlackList(refreshToken);
    }

    public TokenDTO renewRefreshToken(String refreshToken) {
        if (tokenBlackListService.isBlackListed(refreshToken)) {
            throw new RuntimeException("블랙리스트에 포함된 새로고침 토큰입니다.");
        }
        return tokenProvider.renewToken(refreshToken);
    }

    public MemberDTO getAuthenticatedMemberInfo(Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류 발생");
        }
        Long memberId = tryParseMemberId(authentication.getName());
        return memberRepository.findById(memberId)
                .map(member -> MemberDTO.builder()
                        .name(member.getName())
                        .email(member.getEmail())
                        .picture(member.getPicture())
                        .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public void upgradeToAdminRole(Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류 발생");
        }
        Member member = getAuthenticatedMember(authentication);
        upgradeMemberRoleIfNotAdmin(member);
    }

    private Member getAuthenticatedMember(Authentication authentication) {
        Long memberId = tryParseMemberId(authentication.getName());
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));
    }

    private void upgradeMemberRoleIfNotAdmin(Member member) {
        if (member.getRole() != Role.ROLE_ADMIN) {
            member.changeToAdmin();
            memberRepository.save(member);
        }
    }

    private Long tryParseMemberId(String memberIdStr) {
        try {
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 사용자 ID 형식");
        }
    }
}