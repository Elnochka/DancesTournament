
import { useEffect, useRef, useState } from 'react'
import { getActiveDance, updateActiveDance } from '../api/activeDanceApi'

import GenericAdmin from '../components/admin/GenericAdmin'
import AdminRoundControl from '../components/AdminRoundControl'
import type { RoundContext, RoundSettings } from '../types/RoundContext'

import useAuth from '../hooks/useAuth'
import api from '../api/axiosInstance'
import UsersAdmin from './UsersAdmin'

import './AdminDashboard.css'

type AdminTab =
  | 'judges'
  | 'couples'
  | 'participants'
  | 'categoryTournaments'
  | 'participantTournaments'
  | 'roundSettings'
  | 'marks'
  | 'finalMarks'
  | 'skatingResults'
  | 'clubs'
  | 'organizations'
  | 'cities'
  | 'klasses'
  | 'categoryJudges'
  | 'categoryParticipants'
  | 'dances'
  | 'trainers'
  | 'kindOfDances'
  | 'countries'
  | 'users'

export default function AdminDashboard() {
  const [tab, setTab] = useState<AdminTab>('judges')
  const [ctx, setCtx] = useState<RoundContext | null>(null)

  const [settingsLoading, setSettingsLoading] = useState(false)

  const [skatingMessage, setSkatingMessage] = useState<{
    type: 'success' | 'error'
    text: string
  } | null>(null)

  const lastSentState = useRef<string>('')

  const getErrorMessage = (error: unknown, fallback: string): string => {
    if (typeof error === 'object' && error !== null && 'response' in error) {
      const response = (
        error as { response?: { data?: unknown } }
      ).response

      const data = response?.data

      if (typeof data === 'string' && data.trim()) {
        return data
      }

      if (typeof data === 'object' && data !== null) {
        const body = data as Record<string, unknown>

        for (const key of ['message', 'detail', 'error']) {
          if (typeof body[key] === 'string' && body[key]) {
            return body[key]
          }
        }
      }
    }

    return fallback
  }

  useEffect(() => {
    let cancelled = false

    const restoreContext = async () => {
      setSettingsLoading(true)


      try {
        const active = await getActiveDance()

        if (
          !active?.categoryTournamentId ||
          !active.roundSettingsId ||
          !active.danceId
        ) {
          return
        }

        const response = await api.get<RoundSettings[]>(
          '/round-settings',
          {
            params: {
              categoryTournamentId: active.categoryTournamentId,
            },
          },
        )

        if (cancelled) return

        const selectedRound = response.data.find(
          round => round.id === active.roundSettingsId,
        )

        if (!selectedRound) {
          console.warn('Saved round settings were not found')
          return
        }

        const restoredContext: RoundContext = {
          categoryId: active.categoryTournamentId,
          roundSettingsId: active.roundSettingsId,
          round: selectedRound.round,
          danceId: active.danceId,
          couplesToNextRound: selectedRound.couplesToNextRound,
        }

        // Не надсилаємо назад те саме значення, яке щойно відновили.
        lastSentState.current = JSON.stringify(active)

        setCtx(restoredContext)
      } catch (error) {
        if (cancelled) return

        setSkatingMessage({
          type: 'error',
          text: getErrorMessage(
            error,
            'Could not restore active dance',
          ),
        })

        console.error('Could not restore active dance:', error)
      } finally {
        if (!cancelled) {
          setSettingsLoading(false)
        }
      }
    }

    void restoreContext()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!ctx?.categoryId || !ctx.roundSettingsId || !ctx.danceId) {
      return
    }

    const activeDance = {
      categoryTournamentId: ctx.categoryId,
      roundSettingsId: ctx.roundSettingsId,
      danceId: ctx.danceId,
    }

    const stateKey = JSON.stringify(activeDance)

    if (lastSentState.current === stateKey) {
      return
    }

    const saveActiveDance = async () => {
      try {
        await updateActiveDance(activeDance)
        lastSentState.current = stateKey
        console.log('Active dance updated:', activeDance)
      } catch (error) {
        console.error('Could not update active dance:', error)
      }
    }

    void saveActiveDance()
  }, [ctx])

  const { user, logout } = useAuth()

  const [couplesToNextRound, setCouplesToNextRound] =
    useState<number | null>(null)

  useEffect(() => {
    if (!ctx?.roundSettingsId) {
      setCouplesToNextRound(null)
      return
    }

    const loadRoundSettings = async () => {
      try {
        const response = await api.get<RoundSettings>(
          `/round-settings/${ctx.roundSettingsId}`,
        )

        setCouplesToNextRound(response.data.couplesToNextRound)
      } catch (error) {
        console.error('Could not load round settings:', error)
      }
    }

    void loadRoundSettings()
  }, [ctx?.roundSettingsId])

  const moveNextRound = async () => {
    if (!ctx?.categoryId || !ctx?.roundSettingsId) {
      setSkatingMessage({
        type: 'error',
        text: 'Select category and round',
      })
      return
    }

    setSkatingMessage(null)

    try {
      await api.post('/skating/move-next-round', null, {
        params: ctx,
      })

      setSkatingMessage({
        type: 'success',
        text: 'Participants moved to the next round successfully.',
      })
    } catch (error) {
      console.error(error)

      setSkatingMessage({
        type: 'error',
        text: getErrorMessage(
          error,
          'Error moving participants to the next round.',
        ),
      })
    }
  }

  const moveNextDance = async () => {
    if (!ctx?.categoryId || !ctx?.roundSettingsId || !ctx?.danceId) {
      setSkatingMessage({
        type: 'error',
        text: 'Select category, round, and dance',
      })
      return
    }

    setSkatingMessage(null)

    try {
      await api.post('/skating/move-next-dance', null, {
        params: ctx,
      })

      setSkatingMessage({
        type: 'success',
        text: 'Participants moved to the next dance successfully.',
      })
    } catch (error) {
      console.error(error)

      setSkatingMessage({
        type: 'error',
        text: getErrorMessage(
          error,
          'Error moving participants to the next dance.',
        ),
      })
    }
  }

  const calculateSkatingResult = async () => {
    if (!ctx?.categoryId) {
      setSkatingMessage({
        type: 'error',
        text: 'Select category',
      })
      return
    }

    setSkatingMessage(null)

    try {
      await api.post('/skating/final', null, {
        params: ctx,
      })

      setSkatingMessage({
        type: 'success',
        text: 'Skating result calculated successfully.',
      })
    } catch (error) {
      console.error(error)

      setSkatingMessage({
        type: 'error',
        text: getErrorMessage(
          error,
          'Error calculating the final result.',
        ),
      })
    }
  }

  const renderTab = () => {
    switch (tab) {
      case 'judges':
        return (
          <GenericAdmin
            endpoint="/judges"
            title="Judges"
            hiddenColumns={[

              'trainerId',

            ]}
            fields={[
              {
                name: 'nameJudge',
                label: 'Judge name',
              },

              {
                name: 'comment',
                label: 'Comment',
              },
            ]}
            selects={{
              trainerId: {
                endpoint: '/trainers',
                label: 'Trainer',
                labelField: 'nameTrainer',
              },
            }}
            columnLabels={{
              trainerName: 'Trainer',
            }}
          />
        )

      case 'couples':
        return (
          <GenericAdmin
            endpoint="/couples"
            title="Couples"
            hiddenColumns={[

              'participantFemaleId',
              'participantMaleId',

            ]}
            fields={[
              {
                name: 'nameCouple',
                label: 'Couple name',
              },
            ]}
            selects={{
              participantMaleId: {
                endpoint: '/participants',
                label: 'Male participant',
                labelField: item =>
                  `${String(
                    item.firstnameUkr ?? '',
                  )} ${String(
                    item.lastnameUkr ?? '',
                  )}`.trim(),
              },

              participantFemaleId: {
                endpoint: '/participants',
                label: 'Female participant',
                labelField: item =>
                  `${String(
                    item.firstnameUkr ?? '',
                  )} ${String(
                    item.lastnameUkr ?? '',
                  )}`.trim(),
              },
            }}
            columnLabels={{

              participantMaleName: 'Male participant',
              participantFemaleName: 'Female participant',

            }}
            historyActions={[
              {
                label: 'Change members',
                endpoint: item =>
                  `/history/couples/${item.id}/change-members`,
                fields: [
                  {
                    name: 'nameCouple',
                    label: 'Couple',
                    readOnly: true,
                  },
                  {
                    name: 'participantMaleId',
                    label: 'Male participant',
                    endpoint: '/participants',
                    labelField: item =>
                      `${String(
                        item.firstnameUkr ?? '',
                      )} ${String(
                        item.lastnameUkr ?? '',
                      )}`.trim(),
                  },
                  {
                    name: 'participantFemaleId',
                    label: 'Female participant',
                    endpoint: '/participants',
                    labelField: item =>
                      `${String(
                        item.firstnameUkr ?? '',
                      )} ${String(
                        item.lastnameUkr ?? '',
                      )}`.trim(),
                  },
                ],
              },
            ]}
          />
        )

      case 'participants':
        return (
          <GenericAdmin
            endpoint="/participants"
            title="Participants"
            hiddenColumns={[

              'countryId',
              'trainerId',
              'klassId',
            ]}
            fields={[
              {
                name: 'firstnameUkr',
                label: 'First name (UA)',
              },
              {
                name: 'lastnameUkr',
                label: 'Last name (UA)',
              },
              {
                name: 'firstnameEn',
                label: 'First name (EN)',
              },
              {
                name: 'lastnameEn',
                label: 'Last name (EN)',
              },
              {
                name: 'dateBirth',
                label: 'Date of birth',
                type: 'date',
              },
            ]}
            selects={{
              countryId: {
                endpoint: '/countries',
                label: 'Country',
                labelField: 'nameCountry',
              },

              trainerId: {
                endpoint: '/trainers',
                label: 'Trainer',
                labelField: 'nameTrainer',
              },

              klassId: {
                endpoint: '/klasses',
                label: 'Class',
                labelField: 'nameKlass',
              },

              kindOfDanceIds: {
                endpoint: '/kind-of-dances',
                label: 'Dance types',
                labelField: 'nameKindOfDance',
                isMulti: true,
                sourceField: 'kindOfDances',
              },
            }}
            enumSelects={{
              gender: {
                enumName: 'GenderEnum',
                label: 'Gender',
              },
            }}

            columnLabels={{

              countryName: 'Country',
              trainerName: 'Trainer',
              klassName: 'Class',
              gender: 'Gender',
              kindOfDances: 'Dance types',

            }}

            historyActions={[
              {
                label: 'Change trainer',
                endpoint: item =>
                  `/history/participants/${item.id}/change-trainer`,
                fields: [
                  {
                    name: 'lastnameUkr',
                    label: 'Participant',
                    readOnly: true,
                  },
                  {
                    name: 'trainerId',
                    label: 'Trainer',
                    endpoint: '/trainers',
                    labelField: 'nameTrainer',
                  },
                ],
              },
            ]}
          />
        )

      case 'categoryTournaments':
        return (
          <GenericAdmin
            endpoint="/category-tournaments"
            title="Category Tournaments"
            hiddenColumns={[

              'klassId',
              'categoryParticipantId',
              'kindOfDanceId',

            ]}
            fields={[
              {
                name: 'nameCategory',
                label: 'Category name',
              },
            ]}
            selects={{
              klassId: {
                endpoint: '/klasses',
                label: 'Class',
                labelField: 'nameKlass',
              },

              categoryParticipantId: {
                endpoint: '/category-participants',
                label: 'Participant category',
                labelField: 'nameCategory',
              },

              kindOfDanceId: {
                endpoint: '/kind-of-dances',
                label: 'Dance type',
                labelField: 'nameKindOfDance',
              },
            }}
            enumSelects={{
              soloCouple: {
                enumName: 'SoloCoupleEnum',
                label: 'Solo / Couple',
              },
            }}
            columnLabels={{

              countryName: 'Country',
              categoryParticipantName: 'Participant category',
              klassName: 'Class',
              soloCouple: 'Solo / Couple',
              kindOfDanceName: 'Dance types',

            }}
          />
        )

      case 'participantTournaments':
        return (
          <GenericAdmin
            endpoint="/participant-tournaments"
            title="Participant Tournaments"
            hiddenColumns={[

              'participantId',
              'coupleId',
              'categoryTournamentId',

            ]}
            fields={[
              {
                name: 'startNumber',
                label: 'Start number',
                type: 'number',
              },
            ]}
            selects={{
              participantId: {
                endpoint: '/participants',
                label: 'Participant',
                labelField: item =>
                  `${String(
                    item.firstnameUkr ?? '',
                  )} ${String(
                    item.lastnameUkr ?? '',
                  )}`.trim(),
              },

              categoryTournamentId: {
                endpoint: '/category-tournaments',
                label: 'Category tournament',
                labelField: 'nameCategory',
              },

              coupleId: {
                endpoint: '/couples',
                label: 'Couple',
                labelField: 'nameCouple',
              },
            }}
            enumSelects={{
              soloCouple: {
                enumName: 'SoloCoupleEnum',
                label: 'Solo / Couple',
              },
            }}
            columnLabels={{

              participantName: 'Participant',
              categoryTournamentName: 'Category tournament',
              coupleName: 'Couple',
              soloCouple: 'Solo / Couple',
              displayName: 'Male+Female participant for couple',
            }}
          />
        )

      case 'roundSettings':
        return (
          <GenericAdmin
            endpoint="/round-settings"
            title="Round Settings"
            hiddenColumns={[

              'categoryTournamentId',

            ]}

            fields={[
              {
                name: 'roundOrder',
                label: 'Round order',
              },
              {
                name: 'couplesToNextRound',
                label: 'Couples to next round',
              },
            ]}

            selects={{

              categoryTournamentId: {
                endpoint: '/category-tournaments',
                label: 'Category tournament',
                labelField: 'nameCategory',
              },

            }}

            enumSelects={{
              round: {
                enumName: 'RoundEnum',
                label: 'Round',
              },
            }}
            columnLabels={{

              categoryTournamentName: 'Category tournament',
              participantsCount: 'Participant count',
              recommendedCouplesToNextRound: 'Recommended couples to next round',


            }}
          />
        )

      case 'marks':
        return (
          <>
            {couplesToNextRound !== null && (
              <div className="round-settings-info">
                Couples to next round:{' '}
                <strong>{couplesToNextRound}</strong>
              </div>
            )}

            <GenericAdmin
              endpoint="/marks"
              title="Marks"
              filterBy={{
                categoryId: ctx?.categoryId,
                danceId: ctx?.danceId,
                round: ctx?.round,
              }}
              hiddenColumns={[
                'categoryTournamentId',
                'judgeId',
                'danceId',
                'coupleNext',
              ]}
              fields={[
                {
                  name: 'startNumber',
                  label: 'Start number',
                  type: 'number',
                },
                {
                  name: 'passed',
                  label: 'Passed',
                  type: 'boolean',
                },
              ]}
              selects={{
                categoryTournamentId: {
                  endpoint: '/category-tournaments',
                  label: 'Category tournament',
                  labelField: 'nameCategory',
                },
                judgeId: {
                  endpoint: '/judges',
                  label: 'Judge',
                  labelField: 'nameJudge',
                },
                danceId: {
                  endpoint: '/dances',
                  label: 'Dance',
                  labelField: 'nameDance',
                },
              }}
              enumSelects={{
                round: {
                  enumName: 'RoundEnum',
                  label: 'Round',
                },
              }}
              columnLabels={{
                dance: 'Dance',
                judge: 'Judge',
                categoryTournament: 'Category tournament',
                round: 'Round',
              }}
            />
          </>
        )

      // case 'marks':       

      //   return (

      //     <GenericAdmin
      //       endpoint="/marks"
      //       title="Marks"            
      //       filterBy={{
      //         categoryId: ctx?.categoryId,
      //         danceId: ctx?.danceId,
      //         round: ctx?.round,
      //       }}
      //       hiddenColumns={[

      //         'categoryTournamentId',
      //         'judgeId',
      //         'danceId',
      //         'coupleNext',

      //       ]}
      //       fields={[
      //         {
      //           name: 'startNumber',
      //           label: 'Start number',
      //           type: 'number',
      //         },
      //         {
      //           name: 'passed',
      //           label: 'Passed',
      //           type: 'boolean',
      //         },
      //       ]}
      //       selects={{
      //         categoryTournamentId: {
      //           endpoint: '/category-tournaments',
      //           label: 'Category tournament',
      //           labelField: 'nameCategory',
      //         },

      //         judgeId: {
      //           endpoint: '/judges',
      //           label: 'Judge',
      //           labelField: 'nameJudge',
      //         },

      //         danceId: {
      //           endpoint: '/dances',
      //           label: 'Dance',
      //           labelField: 'nameDance',
      //         },
      //       }}
      //       enumSelects={{
      //         round: {
      //           enumName: 'RoundEnum',
      //           label: 'Round',
      //         },
      //       }}
      //       columnLabels={{

      //         dance: 'Dance',
      //         judge: 'Judge',
      //         categoryTournament: 'Category tournament',
      //         round: 'Round',

      //       }}
      //     />
      //   )

      case 'finalMarks':
        return (
          <GenericAdmin
            endpoint="/final-marks"
            title="Final Marks"
            filterBy={{
              categoryId: ctx?.categoryId,
              danceId: ctx?.danceId,
            }}
            hiddenColumns={[

              'categoryTournamentId',
              'judgeId',
              'danceId',
              'participantId',

            ]}
            fields={[
              {
                name: 'startNumber',
                label: 'Start number',
                type: 'number',
              },
              {
                name: 'place',
                label: 'Place',
                type: 'number',
              },
            ]}
            selects={{
              categoryTournamentId: {
                endpoint: '/category-tournaments',
                label: 'Category',
                labelField: 'nameCategory',
              },

              judgeId: {
                endpoint: '/judges',
                label: 'Judge',
                labelField: 'nameJudge',
              },

              danceId: {
                endpoint: '/dances',
                label: 'Dance',
                labelField: 'nameDance',
              },
            }}
            columnLabels={{

              judge: 'Judge',
              dance: 'Dance',
              categoryTournament: 'Category tournament',

            }}
          />
        )

      case 'skatingResults':
        return (
          <>
            <div className="admin-round-section">
              <h2 className="admin-section-title">
                Skating Results
              </h2>

              <AdminRoundControl
                value={ctx}
                onSelect={setCtx}
              />

              {settingsLoading && (
                <p className="skating-message">
                  Restoring saved selection...
                </p>
              )}

              {skatingMessage && (
                <div
                  className={`skating-message skating-message--${skatingMessage.type}`}
                  role={skatingMessage.type === 'error' ? 'alert' : 'status'}
                >
                  {skatingMessage.text}
                </div>
              )}


              <div className="admin-round-actions">
                <button
                  className="admin-action-button"
                  onClick={moveNextRound}
                >
                  Move to next ROUND (Lock ROUND)
                </button>

                <button
                  className="admin-action-button"
                  onClick={moveNextDance}
                >
                  Move to next DANCE (Lock DANCE)
                </button>

                <button
                  className="admin-action-button secondary"
                  onClick={calculateSkatingResult}
                >
                  Calculate FINAL RESULT
                </button>
              </div>
            </div>

            <div className="admin-content">
              <GenericAdmin
                endpoint="/skating-results"
                title="Skating Results"
                hiddenColumns={[

                  'categoryTournamentId',
                  'judgeId',
                  'participantTournamentId',


                ]}
                fields={[
                  {
                    name: 'startNumber',
                    label: 'Start number',
                    type: 'number',
                  },
                  {
                    name: 'finalPlace',
                    label: 'Final place',
                    type: 'number',
                  },
                ]}
                selects={{
                  participantTournamentId: {
                    endpoint: '/participant-tournaments',
                    label: 'Participant tournament',
                    labelField: item =>
                      `${String(
                        item.startNumber ?? '',
                      )} - ${String(
                        item.displayName ?? '',
                      )}`,
                  },

                  categoryTournamentId: {
                    endpoint: '/category-tournaments',
                    label: 'Category tournament',
                    labelField: 'nameCategory',
                  },
                }}
                columnLabels={{
                  participantTournament: 'Participant tournament',
                  judge: 'Judge',
                  categoryTournament: 'Category tournament',

                }}
              />
            </div>
          </>
        )

      case 'trainers':
        return (
          <GenericAdmin
            endpoint="/trainers"
            title="Trainers"
            hiddenColumns={[

              'clubId',
              'categoryJudgeId',
              'organizationId',
              'cityId',
              'countryId',

            ]}
            fields={[
              {
                name: 'nameTrainer',
                label: 'Trainer name',
              },
            ]}
            selects={{
              countryId: {
                endpoint: '/countries',
                label: 'Country',
                labelField: 'nameCountry',
              },

              clubId: {
                endpoint: '/clubs',
                label: 'Club',
                labelField: 'nameClub',
              },

              organizationId: {
                endpoint: '/organizations',
                label: 'Organization',
                labelField: 'nameOrganization',
              },

              cityId: {
                endpoint: '/cities',
                label: 'City',
                labelField: 'nameCity',
              },

              categoryJudgeId: {
                endpoint: '/category-judges',
                label: 'Judge category',
                labelField: 'nameCategory',
              },
            }}
            columnLabels={{
              countryName: 'Country',
              clubName: 'Club',
              organizationName: 'Organization',
              cityName: 'City',
              categoryJudgeName: 'Judge category',
            }}
            historyActions={[
              {
                label: 'Change club',
                endpoint: item =>
                  `/history/trainers/${item.id}/change-club`,
                fields: [
                  {
                    name: 'nameTrainer',
                    label: 'Trainer',
                    readOnly: true,
                  },
                  {
                    name: 'clubId',
                    label: 'Club',
                    endpoint: '/clubs',
                    labelField: 'nameClub',
                  },
                ],
              },
            ]}
          />
        )

      case 'clubs':
        return (
          <GenericAdmin
            endpoint="/clubs"
            title="Clubs"
            fields={[
              {
                name: 'nameClub',
                label: 'Club name',
              },
            ]}
          />
        )

      case 'organizations':
        return (
          <GenericAdmin
            endpoint="/organizations"
            title="Organizations"
            fields={[
              {
                name: 'nameOrganization',
                label: 'Organization name',
              },
            ]}
          />
        )

      case 'cities':
        return (
          <GenericAdmin
            endpoint="/cities"
            title="Cities"
            fields={[
              {
                name: 'nameCity',
                label: 'City name',
              },
            ]}
          />
        )

      case 'klasses':
        return (
          <GenericAdmin
            endpoint="/klasses"
            title="Classes"
            fields={[
              {
                name: 'nameKlass',
                label: 'Class name',
              },
            ]}
          />
        )

      case 'countries':
        return (
          <GenericAdmin
            endpoint="/countries"
            title="Countries"
            fields={[
              {
                name: 'nameCountry',
                label: 'Country name',
              },
            ]}
          />
        )

      case 'dances':
        return (
          <GenericAdmin
            endpoint="/dances"
            title="Dances"
            hiddenColumns={[

              'kindOfDanceId',

            ]}
            fields={[
              {
                name: 'nameDance',
                label: 'Dance name',
              },
              {
                name: 'fullNameDance',
                label: 'Full dance name',
              },
            ]}
            selects={{
              kindOfDanceId: {
                endpoint: '/kind-of-dances',
                labelField: 'nameKindOfDance',
              },
            }}

            columnLabels={{
              kindOfDanceName: 'Dance types',

            }}
          />
        )

      case 'categoryJudges':
        return (
          <GenericAdmin
            endpoint="/category-judges"
            title="Judge Categories"
            fields={[
              {
                name: 'nameCategory',
                label: 'Category name',
              },
            ]}
          />
        )

      case 'categoryParticipants':
        return (
          <GenericAdmin
            endpoint="/category-participants"
            title="Participant Categories"
            fields={[
              {
                name: 'nameCategory',
                label: 'Category name',
              },
              {
                name: 'age',
                label: 'Age',
                type: 'number',
              },
              {
                name: 'ageon',
                label: 'Age on',
                type: 'number',
              },
            ]}
          />
        )

      case 'kindOfDances':
        return (
          <GenericAdmin
            endpoint="/kind-of-dances"
            title="Dance Types"
            fields={[
              {
                name: 'nameKindOfDance',
                label: 'Dance type',
              },
              {
                name: 'fullNameKindOfDance',
                label: 'Full dance type name',
              },
            ]}
          />
        )

      case 'users':
        return <UsersAdmin />

      default:
        return null
    }
  }

  return (
    <div className="admin-dashboard">

      {/* Header */}
      <header className="admin-dashboard-header">
        <div>
          <h1 className="admin-dashboard-title">
            Admin Dashboard
          </h1>

          <p className="admin-dashboard-subtitle">
            Dance competition management
          </p>
        </div>

        <div className="admin-user-info">
          <span className="admin-user-name">
            Welcome, {user?.name}
          </span>

          <button
            className="admin-logout-button"
            onClick={logout}
          >
            Logout
          </button>
        </div>
      </header>


      {/* Navigation */}
      <div className="admin-tabs-container">

        <p className="admin-tabs-label">
          Competition
        </p>

        <div className="admin-tabs">

          <button
            className={`admin-tab ${tab === 'judges' ? 'active' : ''
              }`}
            onClick={() => setTab('judges')}
          >
            Judges
          </button>

          <button
            className={`admin-tab ${tab === 'couples' ? 'active' : ''
              }`}
            onClick={() => setTab('couples')}
          >
            Couples
          </button>

          <button
            className={`admin-tab ${tab === 'participants' ? 'active' : ''
              }`}
            onClick={() => setTab('participants')}
          >
            Participants
          </button>

          <button
            className={`admin-tab ${tab === 'categoryTournaments'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('categoryTournaments')
            }
          >
            Category Tournaments
          </button>

          <button
            className={`admin-tab ${tab === 'participantTournaments'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('participantTournaments')
            }
          >
            Participant Tournaments
          </button>

          <button
            className={`admin-tab ${tab === 'roundSettings' ? 'active' : ''
              }`}
            onClick={() =>
              setTab('roundSettings')
            }
          >
            Round Settings
          </button>

          <button
            className={`admin-tab ${tab === 'marks' ? 'active' : ''
              }`}
            onClick={() => setTab('marks')}
          >
            Marks
          </button>

          <button
            className={`admin-tab ${tab === 'finalMarks' ? 'active' : ''
              }`}
            onClick={() => setTab('finalMarks')}
          >
            Final Marks
          </button>

          <button
            className={`admin-tab ${tab === 'skatingResults'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('skatingResults')
            }
          >
            Skating Results
          </button>

        </div>


        <p
          className="admin-tabs-label"
          style={{ marginTop: 18 }}
        >
          Management
        </p>

        <div className="admin-tabs">

          <button
            className={`admin-tab ${tab === 'trainers' ? 'active' : ''
              }`}
            onClick={() => setTab('trainers')}
          >
            Trainers
          </button>

          <button
            className={`admin-tab ${tab === 'clubs' ? 'active' : ''
              }`}
            onClick={() => setTab('clubs')}
          >
            Clubs
          </button>

          <button
            className={`admin-tab ${tab === 'organizations'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('organizations')
            }
          >
            Organizations
          </button>

          <button
            className={`admin-tab ${tab === 'cities' ? 'active' : ''
              }`}
            onClick={() => setTab('cities')}
          >
            Cities
          </button>

          <button
            className={`admin-tab ${tab === 'klasses' ? 'active' : ''
              }`}
            onClick={() => setTab('klasses')}
          >
            Classes
          </button>

          <button
            className={`admin-tab ${tab === 'countries' ? 'active' : ''
              }`}
            onClick={() => setTab('countries')}
          >
            Countries
          </button>

          <button
            className={`admin-tab ${tab === 'dances' ? 'active' : ''
              }`}
            onClick={() => setTab('dances')}
          >
            Dances
          </button>

          <button
            className={`admin-tab ${tab === 'kindOfDances'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('kindOfDances')
            }
          >
            Dance Types
          </button>

          <button
            className={`admin-tab ${tab === 'categoryJudges'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('categoryJudges')
            }
          >
            Judge Categories
          </button>

          <button
            className={`admin-tab ${tab === 'categoryParticipants'
              ? 'active'
              : ''
              }`}
            onClick={() =>
              setTab('categoryParticipants')
            }
          >
            Participant Categories
          </button>

          <button
            className={`admin-tab ${tab === 'users' ? 'active' : ''
              }`}
            onClick={() => setTab('users')}
          >
            Users
          </button>

        </div>
      </div>


      {/* Current section */}
      <div className="admin-content">
        {renderTab()}
      </div>

    </div>
  )
}