
import useEnum from '../../hooks/useEnum'

interface EnumSelectProps {
  enumName: string
  label: string
  value: unknown
  onChange: (value: string) => void
}

export default function EnumSelect({
  enumName,
  label,
  value,
  onChange,
}: EnumSelectProps) {
  const { values } = useEnum(enumName)

  return (
     <div className="generic-admin-field">
      <label>
        {label}:{' '}
      </label>

      <select
        value={String(value ?? '')}
        onChange={e => onChange(e.target.value)}
      >
        <option value="">-- Select --</option>

        {values.map(enumValue => (
          <option key={enumValue} value={enumValue}>
            {enumValue}
          </option>
        ))}
      </select>
    </div>
  )
}