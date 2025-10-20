package com.monstersinc.stock101.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.monstersinc.stock101.auth.jwt.JwtTokenProvider;
import com.monstersinc.stock101.auth.jwt.JwtUtil;
import com.monstersinc.stock101.community.controller.CommunityController;
import com.monstersinc.stock101.community.model.dto.CommentRequestDto;
import com.monstersinc.stock101.community.model.dto.CommentResponseDto;
import com.monstersinc.stock101.community.model.dto.PostRequestDto;
import com.monstersinc.stock101.community.model.dto.PostResponseDto;
import com.monstersinc.stock101.community.model.service.CommunityService;
import com.monstersinc.stock101.user.model.vo.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = CommunityController.class)
public class CommunityServiceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtUtil jwtUtil;


    // --- Security config for @WebMvcTest slice ---
    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            // 기본 정책: 모든 요청 인증 필요, CSRF 활성화(기본)
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/api/v1/board/posts/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/board/user/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .csrf(Customizer.withDefaults())
                    .httpBasic(Customizer.withDefaults())
                    .formLogin(Customizer.withDefaults());
            return http.build();
        }
    }

    // --- 테스트에서 사용할 가짜 인증 principal ---
    static class TestUser {
        private final long userId;
        TestUser(long userId) { this.userId = userId; }
        public long getUserId() { return userId; }
        @Override public String toString() { return "TestUser(" + userId + ")"; }
    }

    private Authentication authUser(long userId) {
        User principal = User.builder()
                .userId(userId)
                // 필요한 필드만 최소 채우기
                .build();

        return new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
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

    /** 응답에서 posts 배열 노드를 안전하게 추출 (BaseResponseDto 유무와 상관없이) */
    private ArrayNode extractPostsArray(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        // 1) data 래퍼가 있으면 먼저 꺼냄
        JsonNode node = root.has("data") ? root.get("data") : root;

        // 2) 바로 배열이면 반환
        if (node.isArray()) {
            return (ArrayNode) node;
        }

        // 3) 흔한 컨테이너 키들 처리
        // Page 형태: content
        if (node.has("content") && node.get("content").isArray()) {
            return (ArrayNode) node.get("content");
        }
        // 커스텀 목록 키: items
        if (node.has("items") && node.get("items").isArray()) {
            return (ArrayNode) node.get("items");
        }

        // 4) data 안쪽에 다시 items/content가 들어간 경우까지 보강
        if (root.has("data")) {
            JsonNode data = root.get("data");
            if (data.has("items") && data.get("items").isArray()) {
                return (ArrayNode) data.get("items");
            }
            if (data.has("content") && data.get("content").isArray()) {
                return (ArrayNode) data.get("content");
            }
            if (data.isArray()) {
                return (ArrayNode) data;
            }
        }

        throw new AssertionError("응답에서 게시글 배열을 찾지 못했습니다: " + root);
    }

    private ArrayNode extractArray(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        // 1) data 래퍼 우선 분리
        JsonNode node = root.has("data") ? root.get("data") : root;

        // 2) 현재 노드가 배열이면 그대로
        if (node.isArray()) return (ArrayNode) node;

        // 3) 흔한 컨테이너 키들
        String[] keys = {"items", "content", "comments", "list"};
        for (String k : keys) {
            if (node.has(k) && node.get(k).isArray()) {
                return (ArrayNode) node.get(k);
            }
        }

        // 4) data 안쪽에 다시 배열 키가 있는 케이스
        if (root.has("data")) {
            JsonNode data = root.get("data");
            for (String k : keys) {
                if (data.has(k) && data.get(k).isArray()) {
                    return (ArrayNode) data.get(k);
                }
            }
            if (data.isArray()) return (ArrayNode) data;
        }

        throw new AssertionError("응답에서 댓글 배열을 찾지 못했습니다: " + root);
    }

    private CommentResponseDto sampleCommentResponse(
            long commentId,
            long postId,
            long userId,
            String content,
            OffsetDateTime createdAt
    ) {
        return CommentResponseDto.builder()
                .commentId(commentId)
                .content(content)
                .createdAt(createdAt.withNano(0).toString())
                .isDeleted(false)
                .postId(postId)
                .userId(userId)
                .parentCommentId(null)
                .userName("tester")
                .authorTierCode("BRONZE")
                .totalCommentCount(1)
                .imageUrl("https://example.com/tester.jpg")
                .build();
    }

    private String longString(int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, 'a');
        return new String(arr);
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ---------- 테스트 본문 ----------

    @Test
    @DisplayName("게시물 등록 - 201 Created & 응답 바디/서비스 호출 파라미터 검증(opinion 포함)")
    void createPost_success_201() throws Exception {
        // given
        long userId = 11L;
        long newPostId = 501L;

        Map<String, Object> body = Map.of(
                "stockId", 123L,
                "opinion", "매수 의견입니다",
                "content", "근거는 어쩌구 저쩌구"
        );

        when(communityService.saveAPost(eq(userId), any(PostRequestDto.class)))
                .thenReturn(newPostId);

        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setPostId(newPostId);
        responseDto.setStockId(123L);
        responseDto.setOpinion("매수 의견입니다");
        responseDto.setContent("근거는 어쩌구 저쩌구");
        responseDto.setLikedByMe(true);
        responseDto.setCreatedAt(OffsetDateTime.now().toString());
        // 필요한 다른 필드들도 세팅 가능
        when(communityService.getPostDetail(newPostId, userId))
                .thenReturn(responseDto);

        // when
        var result = mockMvc.perform(
                post("/api/v1/board/posts")
                        .with(authentication(authUser(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body))

        );

        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items[0].postId").value((int) newPostId))
                .andExpect(jsonPath("$.items[0].stockId").value(123))
                .andExpect(jsonPath("$.items[0].opinion").value("매수 의견입니다"))
                .andExpect(jsonPath("$.items[0].likedByMe").value(true));

        // 서비스 파라미터 캡처 & DTO 필드 확인
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
    @DisplayName("게시물 등록 - 비로그인 401 Unauthorized")
    void createPost_unauthenticated_401() throws Exception {
        Map<String, Object> body = Map.of(
                "stockId", 123L,
                "opinion", "매수",
                "content", "내용"
        );

        var result = mockMvc.perform(
                post("/api/v1/board/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body))
        );

        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 등록 - CSRF 미포함 403 Forbidden")
    void createPost_missingCsrf_403() throws Exception {
        Map<String, Object> body = Map.of(
                "stockId", 123L,
                "opinion", "매수",
                "content", "내용"
        );

        var result = mockMvc.perform(
                post("/api/v1/board/posts")
                        .with(authentication(authUser(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body))
        );

        result.andExpect(status().isForbidden());
        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("게시물 등록 - @Valid 실패 400 (opinion 누락 / content 길이 초과 등)")
    void createPost_validationFail_400() throws Exception {
        // 1) opinion 누락 (@NotBlank -> OPINION_REQUIRED)
        Map<String, Object> missingOpinion = Map.of(
                "stockId", 123L,
                "content", "내용"
        );

        mockMvc.perform(
                        post("/api/v1/board/posts")
                                .with(authentication(authUser(11L)))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(missingOpinion))
                )
                .andExpect(status().isBadRequest());
        // 컨트롤러의 에러 응답 바디 규약에 맞춰 jsonPath 추가 검증 가능
        // .andExpect(jsonPath("$.message").value("OPINION_REQUIRED"));

        // 2) content 300자 초과 (@Size(max=300) -> CONTENT_TOO_LONG)
        String over300 = "a".repeat(301);
        Map<String, Object> tooLongContent = Map.of(
                "stockId", 123L,
                "opinion", "매수",
                "content", over300
        );

        mockMvc.perform(
                        post("/api/v1/board/posts")
                                .with(authentication(authUser(11L)))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(tooLongContent))
                )
                .andExpect(status().isBadRequest());

        // 서비스는 어떤 경우에도 호출되지 않아야 함
        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("로그인 O → 200 OK, likedByMe=true 등 사용자 맥락 필드 반영")
    void detail_authenticated_200_likedByMeTrue() throws Exception {
        // given
        long userId = 77L;
        long postId = 501L;

        when(communityService.getPostDetail(postId, userId))
                .thenReturn(sampleDetail(postId, 123L, true));

        // when
        var result = mockMvc.perform(
                get("/api/v1/board/posts/{postId}", postId)
                        .with(authentication(authUser(userId)))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].postId").value((int) postId))
                .andExpect(jsonPath("$.items[0].stockId").value(123))
                .andExpect(jsonPath("$.items[0].likedByMe").value(true));

        // 서비스 호출 파라미터 검증
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(communityService).getPostDetail(eq(postId), userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("로그인 X → 200 OK, likedByMe=false(또는 계약대로)")
    void detail_unauthenticated_200_likedByMeFalse() throws Exception {
        // given
        long postId = 888L;

        // 비로그인 규약: 컨트롤러가 userId=null로 서비스 호출
        when(communityService.getPostDetail(postId, null))
                .thenReturn(sampleDetail(postId, 321L, false)); // 또는 null/기본값, 계약에 맞게

        // when
        var result = mockMvc.perform(
                get("/api/v1/board/posts/{postId}", postId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].postId").value((int) postId))
                .andExpect(jsonPath("$.items[0].stockId").value(321))
                .andExpect(jsonPath("$.items[0].likedByMe").value(false));

        // userId=null로 호출되었는지 검증
        verify(communityService).getPostDetail(postId, null);
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("존재하지 않는 postId → 404 Not Found")
    void detail_notFound_404() throws Exception {
        // given
        long postId = 404404L;

        when(communityService.getPostDetail(eq(postId), any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"));

        // when
        var result = mockMvc.perform(
                get("/api/v1/board/posts/{postId}", postId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(communityService).getPostDetail(eq(postId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("비로그인: 200 OK & 크기/정렬 확인, likedByMe 계약(false 또는 null) 확인")
    void list_byStock_anonymous_200_size_and_sort_and_likedByMe_contract() throws Exception {
        long stockId = 777L;

        // 정렬: 최신이 먼저라고 가정하여 postId 3,2,1 순서 예시
        List<PostResponseDto> list = List.of(
                sampleDetail(3L, stockId, false),
                sampleDetail(2L, stockId, false),
                sampleDetail(1L, stockId, false)
        );

        when(communityService.getPostListByStock(eq(stockId), isNull()))
                .thenReturn(list);

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/posts")
                                .param("stockId", String.valueOf(stockId))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        // 리스트 크기 확인
        assertEquals(3, arr.size(), "게시글 개수");

        // 정렬(예: postId 내림차순 가정)
        assertEquals(3L, arr.get(0).get("postId").asLong());
        assertEquals(2L, arr.get(1).get("postId").asLong());
        assertEquals(1L, arr.get(2).get("postId").asLong());

        // likedByMe 계약: 비로그인일 때 false 또는 null을 허용 (프로젝트 정책에 따라 한쪽만 쓰면 바꿔도 됨)
        for (JsonNode n : arr) {
            if (n.has("likedByMe") && !n.get("likedByMe").isNull()) {
                assertFalse(n.get("likedByMe").asBoolean(), "비로그인에서는 likedByMe=false 이어야 함");
            }
        }

        verify(communityService).getPostListByStock(eq(stockId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("로그인: 200 OK & 크기/정렬 + likedByMe=true/false 반영 확인")
    void list_byStock_authenticated_200_size_sort_and_likedByMe_reflected() throws Exception {
        long stockId = 888L;
        long userId = 42L;

        List<PostResponseDto> list = List.of(
                sampleDetail(5L, stockId, true),
                sampleDetail(4L, stockId, false)
        );

        when(communityService.getPostListByStock(eq(stockId), eq(userId)))
                .thenReturn(list);

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/posts")
                                .param("stockId", String.valueOf(stockId))
                                .with(authentication(authUser(userId)))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        assertEquals(2, arr.size());
        assertEquals(5L, arr.get(0).get("postId").asLong());
        assertEquals(4L, arr.get(1).get("postId").asLong());

        assertTrue(arr.get(0).get("likedByMe").asBoolean(), "첫 번째 글은 좋아요=true");
        assertFalse(arr.get(1).get("likedByMe").asBoolean(), "두 번째 글은 좋아요=false");

        verify(communityService).getPostListByStock(eq(stockId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("stockId 누락 → 400")
    void list_byStock_missingParam_400() throws Exception {
        mockMvc.perform(
                        get("/api/v1/board/posts")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("stockId 비정상(숫자 아님) → 400")
    void list_byStock_invalidParam_400() throws Exception {
        mockMvc.perform(
                        get("/api/v1/board/posts")
                                .param("stockId", "abc")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("작성자(로그인) 성공: 200 OK")
    void deletePost_author_200_and_message() throws Exception {
        long postId = 1001L;
        long userId = 42L;

        doNothing().when(communityService).delete(eq(postId));

        mockMvc.perform(
                        delete("/api/v1/board/posts/{postId}", postId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                // 응답 래핑 형태에 따라 아래 두 줄 중 하나를 선택해서 사용
                .andExpect(jsonPath("$.message").value("OK"))
        // .andExpect(jsonPath("$.data.message").value("게시글이 삭제되었습니다."))
        ;

        verify(communityService).delete(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("비로그인: 401 Unauthorized (보안 슬라이스상 DELETE는 인증 필요)")
    void deletePost_unauthenticated_401() throws Exception {
        long postId = 1002L;

        mockMvc.perform(
                        delete("/api/v1/board/posts/{postId}", postId)
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("작성자 아님(로그인): 403 Forbidden")
    void deletePost_notAuthor_403() throws Exception {
        long postId = 1003L;
        long otherUserId = 7L;

        // 컨트롤러/핸들러에서 그대로 전파된다고 가정
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN"))
                .when(communityService).delete(eq(postId));

        mockMvc.perform(
                        delete("/api/v1/board/posts/{postId}", postId)
                                .with(authentication(authUser(otherUserId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(communityService).delete(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("존재하지 않는 postId: 404 Not Found")
    void deletePost_notFound_404() throws Exception {
        long postId = 404404L;
        long userId = 42L;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).delete(eq(postId));

        mockMvc.perform(
                        delete("/api/v1/board/posts/{postId}", postId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(communityService).delete(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("좋아요 토글 - 200 OK & 토글 반영된 최신 PostResponseDto 반환")
    void likeToggle_authenticated_200_returnsUpdatedDto() throws Exception {
        // given
        long userId = 99L;
        long postId = 777L;
        long stockId = 321L;

        // 토글 결과가 반영된 최신 상세 DTO를 리턴한다고 가정 (likedByMe = true 로 예시)
        PostResponseDto updated = sampleDetail(postId, stockId, true);

        when(communityService.likePost(eq(postId), eq(userId)))
                .thenReturn(updated);

        // when
        var result = mockMvc.perform(
                post("/api/v1/board/posts/{postId}/like", postId)
                        .with(authentication(authUser(userId)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].postId").value((int) postId))
                .andExpect(jsonPath("$.items[0].likedByMe").value(true));

        verify(communityService).likePost(eq(postId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("좋아요 토글 - 인증 X → 401 Unauthorized")
    void likeToggle_unauthenticated_401() throws Exception {
        // given
        long postId = 888L;

        // when
        mockMvc.perform(
                        post("/api/v1/board/posts/{postId}/like", postId)
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("좋아요 토글 - 존재하지 않는 postId → 404 Not Found")
    void likeToggle_notFound_404() throws Exception {
        // given
        long userId = 55L;
        long postId = 404404L;

        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).likePost(eq(postId), eq(userId));

        // when
        mockMvc.perform(
                        post("/api/v1/board/posts/{postId}/like", postId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(communityService).likePost(eq(postId), eq(userId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 리스트 - 200 OK & 리스트 구조/정렬(최근순) 확인")
    void commentsList_200_and_sorted_desc() throws Exception {
        // given
        long postId = 1001L;

        // createdAt: c2(최신) -> c1(이전)
        var now = OffsetDateTime.now();
        var c1 = Map.of(
                "commentId", 11L,
                "content", "첫 댓글",
                "authorId", 21L,
                "authorNickname", "alice",
                "createdAt", now.minusMinutes(3).toString()
        );
        var c2 = Map.of(
                "commentId", 12L,
                "content", "두번째 댓글(가장 최신)",
                "authorId", 22L,
                "authorNickname", "bob",
                "createdAt", now.minusMinutes(1).toString()
        );

        // 서비스 레이어 모킹
        when(communityService.getCommentListByPostId(eq(postId)))
                .thenReturn((List) List.of(c2, c1)); // 최신순으로 반환

        // when
        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/posts/{postId}/comments", postId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // 정렬(최근순) 검증: createdAt 역순(c2, c1)
        String json = mvcResult.getResponse().getContentAsString();
        var arr = extractArray(json); // 응답에서 최상위/래핑 내부 배열 안전 추출

        assertThat(arr).isNotNull();
        assertThat(arr.size()).isGreaterThanOrEqualTo(2);

        OffsetDateTime firstCreated = OffsetDateTime.parse(arr.get(0).get("createdAt").asText());
        OffsetDateTime secondCreated = OffsetDateTime.parse(arr.get(1).get("createdAt").asText());
        assertThat(firstCreated).isAfter(secondCreated);

        // 아이디까지 확인(옵션)
        assertThat(arr.get(0).get("commentId").asLong()).isEqualTo(12L);
        assertThat(arr.get(1).get("commentId").asLong()).isEqualTo(11L);

        verify(communityService).getCommentListByPostId(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 리스트 - 존재하지 않는 postId → 404 Not Found")
    void commentsList_notFound_404() throws Exception {
        // given
        long postId = 404404L;

        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).getCommentListByPostId(eq(postId));

        // when & then
        mockMvc.perform(
                        get("/api/v1/board/posts/{postId}/comments", postId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(communityService).getCommentListByPostId(eq(postId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 등록 - 201 Created & 생성된 댓글 바디 반환")
    void createComment_authenticated_201_returnsCreatedBody() throws Exception {
        // given
        long userId = 99L;
        long postId = 1001L;
        long commentId = 5001L;

        var request = new CommentRequestDto();
        request.setContent("첫 댓글입니다!");
        request.setUserId(userId);
        request.setPostId(postId);
        request.setParentCommentId(null);
        var createdAt = OffsetDateTime.now().withNano(0);

        // 생성된 댓글 DTO (예시)
        var createdDto = sampleCommentResponse(commentId, postId, userId, "첫 댓글입니다!", createdAt);

        when(communityService.saveAComment(any(CommentRequestDto.class)))
                .thenReturn(commentId);
        when(communityService.getAComment(eq(commentId)))
                .thenReturn(createdDto);

        // when
        var result = mockMvc.perform(
                post("/api/v1/board/posts/{postId}/comments", postId)
                        .with(authentication(authUser(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].commentId").value((int) commentId))
                .andExpect(jsonPath("$.items[0].postId").value((int) postId))
                .andExpect(jsonPath("$.items[0].userId").value((int) userId))
                .andExpect(jsonPath("$.items[0].content").value("첫 댓글입니다!"));

        verify(communityService).saveAComment(any(CommentRequestDto.class));
        verify(communityService).getAComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 등록 - 인증 X → 401 Unauthorized")
    void createComment_unauthenticated_401() throws Exception {
        // given
        long postId = 1001L;
        var request = new CommentRequestDto();
        request.setContent("비로그인 요청");
        request.setUserId(999L);
        request.setPostId(1001L);
        request.setParentCommentId(null);

        // when & then
        mockMvc.perform(
                        post("/api/v1/board/posts/{postId}/comments", postId)
                                .with(csrf()) // CSRF는 넣어서 인증 실패만 분리
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 등록 - CSRF 미포함 → 403 Forbidden")
    void createComment_csrfMissing_403() throws Exception {
        // given
        long userId = 99L;
        long postId = 1001L;
        var request = new CommentRequestDto();
        request.setContent("CSRF 없음");
        request.setUserId(99L);
        request.setPostId(1001L);
        request.setParentCommentId(null);

        // when & then
        mockMvc.perform(
                        post("/api/v1/board/posts/{postId}/comments", postId)
                                .with(authentication(authUser(userId)))
                                // .with(csrf()) 없음!
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 등록 - postId 존재 X → 404 Not Found")
    void createComment_notFound_404() throws Exception {
        // given
        long userId = 99L;
        long postId = 404404L;
        var request = new CommentRequestDto();
        request.setContent("대상 글 없음");
        request.setUserId(99L);
        request.setPostId(404404L);
        request.setParentCommentId(null);

        doThrow(new ResponseStatusException(NOT_FOUND, "POST_NOT_FOUND"))
                .when(communityService).saveAComment(any(CommentRequestDto.class));

        // when & then
        mockMvc.perform(
                        post("/api/v1/board/posts/{postId}/comments", postId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(communityService).saveAComment(any(CommentRequestDto.class));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 작성자(로그인) 성공: 200 OK & 메시지")
    void deleteComment_author_200_and_message() throws Exception {
        long commentId = 2001L;
        long userId = 42L;

        // 서비스 정상 호출
        doNothing().when(communityService).deleteComment(eq(commentId));

        mockMvc.perform(
                        delete("/api/v1/board/comments/{commentId}", commentId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                // 프로젝트의 응답 래핑 규약에 맞춰 선택
                .andExpect(jsonPath("$.message").value("OK"));
        // .andExpect(jsonPath("$.data.message").value("댓글이 삭제되었습니다."));

        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 비로그인: 401 Unauthorized (DELETE는 인증 필요)")
    void deleteComment_unauthenticated_401() throws Exception {
        long commentId = 2002L;

        mockMvc.perform(
                        delete("/api/v1/board/comments/{commentId}", commentId)
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 작성자 아님(로그인): 403 Forbidden")
    void deleteComment_notAuthor_403() throws Exception {
        long commentId = 2003L;
        long otherUserId = 7L;

        // 컨트롤러/예외핸들러에서 그대로 전파된다고 가정
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN"))
                .when(communityService).deleteComment(eq(commentId));

        mockMvc.perform(
                        delete("/api/v1/board/comments/{commentId}", commentId)
                                .with(authentication(authUser(otherUserId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("댓글 삭제 - 존재하지 않는 commentId: 404 Not Found")
    void deleteComment_notFound_404() throws Exception {
        long commentId = 404404L;
        long userId = 42L;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND"))
                .when(communityService).deleteComment(eq(commentId));

        mockMvc.perform(
                        delete("/api/v1/board/comments/{commentId}", commentId)
                                .with(authentication(authUser(userId)))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(communityService).deleteComment(eq(commentId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("작성글 목록 - 비로그인: 200 OK & 크기/정렬 확인 + likedByMe 계약(false 또는 null) 확인")
    void list_byWriter_anonymous_200_size_sort_likedByMe_contract() throws Exception {
        long writerId = 300L;

        // 최신이 먼저라고 가정하여 postId 3,2,1
        List<PostResponseDto> list = List.of(
                sampleDetail(3L, /*stockId*/ 10L, false),
                sampleDetail(2L, 10L, false),
                sampleDetail(1L, 10L, false)
        );

        // 비로그인: userId=null로 서비스 호출
        when(communityService.getPostListByUserId(eq(writerId), isNull()))
                .thenReturn(list);

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/user/{writerId}", writerId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        // 크기
        assertEquals(3, arr.size(), "게시글 개수");
        // 정렬 (postId 내림차순 가정)
        assertEquals(3L, arr.get(0).get("postId").asLong());
        assertEquals(2L, arr.get(1).get("postId").asLong());
        assertEquals(1L, arr.get(2).get("postId").asLong());

        // likedByMe 계약: 비로그인일 때 false 또는 null 허용
        for (JsonNode n : arr) {
            if (n.has("likedByMe") && !n.get("likedByMe").isNull()) {
                assertFalse(n.get("likedByMe").asBoolean(), "비로그인에서는 likedByMe=false 이어야 함");
            }
        }

        verify(communityService).getPostListByUserId(eq(writerId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("작성글 목록 - 로그인: 200 OK & 정렬/likedByMe 정확성")
    void list_byWriter_authenticated_200_size_sort_and_likedByMe_values() throws Exception {
        long writerId = 301L;
        long viewerId = 42L;

        // 예시: postId 5,4 (최신 우선), 첫 글은 좋아요 true, 두 번째는 false
        List<PostResponseDto> list = List.of(
                sampleDetail(5L, 20L, true),
                sampleDetail(4L, 20L, false)
        );

        when(communityService.getPostListByUserId(eq(writerId), eq(viewerId)))
                .thenReturn(list);

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/user/{writerId}", writerId)
                                .with(authentication(authUser(viewerId)))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        assertEquals(2, arr.size(), "게시글 개수");
        // 정렬 확인
        assertEquals(5L, arr.get(0).get("postId").asLong());
        assertEquals(4L, arr.get(1).get("postId").asLong());

        // 좋아요 여부 계약 확인(로그인 시 명시 true/false)
        assertTrue(arr.get(0).get("likedByMe").asBoolean(), "첫 번째 글 likedByMe=true");
        assertFalse(arr.get(1).get("likedByMe").asBoolean(), "두 번째 글 likedByMe=false");

        verify(communityService).getPostListByUserId(eq(writerId), eq(viewerId));
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("작성글 목록 - 존재하지 않는 writerId: 404 Not Found")
    void list_byWriter_notFound_404() throws Exception {
        long writerId = 404404L;

        when(communityService.getPostListByUserId(eq(writerId), isNull()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        mockMvc.perform(
                        get("/api/v1/board/user/{writerId}", writerId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(communityService).getPostListByUserId(eq(writerId), isNull());
        verifyNoMoreInteractions(communityService);
    }

    @Test
    @DisplayName("작성글 목록 - 로그인: 200 OK & 정렬/likedByMe 정확성 + 모든 글의 작성자 일치")
    void list_myPosts_authenticated_200_and_onlyMine() throws Exception {
        long writerId = 301L;
        long viewerId = 42L;

        // 예시: postId 5,4 (최신 우선), 첫 글은 좋아요 true, 두 번째는 false
        PostResponseDto a = sampleDetail(5L, 20L, true);
        a.setUserId(writerId);
        PostResponseDto b = sampleDetail(4L, 20L, false);
        b.setUserId(writerId);
        List<PostResponseDto> list = List.of(a, b);
        // ✅ 서비스 시그니처가 getPostListByUserId(writerId, viewerId)라면 그대로 사용
        when(communityService.getPostListByUserId(eq(writerId), eq(viewerId)))
                .thenReturn(list);

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/user/{writerId}", writerId)
                                .with(authentication(authUser(viewerId)))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        assertEquals(2, arr.size(), "게시글 개수");

        // 정렬(현재는 postId 내림차순 가정)
        assertEquals(5L, arr.get(0).get("postId").asLong());
        assertEquals(4L, arr.get(1).get("postId").asLong());

        // ✅ likedByMe는 로그인 시 명시적 boolean 이어야 함(누락/널 금지)
        assertTrue(arr.get(0).hasNonNull("likedByMe"), "likedByMe 키가 누락되면 안 됩니다");
        assertTrue(arr.get(1).hasNonNull("likedByMe"), "likedByMe 키가 누락되면 안 됩니다");
        assertTrue(arr.get(0).get("likedByMe").isBoolean(), "likedByMe는 boolean 이어야 합니다");
        assertTrue(arr.get(1).get("likedByMe").isBoolean(), "likedByMe는 boolean 이어야 합니다");
        assertTrue(arr.get(0).get("likedByMe").asBoolean(), "첫 번째 글 likedByMe=true");
        assertFalse(arr.get(1).get("likedByMe").asBoolean(), "두 번째 글 likedByMe=false");

        // ✅ 모든 항목의 작성자가 요청한 writerId와 동일해야 함
        for (JsonNode n : arr) {
            Long actualWriterId = readWriterId(n); // 아래 헬퍼 사용
            assertNotNull(actualWriterId, "작성자 식별자를 찾을 수 없습니다: " + n.toString());
            assertEquals(writerId, actualWriterId.longValue(), "모든 게시글의 작성자는 writerId여야 함");
        }

        verify(communityService).getPostListByUserId(eq(writerId), eq(viewerId));
        verifyNoMoreInteractions(communityService);
    }

    /** 응답에서 작성자 id를 다양한 키로 안전 추출 (프로젝트 구조에 맞게 줄여도 됨) */
    private Long readWriterId(JsonNode n) {
        for (String k : new String[]{"writerId", "userId", "authorId"}) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull()) return v.asLong();
        }
        for (String p : new String[]{"/writer/userId", "/user/userId", "/author/id"}) {
            JsonNode v = n.at(p);
            if (v != null && !v.isMissingNode() && !v.isNull()) return v.asLong();
        }
        return null;
    }

    @Test
    @DisplayName("내가 작성한 글 목록 - 비로그인: 401 Unauthorized")
    void list_myPosts_unauthenticated_401() throws Exception {
        mockMvc.perform(
                        get("/api/v1/board/me")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(communityService);
    }

    @Test
    @DisplayName("내가 작성한 글 목록 - 로그인 성공 & 결과 0건: 200 OK & 빈 리스트 반환")
    void list_myPosts_authenticated_200_emptyList() throws Exception {
        long userId = 42L;

        when(communityService.getPostListByUserId(eq(userId) ,eq(userId)))
                .thenReturn(Collections.emptyList());

        var mvcResult = mockMvc.perform(
                        get("/api/v1/board/me")
                                .with(authentication(authUser(userId)))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ArrayNode arr = extractPostsArray(json);

        assertEquals(0, arr.size(), "게시글이 없을 때 빈 리스트여야 함");

        verify(communityService).getPostListByUserId(eq(userId) ,eq(userId));
        verifyNoMoreInteractions(communityService);
    }
}