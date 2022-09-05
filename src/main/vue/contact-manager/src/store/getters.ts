import { GetterTree } from 'vuex';
import { Recipient, Request } from '../utils';
import { State } from './state';

export type Getters = {
  getRequestById(state: State): (id :string) => Request | undefined;
  getCurrentRequests(state: State): Array<Request>;
  getCurrentRecipients(state: State): Array<Recipient>;
}

export const getters: GetterTree<State, State> & Getters = {
  getRequestById: (state) => (id: string) => state.requests.find((request) => request.uuid === id),
  getCurrentRequests: (state) => {
    const { perPage, currentPage, requests } = state;
    return requests.slice().splice(currentPage * perPage, perPage);
  },
  getCurrentRecipients: (state) => {
    if (state.currentRequest === undefined) {
      return [];
    }
    return state.currentRequest.recipients;
  },
};
