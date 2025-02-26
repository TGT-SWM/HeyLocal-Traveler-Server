package com.heylocal.traveler.repository;

import com.heylocal.traveler.domain.Region;
import com.heylocal.traveler.domain.place.Place;
import com.heylocal.traveler.domain.place.PlaceCategory;
import com.heylocal.traveler.domain.travelon.*;
import com.heylocal.traveler.domain.travelon.list.*;
import com.heylocal.traveler.domain.travelon.opinion.EvaluationDegree;
import com.heylocal.traveler.domain.travelon.opinion.Opinion;
import com.heylocal.traveler.domain.user.User;
import com.heylocal.traveler.domain.user.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.heylocal.traveler.dto.TravelOnDto.TravelOnSortType;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Import(TravelOnRepository.class)
@DataJpaTest
class TravelOnRepositoryTest {
  @Autowired
  private EntityManager em;
  @Autowired
  private TravelOnRepository travelOnRepository;
  private long placeId = 0;

  @Test
  @DisplayName("여행 On 저장 성공")
  void addTravelOnSucceedTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccount")
        .nickname("testNickname")
        .password("encodedPassword")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);
    TravelOn travelOn = getNotPersistedTravelOn(author);

    //WHEN
    travelOnRepository.saveTravelOn(travelOn);

    //THEN
    TravelOn result = em.find(TravelOn.class, travelOn.getId());

    assertAll(
        //성공 케이스 - 1 - SQL Flush 성공
        () -> assertDoesNotThrow(() -> em.flush()),
        //성공 케이스 - 2 - 결과 확인
        () -> assertSame(travelOn, result)
    );
  }

  @Test
  @DisplayName("여행 On 저장 실패 - 존재하지 않는 유저가 작성한 경우")
  void addTravelOnInvalidAuthorTest() {
    //GIVEN
    User author = null;
    TravelOn travelOn = getNotPersistedTravelOn(author);

    //WHEN
    travelOnRepository.saveTravelOn(travelOn);

    //THEN
    TravelOn result = em.find(TravelOn.class, travelOn.getId());

    //실패 케이스 - 1 - SQL Flush 실패
    assertThrows(Exception.class, () -> em.flush());
  }

  @Test
  @DisplayName("여행On 목록 조회")
  void findAllTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String city2A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";
    String city2B = "city2B";

    //여행On 저장
    TravelOn result1 = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn result2 = saveTravelOn(author, stateA, city2A, LocalDateTime.now().minusHours(3), 2);
    TravelOn result3 = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(2), 3);
    TravelOn result4 = saveTravelOn(author, stateB, city2B, LocalDateTime.now().minusHours(1), 4);

    //답변 저장
    saveAnotherOpinion(result1);
    saveAnotherOpinion(result1);

    //WHEN
    List<TravelOn> onlyFirstItem = travelOnRepository.findAll(null, 1, TravelOnSortType.DATE);
    List<TravelOn> sortByCreatedDateTime = travelOnRepository.findAll(null, 4, TravelOnSortType.DATE);
    List<TravelOn> sortByViews = travelOnRepository.findAll(null, 4, TravelOnSortType.VIEWS);
    List<TravelOn> last2Item = travelOnRepository.findAll(result3.getId(), 4, TravelOnSortType.VIEWS);
    List<TravelOn> sortByOpinion = travelOnRepository.findAll(null, 4, TravelOnSortType.OPINIONS);

    //THEN
    assertAll(
        //성공 케이스 - 1 - 페이징 아이템 갯수
        () -> assertSame(1, onlyFirstItem.size()),
        //성공 케이스 - 2 - 작성일 순 정렬
        () -> assertSame(result4, sortByCreatedDateTime.get(0)),
        //성공 케이스 - 3 - 조회수 순 정렬
        () -> assertSame(4, sortByViews.get(0).getViews()),
        //성공 케이스 - 4 - 페이징 & 조회수 순 정렬
        () -> assertSame(result2, last2Item.get(0)),
        //성공 케이스 - 5 - 답변 개수 확인
        () -> assertSame(2, sortByOpinion.get(0).getOpinionList().size()),
        //성공 케이스 - 6 - 답변 개수 순 정렬
        () -> assertSame(result1, sortByOpinion.get(0))
    );
  }

  @Test
  @DisplayName("사용자 ID로 여행 On 조회")
  void findAllByUserIdTest() {
    // GIVEN (User)
    User author = User.builder()
            .accountId("testAccountId")
            .password("testPassword")
            .nickname("testNickname")
            .userRole(UserRole.TRAVELER)
            .build();
    em.persist(author);

    // GIVEN (TravelOn)
    final int travelOnCnt = 5;
    List<TravelOn> travelOns = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < travelOnCnt; i++)
      travelOns.add(saveTravelOn(author, "state", "city", now.plusHours(i), 0));

    // GIVEN (Opinion)
    final int opinionCnt = 3;
    for (int i = 0; i < opinionCnt; i++)
      saveAnotherOpinion(travelOns.get(travelOnCnt - 1));

    // GIVEN (Paging)
    final Long lastItemId = null;
    final int size = 3;

    // WHEN
    List<TravelOn> allTravelOns = travelOnRepository.findAllByUserId(
            author.getId(),
            null,
            travelOnCnt,
            TravelOnSortType.DATE
    );

    List<TravelOn> pagedTravelOns = travelOnRepository.findAllByUserId(
            author.getId(),
            lastItemId,
            size,
            TravelOnSortType.DATE
    );

    // THEN
    assertAll(
            // 성공 케이스 - 1 - 작성한 여행 On 조회
            () -> Assertions.assertThat(allTravelOns.size()).isEqualTo(travelOnCnt),
            // 성공 케이스 - 2 - 받은 lastItemId와 size만큼의 결과 반환
            () -> Assertions.assertThat(pagedTravelOns.size()).isEqualTo(size),
            // 성공 케이스 - 3 - 답변 개수 확인
            () -> Assertions.assertThat(allTravelOns.get(0).getOpinionList().size()).isEqualTo(opinionCnt)
    );
  }

  @Test
  @DisplayName("답변이 달린 여행On 조회")
  void findHasOpinionTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnRegionAWithOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionANoOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(3), 2);
    TravelOn travelOnRegionBWithOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);
    TravelOn travelOnRegionBNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);

    //답변 저장
    saveAnotherOpinion(travelOnRegionAWithOpinion);
    saveAnotherOpinion(travelOnRegionBWithOpinion);

    //WHEN
    List<TravelOn> hasOpinionTravelOnResult = travelOnRepository.findHasOpinion(null, 4, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //답변이 있는 여행 On 조회 결과
        () -> assertSame(2, hasOpinionTravelOnResult.size()),
        () -> assertTrue(hasOpinionTravelOnResult.contains(travelOnRegionAWithOpinion)),
        () -> assertTrue(hasOpinionTravelOnResult.contains(travelOnRegionBWithOpinion)),
        () -> assertFalse(hasOpinionTravelOnResult.contains(travelOnRegionANoOpinion)),
        () -> assertFalse(hasOpinionTravelOnResult.contains(travelOnRegionBNoOpinion))
    );
  }

  @Test
  @DisplayName("답변이 없는 여행On 조회")
  void findNoOpinionTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnRegionAWithOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionANoOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(3), 2);
    TravelOn travelOnRegionBWithOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);
    TravelOn travelOnRegionBNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);

    //답변 저장
    saveAnotherOpinion(travelOnRegionAWithOpinion);
    saveAnotherOpinion(travelOnRegionBWithOpinion);

    //WHEN
    List<TravelOn> noOpinionResult = travelOnRepository.findNoOpinion(null, 4, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //답변이 없는 여행 On 조회 결과
        () -> assertSame(2, noOpinionResult.size()),
        () -> assertTrue(noOpinionResult.contains(travelOnRegionANoOpinion)),
        () -> assertTrue(noOpinionResult.contains(travelOnRegionBNoOpinion)),
        () -> assertFalse(noOpinionResult.contains(travelOnRegionAWithOpinion)),
        () -> assertFalse(noOpinionResult.contains(travelOnRegionBWithOpinion))
    );
  }

  @Test
  @DisplayName("Region 으로 여행On 조회")
  void findAllByRegionTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String cityA = "city1A";
    String stateB = "stateB";
    String cityB = "city1B";

    TravelOn result1 = saveTravelOn(author, stateA, cityA, LocalDateTime.now().minusHours(4), 1);
    TravelOn result2 = saveTravelOn(author, stateA, cityA, LocalDateTime.now().minusHours(3), 2);
    TravelOn result3 = saveTravelOn(author, stateB, cityB, LocalDateTime.now().minusHours(2), 3);
    TravelOn result4 = saveTravelOn(author, stateB, cityB, LocalDateTime.now().minusHours(1), 4);

    Region regionA = result1.getRegion();
    Region regionB = result3.getRegion();

    //WHEN
    List<TravelOn> resultWithRegionA = travelOnRepository.findAllByRegion(regionA, null, 4, TravelOnSortType.DATE);
    List<TravelOn> resultWithRegionB = travelOnRepository.findAllByRegion(regionB, null, 4, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 1 - RegionA 로 조회한 결과
        () -> assertSame(2, resultWithRegionA.size()),
        () -> assertSame(result2, resultWithRegionA.get(0)),
        //성공 케이스 - 2 - RegionB 로 조회한 결과
        () -> assertSame(2, resultWithRegionB.size()),
        () -> assertSame(result4, resultWithRegionB.get(0))
    );
  }

  @Test
  @DisplayName("답변이 있는 여행On을 Region으로 조회")
  void findHasOpinionByRegionTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnWithOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnNoOpinion2 = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(3), 2);
    TravelOn travelOnNoOpinion1 = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);

    //답변 저장
    saveAnotherOpinion(travelOnWithOpinion);

    //Region
    Region regionA = travelOnWithOpinion.getRegion();
    Region regionB = travelOnNoOpinion1.getRegion();

    //WHEN
    List<TravelOn> withOpinionResult = travelOnRepository.findHasOpinionByRegion(regionA, null, 10, TravelOnSortType.DATE);
    List<TravelOn> withOpinionButNotRegionResult = travelOnRepository.findHasOpinionByRegion(regionB, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //답변이 있는 여행On이 존재하는 Region에서 조회
        () -> assertSame(1, withOpinionResult.size()),
        () -> assertSame(travelOnWithOpinion, withOpinionResult.get(0)),
        //답변이 있는 여행On이 존재하지 않는 Region에서 조회
        () -> assertSame(0, withOpinionButNotRegionResult.size())
    );
  }

  @Test
  @DisplayName("답변이 없는 여행On을 Region으로 조회")
  void findNoOpinionByRegionTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnWithOpinion1 = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnWithOpinion2 = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(3), 2);

    //답변 저장
    saveAnotherOpinion(travelOnWithOpinion1);
    saveAnotherOpinion(travelOnWithOpinion2);

    //Region
    Region regionA = travelOnWithOpinion1.getRegion();
    Region regionB = travelOnNoOpinion.getRegion();

    //WHEN
    List<TravelOn> noOpinionResult = travelOnRepository.findNoOpinionByRegion(regionB, null, 10, TravelOnSortType.DATE);
    List<TravelOn> noOpinionResultButNotRegion = travelOnRepository.findNoOpinionByRegion(regionA, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //답변이 없는 여행On이 존재하는 Region에서 조회
        () -> assertSame(1, noOpinionResult.size()),
        () -> assertSame(travelOnNoOpinion, noOpinionResult.get(0)),
        //답변이 없는 여행On이 존재하지 않는 Region에서 조회
        () -> assertSame(0, noOpinionResultButNotRegion.size())
    );
  }

  @Test
  @DisplayName("여행On을 State으로 조회")
  void findAllByStateTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnRegionA1 = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionA2 = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionB = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);

    //WHEN
    List<TravelOn> stateAResult = travelOnRepository.findAllByState(stateA, null, 10, TravelOnSortType.DATE);
    List<TravelOn> stateBResult = travelOnRepository.findAllByState(stateB, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //stateA 로 조회
        () -> assertSame(2, stateAResult.size()),
        //stateB 로 조회
        () -> assertSame(1, stateBResult.size())
    );
  }

  @Test
  @DisplayName("답변이 있는 여행On을 State으로 조회")
  void findHasOpinionByStateTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnRegionAWithOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionANoOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionBWithOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);

    //답변 저장
    saveAnotherOpinion(travelOnRegionAWithOpinion);
    saveAnotherOpinion(travelOnRegionBWithOpinion);

    //WHEN
    List<TravelOn> hasOpinionByStateAResult = travelOnRepository.findHasOpinionByState(stateA, null, 10, TravelOnSortType.DATE);
    List<TravelOn> hasOpinionByStateBResult = travelOnRepository.findHasOpinionByState(stateB, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //stateA 로 조회
        () -> assertSame(1, hasOpinionByStateAResult.size()),
        () -> assertSame(travelOnRegionAWithOpinion, hasOpinionByStateAResult.get(0)),
        //stateB 로 조회
        () -> assertSame(1, hasOpinionByStateBResult.size()),
        () -> assertSame(travelOnRegionBWithOpinion, hasOpinionByStateBResult.get(0))
    );
  }

  @Test
  @DisplayName("답변이 없는 여행On을 State으로 조회")
  void findNoOpinionByStateTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    TravelOn travelOnRegionAWithOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionANoOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionBWithOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    TravelOn travelOnRegionBNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);

    //답변 저장
    saveAnotherOpinion(travelOnRegionAWithOpinion);
    saveAnotherOpinion(travelOnRegionBWithOpinion);

    //WHEN
    List<TravelOn> noOpinionByStateAResult = travelOnRepository.findNoOpinionByState(stateA, null, 10, TravelOnSortType.DATE);
    List<TravelOn> noOpinionByStateBResult = travelOnRepository.findNoOpinionByState(stateB, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //stateA 로 조회
        () -> assertSame(1, noOpinionByStateAResult.size()),
        () -> assertSame(travelOnRegionANoOpinion, noOpinionByStateAResult.get(0)),
        //stateB 로 조회
        () -> assertSame(1, noOpinionByStateBResult.size()),
        () -> assertSame(travelOnRegionBNoOpinion, noOpinionByStateBResult.get(0))
    );
  }

  @Test
  @DisplayName("ID 로 TravelOn 조회")
  void findByIdTest() {
    //GIVEN
    User author = User.builder()
            .accountId("accountId")
            .nickname("nickname")
            .password("password")
            .userRole(UserRole.TRAVELER)
            .build();
    em.persist(author);
    TravelOn travelOnA = saveTravelOn(author, "stateA", "city1", LocalDateTime.now(), 0);
    long travelOnAId = travelOnA.getId();
    long notExistTravelOnId = travelOnAId + 10;

    //WHEN
    Optional<TravelOn> succeedResult = travelOnRepository.findById(travelOnAId);
    Optional<TravelOn> failResult = travelOnRepository.findById(notExistTravelOnId);

    //THEN
    assertAll(
        //성공 케이스 - 1 - 존재하는 ID 로 조회
        () -> assertTrue(succeedResult.isPresent()),
        //실패 케이스 - 1 - 존재하지 않는 ID 로 조회
        () -> assertFalse(failResult.isPresent())
    );
  }

  @Test
  @DisplayName("여행 On 삭제")
  void removeTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";

    //여행On 저장
    TravelOn travelOn = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);

    //WHEN
    travelOnRepository.remove(travelOn);

    //THEN
    assertAll(
        //성공 케이스 - 1 - 삭제가 되었는지
        () -> assertNull(em.find(TravelOn.class, travelOn.getId())),
        //성공 케이스 - 2 - Flush 성공
        () -> assertDoesNotThrow(() -> em.flush())
    );
  }

  @Test
  @DisplayName("모든 여행On을 키워드로 조회")
  void findAllByKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnWithKeyword = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnWithKeyword.setTitle(titleKeyword);
    travelOnWithKeyword.setDescription(descriptionKeyword);

    TravelOn travelOnNoKeyword = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnNoKeyword.setTitle("no keyword");
    travelOnNoKeyword.setDescription("no keyword");

    //WHEN
    List<TravelOn> resultByTitleKeyword = travelOnRepository.findAllByKeyword(titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultByDescriptionKeyword = travelOnRepository.findAllByKeyword(descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(1, resultByTitleKeyword.size()),
        () -> assertSame(travelOnWithKeyword, resultByTitleKeyword.get(0)),
        //성공 케이스 - description 키워드
        () -> assertSame(1, resultByDescriptionKeyword.size()),
        () -> assertSame(travelOnWithKeyword, resultByDescriptionKeyword.get(0))
    );
  }

  @Test
  @DisplayName("답변이 있는 여행On을 키워드로 조회")
  void findHasOpinionByKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnWithKeywordAndOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnWithKeywordAndOpinion.setTitle(titleKeyword);
    travelOnWithKeywordAndOpinion.setDescription(descriptionKeyword);
    saveAnotherOpinion(travelOnWithKeywordAndOpinion);

    TravelOn travelOnWithKeywordNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnWithKeywordNoOpinion.setTitle(titleKeyword);
    travelOnWithKeywordNoOpinion.setDescription(descriptionKeyword);

    //WHEN
    List<TravelOn> resultByTitleKeyword = travelOnRepository.findHasOpinionByKeyword(titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultByDescriptionKeyword = travelOnRepository.findHasOpinionByKeyword(descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(1, resultByTitleKeyword.size()),
        () -> assertSame(travelOnWithKeywordAndOpinion, resultByTitleKeyword.get(0)),
        //성공 케이스 - description 키워드
        () -> assertSame(1, resultByDescriptionKeyword.size()),
        () -> assertSame(travelOnWithKeywordAndOpinion, resultByDescriptionKeyword.get(0))
    );
  }

  @Test
  @DisplayName("답변이 없는 여행On을 키워드로 조회")
  void findNoOpinionByKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnWithKeywordAndOpinion = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnWithKeywordAndOpinion.setTitle(titleKeyword);
    travelOnWithKeywordAndOpinion.setDescription(descriptionKeyword);
    saveAnotherOpinion(travelOnWithKeywordAndOpinion);

    TravelOn travelOnWithKeywordNoOpinion = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnWithKeywordNoOpinion.setTitle(titleKeyword);
    travelOnWithKeywordNoOpinion.setDescription(descriptionKeyword);

    //WHEN
    List<TravelOn> resultByTitleKeyword = travelOnRepository.findNoOpinionByKeyword(titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultByDescriptionKeyword = travelOnRepository.findNoOpinionByKeyword(descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(1, resultByTitleKeyword.size()),
        () -> assertSame(travelOnWithKeywordNoOpinion, resultByTitleKeyword.get(0)),
        //성공 케이스 - description 키워드
        () -> assertSame(1, resultByDescriptionKeyword.size()),
        () -> assertSame(travelOnWithKeywordNoOpinion, resultByDescriptionKeyword.get(0))
    );
  }

  @Test
  @DisplayName("모든 여행On을 지역·키워드로 조회")
  void findAllByRegionAndKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnOfRegionA = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionA.setTitle(titleKeyword);
    travelOnOfRegionA.setDescription(descriptionKeyword);
    saveAnotherOpinion(travelOnOfRegionA);

    TravelOn travelOnOfRegionB = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionB.setTitle(titleKeyword);
    travelOnOfRegionB.setDescription(descriptionKeyword);

    //Region
    Region regionA = travelOnOfRegionA.getRegion();
    Region regionB = travelOnOfRegionB.getRegion();

    //WHEN
    List<TravelOn> resultOfRegionA = travelOnRepository.findAllByRegionAndKeyword(regionA, titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultOfRegionB = travelOnRepository.findAllByRegionAndKeyword(regionB, descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(1, resultOfRegionA.size()),
        () -> assertSame(travelOnOfRegionA, resultOfRegionA.get(0)),
        //성공 케이스 - description 키워드
        () -> assertSame(1, resultOfRegionB.size()),
        () -> assertSame(travelOnOfRegionB, resultOfRegionB.get(0))
    );
  }

  @Test
  @DisplayName("답변이 있는 여행On을 지역·키워드로 조회")
  void findHasOpinionByRegionAndKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnOfRegionA = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionA.setTitle(titleKeyword);
    travelOnOfRegionA.setDescription(descriptionKeyword);
    saveAnotherOpinion(travelOnOfRegionA);

    TravelOn travelOnOfRegionB = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionB.setTitle(titleKeyword);
    travelOnOfRegionB.setDescription(descriptionKeyword);

    //Region
    Region regionA = travelOnOfRegionA.getRegion();
    Region regionB = travelOnOfRegionB.getRegion();

    //WHEN
    List<TravelOn> resultOfRegionAHasOpinion = travelOnRepository.findHasOpinionByRegionAndKeyword(regionA, titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultOfRegionBNoOpinion = travelOnRepository.findHasOpinionByRegionAndKeyword(regionB, descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(1, resultOfRegionAHasOpinion.size()),
        () -> assertSame(travelOnOfRegionA, resultOfRegionAHasOpinion.get(0)),
        //성공 케이스 - description 키워드
        () -> assertSame(0, resultOfRegionBNoOpinion.size())
    );
  }

  @Test
  @DisplayName("답변이 없는 여행On을 지역·키워드로 조회")
  void findNoOpinionByRegionAndKeywordTest() {
    //GIVEN
    User author = User.builder()
        .accountId("testAccountId")
        .password("testPassword")
        .nickname("testNickname")
        .userRole(UserRole.TRAVELER)
        .build();
    em.persist(author);

    String stateA = "stateA";
    String city1A = "city1A";
    String stateB = "stateB";
    String city1B = "city1B";

    //여행On 저장
    String titleKeyword = "title keyword";
    String descriptionKeyword = "description keyword";

    TravelOn travelOnOfRegionA = saveTravelOn(author, stateA, city1A, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionA.setTitle(titleKeyword);
    travelOnOfRegionA.setDescription(descriptionKeyword);
    saveAnotherOpinion(travelOnOfRegionA);

    TravelOn travelOnOfRegionB = saveTravelOn(author, stateB, city1B, LocalDateTime.now().minusHours(4), 1);
    travelOnOfRegionB.setTitle(titleKeyword);
    travelOnOfRegionB.setDescription(descriptionKeyword);

    //Region
    Region regionA = travelOnOfRegionA.getRegion();
    Region regionB = travelOnOfRegionB.getRegion();

    //WHEN
    List<TravelOn> resultOfRegionAHasOpinion = travelOnRepository.findNoOpinionByRegionAndKeyword(regionA, titleKeyword, null, 10, TravelOnSortType.DATE);
    List<TravelOn> resultOfRegionBNoOpinion = travelOnRepository.findNoOpinionByRegionAndKeyword(regionB, descriptionKeyword, null, 10, TravelOnSortType.DATE);

    //THEN
    assertAll(
        //성공 케이스 - 타이틀 키워드
        () -> assertSame(0, resultOfRegionAHasOpinion.size()),
        //성공 케이스 - description 키워드
        () -> assertSame(1, resultOfRegionBNoOpinion.size()),
        () -> assertSame(travelOnOfRegionB, resultOfRegionBNoOpinion.get(0))
    );
  }

  /**
   * <pre>
   * 새 TravelOn 엔티티를 생성해서 반환하는 메서드
   * 단 영속화 처리를 안하고 반환한다.
   * </pre>
   * @param author
   * @return
   */
  private TravelOn getNotPersistedTravelOn(User author) {
    TravelOn travelOn;
    String title = "testTitle";
    LocalDate travelStartDate = LocalDate.now().plusMonths(1);
    LocalDate travelEndDate = LocalDate.now().plusMonths(1).plusDays(3);
    String description = "test description";
    TransportationType transportationType = TransportationType.OWN_CAR;
    int accommodationMaxCost = 100000;
    int foodMaxCost = 100000;
    int drinkMaxCost = 100000;

    Region region = getPersistRegion("경기도", "성남시");

    travelOn = TravelOn.builder()
        .title(title)
        .region(region)
        .views(1)
        .author(author)
        .travelStartDate(travelStartDate)
        .travelEndDate(travelEndDate)
        .description(description)
        .transportationType(transportationType)
        .accommodationMaxCost(accommodationMaxCost)
        .build();
    em.persist(travelOn);

    TravelMember travelMember1 = TravelMember.builder()
            .travelOn(travelOn)
            .memberType(MemberType.CHILD)
            .build();
    TravelMember travelMember2 = TravelMember.builder()
        .travelOn(travelOn)
        .memberType(MemberType.PARENT)
        .build();
    em.persist(travelMember1);
    em.persist(travelMember2);
    travelOn.addTravelMember(travelMember1);
    travelOn.addTravelMember(travelMember2);

    HopeAccommodation accommodation1 = HopeAccommodation.builder()
        .travelOn(travelOn)
        .type(AccommodationType.HOTEL)
        .build();
    HopeAccommodation accommodation2 = HopeAccommodation.builder()
        .travelOn(travelOn)
        .type(AccommodationType.GUEST_HOUSE)
        .build();
    em.persist(accommodation1);
    em.persist(accommodation2);
    travelOn.addHopeAccommodation(accommodation1);
    travelOn.addHopeAccommodation(accommodation2);

    HopeFood food1 = HopeFood.builder()
        .travelOn(travelOn)
        .type(FoodType.GLOBAL)
        .build();
    HopeFood food2 = HopeFood.builder()
        .travelOn(travelOn)
        .type(FoodType.KOREAN)
        .build();
    em.persist(food1);
    em.persist(food2);
    travelOn.addHopeFood(food1);
    travelOn.addHopeFood(food2);

    HopeDrink hopeDrink1 = HopeDrink.builder()
        .travelOn(travelOn)
        .type(DrinkType.BEER)
        .build();
    HopeDrink hopeDrink2 = HopeDrink.builder()
        .travelOn(travelOn)
        .type(DrinkType.BEER)
        .build();
    em.persist(hopeDrink1);
    em.persist(hopeDrink2);
    travelOn.addHopeDrink(hopeDrink1);
    travelOn.addHopeDrink(hopeDrink2);

    TravelTypeGroup travelTypeGroup = TravelTypeGroup.builder()
        .activityTasteType(ActivityTasteType.HARD)
        .placeTasteType(PlaceTasteType.FAMOUS)
        .snsTasteType(SnsTasteType.YES)
        .travelOn(travelOn)
        .build();
    em.persist(travelTypeGroup);
    travelOn.registerTravelTypeGroup(travelTypeGroup);

    //Ignore
    //em.persist(travelOn);

    return travelOn;
  }

  /**
   * 새 TravelOn 엔티티를 저장하는 메서드
   * @param author
   * @param state
   * @param city
   * @param createdDateTime
   * @param views
   * @return
   */
  private TravelOn saveTravelOn(User author, String state, String city, LocalDateTime createdDateTime, int views) {
    TravelOn travelOn;
    String title = "testTitle";
    LocalDate travelStartDate = LocalDate.now().plusMonths(1);
    LocalDate travelEndDate = LocalDate.now().plusMonths(1).plusDays(3);
    String description = "test description";
    TransportationType transportationType = TransportationType.OWN_CAR;
    int accommodationMaxCost = 100000;
    int foodMaxCost = 100000;
    int drinkMaxCost = 100000;

    Region region = getPersistRegion(state, city);

    travelOn = TravelOn.builder()
        .title(title)
        .region(region)
        .views(views)
        .author(author)
        .travelStartDate(travelStartDate)
        .travelEndDate(travelEndDate)
        .description(description)
        .transportationType(transportationType)
        .accommodationMaxCost(accommodationMaxCost)
        .createdDate(createdDateTime)
        .build();
    em.persist(travelOn);

    TravelMember travelMember1 = TravelMember.builder()
        .travelOn(travelOn)
        .memberType(MemberType.CHILD)
        .build();
    TravelMember travelMember2 = TravelMember.builder()
        .travelOn(travelOn)
        .memberType(MemberType.PARENT)
        .build();
    em.persist(travelMember1);
    em.persist(travelMember2);
    travelOn.addTravelMember(travelMember1);
    travelOn.addTravelMember(travelMember2);

    HopeAccommodation accommodation1 = HopeAccommodation.builder()
        .travelOn(travelOn)
        .type(AccommodationType.HOTEL)
        .build();
    HopeAccommodation accommodation2 = HopeAccommodation.builder()
        .travelOn(travelOn)
        .type(AccommodationType.GUEST_HOUSE)
        .build();
    em.persist(accommodation1);
    em.persist(accommodation2);
    travelOn.addHopeAccommodation(accommodation1);
    travelOn.addHopeAccommodation(accommodation2);

    HopeFood food1 = HopeFood.builder()
        .travelOn(travelOn)
        .type(FoodType.GLOBAL)
        .build();
    HopeFood food2 = HopeFood.builder()
        .travelOn(travelOn)
        .type(FoodType.KOREAN)
        .build();
    em.persist(food1);
    em.persist(food2);
    travelOn.addHopeFood(food1);
    travelOn.addHopeFood(food2);

    HopeDrink hopeDrink1 = HopeDrink.builder()
        .travelOn(travelOn)
        .type(DrinkType.BEER)
        .build();
    HopeDrink hopeDrink2 = HopeDrink.builder()
        .travelOn(travelOn)
        .type(DrinkType.BEER)
        .build();
    em.persist(hopeDrink1);
    em.persist(hopeDrink2);
    travelOn.addHopeDrink(hopeDrink1);
    travelOn.addHopeDrink(hopeDrink2);

    TravelTypeGroup travelTypeGroup = TravelTypeGroup.builder()
        .activityTasteType(ActivityTasteType.HARD)
        .placeTasteType(PlaceTasteType.FAMOUS)
        .snsTasteType(SnsTasteType.YES)
        .travelOn(travelOn)
        .build();
    em.persist(travelTypeGroup);
    travelOn.registerTravelTypeGroup(travelTypeGroup);

    em.persist(travelOn);

    return travelOn;
  }

  /**
   * 해당 TravelOn 엔티티에 새 Opinion 엔티티를 추가하는 메서드
   * @param travelOn Opinion 이 추가될 TravelOn 엔티티
   * @return
   */
  private Opinion saveAnotherOpinion(TravelOn travelOn) {
    Place place = savePlace(travelOn.getRegion());
    Opinion opinion = Opinion.builder()
        .travelOn(travelOn)
        .author(travelOn.getAuthor()) //테스트용으로 여행On 작성자가 답변을 달았다고 가정
        .region(travelOn.getRegion())
        .place(place)
        .facilityCleanliness(EvaluationDegree.GOOD)
        .canParking(EvaluationDegree.GOOD)
        .waiting(EvaluationDegree.GOOD)
        .costPerformance(EvaluationDegree.GOOD)
        .build();

    em.persist(opinion);
    opinion.setTravelOn(travelOn);

    return opinion;
  }

  /**
   * 새 Place 엔티티를 저장하는 메서드
   * @param region 새 Place의 Region
   * @return
   */
  private Place savePlace(Region region) {
    Place place = Place.builder()
        .id(++placeId)
        .category(PlaceCategory.CE7)
        .name("placeName")
        .roadAddress("roadAddress")
        .address("address")
        .lat(0.0)
        .lng(0.0)
        .region(region)
        .link("link")
        .build();
    em.persist(place);

    return place;
  }

  /**
   * 영속화된 Region 엔티티를 얻는 메서드
   * @param state
   * @param city
   * @return
   */
  private Region getPersistRegion(String state, String city) {
    String jpql = "select r from Region r" +
        " where r.state = :state" +
        " and r.city = :city";
    Region region;

    try {
      region = em.createQuery(jpql, Region.class)
          .setParameter("state", state)
          .setParameter("city", city)
          .getSingleResult();
    } catch (NoResultException e) {
      region = Region.builder()
          .city(city)
          .state(state)
          .build();
      em.persist(region);
    }

    return region;
  }
}