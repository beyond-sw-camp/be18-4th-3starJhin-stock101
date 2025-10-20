package com.monstersinc.stock101.news;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.monstersinc.stock101.news.model.mapper.NewsMapper;
import com.monstersinc.stock101.news.model.service.NewsServiceImpl;
import com.monstersinc.stock101.news.model.vo.News;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsServiceImpl newsService;

    @Test
    void getPopularNews_정상_리턴() {
        // given
        List<News> mockList = List.of(new News(), new News());
        given(newsMapper.selectPopularNews()).willReturn(mockList);

        // when
        List<News> result = newsService.getPopularNews();

        // then
        assertThat(result).hasSize(2);
        verify(newsMapper).selectPopularNews();
    }

    @Test
    void clickNews_정상_호출() {
        // given
        long newsId = 1L;

        // when
        newsService.clickNews(newsId);

        // then
        verify(newsMapper).updateNewsClick(newsId);
    }
}
