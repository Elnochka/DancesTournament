
import { useEffect, useState } from 'react'
import api from '../api/axiosInstance'

export default function useEnum(enumName: string) {
  const [values, setValues] = useState<string[]>([])

  useEffect(() => {
    api
      .get<string[]>(`/enums/${enumName}`)
      .then(res => setValues(res.data))
      .catch(err =>
        console.error('Enum load error', err),
      )
  }, [enumName])

  return { values }
}