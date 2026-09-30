// Each currency is formatted using the locale that matches it, independent of
// the interface language -- this mirrors how these currencies are actually
// grouped/punctuated in the real world (e.g. R$ 1.234,56 vs $1,234.56).
const CURRENCY_LOCALE = {
  BRL: 'pt-BR',
  USD: 'en-US',
  EUR: 'en-US',
}

export const formatCurrency = (value, currency = 'BRL') => {
  const locale = CURRENCY_LOCALE[currency] || 'en-US'
  return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(value ?? 0)
}

// Dates follow the interface language: pt-BR -> dd/mm/yyyy, en-US -> mm/dd/yyyy
export const formatDate = (value, language = 'pt-BR') => {
  if (!value) return ''
  const d = new Date(value + 'T00:00:00')
  return new Intl.DateTimeFormat(language).format(d)
}
