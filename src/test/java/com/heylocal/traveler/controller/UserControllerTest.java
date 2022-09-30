package com.heylocal.traveler.controller;

import com.heylocal.traveler.dto.PageDto;
import com.heylocal.traveler.dto.PageDto.PageRequest;
import com.heylocal.traveler.dto.TravelOnDto;
import com.heylocal.traveler.dto.TravelOnDto.TravelOnSimpleResponse;
import com.heylocal.traveler.service.TravelOnService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class UserControllerTest {
	@Mock
	private TravelOnService travelOnService;

	@InjectMocks
	private UserController userController;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("작성한 여행 On 조회")
	void getUserTravelOnsTest() {
		// GIVEN (Request)
		long userId = 1L;
		long noTravelOnUserId = userId + 1;
		PageRequest pageRequest = new PageRequest(null, 10);

		// GIVEN (TravelOnSimpleResponse)
		final int travelOnCnt = 10;
		List<TravelOnSimpleResponse> travelOnResp = new ArrayList<>();
		for (int i = 0; i < travelOnCnt; i++)
			travelOnResp.add(new TravelOnSimpleResponse());

		given(travelOnService.inquirySimpleTravelOns(userId, pageRequest)).willReturn(travelOnResp);
		given(travelOnService.inquirySimpleTravelOns(noTravelOnUserId, pageRequest)).willReturn(new ArrayList<TravelOnSimpleResponse>());

		// WHEN
		List<TravelOnSimpleResponse> successResp = userController.getUserTravelOns(userId, pageRequest);
		List<TravelOnSimpleResponse> failResp = userController.getUserTravelOns(noTravelOnUserId, pageRequest);

		// THEN
		assertAll(
				// 성공 케이스 - 1 - 여행 On 조회 성공
				() -> Assertions.assertThat(successResp.size()).isEqualTo(travelOnCnt),
				// 실패 케이스 - 1 - 작성한 여행 On이 없는 경우
				() -> Assertions.assertThat(failResp).isEmpty()
		);
	}
}