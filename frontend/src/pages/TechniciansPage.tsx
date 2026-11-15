import { FormEvent, useEffect, useState } from 'react';
import { EmptyState } from '../components/EmptyState';
import { LoadingState } from '../components/LoadingState';
import { TechnicianCard } from '../components/TechnicianCard';
import { apiErrorMessage } from '../api/errors';
import { useLanguage } from '../i18n/LanguageContext';
import { createTechnician, listTechnicians, updateTechnicianStatus } from '../services/technicianService';
import type { TechnicianAccount } from '../types';
import { Link } from 'react-router-dom';

export function TechniciansPage() {
  const { t } = useLanguage();
  const [technicians, setTechnicians] = useState<TechnicianAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [statusId, setStatusId] = useState<number | null>(null);
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    nip: '',
    password: '',
  });

  async function reload() {
    const page = await listTechnicians();
    setTechnicians(page.content);
  }

  useEffect(() => {
    let cancelled = false;
    listTechnicians()
      .then((page) => {
        if (!cancelled) {
          setTechnicians(page.content);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, t('unableLoadTechnicians')));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [t]);

  async function onToggleStatus(id: number, active: boolean) {
    setError('');
    setSuccess('');
    setStatusId(id);
    try {
      const updated = await updateTechnicianStatus(id, !active);
      setTechnicians((current) => current.map((item) => (item.id === id ? updated : item)));
      setSuccess(updated.active ? t('accountActivated') : t('accountDeactivatedDone'));
    } catch (err) {
      setError(apiErrorMessage(err, t('unableUpdateStatus')));
    } finally {
      setStatusId(null);
    }
  }

  async function onCreate(event: FormEvent) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      await createTechnician(form);
      setForm({ firstName: '', lastName: '', nip: '', password: '' });
      setSuccess(t('technicianCreated'));
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, t('unableCreateTechnician')));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="page">
      <article className="card">
        <h2>{t('createTechnician')}</h2>
        <form className="form-stack" onSubmit={onCreate}>
          <label htmlFor="firstName">{t('firstName')}</label>
          <input
            id="firstName"
            value={form.firstName}
            onChange={(event) => setForm((current) => ({ ...current, firstName: event.target.value }))}
            required
          />
          <label htmlFor="lastName">{t('lastName')}</label>
          <input
            id="lastName"
            value={form.lastName}
            onChange={(event) => setForm((current) => ({ ...current, lastName: event.target.value }))}
            required
          />
          <label htmlFor="nip">{t('nip')}</label>
          <input
            id="nip"
            value={form.nip}
            onChange={(event) => setForm((current) => ({ ...current, nip: event.target.value }))}
            required
          />
          <label htmlFor="password">{t('password')}</label>
          <input
            id="password"
            type="password"
            minLength={6}
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            required
          />
          {error ? <p className="error-text">{error}</p> : null}
          {success ? <p className="success-text">{success}</p> : null}
          <button type="submit" className="primary-button" disabled={saving}>
            {saving ? t('creating') : t('createAccount')}
          </button>
        </form>
      </article>

      {loading ? <LoadingState label={t('loadingTechnicians')} /> : null}
      {!loading && technicians.length === 0 ? <EmptyState label={t('noTechniciansYet')} /> : null}
      <div className="card-list">
        {technicians.map((tech) => (
          <TechnicianCard key={tech.id} code="" name={`${tech.firstName} ${tech.lastName}`}>
            <p className="muted">{t('nip')} {tech.nip}</p>
            <p>{tech.active ? t('statusActive') : t('statusInactive')}</p>
            <button
              type="button"
              className={tech.active ? 'ghost-button' : 'primary-button'}
              disabled={statusId === tech.id}
              onClick={() => void onToggleStatus(tech.id, tech.active)}
            >
              {statusId === tech.id ? t('saving') : tech.active ? t('deactivateAccount') : t('activateAccount')}
            </button>
            <Link className="primary-button" to={`/admin/technicians/${tech.id}`}>
              {t('viewDetails')}
            </Link>
          </TechnicianCard>
        ))}
      </div>
    </section>
  );
}
