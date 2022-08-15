package com.heylocal.traveler.dto;

import com.heylocal.traveler.domain.user.UserType;
import lombok.*;

import javax.validation.constraints.NotEmpty;

public class SigninDto {
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SigninRequest {
    @NotEmpty
    private String accountId;
    @NotEmpty
    private String password;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SigninResponse {
    private long id;
    private String accountId;
    private String nickname;
    private String phoneNumber;
    private UserType userType;
  }
}
