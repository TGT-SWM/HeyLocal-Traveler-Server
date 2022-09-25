package com.heylocal.traveler.domain.plan;

import com.heylocal.traveler.domain.BaseTimeEntity;
import com.heylocal.traveler.domain.Region;
import com.heylocal.traveler.domain.travelon.TravelOn;
import com.heylocal.traveler.domain.user.User;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 여행
 */

@Entity
@Table(name = "PLAN")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Plan extends BaseTimeEntity {
  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  String title;

  @OneToOne(fetch = FetchType.LAZY)
  private TravelOn travelOn;

  @ManyToOne(optional = false)
  private User user;

  @ManyToOne(optional = false)
  private Region region;

  @Column(nullable = false)
  private LocalDate travelStartDate;

  @Column(nullable = false)
  private LocalDate travelEndDate;

  //양방향 설정

  @Builder.Default
  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<DaySchedule> dayScheduleList = new ArrayList<>();

  public void releaseTravelOn() {
    TravelOn temp = this.travelOn;
    this.travelOn = null;

    if (temp != null) {
      temp.releasePlan();
    }
  }

  public void addDaySchedule(DaySchedule daySchedule) {
    dayScheduleList.add(daySchedule);
    if (daySchedule.getPlan() != this)
      daySchedule.register(this);
  }

  public void updateTitle(String title) {
    this.title = title;
  }
}
