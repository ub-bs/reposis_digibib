import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getRequests(state: State): Request[];
}

export const getters: GetterTree<State, RootState> & Getters = {
  getRequests: (state: State) => Object.values(state.cache),
};
