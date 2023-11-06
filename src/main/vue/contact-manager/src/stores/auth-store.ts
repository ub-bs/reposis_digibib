import { defineStore } from 'pinia';
import { JWT, fetchJWT } from '@/utils';
import { useConfigStore } from './config-store';

interface State {
  accessToken: string | undefined
}

export const useAuthStore = defineStore('auth', {
  state: (): State => ({
    accessToken: undefined,
  }),
  actions: {
    async login(): Promise<void> {
      const { webApplicationBaseURL } = useConfigStore();
      try {
        const jwt: JWT = await fetchJWT(webApplicationBaseURL);
        if (!jwt.login_success) {
          throw new Error('unauthorizedError');
        }
        this.accessToken = jwt.access_token;
      } catch (error) {
        throw new Error('unauthorizedError');
      }
    },
  },
});
