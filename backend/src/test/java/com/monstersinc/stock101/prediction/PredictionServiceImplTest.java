package com.monstersinc.stock101.prediction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.monstersinc.stock101.prediction.model.mapper.PredictionMapper;
import com.monstersinc.stock101.prediction.model.service.PredictionServiceImpl;
import com.monstersinc.stock101.prediction.model.vo.Prediction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private PredictionMapper predictionMapper;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    @Test
    void insertPrediction_정상호출() {
        // given
        Prediction mockPrediction = new Prediction();

        // when
        predictionService.insertPrediction(mockPrediction);

        // then
        verify(predictionMapper).insertPrediction(mockPrediction);
    }

    @Test
    void selectById_정상리턴() {
        // given
        Prediction mockPrediction = new Prediction();
        given(predictionMapper.selectById(1L)).willReturn(mockPrediction);

        // when
        Prediction result = predictionService.selectById(1L);

        // then
        assertThat(result).isEqualTo(mockPrediction);
        verify(predictionMapper).selectById(1L);
    }

    @Test
    void selectByUserId_정상리턴() {
        // given
        List<Prediction> mockList = List.of(new Prediction(), new Prediction());
        given(predictionMapper.selectByUserId(100L)).willReturn(mockList);

        // when
        List<Prediction> result = predictionService.selectByUserId(100L);

        // then
        assertThat(result).hasSize(2);
        verify(predictionMapper).selectByUserId(100L);
    }

    @Test
    void existsPendingPrediction_정상리턴_true() {
        // given
        given(predictionMapper.existsPendingPrediction(100L, 5L)).willReturn(true);

        // when
        boolean result = predictionService.existsPendingPrediction(100L, 5L);

        // then
        assertThat(result).isTrue();
        verify(predictionMapper).existsPendingPrediction(100L, 5L);
    }

    @Test
    void existsPendingPrediction_정상리턴_false() {
        // given
        given(predictionMapper.existsPendingPrediction(200L, 7L)).willReturn(false);

        // when
        boolean result = predictionService.existsPendingPrediction(200L, 7L);

        // then
        assertThat(result).isFalse();
        verify(predictionMapper).existsPendingPrediction(200L, 7L);
    }
}
