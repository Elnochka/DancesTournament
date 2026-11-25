
import { useEffect, useState } from 'react'
import axios from 'axios'
import api from '../../api/axiosInstance'
import HistoryField from './HistoryField'
import EnumSelect from './EnumSelect'
import type { ErrorResponse } from '../../types/error'
import './GenericAdmin.css'

interface FieldConfig {
    name: string
    label: string
    type?: string
    min?: number
    max?: number
    step?: number
}

interface SelectOption {
    id: number
    [key: string]: unknown
}

interface SelectConfig {
    endpoint: string
    label?: string
    labelField?: string | ((item: SelectOption) => string)
    isMulti?: boolean
    sourceField?: string
}

type SelectsConfig = Record<string, SelectConfig>

interface EnumSelectConfig {
    enumName: string
    label: string
}

type EnumSelectsConfig = Record<string, EnumSelectConfig>

interface HistoryFieldConfig {
    name: string
    label: string
    type?: string
    endpoint?: string
    labelField?: string | ((item: SelectOption) => string)
    readOnly?: boolean
}

interface HistoryAction {
    label: string
    endpoint: string | ((item: GenericItem) => string)
    fields: HistoryFieldConfig[]
}

interface GenericItem {
    id: number
    locked?: boolean
    [key: string]: unknown
}

interface HistoryContext {
    action: HistoryAction
    item: GenericItem
}

interface FormData {
    [key: string]: unknown
}

type ColumnLabels = Record<string, string>

interface GenericAdminProps {
    endpoint: string
    title: string
    fields?: FieldConfig[]
    selects?: SelectsConfig
    enumSelects?: EnumSelectsConfig
    columnLabels?: ColumnLabels
    judgeMode?: boolean
    viewMode?: boolean
    initialItems?: GenericItem[]
    // onContextChange?: (context: unknown) => void
    historyActions?: HistoryAction[]
    hiddenColumns?: string[]
    onItemsChange?: () => Promise<void>
    filterBy?: {
        categoryId?: number
        // roundSettingsId?: number
        round?: string
        danceId?: number
    }
}

