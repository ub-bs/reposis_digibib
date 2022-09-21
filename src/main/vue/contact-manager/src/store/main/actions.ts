import { ActionTree } from 'vuex';
import axios from 'axios';
import { RootState } from '../types';
import { State } from './state';
import { MutationTypes } from './mutation-types';

export const actions: ActionTree<State, RootState> = {
  async fetchData({ state, commit }): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    try {
      const offset = state.currentPage;
      const limit = state.perPage;
      const response = await axios.get(`api/v2/contacts?offset=${offset}&limit=${limit}`);
      commit(MutationTypes.SET_REQUESTS, response.data);
      commit(MutationTypes.SET_TOTAL_ROWS, response.headers['x-total-count']);
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async removeContactRequest({ commit, state }, id: string): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    try {
      await axios.delete(`api/v2/contacts/${id}`);
      state.requests.splice(state.requests.findIndex((i) => i.uuid === id), 1);
      commit(MutationTypes.SET_TOTAL_ROWS, state.totalRows - 1);
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
};
