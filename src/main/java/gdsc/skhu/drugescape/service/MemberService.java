package gdsc.skhu.drugescape.service;

import com.google.gson.Gson;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Role;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.domain.dto.MemberDTO;
import gdsc.skhu.drugescape.domain.dto.TokenDTO;
import gdsc.skhu.drugescape.jwt.TokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.net.URI;
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
                throw new RuntimeException("Response does not contain access token");
            }
        } else {
            throw new RuntimeException("Failed to retrieve Google access token: " + response.getStatusCode());
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
        throw new RuntimeException("Failed to retrieve member information.");
    }

    @Cacheable(value = "members", key = "#memberId")
    public Member getMemberDetails(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
    }

    public TokenDTO googleLoginSignup(String googleAccessToken) {
        MemberDTO memberDTO = getMemberDTO(googleAccessToken);
        if (Boolean.FALSE.equals(memberDTO.getVerifiedEmail())) {
            throw new RuntimeException("This member does not have email verification.");
        }
        Member member = memberRepository.findByEmail(memberDTO.getEmail()).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(memberDTO.getEmail())
                        .name(memberDTO.getName())
                        .picture(memberDTO.getPicture())
                        .role(Role.USER)
                        .build())
        );
        return tokenProvider.createToken(member);
    }

    public void deactivateTokens(String accessToken, String refreshToken) {
        tokenBlackListService.addToBlackList(accessToken);
        tokenBlackListService.addToBlackList(refreshToken);
    }

    public TokenDTO refreshAccessToken(String refreshToken) {
        if (tokenBlackListService.isBlackListed(refreshToken)) {
            throw new RuntimeException("This is a refresh token included in the blacklist.");
        }
        return tokenProvider.refreshToken(refreshToken);
    }
}