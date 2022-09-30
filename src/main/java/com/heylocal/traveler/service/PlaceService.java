package com.heylocal.traveler.service;

import com.heylocal.traveler.domain.place.Place;
import com.heylocal.traveler.dto.OpinionDto;
import com.heylocal.traveler.dto.PlaceDto;
import com.heylocal.traveler.exception.NotFoundException;
import com.heylocal.traveler.exception.code.NotFoundCode;
import com.heylocal.traveler.mapper.PlaceMapper;
import com.heylocal.traveler.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.heylocal.traveler.dto.OpinionDto.*;
import static com.heylocal.traveler.dto.PlaceDto.*;
import static com.heylocal.traveler.dto.PlaceDto.PlaceResponse;

@Service
@RequiredArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;

  /**
   * 장소 ID 로 장소를 조회하는 메서드
   * @param placeId 조회할 장소의 ID
   * @return
   * @throws NotFoundException
   */
  @Transactional(readOnly = true)
  public PlaceResponse inquiryPlace(long placeId) throws NotFoundException {
    Place place = placeRepository.findById(placeId).orElseThrow(
        () -> new NotFoundException(NotFoundCode.NO_INFO, "존재하지 않는 장소 ID입니다.")
    );
    PlaceResponse response = PlaceMapper.INSTANCE.toPlaceResponseDto(place);

    return response;
  }

  /**
   * 가장 많은 답변으로 선택된 장소를 조회하는 메서드
   * @param size 조회할 장소 개수 (null 인 경우, 5개)
   * @return
   */
  @Transactional(readOnly = true)
  public List<PlaceWithOpinionSizeResponse> inquiryMostOpinedPlace(@Nullable Integer size) {
    List<Place> findResult;
    List<PlaceWithOpinionSizeResponse> result;

    //size 가 null 이라면 5로 설정
    size = (size == null) ? 5 : size;

    //가장 많은 답변으로 선택된 장소 조회
    findResult = placeRepository.findPlaceOrderByOpinionSizeDesc(size);

    //List<Place> -> List<PlaceResponse>
    result = findResult.stream().map(PlaceMapper.INSTANCE::toPlaceWithOpinionSizeResponseDto).collect(Collectors.toList());

    return result;
  }

}