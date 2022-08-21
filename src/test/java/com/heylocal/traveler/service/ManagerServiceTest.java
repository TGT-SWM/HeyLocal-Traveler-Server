package com.heylocal.traveler.service;

import com.heylocal.traveler.domain.order.OrderSheet;
import com.heylocal.traveler.domain.profile.ManagerProfile;
import com.heylocal.traveler.domain.profile.TravelerProfile;
import com.heylocal.traveler.domain.travel.Travel;
import com.heylocal.traveler.domain.user.Manager;
import com.heylocal.traveler.domain.user.Traveler;
import com.heylocal.traveler.domain.userreview.ManagerReview;
import com.heylocal.traveler.dto.ManagerDto.ManagerProfileSimpleResponse;
import com.heylocal.traveler.dto.ManagerDto.ManagerReviewResponse;
import com.heylocal.traveler.dto.PageDto.PageRequest;
import com.heylocal.traveler.repository.ManagerRepository;
import com.heylocal.traveler.repository.ManagerReviewRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ManagerServiceTest {
	@Mock
	private ManagerRepository managerRepository;

	@Mock
	private ManagerReviewRepository managerReviewRepository;

	@InjectMocks
	private ManagerService managerService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("매니저 간단 프로필 조회")
	void findSimpleProfileById() {
		// GIVEN
		Long managerId = 1L;
		Manager manager = createManager(managerId);
		given(managerRepository.findOne(managerId)).willReturn(manager);

		// WHEN
		ManagerProfileSimpleResponse response = managerService.findSimpleProfileById(managerId);

		// THEN
		// 1. 유효한 결과 반환 (해당 ID의 매니저가 존재)
		assertThat(response).isNotNull();
		// 2. 의도한 결과 반환 (매니저 ID가 일치)
		assertThat(response.getId()).isEqualTo(managerId);
	}

	@Test
	@DisplayName("매니저 프로필 조회")
	void findProfileById() {
		// GIVEN
		Long managerId = 1L;
		Manager manager = createManager(managerId);
		given(managerRepository.findOne(managerId)).willReturn(manager);

		// WHEN
		ManagerProfileSimpleResponse response = managerService.findSimpleProfileById(managerId);

		// THEN
		// 1. 유효한 결과 반환 (해당 ID의 매니저가 존재)
		assertThat(response).isNotNull();
		// 2. 의도한 결과 반환 (매니저 ID가 일치)
		assertThat(response.getId()).isEqualTo(managerId);
	}

	@Test
	@DisplayName("매니저 리뷰 조회")
	void findReviews() {
		// GIVEN
		// 요청 객체
		long managerId = 1L;
		long notExistsManagerId = 2L;
		int page = 1;
		int pageSize = 10;
		PageRequest pageRequest = new PageRequest(page, pageSize);

		// Manager
		Manager manager = Manager.builder().id(managerId).build();

		// Traveler
		Traveler writer = Traveler.builder()
				.nickname("nickname")
				.userProfile(new TravelerProfile("imageUrl"))
				.build();

		// OrderSheet
		OrderSheet orderSheet = OrderSheet.builder().writer(writer).build();

		// Travel
		Travel travel = Travel.builder()
				.orderSheet(orderSheet)
				.manager(manager)
				.build();

		// ManagerReview
		ManagerReview managerReview = new ManagerReview(travel, null, null, null, null, null);

		// 가짜 리뷰 리스트 반환
		List<ManagerReview> reviews = new ArrayList<>();
		for (int i = 0; i < page * pageSize; i++) {
			reviews.add(managerReview);
		}
		given(managerReviewRepository.findAll(managerId, pageRequest)).willReturn(reviews);

		// WHEN
		List<ManagerReviewResponse> existsResponse = managerService.findReviews(managerId, pageRequest);
		List<ManagerReviewResponse> notExistsResponse = managerService.findReviews(notExistsManagerId, pageRequest);


		// THEN
		// 해당 페이지 조회 시 pageSize 만큼의 매니저 리뷰를 반환하는지
		Assertions.assertAll(
				// 성공 케이스 - 1 - 등록된 매니저 리뷰를 성공적으로 조회하는지
				() -> assertThat(existsResponse.size()).isEqualTo(pageSize),
				// 실패 케이스 - 1 - 매니저에게 등록된 리뷰가 존재하지 않는 경우
				() -> Assertions.assertTrue(notExistsResponse.isEmpty())
		);

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