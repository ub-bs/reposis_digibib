import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getRequestByUUID(state: State): (slug: string) => Request| undefined;
  getRequests(state: State): Request[];
}

export const getters: GetterTree<State, RootState> & Getters = {
  getRequestByUUID: (state: State) => (slug: string) => state.requests.find((r) => r.uuid === slug),
  getRequests: (state: State) => Object.values(state.cache),
};
