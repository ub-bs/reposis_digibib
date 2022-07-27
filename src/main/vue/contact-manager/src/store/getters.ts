import { GetterTree } from 'vuex';
import { State, Request } from './state';

export type Getters = {
  getRequestById(state: State): (id :number) => Request | undefined
}

export const getters: GetterTree<State, State> & Getters = {
  // eslint-disable-next-line arrow-body-style
  getRequestById: (state) => (id: number) => {
    return state.requests.find((request) => request.id === id);
  },
};
