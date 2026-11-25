

import GenericAdmin from '../components/admin/GenericAdmin'
import useAuth from '../hooks/useAuth'
import './DancerDashboard.css'

export default function DancerDashboard() {
  const { user, logout } = useAuth()

  return (
    <div className="dancer-dashboard">
      <header className="dancer-dashboard-header">
        <div>
          <h1>Dancer Dashboard</h1>

          <p>
            Welcome, {user?.name}
          </p>
        </div>

        <button
          className="dancer-logout-button"
          onClick={logout}
        >
          Logout
        </button>
      </header>

      <section className="dancer-section">
        {/* <h2>Participants</h2> */}

        <GenericAdmin
          endpoint="/participants"
          title="Participants"
          hiddenColumns={[

            'countryId',
            'trainerId',
            'klassId',
          ]}
          columnLabels={{

            countryName: 'Country',
            trainerName: 'Trainer',
            klassName: 'Class',
            gender: 'Gender',
            kindOfDances: 'Dance types',
            dateBirth: 'Birth Date',
            lastnameEn: 'Last Name (EN)',
            firstnameEn: 'First Name (EN)',
            lastnameUkr: 'Last Name (UA)',
            firstnameUkr: 'First Name (UA)',

          }}
          viewMode={true}
        />
      </section>

      <section className="dancer-section">
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
          columnLabels={{
            countryName: 'Country',
            clubName: 'Club',
            organizationName: 'Organization',
            cityName: 'City',
            categoryJudgeName: 'Judge category',
            nameTrainer: 'Trainer',
          }}
          viewMode={true}
        />
      </section>

      <section className="dancer-section">
        {/* <h2>Couples</h2> */}

        <GenericAdmin
          endpoint="/couples"
          title="Couples"
          hiddenColumns={[

            'participantFemaleId',
            'participantMaleId',

          ]}
          columnLabels={{

            participantMaleName: 'Male participant',
            participantFemaleName: 'Female participant',
            nameCouple: 'Couple Name',

          }}
          viewMode={true}
        />
      </section>
    </div>
  )
}

