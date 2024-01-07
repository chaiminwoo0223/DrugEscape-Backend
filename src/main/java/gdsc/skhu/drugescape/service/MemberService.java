package gdsc.skhu.drugescape.service;

import com.google.gson.Gson;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Role;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.dto.MemberDTO;
import gdsc.skhu.drugescape.dto.TokenDTO;
import gdsc.skhu.drugescape.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.util.Map;
import java.util.Optional;

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

    public TokenDTO loginOrSignupWithGoogle(String code) {
        try {
            String accessToken = fetchAccessToken(code);
            MemberDTO memberDTO = fetchMemberFromGoogle(accessToken);
            return processMember(memberDTO);
        } catch (Exception e) {
            throw new RuntimeException("Google 로그인/가입 처리 중 오류 발생", e);
        }
    }

    private String fetchAccessToken(String code) {
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
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> responseMap = gson.fromJson(response.getBody(), type);
            if (responseMap != null && responseMap.containsKey("access_token")) {
                return responseMap.get("access_token");
            } else {
                throw new RuntimeException("응답에서 엑세스 토큰을 찾을 수 없습니다. 응답: " + response.getBody());
            }
        } else {
            throw new RuntimeException("Google 엑세스 토큰 요청 실패: 상태 코드 " + response.getStatusCode());
        }
    }

    private MemberDTO fetchMemberFromGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            Gson gson = new Gson();
            return gson.fromJson(response.getBody(), MemberDTO.class);
        } else {
            throw new RuntimeException("Google 사용자 정보 요청 실패: 상태 코드 " + response.getStatusCode());
        }
    }

    private TokenDTO processMember(MemberDTO memberDTO) {
        Optional<Member> existingMember = memberRepository.findByEmail(memberDTO.getEmail());
        Member member = existingMember.orElseGet(() -> registerNewMember(memberDTO));
        return tokenProvider.createToken(member);
    }

    private Member registerNewMember(MemberDTO memberDTO) {
        Member member = Member.builder()
                .name(memberDTO.getName())
                .email(memberDTO.getEmail())
                .picture(memberDTO.getPicture())
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    public void logout(String accessToken) {
        tokenBlackListService.addToBlackList(accessToken);
    }

    public TokenDTO refresh(String refreshToken) {
        return tokenProvider.refreshToken(refreshToken);
    }
}