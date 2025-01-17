/**
 * packageName    : com.heylocal.traveler.domain.travelon.list
 * fileName       : HopeFood
 * author         : 우태균
 * date           : 2022/09/19
 * description    : 여행On 질문 항목 - 희망 음식 엔티티
 */

package com.heylocal.traveler.domain.travelon.list;

import com.heylocal.traveler.domain.BaseTimeEntity;
import com.heylocal.traveler.domain.travelon.TravelOn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "HOPE_FOOD")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class HopeFood extends BaseTimeEntity {
  @Id @GeneratedValue
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FoodType type;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private TravelOn travelOn;

  public void registerAt(TravelOn travelOn) {
    this.travelOn = travelOn;
    if (!travelOn.getHopeFoodSet().contains(this)) {
      travelOn.getHopeFoodSet().add(this);
    }
  }
}