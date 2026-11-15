import type { ReactNode } from 'react';

export function TechnicianCard({
  code,
  name,
  children,
}: {
  code: string;
  name: string;
  children?: ReactNode;
}) {
  return (
    <article className="card">
      {code ? <p className="eyebrow">{code}</p> : null}
      <h2>{name}</h2>
      {children}
    </article>
  );
}
