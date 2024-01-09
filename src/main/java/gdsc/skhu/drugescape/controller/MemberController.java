package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.MemberDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.dto.TokenDTO;
import gdsc.skhu.drugescape.jwt.TokenProvider;
import gdsc.skhu.drugescape.service.MemberService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequestMapping("/drugescape")
@RestController
@RequiredArgsConstructor
@Tag(name = "Member API", description = "사용자 관련 API")
public class MemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "Google OAuth2 로그인/회원가입", description = "Google OAuth2를 통한 로그인 및 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인/회원가입 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/LoginSignup")
    public TokenDTO googleLoginSignup(@RequestParam(name = "code") String code) {
        try {
            String googleAccessToken = memberService.getGoogleAccessToken(code);
            return memberService.loginOrSignUp(googleAccessToken);
        } catch (RuntimeException e) {
            log.error("Runtime Exception: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unhandled Exception: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 토큰", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenDTO tokenDTO) {
        memberService.logout(tokenDTO.getAccessToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 갱신", description = "만료된 토큰을 새로 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 만료된 토큰", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        TokenDTO newToken = memberService.refresh(tokenDTO.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }

    @Operation(summary = "프로필 조회", description = "사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(schema = @Schema(implementation = MemberDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorizationHeader.substring(7);
        if (!tokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            String userEmail = tokenProvider.getEmailFromToken(token);
            MemberDTO member = memberService.getMemberByEmail(userEmail);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}