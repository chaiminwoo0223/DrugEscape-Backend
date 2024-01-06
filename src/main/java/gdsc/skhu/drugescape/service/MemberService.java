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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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
    private final PasswordEncoder passwordEncoder;
    private final TokenBlackListService tokenBlackListService;

    public MemberService(@Value("${GOOGLE_TOKEN_URL}") String googleTokenUrl,
                         @Value("${GOOGLE_CLIENT_ID}") String googleClientId,
                         @Value("${GOOGLE_CLIENT_SECRET}") String googleClientSecret,
                         @Value("${GOOGLE_REDIRECT_URI}") String googleRedirectUri,
                         MemberRepository memberRepository,
                         TokenProvider tokenProvider,
                         PasswordEncoder passwordEncoder,
                         TokenBlackListService tokenBlackListService) {
        this.GOOGLE_TOKEN_URL = googleTokenUrl;
        this.GOOGLE_CLIENT_ID = googleClientId;
        this.GOOGLE_CLIENT_SECRET = googleClientSecret;
        this.GOOGLE_REDIRECT_URI = googleRedirectUri;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlackListService = tokenBlackListService;
    }

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = Map.of(
                "code", code,
                "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
                "client_id", GOOGLE_CLIENT_ID,
                "client_secret", GOOGLE_CLIENT_SECRET,
                "redirect_uri", GOOGLE_REDIRECT_URI,
                "grant_type", "authorization_code"
        );
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String json = responseEntity.getBody();
            Gson gson = new Gson();
            return gson.fromJson(json, TokenDTO.class)
                    .getAccessToken();
        }
        throw new RuntimeException("구글 엑세스 토큰을 가져오는데 실패했습니다.");
    }

    public MemberDTO getAccountDTO(String accessToken) {
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

        throw new RuntimeException("유저 정보를 가져오는데 실패했습니다.");
    }

    public TokenDTO login(String code) {
        String googleAccessToken = getGoogleAccessToken(code);  // 구글에서 액세스 토큰을 얻음
        MemberDTO memberDTO = getAccountDTO(googleAccessToken); // 액세스 토큰으로부터 사용자 정보를 얻음
        Optional<Member> existingMember = memberRepository.findByEmail(memberDTO.getEmail()); // 이메일로 기존 회원 조회
        if (existingMember.isPresent()) {
            return tokenProvider.createToken(existingMember.get()); // 이미 존재하는 사용자인 경우, 토큰 생성하여 로그인 처리
        } else {
            return signup(memberDTO); // 회원가입 메소드 호출
        }
    }

    public void logout(String token) {
        tokenBlackListService.addToBlackList(token);
    }

    public TokenDTO signup(MemberDTO memberDTO) {
        String hashedPassword = passwordEncoder.encode(memberDTO.getPassword());
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        Member newMember = memberRepository.save(Member.builder()
                .name(memberDTO.getName())
                .email(memberDTO.getEmail())
                .password(hashedPassword)
                .picture(memberDTO.getPicture())
                .role(Role.USER)
                .build());
        return tokenProvider.createToken(newMember);
    }

    public TokenDTO refresh(String expiredToken) {
        return tokenProvider.refreshToken(expiredToken);
    }
}