
import { useEffect, useState } from 'react'
import GenericAdmin from '../components/admin/GenericAdmin'
import useAuth from '../hooks/useAuth'
import './TrainerDashboard.css'

export default function TrainerDashboard() {
    const { user, logout } = useAuth()

    const [loading, setLoading] = useState(true)

    useEffect(() => {
        setLoading(false)
    }, [])

    if (!user) {
        return (
            <div className="trainer-dashboard-loading">
                Loading user...
            </div>
        )
    }

    if (loading) {
        return (
            <div className="trainer-dashboard-loading">
                Loading...
            </div>
        )
    }

    return (
        <div className="trainer-dashboard">

            <div className="trainer-dashboard-header">
                <div>
                    <h1>Trainer Dashboard</h1>
                    <p>Trainer: {user.name}</p>
                </div>

                <button
                    className="trainer-logout-button"
                    onClick={logout}
                >
                    Logout
                </button>
            </div>

            <section className="trainer-section">
                {/* <h2>Participants</h2> */}

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
                            label: 'First Name (UA)',
                        },
                        {
                            name: 'lastnameUkr',
                            label: 'Last Name (UA)',
                        },
                        {
                            name: 'firstnameEn',
                            label: 'First Name (EN)',
                        },
                        {
                            name: 'lastnameEn',
                            label: 'Last Name (EN)',
                        },
                        {
                            name: 'dateBirth',
                            label: 'Birth Date',
                            type: 'date',
                        },
                    ]}
                    selects={{
                        countryId: {
                            endpoint: '/countries',
                            labelField: 'nameCountry',
                        },
                        trainerId: {
                            endpoint: '/trainers',
                            labelField: 'nameTrainer',
                        },
                        klassId: {
                            endpoint: '/klasses',
                            labelField: 'nameKlass',
                        },
                        kindOfDanceIds: {
                            endpoint: '/kind-of-dances',
                            labelField:
                                'nameKindOfDance',
                            isMulti: true,
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

                />
            </section>

            <section className="trainer-section">
                {/* <h2>Trainers</h2> */}

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
                            labelField: 'nameCountry',
                        },
                        clubId: {
                            endpoint: '/clubs',
                            labelField: 'nameClub',
                        },
                        organizationId: {
                            endpoint: '/organizations',
                            labelField:
                                'nameOrganization',
                        },
                        cityId: {
                            endpoint: '/cities',
                            labelField: 'nameCity',
                        },
                        categoryJudgeId: {
                            endpoint:
                                '/category-judges',
                            labelField:
                                'nameCategory',
                        },
                    }}
                    columnLabels={{
                        countryName: 'Country',
                        clubName: 'Club',
                        organizationName: 'Organization',
                        cityName: 'City',
                        categoryJudgeName: 'Judge category',
                    }}
                
                />
            </section>

            <section className="trainer-section">
             {/* <h2>Couples</h2> */}

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
                            label: 'Couple Name',
                        },
                    ]}
                    selects={{
                        participantMaleId: {
                            endpoint:
                                '/participants',
                            label: 'Participant Male',
                            labelField: item =>
                                `${String(
                                    item.firstnameUkr ?? '',
                                )} ${String(
                                    item.lastnameUkr ?? '',
                                )}`.trim(),
                        },
                        participantFemaleId: {
                            endpoint:
                                '/participants',
                            label: 'Participant Female',
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
                />
            </section>

        </div>
    )
}