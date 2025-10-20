package com.monstersinc.stock101.community;

import com.monstersinc.stock101.community.controller.CommunityController;
import com.monstersinc.stock101.community.model.dto.CommentRequestDto;
import com.monstersinc.stock101.community.model.dto.CommentResponseDto;
import com.monstersinc.stock101.community.model.dto.PostRequestDto;
import com.monstersinc.stock101.community.model.dto.PostResponseDto;
import com.monstersinc.stock101.community.model.service.CommunityService;
import com.monstersinc.stock101.user.model.vo.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;


@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {

    @InjectMocks
    private CommunityController controller;

    @Mock
    private CommunityService communityService;

    // --- helper: 인증 사용자 목 생성 ---
    private User mockUser(long userId) {
        User u = mock(User.class);
        when(u.getUserId()).thenReturn(userId);
        return u;
    }

    private PostResponseDto sampleDetail(long postId, long stockId, boolean likedByMe) {
        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(postId);
        dto.setStockId(stockId);
        dto.setOpinion("매수 의견");
        dto.setContent("상세 내용");
        dto.setLikedByMe(likedByMe);
        dto.setLikeCount(10);
        dto.setCommentCount(2);
        dto.setCreatedAt(OffsetDateTime.now().toString());
        return dto;
    }

    private CommentResponseDto commentDto(
            long commentId, long postId, long userId,
            String nickname, String content, OffsetDateTime createdAt) {

        CommentResponseDto dto = new CommentResponseDto();
        dto.setCommentId(commentId);
        dto.setPostId(postId);
        dto.setUserId(userId);
        dto.setUserName(nickname);
        dto.setContent(content);
        dto.setCreatedAt(createdAt.toString());
        return dto;
    }

    // ---------- 테스트 본문 ----------
    @Test
    @DisplayName("게시물 등록 - 201 Created & 서비스 호출 파라미터(opinion 포함) 검증")
    void createPost_success_201() {
        // given
        long userId = 11L;
        long newPostId = 501L;

        PostRequestDto req = new PostRequestDto();
        req.setStockId(123L);
        req.setOpinion("매수 의견입니다");
        req.setContent("근거는 어쩌구 저쩌구");

        when(communityService.saveAPost(eq(userId), any(PostRequestDto.class)))
                .thenReturn(newPostId);

        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(newPostId);
        dto.setStockId(123L);
        dto.setOpinion("매수 의견입니다");
        dto.setContent("근거는 어쩌구 저쩌구");
        dto.setLikedByMe(true);
        dto.setCreatedAt(OffsetDateTime.now().toString());

        when(communityService.getPostDetail(newPostId, userId))
                .thenReturn(dto);

        // when
        ResponseEntity<?> resp = controller.create(mockUser(userId), req);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(CREATED);
        assertThat(resp.getBody()).isNotNull();

        // 서비스 파라미터 캡처
        ArgumentCaptor<PostRequestDto> captor = ArgumentCaptor.forClass(PostRequestDto.class);
        verify(communityService).saveAPost(eq(userId), captor.capture());
        PostRequestDto passed = captor.getValue();
        assertThat(passed.getStockId()).isEqualTo(123L);
        assertThat(passed.getOpinion()).isEqualTo("매수 의견입니다");
        assertThat(passed.getContent()).isEqualTo("근거는 어쩌구 저쩌구");

        verify(communityService).getPostDetail(newPostId, userId);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 상세 - 로그인 사용자: userId로 서비스 호출")
    void detail_authenticated_200() {
        // given
        long userId = 42L;
        long postId = 321L;

        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(postId);
        dto.setStockId(321L);
        dto.setLikedByMe(false);

        when(communityService.getPostDetail(postId, userId)).thenReturn(dto);

        // when
        ResponseEntity<?> resp = controller.detail(mockUser(userId), postId);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).getPostDetail(postId, userId);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 상세 - 비로그인: userId=null로 서비스 호출")
    void detail_anonymous_200() {
        // given
        long postId = 321L;

        PostResponseDto dto = new PostResponseDto();
        dto.setPostId(postId);
        dto.setStockId(321L);
        dto.setLikedByMe(false);

        when(communityService.getPostDetail(postId, null)).thenReturn(dto);

        // when
        ResponseEntity<?> resp = controller.detail(null, postId);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).getPostDetail(postId, null);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 상세 - 서비스에서 404 발생 시 예외 전파")
    void detail_notFound_propagates() {
        // given
        long postId = 404404L;
        when(communityService.getPostDetail(eq(postId), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"));

        // when & then
        assertThrows(ResponseStatusException.class,
                () -> controller.detail(null,postId));
        verify(communityService).getPostDetail(eq(postId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 삭제 - 성공: 200 OK & 서비스 호출")
    void delete_ok_200() {
        // given
        long postId = 1001L;
        doNothing().when(communityService).delete(postId);

        // when
        ResponseEntity<?> resp = controller.delete(postId);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(OK);
        verify(communityService).delete(postId);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 삭제 - 서비스에서 404 발생 시 예외 전파")
    void delete_notFound_propagates() {
        // given
        long postId = 9999L;
        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).delete(postId);

        // when & then
        assertThrows(ResponseStatusException.class,
                () -> controller.delete(postId));
        verify(communityService).delete(postId);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("좋아요 토글 - 200 OK & 토글 반영된 최신 PostResponseDto 반환")
    void likeToggle_authenticated_200_returnsUpdatedDto() {
        // given
        long userId = 99L;
        long postId = 777L;
        long stockId = 321L;

        PostResponseDto updated = sampleDetail(postId, stockId, true);
        when(communityService.likePost(eq(postId), eq(userId))).thenReturn(updated);

        // when
        ResponseEntity<?> resp = controller.like(mockUser(userId), postId);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).likePost(eq(postId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("좋아요 토글 - 존재하지 않는 postId → ResponseStatusException(404) 전파")
    void likeToggle_notFound_propagates404() {
        // given
        long userId = 55L;
        long postId = 404404L;

        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).likePost(eq(postId), eq(userId));

        // when & then
        assertThrows(ResponseStatusException.class,
                () -> controller.like(mockUser(userId), postId));

        verify(communityService).likePost(eq(postId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 리스트 - 200 OK & 서비스가 준 최신순 리스트 그대로 반환")
    void commentsList_200_and_sorted_desc() {
        // given
        long postId = 1001L;
        var now = OffsetDateTime.now();
        var c1 = commentDto(11L, postId, 21L, "alice", "첫 댓글", now.minusMinutes(3));
        var c2 = commentDto(12L, postId, 22L, "bob", "두번째(최신)", now.minusMinutes(1));

        var list = new ArrayList<>(List.of(c2, c1));
        when(communityService.getCommentListByPostId(eq(postId)))
                .thenReturn((List) list); // 최신순

        // when
        ResponseEntity<?> resp = controller.listComments(postId);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull(); // 래퍼 타입 내부 구조는 단위테스트에선 생략
        verify(communityService).getCommentListByPostId(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 리스트 - 존재하지 않는 postId → 404 전파")
    void commentsList_notFound_propagates404() {
        long postId = 404404L;
        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).getCommentListByPostId(eq(postId));

        assertThrows(ResponseStatusException.class, () -> controller.listComments(postId));
        verify(communityService).getCommentListByPostId(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    // ========== 댓글 등록 ==========

    @Test
    @DisplayName("댓글 등록 - 201 Created & 생성된 댓글 DTO 반환")
    void createComment_authenticated_201_returnsCreatedBody() {
        // given
        long userId = 99L;
        long postId = 1001L;
        long commentId = 5001L;

        var req = new CommentRequestDto();
        req.setContent("첫 댓글입니다!");
        // 컨트롤러가 userId/postId를 override 한다면, 여기 값은 무시될 수 있음
        req.setUserId(9999L);
        req.setPostId(9999L);

        var createdAt = OffsetDateTime.now().withNano(0);
        var createdDto = commentDto(commentId, postId, userId, "nick", "첫 댓글입니다!", createdAt);

        when(communityService.saveAComment(any(CommentRequestDto.class))).thenReturn(commentId);
        when(communityService.getAComment(eq(commentId))).thenReturn((CommentResponseDto) createdDto);

        // when
        ResponseEntity<?> resp = controller.create(mockUser(userId), postId, req);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(CREATED);
        assertThat(resp.getBody()).isNotNull();

        // 전달 DTO 필드 검증 (postId/userId/content)
        ArgumentCaptor<CommentRequestDto> captor = ArgumentCaptor.forClass(CommentRequestDto.class);
        verify(communityService).saveAComment(captor.capture());
        CommentRequestDto passed = captor.getValue();
        assertThat(passed.getPostId()).isEqualTo(postId);
        assertThat(passed.getUserId()).isEqualTo(userId);
        assertThat(passed.getContent()).isEqualTo("첫 댓글입니다!");

        verify(communityService).getAComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 등록 - 대상 글 없음 → 404 전파")
    void createComment_notFound_propagates404() {
        long userId = 99L;
        long postId = 404404L;

        var req = new CommentRequestDto();
        req.setContent("대상 글 없음");

        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).saveAComment(any(CommentRequestDto.class));

        assertThrows(ResponseStatusException.class,
                () -> controller.create(mockUser(userId), postId, req));

        verify(communityService).saveAComment(any(CommentRequestDto.class));
        verifyNoMoreInteractions(communityService);
    }

    // ========== 댓글 삭제 ==========

    @Test
    @DisplayName("댓글 삭제 - 작성자(로그인) 성공: 200 OK")
    void deleteComment_author_ok_200() {
        long commentId = 2001L;
        long userId = 42L;

        doNothing().when(communityService).deleteComment(eq(commentId));

        ResponseEntity<?> resp = controller.deleteComment(commentId);

        assertThat(resp.getStatusCode()).isEqualTo(OK);
        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 작성자 아님: 403 전파")
    void deleteComment_notAuthor_propagates403() {
        long commentId = 2003L;
        long userId = 7L;

        doThrow(new ResponseStatusException(FORBIDDEN, "FORBIDDEN"))
                .when(communityService).deleteComment(eq(commentId));

        assertThrows(ResponseStatusException.class,
                () -> controller.deleteComment(commentId));

        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 존재하지 않는 commentId: 404 전파")
    void deleteComment_notFound_propagates404() {
        long commentId = 404404L;
        long userId = 42L;

        doThrow(new ResponseStatusException(NOT_FOUND, "COMMENT_NOT_FOUND"))
                .when(communityService).deleteComment(eq(commentId));

        assertThrows(ResponseStatusException.class,
                () -> controller.deleteComment(commentId));

        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("작성글 목록 - 비로그인: 200 OK (서비스 호출만 검증)")
    void list_byWriter_anonymous_ok() {
        long writerId = 300L;
        List<PostResponseDto> list = List.of(
                sampleDetail(3L, 10L, false),
                sampleDetail(2L, 10L, false),
                sampleDetail(1L, 10L, false)
        );

        when(communityService.getPostListByUserId(eq(writerId), isNull()))
                .thenReturn(list);

        ResponseEntity<?> resp = controller.getPostByUserId(null, writerId);

        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).getPostListByUserId(eq(writerId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    // =========================
    // 작성글 목록 - 로그인 성공
    // =========================
    @Test
    @DisplayName("작성글 목록 - 로그인: 200 OK (서비스 호출만 검증)")
    void list_byWriter_authenticated_ok() {
        long writerId = 301L;
        long viewerId = 42L;

        List<PostResponseDto> list = List.of(
                sampleDetail(5L, 20L, true),
                sampleDetail(4L, 20L, false)
        );

        when(communityService.getPostListByUserId(eq(writerId), eq(viewerId)))
                .thenReturn(list);

        ResponseEntity<?> resp = controller.getPostByUserId(mockUser(viewerId), writerId);

        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).getPostListByUserId(eq(writerId), eq(viewerId));
        verifyNoMoreInteractions(communityService);
    }

    // =========================
    // 작성글 목록 - 404 전파
    // =========================
    @Test
    @DisplayName("작성글 목록 - 존재하지 않는 writerId: ResponseStatusException(404) 전파")
    void list_byWriter_notFound_propagates404() {
        long writerId = 404404L;

        when(communityService.getPostListByUserId(eq(writerId), isNull()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "USER_NOT_FOUND"));

        assertThrows(ResponseStatusException.class,
                () -> controller.getPostByUserId(null, writerId));

        verify(communityService).getPostListByUserId(eq(writerId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    // =========================
    // 내가 작성한 글 - 로그인 성공(빈 리스트)
    // =========================
    @Test
    @DisplayName("내가 작성한 글 목록 - 로그인 성공 & 결과 0건: 200 OK")
    void list_myPosts_authenticated_ok_empty() {
        long userId = 42L;

        when(communityService.getPostListByUserId(eq(userId), eq(userId)))
                .thenReturn(List.of());

        ResponseEntity<?> resp = controller.getMyPosts(mockUser(userId));

        assertThat(resp.getStatusCode()).isEqualTo(OK);
        assertThat(resp.getBody()).isNotNull();
        verify(communityService).getPostListByUserId(eq(userId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }
}