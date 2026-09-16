export default function BadgeEstado({ estado }) {
  const clave = String(estado || "").toLowerCase();
  return <span className={`badge badge-estado badge-${clave}`}>{estado}</span>;
}
