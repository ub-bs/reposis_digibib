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
  setModal(state, { show, id }): void {
    state.showModal = show;
    state.showRequestId = id;
  },
  setEditRecipient(state, id): void {
    state.editRecipientId = id;
  },
};
