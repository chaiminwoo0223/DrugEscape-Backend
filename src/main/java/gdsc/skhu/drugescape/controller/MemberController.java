package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.dto.TokenDTO;
import gdsc.skhu.drugescape.service.MemberService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

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
    @GetMapping("/LoginSignup") // 요청 방식을 구체화 Get or Post or Request
    public ResponseEntity<?> googleLoginSignup(@RequestParam(name = "code") String code) {
        try {
            String googleAccessToken = memberService.getGoogleAccessToken(code);
            TokenDTO tokenDTO = memberService.loginOrSignUp(googleAccessToken);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://localhost:8080/drugescape/main"));
            return new ResponseEntity<>(tokenDTO, headers, HttpStatus.FOUND);
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
        memberService.logout(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 갱신", description = "만료된 액세스 토큰에 대해 새 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 만료된 토큰", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        TokenDTO newToken = memberService.refresh(tokenDTO.getRefreshToken());
        return ResponseEntity.ok(newToken);
    }

    @Operation(summary = "메인 페이지로 이동", description = "메인 페이지로 이동합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메인 페이지로 이동 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/main")
    public String Main() {
        return "메인 페이지로 넘어갑니다.";
    }
}