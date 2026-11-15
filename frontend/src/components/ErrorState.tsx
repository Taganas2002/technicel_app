export function ErrorState({ message }: { message: string }) {
  return <p className="state error-text">{message}</p>;
}
