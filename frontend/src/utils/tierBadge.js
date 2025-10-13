const tierBadgeMap = {
  BRONZE: new URL('../../resource/tier_badge/BRONZE.png', import.meta.url).href,
  SILVER: new URL('../../resource/tier_badge/SILVER.png', import.meta.url).href,
  GOLD: new URL('../../resource/tier_badge/GOLD.png', import.meta.url).href,
  DIAMOND: new URL('../../resource/tier_badge/DIAMOND.png', import.meta.url).href,
}

export function getTierBadgeSrc(tierCode) {
  if (!tierCode) {
    return tierBadgeMap.BRONZE
  }
  const normalized = tierCode.toString().toUpperCase()
  return tierBadgeMap[normalized] ?? tierBadgeMap.BRONZE
}

export const availableTierBadges = Object.freeze({ ...tierBadgeMap })
