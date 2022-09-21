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
  store.commit('main/SET_PER_PAGE', 1);
  let authError = false;
  if (process.env.NODE_ENV === 'development') {
    axios.defaults.headers.common.Authorization = 'Basic YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=';
  } else {
    try {
      const jwtResponse = await axios.get('rsc/jwt');
      const jwtToken = jwtResponse.data.access_token;
      axios.defaults.headers.common.Authorization = `Bearer ${jwtToken}`;
    } catch (error) {
      authError = true;
    }
  }
  if (!authError) {
    await store.dispatch('main/fetchData');
  } else {
    store.commit('main/SET_ERROR_CODE', 'unknown');
  }
  store.commit('main/SET_IS_BOOTED', true);
})();
