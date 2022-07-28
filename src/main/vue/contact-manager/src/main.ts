import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import axios from 'axios';
import store from './store';
import ContactManager from './App.vue';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function initialize(i18nData: any) {
  const i18n = createI18n({
    locale: '_',
    messages: {
      _: i18nData,
    },
  });
  const app = createApp(ContactManager);
  store.commit('setCurrentPage', 0);
  store.commit('setPerPage', 1);
  store.dispatch('fetchData');
  app.use(store);
  app.use(i18n);
  app.mount('#app');
}

if (process.env.NODE_ENV === 'development') {
  axios.defaults.baseURL = 'http://localhost:8291/mir/';
  axios.defaults.headers.common.Authorization = 'Basic YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=';
}

fetch('http://localhost:8291/mir/rsc/locale/translate/digibib.contact.frontend.*')
  .then((response) => response.json())
  .then((data) => initialize(data))
  .catch(() => initialize({}));
