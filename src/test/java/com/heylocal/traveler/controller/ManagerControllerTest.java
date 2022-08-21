package com.heylocal.traveler.controller;

import com.heylocal.traveler.controller.exception.NotFoundException;
import com.heylocal.traveler.domain.post.Post;
import com.heylocal.traveler.domain.profile.ManagerProfile;
import com.heylocal.traveler.domain.user.Manager;
import com.heylocal.traveler.domain.userreview.ManagerReview;
import com.heylocal.traveler.dto.ManagerDto.ManagerProfileResponse;
import com.heylocal.traveler.dto.ManagerDto.ManagerProfileSimpleResponse;
import com.heylocal.traveler.dto.ManagerDto.ManagerReviewResponse;
import com.heylocal.traveler.dto.PageDto.PageRequest;
import com.heylocal.traveler.service.ManagerService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

class ManagerControllerTest {
	@Mock
	private ManagerService managerService;

	@InjectMocks
	private ManagerController managerController;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("매니저 프로필 조회 컨트롤러 (simple=false)")
	void managersManagerIdProfileGetTest() {
		// GIVEN
		boolean simple = false; // 매니저의 전체 프로필 조회
		Long managerId = 1L;
		Manager manager = createManager(managerId);
		List<Post> postList = new ArrayList<>();
		ManagerProfileResponse response = ManagerProfileResponse.from(manager, postList);
		given(managerService.findProfileById(managerId)).willReturn(response);

		// WHEN - THEN
		try {
			ManagerProfileResponse actual = managerController.managersManagerIdProfileGet(managerId, simple);
			// 2. 의도한 결과를 반환하는지 (매니저 ID 일치)
			Assertions.assertThat(actual.getId()).isEqualTo(managerId);
		} catch (NotFoundException e) {
			// 1. NotFoundException이 발생하지 않는지
			Assertions.fail("예외 발생: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("매니저 프로필 조회 컨트롤러 (simple=true)")
	void managersManagerIdProfileGetSimpleTest() {
		// GIVEN
		boolean simple = true; // 매니저의 간단 프로필 조회
		Long managerId = 1L;
		Manager manager = createManager(managerId);
		ManagerProfileSimpleResponse response = ManagerProfileSimpleResponse.from(manager);
		given(managerService.findSimpleProfileById(managerId)).willReturn(response);

		// WHEN - THEN
		try {
			ManagerProfileSimpleResponse actual = managerController.managersManagerIdProfileGet(managerId, simple);
			// 2. 의도한 결과를 반환하는지 (매니저 ID 일치)
			Assertions.assertThat(actual.getId()).isEqualTo(managerId);
		} catch (NotFoundException e) {
			// 1. NotFoundException이 발생하지 않는지
			Assertions.fail("예외 발생: " + e.getMessage());
		}
	}

	@Test
	@DisplayName("매니저 리뷰 조회 컨트롤러")
	void managersManagerReviews() {
		// GIVEN
		// 매니저 리뷰 등록하도록 수정
		long managerId = 1L;
		int page = 1;
		int pageSize = 10;
		PageRequest pageRequest = new PageRequest(page, pageSize);

		// 가짜 ManagerReviewResponse 반환
		List<ManagerReviewResponse> response = new ArrayList<>();
		for (int i = 0; i < pageSize; i++) {
			response.add(new ManagerReviewResponse());
		}
		given(managerService.findReviews(managerId, pageRequest)).willReturn(response);

		// WHEN
		List<ManagerReviewResponse> actual = managerController.managersManagerReviews(managerId, pageRequest);

		// THEN
		Assertions.assertThat(actual.size()).isEqualTo(response.size());
	}

	// 매니저 객체 생성해 반환
	private Manager createManager(Long id) {
		return Manager.builder()
				.id(id)
				.realName("김현지")
				.userProfile(new ManagerProfile())
				.build();
	}
}