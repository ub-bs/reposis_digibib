import { defineStore } from 'pinia';

interface State {
  webApplicationBaseURL: string
}

export const useConfigStore = defineStore('config', {
  state: (): State => ({
    webApplicationBaseURL: '',
  }),
});
