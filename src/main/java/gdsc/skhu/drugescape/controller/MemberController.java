package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.dto.*;
import gdsc.skhu.drugescape.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Member API", description = "사용자 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "Google OAuth2 콜백", description = "Google OAuth2 callback")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("api/oauth2/callback/google")
    public ResponseEntity<TokenDTO> googleOAuth2Callback(@RequestParam("code") String code) {
        try {
            TokenDTO token = memberService.login(code);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "로그인", description = "Login")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestParam("code") String code) {
        TokenDTO token = memberService.login(code);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "로그아웃", description = "Logout")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenDTO tokenDTO) {
        memberService.logout(tokenDTO.getAccessToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입", description = "Signup")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping("/signup")
    public ResponseEntity<TokenDTO> signup(@RequestParam("code") String code) {
        String googleAccessToken = memberService.getGoogleAccessToken(code);
        MemberDTO memberDTO = memberService.getAccountDTO(googleAccessToken);
        TokenDTO token = memberService.signup(memberDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @Operation(summary = "리프레시", description = "Refresh")
    @ApiResponse(responseCode = "200", description = "OK")
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        TokenDTO newToken = memberService.refresh(tokenDTO.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }
}