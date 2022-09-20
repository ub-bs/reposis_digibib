import { MutationTree } from 'vuex';
import { State } from './state';

export const mutations: MutationTree<State> = {
  setShowRequestModal(state, show): void {
    state.showRequestModal = show;
  },
  setEditRecipientId(state, id): void {
    state.editRecipientId = id;
  },
  setCurrentRequest(state, request): void {
    state.currentRequest = request;
  },
  setModalErrorCode(state, errorCode): void {
    state.modalErrorCode = errorCode;
  },
  setModalInfoCode(state, infoCode): void {
    state.modalInfoCode = infoCode;
  },
};
