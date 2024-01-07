package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.dto.*;
import gdsc.skhu.drugescape.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RequestMapping("/api/oauth2")
@RestController
@RequiredArgsConstructor
@Tag(name = "Member API", description = "사용자 관련 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "Google OAuth2 로그인/회원가입", description = "Google OAuth2를 통한 로그인 및 회원가입을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그인/회원가입 성공")
    @GetMapping("/LoginSignup")
    public TokenDTO googleLoginSignup(@RequestParam(name = "code") String code) {
        try {
            String googleAccessToken = memberService.getGoogleAccessToken(code);
            return memberService.loginOrSignUp(googleAccessToken);
        } catch (Exception e) {
            throw new RuntimeException("로그인/회원가입 처리 중 오류 발생", e);
        }
    }

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 시킵니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenDTO tokenDTO) {
        memberService.logout(tokenDTO.getAccessToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 갱신", description = "만료된 토큰을 새로 갱신합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        TokenDTO newToken = memberService.refresh(tokenDTO.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }
}