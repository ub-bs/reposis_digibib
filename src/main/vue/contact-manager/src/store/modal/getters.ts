import { GetterTree } from 'vuex';
import { Recipient, Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getCurrentRecipients(state: State): Array<Recipient>;
}

export const getters: GetterTree<State, State> & Getters = {
  getCurrentRecipients: (state) => {
    if (state.currentRequest === undefined) {
      return [];
    }
    return state.currentRequest.recipients;
  },
};
