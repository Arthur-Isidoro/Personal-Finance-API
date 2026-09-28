export default function Loading({ label = 'Loading…' }) {
  return (
    <div className="loading-row">
      <span className="spinner" />
      <span>{label}</span>
    </div>
  )
}
