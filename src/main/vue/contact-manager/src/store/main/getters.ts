import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getRequestById(state: State): (id :string) => Request | undefined;
  getCurrentRequests(state: State): Array<Request>;
}

export const getters: GetterTree<State, RootState> & Getters = {
  getRequestById: (state) => (id: string) => state.requests.find((request) => request.uuid === id),
  getCurrentRequests: (state) => {
    const { perPage, currentPage, requests } = state;
    return requests.slice().splice(currentPage * perPage, perPage);
  },
};
