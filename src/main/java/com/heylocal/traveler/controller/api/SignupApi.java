/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.34).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.heylocal.traveler.controller.api;

import com.heylocal.traveler.dto.ErrorMessageResponse;
import com.heylocal.traveler.exception.controller.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.heylocal.traveler.dto.SignupDto.SignupRequest;
import static com.heylocal.traveler.dto.SignupDto.UserInfoCheckResponse;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-08-12T04:12:44.357Z[GMT]")
@RequestMapping("/signup")
public interface SignupApi {

    @Operation(summary = "아이디 중복 확인", description = "", tags = {"Signup"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "잘못된 아이디 형식", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class)))
    })
    @GetMapping("/accountid")
    UserInfoCheckResponse signupIdGet(
        @Parameter(in = ParameterIn.QUERY, description = "확인할 아이디", required = true) @RequestParam String accountId) throws BadRequestException;


    @Operation(summary = "전화번호 중복 확인 및 매니저로 등록되어 있는지 확인", description = "서비스 관리자도 중복 불가능", tags = {"Signup"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "잘못된 전화번호 형식이거나 중복인 경우", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class)))
    })
    @GetMapping("/phone-num")
    UserInfoCheckResponse signupPhoneNumGet(
        @Parameter(in = ParameterIn.QUERY, description = "확인할 전화번호 (하이픈 필수)", required = true) @RequestParam String phoneNumber) throws BadRequestException;


    @Operation(summary = "회원가입", description = "", tags = {"Signup"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "입력 값이 잘못된 형식이거나 중복인 경우", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = { "application/json" })
    void signupPost(
        @Parameter(in = ParameterIn.DEFAULT, description = "", required = true) @Validated @RequestBody SignupRequest request,
        BindingResult bindingResult) throws BadRequestException;

}

