/* eslint-disable */
import { ActionTree } from 'vuex';
import axios, { AxiosError } from 'axios';
import { State } from './state';
import { Recipient, RequestState, ErrorResponse } from '../utils';

export const actions: ActionTree<State, State> = {
  async fetchData({ commit }): Promise<void> {
    commit('setApplicationErrorCode', undefined);
    commit('setLoading', true);
    try {
      const response = await axios.get('api/v2/contacts');
      commit('setRequests', response.data);
      commit('setTotalRows', response.headers['x-total-count']);
    } catch (error) {
      if (error instanceof Error) {
        commit('setApplicationErrorCode', error.message);
      } else {
        commit('setApplicationErrorCode', 'unknown');
      }
    } finally {
      commit('setLoading', false);
    }
  },
  async removeContactRequest({ commit, state }, id: string): Promise<void> {
    commit('setApplicationErrorCode', undefined);
    try {
      await axios.delete(`api/v2/contacts/${id}`);
      state.requests.splice(state.requests.findIndex((i) => i.uuid === id), 1);
      commit('setTotalRows', state.totalRows - 1);
    } catch (error) {
      if (error instanceof Error) {
        commit('setApplicationErrorCode', error.message);
      } else {
        commit('setApplicationErrorCode', 'unknown');
      }
    }
  },
  async forwardCurrentRequest({ commit, state } ): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      if (state.currentRequest) {
        await axios.post(`api/v2/contacts/${state.currentRequest.uuid}/forward`);
        state.currentRequest.state = RequestState.Sending;
      }
    } catch (error) {
      if (error instanceof Error) {
        commit('setModalErrorCode', error.message);
      } else {
        commit('setModalErrorCode', 'unknown');
      }
    }
  },
  async addRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      if (state.currentRequest) {
        await axios.post(`api/v2/contacts/${state.currentRequest.uuid}/recipients`, recipient);
        state.currentRequest.recipients.push(recipient);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit('setModalErrorCode', error.message);
      } else {
        commit('setModalErrorCode', 'unknown');
      }
    }
  },
  async removeRecipient({ commit, state }, recipientId: String): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      if (state.currentRequest) {
        await axios.delete(`api/v2/contacts/${state.currentRequest.uuid}/recipients/${recipientId}`);
        const recipients = state.currentRequest.recipients;
        state.currentRequest.recipients = state.currentRequest.recipients.filter(item => item.mail != recipientId);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit('setModalErrorCode', error.message);
      } else {
        commit('setModalErrorCode', 'unknown');
      }
    }
  },
  async updateRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      if (state.currentRequest) {
        await axios.put(`api/v2/contacts/${state.currentRequest.uuid}/recipients/${state.editRecipientId}`, recipient);
        const recipients = state.currentRequest.recipients;
        state.currentRequest.recipients = recipients.filter(item => item.mail != state.editRecipientId);
        state.currentRequest.recipients.push(recipient);
        commit('setEditRecipientId', undefined);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit('setModalErrorCode', error.message);
      } else {
        commit('setModalErrorCode', 'unknown');
      }
    }
  },
  async showRequestModal({ commit, state, getters }, id: string): Promise<void> {
    const currentRequest = getters.getRequestById(id);
    commit('setCurrentRequest', currentRequest);
    commit('setShowRequestModal', true);
  },
  async hideRequestModal({ commit }): Promise<void> {
    commit('setShowRequestModal', false);
    commit('setCurrentRequest', undefined);
    commit('setModalErrorCode', undefined);
  },
};
