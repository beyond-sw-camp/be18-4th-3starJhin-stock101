const basePredictions = [
  {
    predictionId: 1,
    stockId: 100,
    stockName: '테슬라',
    userId: 1,
    author: '사용자 이름',
    predictedAt: '2024-09-01T21:29:00',
    prediction: 'UP',
    result: 'SUCCESS',
    sentimentLabel: 'Strong Buy',
    content:
      '테슬라가 4분기 실적에서 기대를 뛰어넘었어요! 자율주행과 에너지 저장 사업에 집중하는 만큼 2024년엔 큰 성장이 기대됩니다. 저는 강력 매수 의견입니다!',
    likes: 24,
    comments: 8,
  },
  {
    predictionId: 2,
    stockId: 101,
    stockName: '애플',
    userId: 1,
    author: '사용자 이름',
    predictedAt: '2024-08-27T10:20:00',
    prediction: 'UP',
    result: 'FAILURE',
    sentimentLabel: 'Moderate Buy',
    content: '신제품 출시 모멘텀을 기대하며 단기 반등을 노리고 있습니다.',
    likes: 12,
    comments: 3,
  },
  {
    predictionId: 3,
    stockId: 102,
    stockName: '엔비디아',
    userId: 1,
    author: '사용자 이름',
    predictedAt: '2024-08-15T09:10:00',
    prediction: 'DOWN',
    result: 'SUCCESS',
    sentimentLabel: 'Take Profit',
    content: 'AI 서버 수요가 둔화될 수 있어 단기 조정을 예상합니다.',
    likes: 18,
    comments: 5,
  },
  {
    predictionId: 4,
    stockId: 103,
    stockName: '삼성전자',
    userId: 1,
    author: '사용자 이름',
    predictedAt: '2024-09-10T16:45:00',
    prediction: 'UP',
    result: 'PENDING',
    sentimentLabel: 'Strong Buy',
    content: '메모리 업황이 반등하고 있어 연말까지는 우상향 추세가 유지될 것으로 보입니다.',
    likes: 30,
    comments: 11,
  },
]

// Posts are now fetched from API - no mock data needed

export const myProfile = {
  user: {
    id: 1,
    name: '사용자 이름',
    statusMessage: '사용자 상태 메시지 (한 마디)',
    badge: 'PRO',
  },
  predictions: basePredictions,
  posts: [], // Posts will be fetched from API
}

export const otherProfile = {
  user: {
    id: 2,
    name: '다른 사용자',
    statusMessage: '최근 에너지 섹터를 집중 분석 중입니다.',
    badge: 'TOP10',
  },
  predictions: basePredictions.map((prediction) => ({
    ...prediction,
    userId: 2,
    author: '다른 사용자',
    predictionId: prediction.predictionId + 100,
  })),
  posts: [], // Posts will be fetched from API
}

export function calculatePredictionSummary(predictions) {
  const total = predictions.length
  const success = predictions.filter((item) => item.result === 'SUCCESS').length
  const failure = predictions.filter((item) => item.result === 'FAILURE').length
  const pending = predictions.filter(
    (item) => item.result === null || item.result === 'PENDING',
  ).length
  const resolved = success + failure
  const accuracy = resolved === 0 ? 0 : Math.round((success / resolved) * 100)

  return { total, success, failure, pending, accuracy }
}
