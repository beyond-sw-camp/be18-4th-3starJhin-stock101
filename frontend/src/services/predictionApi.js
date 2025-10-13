import apiClient from '@/api/index.js'

/**
 * 현재 사용자의 예측 목록을 가져옵니다
 */
export const getUserPredictions = async () => {
  try {
    const response = await apiClient.get('/api/v1/prediction/user/me')
    return response.data
  } catch (error) {
    console.error('Failed to fetch user predictions:', error)
    throw error
  }
}

/**
 * 특정 사용자의 예측 목록을 가져옵니다
 */
export const getUserPredictionsById = async (userId) => {
  try {
    const response = await apiClient.get(`/api/v1/prediction/user/${userId}`)
    return response.data
  } catch (error) {
    console.error(`Failed to fetch predictions for user ${userId}:`, error)
    throw error
  }
}

/**
 * API 응답 데이터를 컴포넌트에서 사용할 형태로 변환합니다
 */
export const transformPredictionData = (apiResponse) => {
  if (!apiResponse || !apiResponse.items) {
    return []
  }

  return apiResponse.items.map((prediction) => ({
    predictionId: prediction.predictionId,
    stockId: prediction.stockId,
    stockName: prediction.stockName || `Stock ${prediction.stockId}`, // Use actual stockName from API or fallback
    userId: prediction.userId,
    author: `User ${prediction.userId}`, // 실제로는 사용자 이름 API에서 가져와야 함
    predictedAt: prediction.predictedAt,
    prediction: prediction.prediction,
    result: prediction.result, // null 값을 그대로 유지
    evaluatedAt: prediction.evaluatedAt,
    actualPrice: prediction.actualPrice,
    sentimentLabel: prediction.prediction === 'UP' ? 'Buy' : 'Sell',
    content: `${prediction.prediction} 예측`, // 실제로는 예측 내용이 있어야 함
    likes: Math.floor(Math.random() * 50), // 임시 데이터
    comments: Math.floor(Math.random() * 20), // 임시 데이터
  }))
}
