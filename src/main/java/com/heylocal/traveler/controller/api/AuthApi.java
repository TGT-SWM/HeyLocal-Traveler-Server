/**
 * packageName    : com.heylocal.traveler.controller.api
 * fileName       : AuthApi
 * author         : 우태균
 * date           : 2022/08/20
 * description    : 인증 API 인터페이스
 */

package com.heylocal.traveler.controller.api;

import com.heylocal.traveler.dto.ErrorMessageResponse;
import com.heylocal.traveler.exception.BadRequestException;
import com.heylocal.traveler.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.heylocal.traveler.dto.AuthTokenDto.TokenPairRequest;
import static com.heylocal.traveler.dto.AuthTokenDto.TokenPairResponse;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-08-12T04:12:44.357Z[GMT]")
@RequestMapping("/auth")
public interface AuthApi {

    @Operation(summary = "Token 재발급", description = "", tags = {"Auth"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "재발급 성공"),
        @ApiResponse(responseCode = "400", description = "- `BAD_INPUT_FORM`: 입력 값의 형식이 올바르지 않을 때", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "`NOT_EXPIRED_ACCESS_TOKEN` 와 `NOT_MATCH_PAIR` 오류가 발생한 경우, 관련된 Refresh·Access 토큰이 모두 제거된다.\n\n- `NOT_EXIST_REFRESH_TOKEN`: 해당 Refresh 토큰이 존재하지 않을 때\n\n- `EXPIRED_REFRESH_TOKEN`: Refresh Token이 만료되었을 때\n\n- `NOT_EXPIRED_ACCESS_TOKEN`: 해당 Access Token이 아직 만료되지 않았을 때\n\n- `NOT_MATCH_PAIR`: 해당 Access Token과 Refresh Token이 매치되지 않을 경우", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class)))
    })
    @PutMapping(value = "/access-token", consumes = { "application/json" })
    TokenPairResponse reissueTokenPair(
        @Parameter(in = ParameterIn.DEFAULT, description = "", required=true) @Validated @RequestBody TokenPairRequest request,
        BindingResult bindingResult) throws BadRequestException, UnauthorizedException;

}

