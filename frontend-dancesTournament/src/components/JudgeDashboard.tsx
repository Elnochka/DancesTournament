
import { useEffect, useState } from 'react'
import { getActiveDance } from '../api/activeDanceApi'

import type { ActiveDanceState } from '../types/ActiveDanceState'
import GenericAdmin from '../components/admin/GenericAdmin'
import useAuth from '../hooks/useAuth'
import api from '../api/axiosInstance'
import './JudgeDashboard.css'
import type {
    CategoryTournament,
    Dance,
    RoundSettings,
} from '../types/RoundContext'

interface Mark {
    id: number
    startNumber: number
    judge: string
    passed: boolean
    [key: string]: unknown

}

interface FinalMark {
    id: number
    startNumber: number
    judge: string
    place: number
    [key: string]: unknown
}

export default function JudgeDashboard() {
    const { user, logout } = useAuth()

    const [marks, setMarks] = useState<Mark[]>([])
    const [finalMarks, setFinalMarks] = useState<FinalMark[]>([])
    const [loading, setLoading] = useState(true)

    const [activeDance, setActiveDance] =
        useState<ActiveDanceState | null>(null)

    const [activeDanceName, setActiveDanceName] =
        useState('')

    const [activeCategoryName, setActiveCategoryName] =
        useState('')

    const [activeRoundName, setActiveRoundName] =
        useState('')

    const [couplesToNextRound, setCouplesToNextRound] =
        useState<number | null>(null)

    useEffect(() => {
        let cancelled = false

        const loadActiveDance = async () => {
            try {
                const state = await getActiveDance()

                if (cancelled) return

                setActiveDance(state)

                if (
                    !state?.categoryTournamentId ||
                    !state.roundSettingsId ||
                    !state.danceId
                ) {
                    setActiveDanceName('')
                    setActiveCategoryName('')
                    setActiveRoundName('')
                    setCouplesToNextRound(null)
                    return
                }

                const [categoriesResponse, dancesResponse, roundsResponse] =
                    await Promise.all([
                        api.get<CategoryTournament[]>('/category-tournaments'),
                        api.get<Dance[]>('/dances', {
                            params: {
                                categoryTournamentId:
                                    state.categoryTournamentId,
                            },
                        }),
                        api.get<RoundSettings[]>('/round-settings', {
                            params: {
                                categoryTournamentId:
                                    state.categoryTournamentId,
                            },
                        }),
                    ])

                if (cancelled) return

                const category = categoriesResponse.data.find(
                    item => item.id === state.categoryTournamentId,
                )

                const dance = dancesResponse.data.find(
                    item => item.id === state.danceId,
                )

                const round = roundsResponse.data.find(
                    item => item.id === state.roundSettingsId,
                )

                setActiveCategoryName(category?.nameCategory ?? '')
                setActiveDanceName(dance?.nameDance ?? '')
                setActiveRoundName(round?.round ?? '')
                setCouplesToNextRound(round?.couplesToNextRound ?? null)
            } catch (error) {
                if (!cancelled) {
                    console.error('Active dance load error:', error)
                }
            }
        }

        void loadActiveDance()

        const intervalId = window.setInterval(() => {
            void loadActiveDance()
        }, 3000)

        return () => {
            cancelled = true
            window.clearInterval(intervalId)
        }
    }, [])

    const loadMarks = async () => {
        if (!user) {
            return
        }

        try {
            const response =
                await api.get<Mark[]>('/marks')

            const myMarks = response.data.filter(
                mark => mark.judge === user.name,
            )

            setMarks(myMarks)
        } catch (error) {
            console.error(
                'Marks load error:',
                error,
            )
        }
    }

    const loadFinalMarks = async () => {
        if (!user) {
            return
        }

        try {
            const response =
                await api.get<FinalMark[]>('/final-marks')

            const myFinalMarks =
                response.data.filter(
                    finalMark =>
                        finalMark.judge === user.name,
                )

            setFinalMarks(myFinalMarks)
        } catch (error) {
            console.error(
                'Final marks load error:',
                error,
            )
        }
    }

    useEffect(() => {
        if (!user) {
            return
        }

        async function loadData() {
            try {
                await Promise.all([
                    loadMarks(),
                    loadFinalMarks(),
                ])
            } finally {
                setLoading(false)
            }
        }

        loadData()
    }, [user])


    if (!user) {
        return <div>Loading user...</div>
    }

    if (loading) {
        return (
            <div className="judge-dashboard-loading">
                Loading...
            </div>
        )
    }

    return (
        <div className="judge-dashboard">
            <div className="judge-dashboard-header">
                <div>
                    <h1>Judge Dashboard</h1>
                    <p>Judge: {user.name}</p>
                </div>

                <button
                    className="judge-logout-button"
                    onClick={logout}
                >
                    Logout
                </button>

                <section className="judge-section active-dance-section">
                    <h2>Поточний вибір адміністратора</h2>

                    {activeDance?.danceId ? (
                        <>
                            <p>
                                <strong>Категорія:</strong>{' '}
                                {activeCategoryName || activeDance.categoryTournamentId}
                            </p>

                            <p>
                                <strong>Раунд:</strong>{' '}
                                {activeRoundName || activeDance.roundSettingsId}
                            </p>

                            <p>
                                <strong>Танець:</strong>{' '}
                                {activeDanceName || `ID ${activeDance.danceId}`}
                            </p>
                        </>
                    ) : (
                        <p>Адміністратор ще не вибрав активний танець.</p>
                    )}
                </section>

            </div>

            <section className="judge-section">

                {couplesToNextRound !== null && (
                    <div className="round-settings-info">
                        Couples to next round:{' '}
                        <strong>{couplesToNextRound}</strong>
                    </div>
                )}

                <GenericAdmin
                    endpoint="/marks"
                    title="Marks"
                    filterBy={
                        activeDance
                            ? {
                                categoryId: activeDance.categoryTournamentId ?? undefined,
                                danceId: activeDance.danceId ?? undefined,
                                round: activeRoundName || undefined,
                            }
                            : undefined

                    }
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
                        },
                        {
                            name: 'passed',
                            label: 'Passed',
                            type: 'boolean',
                        },
                    ]}
                    columnLabels={{

                        judge: 'Judge',
                        dance: 'Dance',
                        categoryTournament: 'Category tournament',
                        round: 'Round',

                    }}
                    judgeMode={true}
                    initialItems={marks}
                    onItemsChange={loadMarks}
                />
            </section>

            <section className="judge-section">

                <GenericAdmin
                    endpoint="/final-marks"
                    title="Final Marks"
                    filterBy={
                        activeDance
                            ? {
                                categoryId: activeDance.categoryTournamentId ?? undefined,
                                danceId: activeDance.danceId ?? undefined,
                            }
                            : undefined
                    }
                    hiddenColumns={[

                        'categoryTournamentId',
                        'judgeId',
                        'participantId',

                    ]}
                    fields={[
                        {
                            name: 'startNumber',
                            label: 'Start number',
                        },
                        {
                            name: 'place',
                            label: 'Place',
                            type: 'number',
                            min: 1,
                        },
                    ]}
                    columnLabels={{

                        judge: 'Judge',
                        categoryTournament: 'Category tournament',

                    }}
                    judgeMode={true}
                    initialItems={finalMarks}
                    onItemsChange={loadFinalMarks}
                />
            </section>
        </div>
    )
}