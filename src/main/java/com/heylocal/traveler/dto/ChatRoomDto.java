/**
 * packageName    : com.heylocal.traveler.dto
 * fileName       : ChatRoomDto
 * author         : 신우진
 * date           : 2022/08/28
 * description    : 채팅방 관련 DTO
 */

package com.heylocal.traveler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class ChatRoomDto {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "채팅방 정보 응답 DTO")
	public static class ChatRoomResponse {
		long id;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "채팅 메시지 응답 DTO")
	public static class ChatMessageResponse {
		long id;
	}
}
