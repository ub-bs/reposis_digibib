import { createApp } from 'vue';
import { createI18n } from 'vue-i18n';
import { ModalPlugin, PaginationPlugin } from 'bootstrap-vue';
import 'bootstrap-vue/dist/bootstrap-vue.css';
import { fetchI18n, getWebApplicationBaseURL } from '@/utils';
import { useConfigStore } from '@/stores';
import { createPinia } from 'pinia';
import ContactManager from '@/App.vue';

const webApplicationBaseURL = getWebApplicationBaseURL() as string;

(async () => {
  const app = createApp(ContactManager);
  const data = await fetchI18n(webApplicationBaseURL);
  const i18n = createI18n({
    locale: '_',
    messages: {
      _: data,
    },
  });
  app.use(createPinia());
  const configStore = useConfigStore();
  configStore.webApplicationBaseURL = webApplicationBaseURL;
  app.use(i18n);
  app.use(ModalPlugin);
  app.use(PaginationPlugin);
  app.config.errorHandler = (err, instance, info) => {
    // eslint-disable-next-line
    console.error('Global error:', err);
    // eslint-disable-next-line
    console.log('Vue instance:', instance);
    // eslint-disable-next-line
    console.log('Error info:', info);
  };
  app.mount('#app');
})();
