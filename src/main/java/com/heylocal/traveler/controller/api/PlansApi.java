package com.heylocal.traveler.controller.api;

import com.heylocal.traveler.dto.ErrorMessageResponse;
import com.heylocal.traveler.dto.LoginUser;
import com.heylocal.traveler.dto.PlanDto.PlanListResponse;
import com.heylocal.traveler.dto.PlanDto.PlanPlacesRequest;
import com.heylocal.traveler.dto.PlanDto.PlanPlacesResponse;
import com.heylocal.traveler.dto.PlanDto.PlanRequest;
import com.heylocal.traveler.exception.controller.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RequestMapping("/plans")
public interface PlansApi {
	@Operation(summary = "작성한 플랜 조회", description = "작성한 플랜의 목록을 조회합니다.", tags = {"Plans"})
	@GetMapping()
	PlanListResponse getPlans(
			@ApiIgnore LoginUser loginUser
	);

	@Operation(summary = "플랜 등록", description = "플랜을 등록합니다.", tags = {"Plans"})
	@PostMapping()
	ResponseEntity<Void> createPlan(
			@Parameter(in = ParameterIn.DEFAULT, description = "플랜 정보", required = true) PlanRequest request
	);

	@Operation(summary = "플랜 수정", description = "플랜을 수정합니다.", tags = {"Plans"})
	@PutMapping("/{planId}")
	ResponseEntity<Void> updatePlan(
			@Parameter(in = ParameterIn.PATH, description = "플랜 ID", required = true) long planId,
			@Parameter(in = ParameterIn.DEFAULT, description = "플랜 정보", required = true) PlanRequest request
	);

	@Operation(summary = "플랜 삭제", description = "플랜을 삭제합니다.", tags = {"Plans"})
	@DeleteMapping("/{planId}")
	ResponseEntity<Void> deletePlan(
			@Parameter(in = ParameterIn.PATH, description = "플랜 ID", required = true) long planId
	);

	/*
	 * 아래는 플랜 내 장소들에 대한 컨트롤러입니다.
	 */

	@Operation(summary = "플랜의 장소 목록 조회", description = "플랜의 장소 목록 조회", tags = {"Plans"})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "플랜의 장소 목록 조회 성공"),
			@ApiResponse(responseCode = "404", description = "- `NO_INFO`: 존재하지 않는 정보일 때", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageResponse.class)))
	})
	@GetMapping("/{planId}/places")
	List<PlanPlacesResponse> getPlacesInPlan(
			@Parameter(in = ParameterIn.PATH, description = "플랜 ID", required = true) @PathVariable long planId
	) throws NotFoundException;

	@Operation(summary = "플랜의 장소 목록 수정", description = "플랜의 장소 목록 수정", tags = {"Plans"})
	@PutMapping("/{planId}/places")
	ResponseEntity<Void> updatePlaceInPlan(
			@Parameter(in = ParameterIn.PATH, description = "플랜 ID", required = true) long planId,
			@Parameter(in = ParameterIn.DEFAULT, description = "", required = true) PlanPlacesRequest request
	);
}
