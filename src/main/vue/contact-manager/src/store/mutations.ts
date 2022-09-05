import { MutationTree } from 'vuex';
import { State } from './state';

export const mutations: MutationTree<State> = {
  setRequests(state, requests): void {
    state.requests = requests;
  },
  setLoading(state, value): void {
    state.loading = value;
  },
  setCurrentPage(state, page): void {
    state.currentPage = page;
  },
  setPerPage(state, perPage): void {
    state.perPage = perPage;
  },
  setTotalRows(state, totalRows): void {
    state.totalRows = totalRows;
  },
  setShowRequestModal(state, show): void {
    state.showRequestModal = show;
  },
  setEditRecipient(state, id): void {
    state.editRecipientId = id;
  },
  setCurrentRequest(state, request): void {
    state.currentRequest = request;
  },
  setModalErrorCode(state, errorCode): void {
    state.modalErrorCode = errorCode;
  },
};
