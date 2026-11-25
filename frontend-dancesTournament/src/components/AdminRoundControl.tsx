
import { useEffect, useState } from 'react'
import api from '../api/axiosInstance'
import './AdminRoundControl.css'
import type {
  CategoryTournament,
  Dance,
  RoundSettings,
  RoundContext,
} from '../types/RoundContext'

interface AdminRoundControlProps {
  onSelect: (context: RoundContext) => void
  value?: RoundContext | null
}

export default function AdminRoundControl({
  onSelect,
  value,
}: AdminRoundControlProps) {
  const [categories, setCategories] = useState<CategoryTournament[]>([])
  const [dances, setDances] = useState<Dance[]>([])
  const [roundSettingsList, setRoundSettingsList] = useState<RoundSettings[]>([])
  const [categoryId, setCategoryId] = useState(
    () => value?.categoryId?.toString() ?? '',
  )

  const [roundSettingsId, setRoundSettingsId] = useState(
    () => value?.roundSettingsId?.toString() ?? '',
  )

  const [danceId, setDanceId] = useState(
    () => value?.danceId?.toString() ?? '',
  )
  const [settingsLoading, setSettingsLoading] = useState(false)
  const [settingsError, setSettingsError] = useState('')

  useEffect(() => {
    if (!value) return

    setCategoryId(String(value.categoryId))
    setRoundSettingsId(String(value.roundSettingsId))
    setDanceId(
      value.danceId != null ? String(value.danceId) : '',
    )
  }, [
    value?.categoryId,
    value?.roundSettingsId,
    value?.danceId,
  ])

  useEffect(() => {
    const loadData = async () => {
      try {
        const [categoriesResponse] = await Promise.all([
          api.get<CategoryTournament[]>('/category-tournaments'),
        ])
        setCategories(categoriesResponse.data)
      } catch (error) {
        console.error('Error loading categories:', error)
      }
    }
    void loadData()
  }, [])

  useEffect(() => {
    if (!categoryId) {
      setDances([])
      setDanceId('')
      return
    }

    let cancelled = false

    const loadDances = async () => {
      try {
        const response = await api.get<Dance[]>('/dances', {
          params: { categoryTournamentId: Number(categoryId) },
        })

        if (!cancelled) {
          setDances(response.data)
        }
      } catch (error) {
        console.error('Error loading dances:', error)

        if (!cancelled) {
          setDances([])
          setDanceId('')
        }
      }
    }

    void loadDances()

    return () => {
      cancelled = true
    }
  }, [categoryId])

  useEffect(() => {
    if (!categoryId) {
      setRoundSettingsList([])
      setRoundSettingsId('')
      setSettingsLoading(false)
      setSettingsError('')
      return
    }

    let cancelled = false
    const loadRounds = async () => {
      setSettingsLoading(true)
      setSettingsError('')

      try {
        const response = await api.get<RoundSettings[]>('/round-settings', {
          params: { categoryTournamentId: Number(categoryId) },
        })
        if (!cancelled) {
          setRoundSettingsList(
            [...response.data].sort(
              (a, b) => (a.roundOrder ?? 0) - (b.roundOrder ?? 0),
            ),
          )
        }
      } catch (error) {
        console.error('Error loading rounds:', error)
        if (!cancelled) setSettingsError('Could not load rounds.')
      } finally {
        if (!cancelled) setSettingsLoading(false)
      }
    }

    void loadRounds()
    return () => {
      cancelled = true
    }
  }, [categoryId])

  useEffect(() => {
    if (!categoryId || !roundSettingsId) return

    const selectedSettings = roundSettingsList.find(
      settings => settings.id === Number(roundSettingsId),
    )

    if (!selectedSettings) return

    onSelect({
      categoryId: Number(categoryId),
      roundSettingsId: selectedSettings.id,
      round: selectedSettings.round,
      ...(danceId ? { danceId: Number(danceId) } : {}),
      couplesToNextRound: selectedSettings.couplesToNextRound,
    })
  }, [categoryId, roundSettingsId, danceId, roundSettingsList, onSelect])

  const handleCategoryChange = (value: string) => {
    setCategoryId(value)
    setRoundSettingsId('')
    setDanceId('')
    setSettingsError('')
  }

  const handleDanceChange = (value: string) => {
    setDanceId(value)
  }

  const selectedDanceIndex = dances.findIndex(
    dance => dance.id === Number(danceId),
  )

  const handlePreviousDance = () => {
    if (selectedDanceIndex > 0) {
      setDanceId(String(dances[selectedDanceIndex - 1].id))
    }
  }

  const handleNextDance = () => {
    if (
      selectedDanceIndex >= 0 &&
      selectedDanceIndex < dances.length - 1
    ) {
      setDanceId(String(dances[selectedDanceIndex + 1].id))
    }
  }

  return (

    <div className="round-control">
      <label>
        Category
        <select
          value={categoryId}
          onChange={event => handleCategoryChange(event.target.value)}
        >
          <option value="">Select category</option>
          {categories.map(category => (
            <option key={category.id} value={category.id}>
              {category.nameCategory}
            </option>
          ))}
        </select>
      </label>

      <label>
        Round
        <select
          value={roundSettingsId}
          onChange={event => setRoundSettingsId(event.target.value)}
          disabled={!categoryId || settingsLoading}
        >
          <option value="">Select round</option>
          {roundSettingsList.map(settings => (
            <option key={settings.id} value={settings.id}>
              {settings.round.replaceAll('_', ' ')}
            </option>
          ))}
        </select>
      </label>

      {settingsLoading && <p>Loading rounds...</p>}
      {settingsError && (
        <p className="round-settings-error" role="alert">
          {settingsError}
        </p>
      )}

      <label>
        Dance
        <select
          value={danceId}
          onChange={event => handleDanceChange(event.target.value)}
        >
          <option value="">Select dance</option>
          {dances.map(dance => (
            <option key={dance.id} value={dance.id}>
              {dance.nameDance}
            </option>
          ))}
        </select>

        <div className="dance-navigation">
          <button
            type="button"
            onClick={handlePreviousDance}
            disabled={selectedDanceIndex <= 0}
          >
            ← Previous DANCE
          </button>

          <span>
            {selectedDanceIndex >= 0
              ? `${selectedDanceIndex + 1} / ${dances.length}`
              : 'Select dance'}
          </span>

          <button
            type="button"
            onClick={handleNextDance}
            disabled={
              selectedDanceIndex < 0 ||
              selectedDanceIndex >= dances.length - 1
            }
          >
            Next DANCE →
          </button>
        </div>

      </label>

    </div>
  )
}
