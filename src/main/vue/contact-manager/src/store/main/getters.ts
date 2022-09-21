import { GetterTree } from 'vuex';
import { RootState } from '../types';
import { Request } from '../../utils';
import { State } from './state';

export type Getters = {
  getRequestById(state: State): (id :string) => Request | undefined;
}

export const getters: GetterTree<State, RootState> & Getters = {
  getRequestById: (state) => (id: string) => state.requests.find((request) => request.uuid === id),
};
