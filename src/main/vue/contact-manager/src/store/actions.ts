import { ActionTree } from 'vuex';
import axios from 'axios';
import { State } from './state';

export const actions: ActionTree<State, State> = {
  async fetchData({ commit, state }): Promise<void> {
    commit('setLoading', true);
    try {
      const response = await axios.get('api/v2/contacts');
      commit('setRequests', response.data);
      commit('setTotalRows', response.headers['x-total-count']);
    } catch (error) {
      console.error(error);
    }
    commit('setLoading', false);
  },
  async removeContactRequest({ commit, state }, id: number): Promise<void> {
    try {
      await axios.delete(`api/v2/contacts/${id}`);
      state.requests.splice(state.requests.findIndex((i) => i.id === id), 1);
      commit('setTotalRows', state.totalRows - 1);
    } catch (error) {
      console.error(error);
    }
  },
};
