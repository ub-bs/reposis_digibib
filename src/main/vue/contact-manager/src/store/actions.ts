import { ActionTree } from 'vuex';
import { State } from './state';

// const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const actions: ActionTree<State, State> = {
  async fetchData({ commit, state }): Promise<void> {
    commit('setLoading', true);
    if (state.currentPage === 0) {
      commit('setRequests', [
        {
          id: 1,
          name: 'Lennard Golsch',
          email: 'l.golsch@tu-braunschweig.de',
          orcid: '1112-1212-1221',
          created: new Date(),
          state: 'ready',
          message: 'Message',
          objectID: 'mir_mods_12121212',
          recipients: [
            {
              name: 'Lennard Golsch',
              source: 'ORCID',
              email: 'l.golsch@tu-braunschweig.de',
            },
          ],
        },
      ]);
    } else {
      commit('setRequests', [
        {
          id: 2,
          name: 'Name2',
          email: 'Email',
          orcid: '1112-1212-1221',
          created: new Date(),
          state: 'ready',
          message: 'Message',
          objectID: 'mir_mods_12121212',
        },
      ]);
    }
    commit('setTotalRows', 2);
    commit('setLoading', false);
  },
};
