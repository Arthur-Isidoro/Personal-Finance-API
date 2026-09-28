export default function Select({ label, id, children, ...props }) {
  return (
    <div className="field">
      {label && <label htmlFor={id}>{label}</label>}
      <select id={id} className="input" {...props}>
        {children}
      </select>
    </div>
  )
}
