/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.34).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.heylocal.traveler.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-08-12T04:12:44.357Z[GMT]")
@RequestMapping("/search")
public interface SearchApi {

    @Operation(summary = "매니저 검색", description = "지역 정보 및 테마로 매니저 검색, 모든 쿼리 파라미터에 입력된 정보가 없다면 4xx 오류 발생", tags = {"Search"})
    @GetMapping("/managers")
    ResponseEntity<Void> searchManagersGet(
        @Parameter(in = ParameterIn.QUERY, description = "시/도") @RequestParam String state,
        @Parameter(in = ParameterIn.QUERY, description = "시") @RequestParam String city,
        @Parameter(in = ParameterIn.QUERY, description = "테마 id") @RequestParam long themeId);


    @Operation(summary = "현재 가장 인기있는 여행 도시 조회", description = "")
    @GetMapping("/popular-city")
    ResponseEntity<Void> searchPopularCityGet(
        @Parameter(in = ParameterIn.QUERY, description = "조회할 페이지 번호", required = true) @RequestParam int page);


    @Operation(summary = "랜덤 매니저 조회", description = "", tags = {"Search"})
    @GetMapping("/random-managers")
    ResponseEntity<Void> searchRandomManagersGet(
        @Parameter(in = ParameterIn.QUERY, description = "조회할 페이지 번호", required = true) @RequestParam int page);


    @Operation(summary = "키워드로 테마 조회", description = "테마 추가일 내림차순으로 응답", tags = {"Search"})
    @GetMapping("/theme")
    ResponseEntity<Void> searchThemeGet(
        @Parameter(in = ParameterIn.QUERY, description = "조회할 페이지 번호", required = true) @RequestParam int page,
        @Parameter(in = ParameterIn.QUERY, description = "검색할 테마 이름 키워드 (공란가능)") @RequestParam String keyword);

}

