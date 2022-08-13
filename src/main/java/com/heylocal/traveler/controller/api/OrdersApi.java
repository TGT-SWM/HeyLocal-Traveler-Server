/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.34).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.heylocal.traveler.controller.api;

import com.heylocal.traveler.dto.Sample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-08-12T04:12:44.357Z[GMT]")
@RequestMapping("/orders")
public interface OrdersApi {

    @Operation(summary = "해당 매니저에게 특정 의뢰서로 매칭 요청", description = "", tags = {"Orders"})
    @PostMapping(value = "/request", consumes = { "application/json" })
    ResponseEntity<Void> ordersRequestPost(
        @Parameter(in = ParameterIn.DEFAULT, description = "매칭 요청할 매니저 id / 매칭 요청할 의뢰서 id", required = true) @Validated @RequestBody Sample body);


    @Operation(summary = "내 매칭 요청 현황 리스트 조회", description = "", tags = {"Orders"})
    @GetMapping("/orders/statuses")
    ResponseEntity<Void> ordersStatusesGet(
        @Parameter(in = ParameterIn.QUERY, description = "매칭 현황을 확인할 여행 의뢰서 id", required = true) @RequestParam long orderSheetId,
        @Parameter(in = ParameterIn.QUERY, description = "traveler:내가 보낸 매칭 요청, manager: 받은 매칭 요청", required = true) @RequestParam String from);

}

