package com.heylocal.traveler.repository;

import com.heylocal.traveler.domain.token.AccessToken;
import com.heylocal.traveler.domain.token.RefreshToken;
import com.heylocal.traveler.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Access Token, Refresh Token Repository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRepository {
  private final EntityManager em;

  /**
   * AccessToken 과 Refresh Token 쌍을 저장하는 메서드
   * @param accessValue Access Token 값
   * @param refreshValue Refresh Token 값
   */
  public RefreshToken saveTokenPair(long userId, String accessValue, LocalDateTime accessExpired, String refreshValue, LocalDateTime refreshExpired) {
    RefreshToken refreshToken;
    AccessToken accessToken;
    User user;

    user = em.find(User.class, userId);
    refreshToken = RefreshToken.builder()
        .tokenValue(refreshValue)
        .expiredDateTime(refreshExpired)
        .user(user)
        .build();
    accessToken = AccessToken.builder()
        .tokenValue(accessValue)
        .expiredDateTime(accessExpired)
        .user(user)
        .build();
    refreshToken.associateAccessToken(accessToken);

    //Cascade.ALL 옵션 때문에, 부모 엔티티(RefreshToken)이 영속화될 때, 자식 엔티티(Access Token)도 영속화된다.
    em.persist(refreshToken);

    return refreshToken;
  }

  /**
   * Refresh Token 값으로 Refresh Token 엔티티 조회하는 메서드
   * @param tokenValue refresh token 값
   * @return
   */
  public Optional<RefreshToken> findRefreshTokenByValue(String tokenValue) {
    RefreshToken refreshToken = null;
    String jpql = "select r from RefreshToken  r" +
        " where r.tokenValue = :tokenValue";

    try {
      refreshToken = em.createQuery(jpql, RefreshToken.class)
          .setParameter("tokenValue", tokenValue)
          .getSingleResult();
    } catch (NoResultException e) {
      return Optional.empty();
    }

    return Optional.of(refreshToken);
  }

  /**
   * Access Token 값으로 Access Token 엔티티 조회하는 메서드
   * @param tokenValue access token 값
   * @return
   */
  public Optional<AccessToken> findAccessTokenByValue(String tokenValue) {
    AccessToken accessToken = null;
    String jpql = "select a from AccessToken  a" +
        " where a.tokenValue = :tokenValue";

    try {
      accessToken = em.createQuery(jpql, AccessToken.class)
          .setParameter("tokenValue", tokenValue)
          .getSingleResult();
    } catch (NoResultException e) {
      return Optional.empty();
    }

    return Optional.of(accessToken);
  }

  /**
   * <pre>
   * 사용자 id(pk)로 관련된 Access·Refresh 토큰을 제거하는 메서드
   * 비즈니스 로직상 토큰을 삭제하고, 새 토큰을 저장할 때 사용됨.
   * 하지만 하나의 트랜잭션에서 동일한 클래스의 엔티티를 삭제하고 추가하는 것이 문제를 일으킴.
   * 따라서 `em.remove()` 를 사용하지 않고, 직접 JPQL을 실행하는 방식으로 작성함.
   *
   * 유사 이슈 보고: <a href="https://github.com/spring-projects/spring-data-jpa/issues/1100">Github Issue</a>
   * 관련 Jira Issue: <a href="https://swm13-tgt.atlassian.net/browse/S3T-359">Jira Issue</a>
   * </pre>
   * @param userId
   * @exception NoResultException 해당 userId를 갖는 Refresh 토큰이 없는 경우
   */
  public void removeTokenPairByUserId(long userId) throws NoResultException {
    String jpql = "delete from AccessToken a" +
        " where a.user.id = :userId";

    em.createQuery(jpql)
        .setParameter("userId", userId)
        .executeUpdate();

    jpql = "delete from RefreshToken r" +
        " where r.user.id = :userId";

    em.createQuery(jpql)
        .setParameter("userId", userId)
        .executeUpdate();
  }

  /**
   * Refresh Token 의 값으로 Refresh Token 과 Access Token 쌍을 모두 삭제하는 메서드
   * @param refreshTokenValue 삭제할 Refresh Token 값
   */
  public void removeTokenPairByRefreshValue(String refreshTokenValue) {
    RefreshToken refreshToken = findRefreshTokenByValue(refreshTokenValue).orElseThrow(
        () -> new IllegalArgumentException("존재하지 않는 Refresh Token 값입니다.")
    );
    em.remove(refreshToken);
  }

  /**
   * Access Token 의 값으로 Refresh Token 과 Access Token 쌍을 모두 삭제하는 메서드
   * @param accessTokenValue 삭제할 Access Token 값
   */
  public void removeTokenPairByAccessValue(String accessTokenValue) {
    AccessToken accessToken = findAccessTokenByValue(accessTokenValue).orElseThrow(
        () -> new IllegalArgumentException("존재하지 않는 Access Token 값입니다.")
    );
    em.remove(accessToken.getRefreshToken());
  }
}