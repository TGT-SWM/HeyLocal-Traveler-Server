package com.heylocal.traveler.domain.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 여행자 프로필
 */

@Entity
@Table(name = "TRAVELER_PROFILE")
@DiscriminatorValue("TRAVELER")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TravelerProfile extends UserProfile {
  private String imageUrl;
}