import { ActionTree } from 'vuex';
import { RootState } from '../types';
import { State } from './state';
import { MutationTypes } from './mutation-types';
import { getRequests, removeRequest } from '../../api/service';

export const actions: ActionTree<State, RootState> = {
  async fetchData({ state, commit }): Promise<void> {
    commit(MutationTypes.SET_LOADING, true);
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    try {
      const offset = state.currentPage * state.perPage;
      const limit = offset + state.perPage;
      const response = await getRequests(offset, limit);
      commit(MutationTypes.SET_REQUESTS, response.data);
      commit(MutationTypes.SET_TOTAL_ROWS, response.headers['x-total-count']);
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    } finally {
      commit(MutationTypes.SET_LOADING, false);
    }
  },
  async removeContactRequest({ commit, state }, id: string): Promise<void> {
    commit(MutationTypes.SET_LOADING, true);
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    try {
      await removeRequest(id);
      const { currentPage } = state;
      if (state.requests.length === 0 || currentPage > 0) {
        commit(MutationTypes.SET_CURRENT_PAGE, currentPage - 1);
        const offset = state.currentPage * state.perPage;
        const limit = offset + state.perPage;
        const response = await getRequests(offset, limit);
        commit(MutationTypes.SET_REQUESTS, response.data);
        commit(MutationTypes.SET_TOTAL_ROWS, response.headers['x-total-count']);
      } else {
        state.requests.splice(state.requests.findIndex((i) => i.uuid === id), 1);
        commit(MutationTypes.SET_TOTAL_ROWS, state.totalRows - 1);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    } finally {
      commit(MutationTypes.SET_LOADING, false);
    }
  },
};
