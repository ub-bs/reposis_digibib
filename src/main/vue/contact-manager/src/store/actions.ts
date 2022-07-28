import { ActionTree } from 'vuex';
import axios from 'axios';
import { State } from './state';

// const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const actions: ActionTree<State, State> = {
  async fetchData({ commit, state }): Promise<void> {
    commit('setLoading', true);
    const response = await axios.get('api/v2/contacts');
    commit('setRequests', response.data);
    commit('setTotalRows', 1); // total header
    commit('setLoading', false);
  },
};
