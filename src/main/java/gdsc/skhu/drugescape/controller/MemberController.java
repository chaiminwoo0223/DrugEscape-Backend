package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.MemberDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.dto.TokenDTO;
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

import java.security.Principal;

@Slf4j
@RequestMapping("/drugescape")
@RestController
@RequiredArgsConstructor
@Tag(name = "Member API", description = "사용자 관련 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "로그인/회원가입", description = "Google OAuth2를 통한 로그인 및 회원가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인/회원가입 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/login") // 다이렉트 로그인 추가 구현
    public ResponseEntity<?> loginOrSignUp(TokenDTO tokenDTO) {
        try {
            return ResponseEntity.ok(tokenDTO);
        } catch (RuntimeException e) {
            log.error("런타임 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("처리되지 않은 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.", e);
        }
    }

    @Operation(summary = "로그아웃", description = "사용자의 현재 세션을 종료하고 토큰을 무효화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 토큰 형식", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody TokenDTO tokenDTO) {
        try {
            memberService.deactivateTokens(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("로그아웃 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "토큰 갱신", description = "만료된 액세스 토큰에 대해 새 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 만료된 토큰", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        try {
            TokenDTO newToken = memberService.renewRefreshToken(tokenDTO.getRefreshToken());
            return ResponseEntity.ok(newToken);
        } catch (Exception e) {
            log.error("토큰 갱신 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "콜백", description = "Google OAuth2를 통해 리디렉트된 요청을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콜백 처리 성공, 사용자는 성공적으로 인증되었으며 액세스 토큰이 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청, 요청에 필요한 'code' 매개변수가 누락되었거나 잘못된 값이 제공되었습니다.", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패, 제공된 'code'가 유효하지 않거나 만료되었습니다.", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류, Google OAuth2 처리 중 예기치 않은 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/callback")
    public ResponseEntity<?> googleOAuth2Callback(@RequestParam(name = "code") String code) {
        try {
            String googleAccessToken = memberService.getGoogleAccessToken(code);
            TokenDTO tokenDTO = memberService.googleLoginSignup(googleAccessToken);
            return ResponseEntity.ok(tokenDTO);
        } catch (RuntimeException e) {
            log.error("런타임 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("처리되지 않은 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.", e);
        }
    }

    @Operation(summary = "메인 페이지", description = "메인 페이지로 이동하며, 인증된 사용자의 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메인 페이지로 이동 성공, 사용자 정보 포함", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/main")
    public ResponseEntity<?> main(Principal principal) {
        if (principal == null) {
            log.warn("Principal 객체가 null입니다. 사용자가 인증되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseErrorDTO("인증되지 않은 사용자", HttpStatus.UNAUTHORIZED.value()));
        }
        try {
            MemberDTO memberInfo = memberService.getAuthenticatedMemberInfo(principal);
            return ResponseEntity.ok(memberInfo);
        } catch (ResponseStatusException e) {
            log.warn("메인 페이지 요청 처리 중 예외 발생: 상태 코드 = {}, 이유 = {}", e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ResponseErrorDTO(e.getReason(), e.getStatusCode().value()));
        } catch (Exception e) {
            log.error("메인 페이지 요청 처리 중 예기치 않은 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseErrorDTO("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}