export default function GenericAdmin({
    endpoint,
    title,
    fields = [],
    selects = {},
    enumSelects = {},
    columnLabels = {},
    judgeMode = false,
    viewMode = false,
    initialItems,
    // onContextChange,
    onItemsChange,
    historyActions = [],
    hiddenColumns = [],
    filterBy,
}: GenericAdminProps) {

    const [items, setItems] = useState<GenericItem[]>(
        initialItems ?? [],
    )
    const [form, setForm] = useState<FormData>({
        kindOfDanceIds: [],
    })

    const [editingId, setEditingId] =
        useState<number | null>(null)

    const [fieldErrors, setFieldErrors] =
        useState<Record<string, string>>({})

    const [historyCtx, setHistoryCtx] =
        useState<HistoryContext | null>(null)

    const [selectOptions, setSelectOptions] =
        useState<Record<string, SelectOption[]>>({})

    const [selectsLoaded, setSelectsLoaded] =
        useState(false)

    const [search, setSearch] = useState('')

    const [sortField, setSortField] =
        useState<string | null>(null)

    const [sortDirection, setSortDirection] =
        useState<'asc' | 'desc'>('asc')

    type InlineStatus = {
        status: 'saving' | 'saved' | 'error'
        message?: string
    }

    const [inlineStatus, setInlineStatus] = useState<
        Record<string, InlineStatus>
    >({})

    const [inlineError, setInlineError] = useState<string | null>(null)

    const getColumnLabel = (key: string): string => {
        if (columnLabels[key]) {
            return columnLabels[key]
        }

        const field = fields.find(field => field.name === key)

        if (field) {
            return field.label
        }

        return key
    }

    useEffect(() => {
        if (initialItems) {
            setItems(initialItems)
        }
    }, [initialItems])


    /*
     * Load select options
     */
    useEffect(() => {
        async function loadSelects() {
            const entries = Object.entries(selects)

            if (entries.length === 0) {
                setSelectsLoaded(true)
                return
            }

            try {
                const results = await Promise.all(
                    entries.map(
                        async ([fieldName, config]) => {
                            const response =
                                await api.get<SelectOption[]>(
                                    config.endpoint,
                                )

                            return [
                                fieldName,
                                response.data,
                            ] as const
                        },
                    ),
                )

                setSelectOptions(
                    Object.fromEntries(results),
                )

                setSelectsLoaded(true)
            } catch (error) {
                console.error(
                    'Select options load error:',
                    error,
                )

                setSelectsLoaded(true)
            }
        }

        loadSelects()
    }, [selects])

    /*
     * Load table data
     */
    const loadList = async () => {
        try {
            const response =
                await api.get<GenericItem[]>(endpoint)

            setItems(response.data)
        }
        catch (error) {
            console.error(
                'GenericAdmin load error:',
                error,
            )
        }
    }

    useEffect(() => {
        if (!selectsLoaded) {
            return
        }

        if (initialItems !== undefined) {
            setItems(initialItems)
            return
        }

        loadList()
    }, [selectsLoaded, endpoint, initialItems])


    /*
     * Update form field
     */
    const updateField = (
        name: string,
        value: unknown,
    ) => {
        setForm(previous => ({
            ...previous,
            [name]: value,
        }))
    }

    const updateInlineField = async (
        item: GenericItem,
        field: 'passed' | 'place',
        value: boolean | number,
    ) => {
        const statusKey = `${item.id}-${field}`

        setInlineError(null)

        setInlineStatus(prev => ({
            ...prev,
            [statusKey]: {
                status: 'saving',
            },
        }))

        try {
            await api.patch(`${endpoint}/${item.id}`, {
                [field]: value,
            })

            if (initialItems !== undefined) {
                await onItemsChange?.()
            } else {
                await loadList()
            }

            setInlineStatus(prev => ({
                ...prev,
                [statusKey]: {
                    status: 'saved',
                },
            }))

        } catch (error: unknown) {
            const response = (
                error as {
                    response?: {
                        data?: {
                            message?: string
                            error?: string
                            details?: Record<string, string>
                        }
                    }
                }
            ).response?.data

            const message =
                response?.details
                    ? Object.values(response.details).join('; ')
                    : response?.message ||
                    response?.error ||
                    'Could not save changes'

            setInlineStatus(prev => ({
                ...prev,
                [statusKey]: {
                    status: 'error',
                },
            }))

            setInlineError(message)
        }
    }

    // const updateInlineField = async (
    //     item: GenericItem,
    //     field: 'passed' | 'place',
    //     value: boolean | number,
    // ) => {
    //     const statusKey = `${item.id}-${field}`

    //     setInlineStatus(prev => ({
    //         ...prev,
    //         [statusKey]: 'saving',
    //     }))

    //     try {
    //         await api.patch(`${endpoint}/${item.id}`, {
    //             [field]: value,
    //         })

    //         if (initialItems !== undefined) {
    //             await onItemsChange?.()
    //         } else {
    //             await loadList()
    //         }

    //         setInlineStatus(prev => ({
    //             ...prev,
    //             [statusKey]: 'saved',
    //         }))
    //     } catch (error) {
    //         console.error(`Could not update ${field}:`, error)

    //         setInlineStatus(prev => ({
    //             ...prev,
    //             [statusKey]: 'error',
    //         }))
    //     }
    // }

    /*
     * Edit
     */
    const edit = (item: GenericItem) => {
        const normalized: FormData = {
            ...item,
        }

        if (Array.isArray(item.kindOfDances)) {
            normalized.kindOfDanceIds =
                item.kindOfDances.map(
                    dance =>
                        typeof dance === 'object' &&
                            dance !== null
                            ? (
                                dance as SelectOption
                            ).id
                            : dance,
                )
        }

        Object.entries(selects).forEach(
            ([fieldName, config]) => {
                const sourceField =
                    config.sourceField ?? fieldName

                const value = item[sourceField]

                if (Array.isArray(value)) {
                    normalized[fieldName] =
                        value.map(itemValue =>
                            typeof itemValue === 'object' &&
                                itemValue !== null
                                ? (
                                    itemValue as SelectOption
                                ).id
                                : itemValue,
                        )
                } else if (
                    typeof value === 'object' &&
                    value !== null
                ) {
                    normalized[fieldName] =
                        (value as SelectOption).id
                } else {
                    normalized[fieldName] = value
                }
            },
        )

        Object.keys(enumSelects).forEach(
            fieldName => {
                const value = item[fieldName]

                if (
                    typeof value === 'object' &&
                    value !== null
                ) {
                    normalized[fieldName] = (
                        value as Record<
                            string,
                            unknown
                        >
                    ).name
                }
            },
        )

        setEditingId(item.id)
        setForm(normalized)
        setFieldErrors({})
    }

    /*
     * Delete
     */
    const remove = async (id: number) => {
        if (
            !window.confirm(
                'Are you sure you want to delete this item?',
            )
        ) {
            return
        }

        try {
            await api.delete(
                `${endpoint}/${id}`,
            )

            await loadList()
        } catch (error) {
            console.error(
                'Delete error:',
                error,
            )
        }
    }

    /*
     * Save / Update
     */
    const save = async () => {
        setFieldErrors({})

        const payload: Record<
            string,
            unknown
        > = {}

        fields.forEach(field => {
            const value = form[field.name]

            if (field.type === 'boolean') {
                payload[field.name] = !!value
                return
            }

            if (
                value === undefined ||
                value === ''
            ) {
                return
            }

            if (field.type === 'number') {
                payload[field.name] =
                    Number(value)
                return
            }

            payload[field.name] = value
        })

        Object.keys(selects).forEach(
            fieldName => {
                const value = form[fieldName]

                if (
                    value !== undefined &&
                    value !== ''
                ) {
                    payload[fieldName] = value
                }
            },
        )

        Object.keys(enumSelects).forEach(
            fieldName => {
                const value = form[fieldName]

                if (
                    value !== undefined &&
                    value !== ''
                ) {
                    payload[fieldName] = value
                }
            },
        )

        try {
            if (editingId !== null) {
                await api.patch(
                    `${endpoint}/${editingId}`,
                    payload,
                )
            } else {
                await api.post(
                    endpoint,
                    payload,
                )
            }

            resetForm()

            if (initialItems !== undefined) {
                await onItemsChange?.()
                return
            }

            await loadList()

        } catch (error: unknown) {

            if (axios.isAxiosError<ErrorResponse>(error)) {
                const response = error.response?.data

                if (response?.details && Object.keys(response.details).length > 0) {
                    setFieldErrors(response.details)
                } else {
                    setFieldErrors({
                        _general:
                            response?.message ||
                            response?.error ||
                            'Unexpected error',
                    })
                }
            } else {
                setFieldErrors({
                    _general: 'Unexpected error',
                })
            }
        }

        // } catch (error: any) {

        //     const response = error.response?.data

        //     if (response?.details) {
        //         setFieldErrors(response.details)
        //     } else {
        //         setFieldErrors({
        //             _general:
        //                 response?.message ||
        //                 response?.error ||
        //                 'Unexpected error',
        //         })
        //     }
        // }

    }

    /*
     * History action
     */
    const submitHistoryAction =
        async () => {
            if (!historyCtx) {
                return
            }

            const {
                action,
                item,
            } = historyCtx

            try {
                const payload: Record<
                    string,
                    unknown
                > = {}

                action.fields.forEach(
                    field => {
                        if (field.readOnly) {
                            return
                        }
                        const value =
                            form[field.name]

                        if (
                            value !== undefined &&
                            value !== ''
                        ) {
                            payload[field.name] =
                                value
                        }
                    },
                )

                const url =
                    typeof action.endpoint ===
                        'function'
                        ? action.endpoint(item)
                        : action.endpoint

                await api.post(
                    url,
                    payload,
                )

                setHistoryCtx(null)
                resetForm()
                await loadList()

            } catch (error: unknown) {
                if (axios.isAxiosError<ErrorResponse>(error)) {
                    alert(
                        error.response?.data?.message ||
                        'History action failed',
                    )
                } else {
                    alert('History action failed')
                }
            }
            
            // } catch (error: any) {
            //     alert(
            //         error.response?.data
            //             ?.message ||
            //         'History action failed',
            //     )
            // }
        }

    /*
     * Reset form
     */
    const resetForm = () => {
        setEditingId(null)

        setForm({
            kindOfDanceIds: [],
        })

        setFieldErrors({})
    }

    /*
     * Sorting
     */
    const sortBy = (
        field: string,
    ) => {
        const direction =
            sortField === field &&
                sortDirection === 'asc'
                ? 'desc'
                : 'asc'

        setSortField(field)
        setSortDirection(direction)

        setItems(previous =>
            [...previous].sort(
                (first, second) => {
                    const value1 =
                        first[field]

                    const value2 =
                        second[field]

                    if (value1 === value2) {
                        return 0
                    }

                    if (
                        value1 === undefined ||
                        value1 === null
                    ) {
                        return 1
                    }

                    if (
                        value2 === undefined ||
                        value2 === null
                    ) {
                        return -1
                    }

                    const string1 =
                        String(value1)

                    const string2 =
                        String(value2)

                    const comparison =
                        string1.localeCompare(
                            string2,
                            undefined,
                            {
                                numeric: true,
                                sensitivity: 'base',
                            },
                        )

                    return direction === 'asc'
                        ? comparison
                        : -comparison
                },
            ),
        )
    }

    /*
     * Search
     */

    const filteredItems = items.filter(item => {
        if (
            filterBy?.categoryId !== undefined &&
            Number(item.categoryTournamentId) !== filterBy.categoryId
        ) {
            return false
        }

        if (
            filterBy?.danceId !== undefined &&
            Number(item.danceId) !== filterBy.danceId
        ) {
            return false
        }

        if (
            filterBy?.round !== undefined &&
            String(item.round) !== filterBy.round
        ) {
            return false
        }

        return JSON.stringify(item)
            .toLowerCase()
            .includes(search.toLowerCase())
    })

    /*
     * PDF
     */

    const prepareForPdf = () => {
        return filteredItems.map(item => ({
            columns: Object.fromEntries(
                Object.entries(item)
                    .filter(
                        ([key]) =>
                            !hiddenColumns.includes(key),
                    )
                    .map(
                        ([key, value]) => {
                            if (typeof value === 'boolean') {
                                return [
                                    key,
                                    value ? '✔️' : '❌',
                                ]
                            }

                            if (Array.isArray(value)) {
                                return [
                                    key,
                                    value
                                        .map(itemValue =>
                                            typeof itemValue === 'object' &&
                                                itemValue !== null
                                                ? String(
                                                    (
                                                        itemValue as Record<
                                                            string,
                                                            unknown
                                                        >
                                                    ).name ??
                                                    JSON.stringify(
                                                        itemValue,
                                                    ),
                                                )
                                                : String(itemValue),
                                        )
                                        .join(', '),
                                ]
                            }

                            if (
                                typeof value === 'object' &&
                                value !== null
                            ) {
                                return [
                                    key,
                                    JSON.stringify(value),
                                ]
                            }

                            return [
                                key,
                                String(value ?? ''),
                            ]
                        },
                    ),
            ),
        }))
    }

    const printPdf = async () => {
        try {
            const rows = prepareForPdf()

            const response = await api.post(
                '/document/report',
                {
                    rows,
                },
                {
                    responseType: 'blob',
                },
            )

            const blob = new Blob(
                [response.data],
                {
                    type: 'application/pdf',
                },
            )

            const url =
                window.URL.createObjectURL(blob)

            window.open(url)
        } catch (error) {
            console.error(
                'PDF generation error:',
                error,
            )

            alert('Error generating PDF')
        }
    }

    return (
        <div className="generic-admin">

            {/* Header */}

            <div className="generic-admin-header">
                <div>
                    <h2 className="generic-admin-title">
                        {title}
                    </h2>

                    <p className="generic-admin-count">
                        {filteredItems.length}{' '}
                        {filteredItems.length === 1
                            ? 'record'
                            : 'records'}
                    </p>
                </div>

                <div className="generic-admin-header-actions">
                    <button
                        className="generic-admin-pdf-button"
                        onClick={printPdf}
                    >
                        Print PDF
                    </button>
                </div>
            </div>


            {/* General error */}

            {fieldErrors._general && (
                <div className="generic-admin-error">
                    {fieldErrors._general}
                </div>
            )}


            {/* Form */}

            {!viewMode && (
                <div className="generic-admin-form">

                    <div className="generic-admin-form-grid">

                        {fields.map(field => (
                            <div
                                className="generic-admin-field"
                                key={field.name}
                            >
                                <label>
                                    {field.label}
                                </label>

                                {field.type ===
                                    'boolean' ? (
                                    <label className="generic-admin-checkbox">
                                        <input
                                            type="checkbox"
                                            checked={
                                                !!form[
                                                field.name
                                                ]
                                            }
                                            onChange={e =>
                                                updateField(
                                                    field.name,
                                                    e.target
                                                        .checked,
                                                )
                                            }
                                        />

                                        <span>
                                            {field.label}
                                        </span>
                                    </label>
                                ) : (
                                    <input
                                        type={
                                            field.type ||
                                            'text'
                                        }
                                        min={
                                            field.type ===
                                                'number'
                                                ? 1
                                                : undefined
                                        }
                                        value={String(
                                            form[
                                            field.name
                                            ] ?? '',
                                        )}
                                        onChange={e =>
                                            updateField(
                                                field.name,
                                                e.target
                                                    .value,
                                            )
                                        }
                                        className={
                                            fieldErrors[
                                                field.name
                                            ]
                                                ? 'input-error'
                                                : ''
                                        }
                                    />
                                )}

                                {fieldErrors[
                                    field.name
                                ] && (
                                        <span className="field-error">
                                            {
                                                fieldErrors[
                                                field.name
                                                ]
                                            }
                                        </span>
                                    )}
                            </div>
                        ))}


                        {/* Selects */}

                        {Object.entries(
                            selects,
                        ).map(
                            ([
                                fieldName,
                                config,
                            ]) => (
                                <div
                                    className="generic-admin-field"
                                    key={fieldName}
                                >
                                    <label>
                                        {config.label ||
                                            (typeof config.labelField ===
                                                'string'
                                                ? config.labelField
                                                : fieldName)}
                                    </label>

                                    <select
                                        multiple={
                                            config.isMulti
                                        }
                                        value={
                                            config.isMulti
                                                ? (
                                                    (form[
                                                        fieldName
                                                    ] as
                                                        | unknown[]
                                                        | undefined) ||
                                                    []
                                                ).map(
                                                    String,
                                                )
                                                : String(
                                                    form[
                                                    fieldName
                                                    ] ?? '',
                                                )
                                        }
                                        onChange={e => {
                                            if (
                                                config.isMulti
                                            ) {
                                                const ids =
                                                    Array.from(
                                                        e.target
                                                            .selectedOptions,
                                                    ).map(
                                                        option =>
                                                            Number(
                                                                option.value,
                                                            ),
                                                    )

                                                updateField(
                                                    fieldName,
                                                    ids,
                                                )
                                            } else {
                                                updateField(
                                                    fieldName,
                                                    Number(
                                                        e.target
                                                            .value,
                                                    ),
                                                )
                                            }
                                        }}
                                    >
                                        {!config.isMulti && (
                                            <option value="">
                                                -- Select --
                                            </option>
                                        )}

                                        {(
                                            selectOptions[
                                            fieldName
                                            ] || []
                                        ).map(
                                            option => (
                                                <option
                                                    key={
                                                        option.id
                                                    }
                                                    value={
                                                        option.id
                                                    }
                                                >
                                                    {typeof config.labelField === 'function'
                                                        ? config.labelField(option)
                                                        : config.labelField
                                                            ? String(option[config.labelField] ?? '')
                                                            : String(option.id)}
                                                </option>
                                            ),
                                        )}
                                    </select>
                                </div>
                            ),
                        )}


                        {/* Enums */}

                        {Object.entries(enumSelects).map(
                            ([fieldName, config]) => (
                                <EnumSelect
                                    key={fieldName}
                                    enumName={config.enumName}
                                    label={config.label}
                                    value={form[fieldName]}
                                    onChange={value =>
                                        updateField(fieldName, value)
                                    }
                                />
                            ),
                        )}


                    </div>


                    {/* Form buttons */}

                    {!judgeMode && (
                        <div className="generic-admin-form-actions">

                            <button
                                className="generic-admin-save-button"
                                onClick={save}
                            >
                                {editingId !== null
                                    ? 'Update'
                                    : 'Save'}
                            </button>

                            {editingId !== null && (
                                <button
                                    className="generic-admin-cancel-button"
                                    onClick={
                                        resetForm
                                    }
                                >
                                    Cancel
                                </button>
                            )}

                        </div>
                    )}

                    {judgeMode &&
                        editingId !== null && (
                            <div className="generic-admin-form-actions">
                                <button
                                    className="generic-admin-save-button"
                                    onClick={save}
                                >
                                    Update
                                </button>

                                <button
                                    className="generic-admin-cancel-button"
                                    onClick={
                                        resetForm
                                    }
                                >
                                    Cancel
                                </button>
                            </div>
                        )}

                </div>
            )}


            {/* Search */}

            <div className="generic-admin-toolbar">

                <div className="generic-admin-search-wrapper">
                    <span className="generic-admin-search-icon">
                        🔍
                    </span>

                    <input
                        type="text"
                        placeholder="Search..."
                        value={search}
                        onChange={e =>
                            setSearch(
                                e.target.value.toLowerCase(),
                            )
                        }
                    />
                </div>

                {search && (
                    <button
                        className="generic-admin-clear-search"
                        onClick={() =>
                            setSearch('')
                        }
                    >
                        Clear
                    </button>
                )}

            </div>


            {/* Table */}

            <div className="generic-admin-table-wrapper">

                {inlineError && (
                    <div className="inline-error-message">
                        <strong>Error:</strong> {inlineError}
                    </div>
                )}

                <table className="generic-admin-table">


                    <thead>
                        <tr>
                            {items.length > 0 &&
                                Object.keys(items[0])
                                    .filter(key => !hiddenColumns.includes(key))
                                    .map(key => {

                                        return (
                                            <th
                                                key={key}
                                                onClick={() => sortBy(key)}
                                            >
                                                <span>
                                                    {getColumnLabel(key)}
                                                </span>

                                                {sortField === key && (
                                                    <span className="sort-indicator">
                                                        {sortDirection === 'asc'
                                                            ? '↑'
                                                            : '↓'}
                                                    </span>
                                                )}
                                            </th>
                                        )
                                    })}

                            <th className="actions-column">
                                Actions
                            </th>
                        </tr>
                    </thead>

                    <tbody>

                        {filteredItems.length ===
                            0 ? (
                            <tr>
                                <td
                                    colSpan={
                                        items.length > 0
                                            ? Object.keys(items[0]).filter(
                                                key =>
                                                    !hiddenColumns.includes(key),
                                            ).length + 1
                                            : 1
                                    }

                                    className="generic-admin-empty"
                                >
                                    No records found
                                </td>
                            </tr>
                        ) : (
                            filteredItems.map(
                                item => (
                                    <tr
                                        key={item.id}
                                        className={
                                            item.locked
                                                ? 'locked-row'
                                                : ''
                                        }
                                    >

                                        {
                                            Object.entries(item)
                                                .filter(
                                                    ([key]) =>
                                                        !hiddenColumns.includes(key),
                                                )
                                                .map(
                                                    ([key, value]) => (

                                                        <td
                                                            key={key}
                                                        >
                                                            {key === 'passed' &&
                                                                !endpoint.toLowerCase().includes('final') ? (
                                                                <div className="inline-edit-cell">
                                                                    <input
                                                                        type="checkbox"
                                                                        checked={Boolean(value)}
                                                                        disabled={Boolean(item.locked)}
                                                                        onChange={event =>
                                                                            updateInlineField(
                                                                                item,
                                                                                'passed',
                                                                                event.target.checked,
                                                                            )
                                                                        }
                                                                    />

                                                                    <span
                                                                        className={`inline-status ${inlineStatus[`${item.id}-${key}`]?.status === 'error'
                                                                            ? 'inline-status--error'
                                                                            : ''
                                                                            }`}
                                                                    >
                                                                        {inlineStatus[`${item.id}-${key}`]?.status === 'saving'
                                                                            ? 'Saving...'
                                                                            : inlineStatus[`${item.id}-${key}`]?.status === 'saved'
                                                                                ? 'Saved'
                                                                                : inlineStatus[`${item.id}-${key}`]?.status === 'error'
                                                                                    ? 'Error'
                                                                                    : ''}
                                                                    </span>

                                                                    {/* <span className="inline-status">
                                                                        {inlineStatus[`${item.id}-passed`] === 'saving'
                                                                            ? 'Saving...'
                                                                            : inlineStatus[`${item.id}-passed`] === 'saved'
                                                                                ? 'Saved'
                                                                                : inlineStatus[`${item.id}-passed`] === 'error'
                                                                                    ? 'Error!'
                                                                                    : ''}
                                                                    </span> */}

                                                                </div>

                                                            ) : key === 'place' &&
                                                                endpoint.toLowerCase().includes('final') ? (
                                                                <div className="inline-edit-cell">
                                                                    <input
                                                                        type="number"
                                                                        min={0}
                                                                        defaultValue={String(value ?? '')}
                                                                        disabled={Boolean(item.locked)}

                                                                        onBlur={event => {
                                                                            const rawValue = event.target.value.trim()

                                                                            const newValue = rawValue === ''
                                                                                ? 0
                                                                                : Number(rawValue)

                                                                            if (
                                                                                !Number.isInteger(newValue) ||
                                                                                newValue < 0
                                                                            ) {
                                                                                event.target.value = String(value ?? 0)
                                                                                return
                                                                            }

                                                                            if (newValue !== Number(value ?? 0)) {
                                                                                void updateInlineField(item, 'place', newValue)
                                                                            }
                                                                        }}

                                                                    // onBlur={event => {
                                                                    //     const rawValue = event.target.value.trim()

                                                                    //     if (rawValue === '') {
                                                                    //         return
                                                                    //     }

                                                                    //     const newValue = Number(rawValue)

                                                                    //     if (
                                                                    //         !Number.isInteger(newValue) ||
                                                                    //         newValue < 0
                                                                    //     ) {
                                                                    //         return
                                                                    //     }

                                                                    //     if (newValue !== Number(value)) {
                                                                    //         void updateInlineField(
                                                                    //             item,
                                                                    //             'place',
                                                                    //             newValue,
                                                                    //         )
                                                                    //     }
                                                                    // }}
                                                                    />

                                                                    <span
                                                                        className={`inline-status ${inlineStatus[`${item.id}-${key}`]?.status === 'error'
                                                                            ? 'inline-status--error'
                                                                            : ''
                                                                            }`}
                                                                    >
                                                                        {inlineStatus[`${item.id}-${key}`]?.status === 'saving'
                                                                            ? 'Saving...'
                                                                            : inlineStatus[`${item.id}-${key}`]?.status === 'saved'
                                                                                ? 'Saved'
                                                                                : inlineStatus[`${item.id}-${key}`]?.status === 'error'
                                                                                    ? 'Error'
                                                                                    : ''}
                                                                    </span>

                                                                    {/* <span className="inline-status">
                                                                        {inlineStatus[`${item.id}-place`] === 'saving'
                                                                            ? 'Saving...'
                                                                            : inlineStatus[`${item.id}-place`] === 'saved'
                                                                                ? 'Saved'
                                                                                : inlineStatus[`${item.id}-place`] === 'error'
                                                                                    ? 'Error!'
                                                                                    : ''}
                                                                    </span> */}

                                                                </div>
                                                            ) : typeof value === 'boolean' ? (
                                                                value ? '✔️' : '❌'
                                                            ) : Array.isArray(value) ? (
                                                                value
                                                                    .map(itemValue =>
                                                                        typeof itemValue === 'object' &&
                                                                            itemValue !== null
                                                                            ? String(
                                                                                (
                                                                                    itemValue as Record<string, unknown>
                                                                                ).name ?? JSON.stringify(itemValue),
                                                                            )
                                                                            : String(itemValue),
                                                                    )
                                                                    .join(', ')
                                                            ) : typeof value === 'object' && value !== null ? (
                                                                JSON.stringify(value)
                                                            ) : (
                                                                String(value ?? '')
                                                            )}
                                                            {/* {typeof value ===
                                                                'boolean'
                                                                ? value
                                                                    ? '✔️'
                                                                    : '❌'
                                                                : Array.isArray(
                                                                    value,
                                                                )
                                                                    ? value
                                                                        .map(
                                                                            itemValue =>
                                                                                typeof itemValue ===
                                                                                    'object' &&
                                                                                    itemValue !==
                                                                                    null
                                                                                    ? String(
                                                                                        (
                                                                                            itemValue as Record<
                                                                                                string,
                                                                                                unknown
                                                                                            >
                                                                                        ).name ??
                                                                                        JSON.stringify(
                                                                                            itemValue,
                                                                                        ),
                                                                                    )
                                                                                    : String(
                                                                                        itemValue,
                                                                                    ),
                                                                        )
                                                                        .join(
                                                                            ', ',
                                                                        )
                                                                    : typeof value ===
                                                                        'object' &&
                                                                        value !==
                                                                        null
                                                                        ? JSON.stringify(
                                                                            value,
                                                                        )
                                                                        : String(
                                                                            value ??
                                                                            '',
                                                                        )} */}
                                                        </td>
                                                    ),
                                                )}

                                        <td className="actions-cell">

                                            {!viewMode && (
                                                <button
                                                    className="table-action edit"
                                                    onClick={() =>
                                                        edit(item)
                                                    }
                                                    disabled={
                                                        item.locked
                                                    }
                                                    title={
                                                        item.locked
                                                            ? 'This round is locked'
                                                            : 'Edit'
                                                    }
                                                >
                                                    Edit
                                                </button>
                                            )}


                                            {historyActions.map(
                                                (
                                                    action,
                                                    index,
                                                ) => (
                                                    <button
                                                        key={
                                                            index
                                                        }
                                                        className="table-action history"

                                                        onClick={() => {
                                                            setForm(
                                                                { ...item, },
                                                            )

                                                            setHistoryCtx(
                                                                {
                                                                    action,
                                                                    item,
                                                                },
                                                            )
                                                        }}
                                                        disabled={
                                                            item.locked
                                                        }
                                                    >
                                                        {
                                                            action.label
                                                        }
                                                    </button>
                                                ),
                                            )}


                                            {!judgeMode &&
                                                !viewMode && (
                                                    <button
                                                        className="table-action delete"
                                                        onClick={() =>
                                                            remove(
                                                                item.id,
                                                            )
                                                        }
                                                        disabled={
                                                            item.locked
                                                        }
                                                        title={
                                                            item.locked
                                                                ? 'This round is locked'
                                                                : 'Delete'
                                                        }
                                                    >
                                                        Delete
                                                    </button>
                                                )}

                                        </td>

                                    </tr>
                                ),
                            )
                        )}

                    </tbody>

                </table>

            </div>


            {/* History modal */}

            {historyCtx && (
                <div className="generic-admin-modal">

                    <div className="generic-admin-modal-content">

                        <div className="generic-admin-modal-header">

                            <h3>
                                {
                                    historyCtx
                                        .action
                                        .label
                                }
                            </h3>

                            <button
                                className="generic-admin-modal-close"
                                onClick={() =>
                                    setHistoryCtx(
                                        null,
                                    )
                                }
                            >
                                ×
                            </button>

                        </div>


                        <div className="generic-admin-modal-body">

                            {historyCtx.action.fields.map(
                                field => (
                                    <HistoryField
                                        key={
                                            field.name
                                        }
                                        field={field}
                                        value={
                                            form[
                                            field.name
                                            ]
                                        }
                                        onChange={value =>
                                            setForm(
                                                previous => ({
                                                    ...previous,
                                                    [field.name]:
                                                        value,
                                                }),
                                            )
                                        }
                                    />
                                ),
                            )}

                        </div>


                        <div className="generic-admin-modal-actions">

                            <button
                                className="generic-admin-save-button"
                                onClick={
                                    submitHistoryAction
                                }
                            >
                                Confirm
                            </button>

                            <button
                                className="generic-admin-cancel-button"
                                onClick={() =>
                                    setHistoryCtx(
                                        null,
                                    )
                                }
                            >
                                Cancel
                            </button>

                        </div>

                    </div>

                </div>
            )}

        </div>
    )
}