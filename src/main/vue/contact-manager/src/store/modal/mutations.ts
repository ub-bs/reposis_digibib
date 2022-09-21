import { MutationTree } from 'vuex';
import { State } from './state';
import { MutationTypes } from './mutation-types';
import { Request } from '../../utils';

export type Mutations<S = State> = {
  [MutationTypes.SET_SHOW_REQUEST_MODAL](state: S, payload: boolean): void;
  [MutationTypes.SET_EDIT_RECIPIENT_ID](state: S, payload: string | undefined): void;
  [MutationTypes.SET_CURRENT_REQUEST](state: S, payload: Request): void;
  [MutationTypes.SET_INFO_CODE](state: S, payload: string): void;
  [MutationTypes.SET_ERROR_CODE](state: S, payload: string): void;
}

export const mutations: MutationTree<State> & Mutations = {
  [MutationTypes.SET_SHOW_REQUEST_MODAL](state: State, show: boolean): void {
    state.showRequestModal = show;
  },
  [MutationTypes.SET_EDIT_RECIPIENT_ID](state: State, id: string | undefined): void {
    state.editRecipientId = id;
  },
  [MutationTypes.SET_CURRENT_REQUEST](state: State, request: Request): void {
    state.currentRequest = request;
  },
  [MutationTypes.SET_ERROR_CODE](state: State, errorCode: string): void {
    state.errorCode = errorCode;
  },
  [MutationTypes.SET_INFO_CODE](state: State, infoCode: string): void {
    state.infoCode = infoCode;
  },
};
