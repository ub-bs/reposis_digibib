import { MutationTree } from 'vuex';
import { State } from './state';
import { MutationTypes } from './mutation-types';
import { Request } from '../../utils';

export type Mutations<S = State> = {
  [MutationTypes.SET_REQUESTS](state: S, payload: Request[]): void;
  [MutationTypes.SET_IS_BOOTED](state: S, payload: boolean): void;
  [MutationTypes.SET_LOADING](state: S, payload: boolean): void;
  [MutationTypes.SET_CURRENT_PAGE](state: S, payload: number): void;
  [MutationTypes.SET_PER_PAGE](state: S, payload: number): void;
  [MutationTypes.SET_TOTAL_ROWS](state: S, payload: number): void;
  [MutationTypes.SET_ERROR_CODE](state: S, payload: string): void;
}

export const mutations: MutationTree<State> & Mutations = {
  [MutationTypes.SET_REQUESTS](state: State, requests: Request[]): void {
    state.requests = requests;
  },
  [MutationTypes.SET_IS_BOOTED](state: State, booted: boolean): void {
    state.isBooted = booted;
  },
  [MutationTypes.SET_LOADING](state: State, loading: boolean): void {
    state.loading = loading;
  },
  [MutationTypes.SET_CURRENT_PAGE](state: State, page: number): void {
    state.currentPage = page;
  },
  [MutationTypes.SET_PER_PAGE](state: State, perPage: number): void {
    state.perPage = perPage;
  },
  [MutationTypes.SET_TOTAL_ROWS](state: State, totalRows: number): void {
    state.totalRows = totalRows;
  },
  [MutationTypes.SET_ERROR_CODE](state: State, errorCode: string): void {
    state.errorCode = errorCode;
  },
};
