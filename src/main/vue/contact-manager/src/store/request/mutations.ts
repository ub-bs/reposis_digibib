import { MutationTree } from 'vuex';
import { State } from './state';
import { MutationTypes } from './mutation-types';
import { Request } from '../../utils';

export type Mutations<S = State> = {
  [MutationTypes.SET_REQUEST](state: S, payload: Request | undefined): void;
  [MutationTypes.SET_INFO_CODE](state: S, payload: string | undefined): void;
  [MutationTypes.SET_ERROR_CODE](state: S, payload: string | undefined): void;
}

export const mutations: MutationTree<State> & Mutations = {
  [MutationTypes.SET_REQUEST](state: State, request: Request | undefined): void {
    state.request = request;
  },
  [MutationTypes.SET_ERROR_CODE](state: State, errorCode: string | undefined): void {
    state.errorCode = errorCode;
  },
  [MutationTypes.SET_INFO_CODE](state: State, infoCode: string | undefined): void {
    state.infoCode = infoCode;
  },
};
