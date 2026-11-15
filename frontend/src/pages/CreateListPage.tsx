import { Link, useNavigate } from 'react-router-dom';
import { CreateListForm } from '../components/CreateListForm';
import { useLanguage } from '../i18n/LanguageContext';

export function CreateListPage() {
  const navigate = useNavigate();
  const { t } = useLanguage();
  return (
    <section className="page">
      <article className="card">
        <h2>{t('createList')}</h2>
        <p className="muted">{t('createListHint')}</p>
        <CreateListForm onCreated={() => navigate('/lists/current')} />
        <Link className="ghost-button" to="/dashboard">
          {t('backToDashboard')}
        </Link>
      </article>
    </section>
  );
}
