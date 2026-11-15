export function PlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <section className="page">
      <article className="card">
        <h2>{title}</h2>
        <p className="muted">{description}</p>
      </article>
    </section>
  );
}
