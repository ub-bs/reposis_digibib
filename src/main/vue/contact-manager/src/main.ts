import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import axios from 'axios';
import store from './store';
import ContactManager from './App.vue';
import { ErrorResponse } from './utils';

if (process.env.NODE_ENV === 'development') {
  axios.defaults.baseURL = 'http://134.169.20.124/mir/';
} else {
  // eslint-disable-next-line
  axios.defaults.baseURL = (window as any).webApplicationBaseURL;
}

axios.interceptors.response.use((response) => response, (error) => {
  if (axios.isAxiosError(error) && error.response) {
    if (error.response.status === 401 || error.response.status === 403) {
      return Promise.reject(new Error('unauthorizedError'));
    }
    const { errorCode } = (error.response.data as ErrorResponse);
    if (errorCode && errorCode !== 'UNKNOWN') {
      return Promise.reject(new Error(errorCode));
    }
  }
  return Promise.reject(new Error('unknown'));
});

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
  store.commit('setCurrentPage', 0);
  store.commit('setPerPage', 4);
  app.use(store);
  app.mount('#app');

  if (process.env.NODE_ENV === 'development') {
    axios.defaults.headers.common.Authorization = 'Basic YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=';
    await store.dispatch('fetchData');
  } else {
    try {
      const jwtResponse = await axios.get('rsc/jwt');
      const jwtToken = jwtResponse.data.access_token;
      axios.defaults.headers.common.Authorization = `Bearer ${jwtToken}`;
      await store.dispatch('fetchData');
    } catch (error) {
      if (error instanceof Error) {
        store.commit('setApplicationErrorCode', error.message);
      } else {
        store.commit('setApplicationErrorCode', 'unknown');
      }
    }
  }
})();
