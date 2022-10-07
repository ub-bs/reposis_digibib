import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Recipient, Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getRecipientByUUID(state: State): (slug: string, recipientUUID: string) => Recipient | undefined;
  getRequests(state: State): Request[];
}

export const getters: GetterTree<State, RootState> & Getters = {
  getRecipientByUUID: (state: State) => (slug: string, recipientUUID: string) => state.cache[slug]
    .recipients.find((r) => r.uuid === recipientUUID),
  getRequests: (state: State) => Object.values(state.cache),
};
