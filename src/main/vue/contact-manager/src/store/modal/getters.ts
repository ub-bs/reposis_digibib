import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Recipient } from '../../utils';
import { State } from './state';

export type Getters = {
  getCurrentRecipients(state: State): Array<Recipient>;
}

export const getters: GetterTree<State, RootState> & Getters = {
  getCurrentRecipients: (state) => {
    if (state.currentRequest === undefined) {
      return [];
    }
    return state.currentRequest.recipients;
  },
};
