

import api from './axiosInstance'
import type { ActiveDanceState } from '../types/ActiveDanceState'

export async function getActiveDance(): Promise<ActiveDanceState | null> {
  const response = await api.get<ActiveDanceState | null>('/active-dance')
  return response.data
}

export async function updateActiveDance(
  state: ActiveDanceState,
): Promise<ActiveDanceState> {
  const response = await api.put<ActiveDanceState>('/active-dance', state)
  return response.data
}
