import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import ContactForm from './App.vue';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function initialize(i18nData: any) {
  const i18n = createI18n({
    locale: '_',
    messages: {
      _: i18nData,
    },
  });
  const app = createApp(ContactForm, {
    objectId: 'dbbs_mods_00000006',
    baseUrl: 'http://localhost:8291/mir/',
  });
  app.use(i18n);
  app.mount('#app');
}

fetch('http://localhost:8291/mir/rsc/locale/translate/digibib.contact.frontend.*')
  .then((response) => response.json())
  .then((data) => initialize(data))
  .catch(() => initialize({}));
