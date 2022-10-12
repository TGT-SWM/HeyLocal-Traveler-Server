/**
 * packageName    : com.heylocal.traveler.domain.token
 * fileName       : AccessToken
 * author         : 우태균
 * date           : 2022/09/16
 * description    : 사용자 인가 Access Token 엔티티
 */

package com.heylocal.traveler.domain.token;

import com.heylocal.traveler.domain.BaseTimeEntity;
import com.heylocal.traveler.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACCESS_TOKEN")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class AccessToken extends BaseTimeEntity {
  @Id @GeneratedValue
  private long id;
  @Column(length = 510, nullable = false)
  private String tokenValue;
  @Column(nullable = false)
  private LocalDateTime expiredDateTime;
  @OneToOne
  private RefreshToken refreshToken;
  @OneToOne(fetch = FetchType.LAZY)
  private User user;

  public void associateRefreshToken(RefreshToken refreshToken) {
    this.refreshToken = refreshToken;

    if (refreshToken.getAccessToken() != this) {
      refreshToken.associateAccessToken(this);
    }
  }

  public void associateUser(User user) {
    this.user = user;
    if (user.getAccessToken() != this) {
      user.registerAccessToken(this);
    }
  }

  public void updateTokenValue(String newTokenValue) {
    this.tokenValue = newTokenValue;
  }
}
