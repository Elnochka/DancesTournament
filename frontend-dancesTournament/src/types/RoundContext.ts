
export interface CategoryTournament {
  id: number
  nameCategory: string
}

export interface Dance {
  id: number
  nameDance: string
}

export interface RoundSettings {
  id: number
  categoryTournamentId: number
  round: string
  roundOrder?: number
  couplesToNextRound: number
}

export interface RoundContext {
  categoryId: number
  roundSettingsId: number
  round: string
  danceId?: number
  couplesToNextRound: number
}