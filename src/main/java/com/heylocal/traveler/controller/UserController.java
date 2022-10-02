package com.heylocal.traveler.controller;

import com.heylocal.traveler.controller.api.UsersApi;
import com.heylocal.traveler.dto.LoginUser;
import com.heylocal.traveler.dto.OpinionDto;
import com.heylocal.traveler.dto.PageDto;
import com.heylocal.traveler.dto.PageDto.PageRequest;
import com.heylocal.traveler.dto.TravelOnDto.TravelOnSimpleResponse;
import com.heylocal.traveler.exception.BadRequestException;
import com.heylocal.traveler.exception.ForbiddenException;
import com.heylocal.traveler.exception.NotFoundException;
import com.heylocal.traveler.exception.code.BadRequestCode;
import com.heylocal.traveler.exception.code.ForbiddenCode;
import com.heylocal.traveler.exception.code.SignupCode;
import com.heylocal.traveler.service.TravelOnService;
import com.heylocal.traveler.service.UserService;
import com.heylocal.traveler.util.error.BindingErrorMessageProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

import static com.heylocal.traveler.dto.UserDto.UserProfileRequest;
import static com.heylocal.traveler.dto.UserDto.UserProfileResponse;

@Tag(name = "Users")
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {
	@Value("${heylocal.signup.pattern.nickname}")
	private String nicknamePattern;
	private final TravelOnService travelOnService;
	private final UserService userService;
	private final BindingErrorMessageProvider errorMessageProvider;

	/**
	 * 사용자 프로필 조회 핸들러
	 * @param userId
	 * @return
	 */
	@Override
	public UserProfileResponse getUserProfile(long userId) throws NotFoundException {
		UserProfileResponse response;

		response = userService.inquiryUserProfile(userId); //ProfileResponse DTO 구하기

		return response;
	}

	/**
	 * 사용자 프로필 수정 핸들러
	 * @param userId
	 * @param request
	 * @return
	 */
	@Override
	public void updateUserProfile(long userId, UserProfileRequest request,
																BindingResult bindingResult, LoginUser loginUser) throws BadRequestException, ForbiddenException, NotFoundException {
		//수정 권한 검증
		boolean canUpdate = userService.canUpdateProfile(userId, loginUser);
		if (!canUpdate) throw new ForbiddenException(ForbiddenCode.NO_PERMISSION, "프로필 수정 권한이 없습니다.");

		//기본 값 검증
		if (bindingResult.hasFieldErrors()) {
			String fieldErrMsg = errorMessageProvider.getFieldErrMsg(bindingResult);
			throw new BadRequestException(BadRequestCode.BAD_INPUT_FORM, fieldErrMsg);
		}

		//닉네임 형식 검증
		validateNicknameFormat(request.getNickname());

		//프로필 업데이트
		userService.updateProfile(userId, request);
	}

	/**
	 * 특정 사용자가 작성한 여행 On의 목록을 페이징하여 조회합니다.
	 * @param userId 사용자 ID
	 * @param pageRequest 요청하는 페이지 정보
	 * @return 여행 On 목록
	 */
	@Override
	public List<TravelOnSimpleResponse> getUserTravelOns(long userId, PageRequest pageRequest) {
		return travelOnService.inquirySimpleTravelOns(userId, pageRequest);
	}

	/**
	 * @param userId
	 * @param pageRequest
	 * @return
	 */
	@Override
  public List<OpinionDto.OpinionWithPlaceResponse> getUserOpinions(long userId, PageDto.PageRequest pageRequest) {
		return null;
	}

	/**
	 * @return
	 */
	@Override
	public List<UserProfileResponse> getRanking() {
		return null;
	}

	/**
	 * <pre>
	 * 닉네임 형식 검증
	 * 숫자 + 영어 조합, 2자 이상, 20자 이하
	 * </pre>
	 * @param nickname 검증할 닉네임
	 * @return 조건에 부합하면 true 반환
	 * @throws BadRequestException 조건에 부합하지 않는다면, 발생하는 예외
	 */
	private boolean validateNicknameFormat(String nickname) throws BadRequestException {
		if (!Pattern.matches(nicknamePattern, nickname)) {
			throw new BadRequestException(SignupCode.WRONG_NICKNAME_FORMAT, "닉네임 형식이 잘못되었습니다. 닉네임은 숫자 + 영어 조합, 2자 이상, 20자 이하입니다.");
		}

		return true;
	}
}
