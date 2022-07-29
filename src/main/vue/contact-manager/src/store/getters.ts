import { GetterTree } from 'vuex';
import { State, Request } from './state';

export type Getters = {
  getRequestById(state: State): (id :number) => Request | undefined;
  getCurrentRequests(state: State): Array<Request>;
}

export const getters: GetterTree<State, State> & Getters = {
  getRequestById: (state) => (id: number) => state.requests.find((request) => request.id === id),
  getCurrentRequests: (state) => {
    const { perPage, currentPage, requests } = state;
    return requests.slice().splice(currentPage * perPage, perPage);
  },
};
