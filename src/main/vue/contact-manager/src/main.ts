import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import axios from 'axios';
import store from './store';
import ContactManager from './App.vue';

if (process.env.NODE_ENV === 'development') {
  axios.defaults.baseURL = 'http://134.169.20.124/mir/';
} else {
  // eslint-disable-next-line
  axios.defaults.baseURL = (window as any).webApplicationBaseURL;
}

(async () => {
  const app = createApp(ContactManager);
  const i18nResponse = await axios.get('rsc/locale/translate/digibib.contact.frontend.*');
  const i18n = createI18n({
    locale: '_',
    messages: {
      _: i18nResponse.data,
    },
  });
  app.use(i18n);
  app.use(store);
  app.mount('#app');
})();
