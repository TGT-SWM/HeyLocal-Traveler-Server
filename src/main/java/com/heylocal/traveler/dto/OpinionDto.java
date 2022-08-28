package com.heylocal.traveler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class OpinionDto {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "답변 생성을 위한 요청 DTO")
	public static class OpinionRequest {
		long id;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "여행 On에 대한 답변 응답 DTO")
	public static class OpinionResponse {
		long id;
	}
}
