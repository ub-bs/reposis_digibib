import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Recipient } from '../../utils';
import { State } from './state';

export type Getters = {
  getCurrentRecipients(state: State): Array<Recipient>;
  getRecipientByUUID(state: State): (uuid: string) => Recipient | undefined;
}

export const getters: GetterTree<State, RootState> & Getters = {
  getCurrentRecipients: (state: State) => {
    if (state.request === undefined) {
      return [];
    }
    return state.request.recipients;
  },
  getRecipientByUUID: (state: State) => (uuid: string) => {
    if (state.request && state.request.recipients) {
      return state.request.recipients.find((r) => r.uuid === uuid);
    }
    return undefined;
  },
};
