
import { useEffect, useState } from 'react'
import api from '../../api/axiosInstance'

interface SelectOption {
  id: number
  [key: string]: unknown
}

export interface HistoryFieldConfig {
  name: string
  label: string
  type?: string
  endpoint?: string
  labelField?: string | ((item: SelectOption) => string)
  readOnly?: boolean
}

interface HistoryFieldProps {
  field: HistoryFieldConfig
  value: unknown
  onChange: (value: string) => void
}

export default function HistoryField({
  field,
  value,
  onChange,
}: HistoryFieldProps) {
  const [options, setOptions] = useState<SelectOption[]>([])

  useEffect(() => {
    if (!field.endpoint) {
      return
    }

    api
      .get<SelectOption[]>(field.endpoint)
      .then(res => setOptions(res.data))
      .catch(err =>
        console.error(
          'HistoryField load error',
          err,
        ),
      )
  }, [field.endpoint, field.readOnly])

  // READ ONLY
  if (field.readOnly) {
    return (
      <div style={{ marginBottom: 12 }}>
        <label>
          {field.label}:{' '}
        </label>

        <input
          type="text"
          value={String(value ?? '')}
          readOnly
        />
      </div>
    )
  }
  
  // SELECT
  if (field.endpoint) {
    return (
      <div style={{ marginBottom: 12 }}>
        <label>
          {field.label}:{' '}
        </label>

        <select
          value={String(value ?? '')}
          onChange={e =>
            onChange(e.target.value)
          }
        >
          <option value="">
            -- Select --
          </option>

          {options.map(option => (
            <option
              key={option.id}
              value={option.id}
            >
              {typeof field.labelField ===
                'function'
                ? field.labelField(option)
                : String(
                  option[
                  field.labelField ?? ''
                  ] ?? '',
                )}
            </option>
          ))}
        </select>
      </div>
    )
  }

  // INPUT
  return (
    <div style={{ marginBottom: 12 }}>
      <label>
        {field.label}:{' '}
      </label>

      <input
        type={field.type || 'text'}
        value={String(value ?? '')}
        onChange={e =>
          onChange(e.target.value)
        }
      />
    </div>
  )
}