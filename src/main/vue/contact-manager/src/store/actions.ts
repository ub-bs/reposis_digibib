/* eslint-disable */
import { ActionTree } from 'vuex';
import axios, { AxiosError } from 'axios';
import { State } from './state';
import { Recipient, ErrorResponse } from '../utils';

export const actions: ActionTree<State, State> = {
  async fetchData({ commit }): Promise<void> {
    try {
      commit('setLoading', true);
      const response = await axios.get('api/v2/contacts');
      commit('setRequests', response.data);
      commit('setTotalRows', response.headers['x-total-count']);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const errorStatus = error.response.status;
        if (errorStatus === 401 || errorStatus === 403) {
          commit('setApplicationErrorCode', 'unauthorizedError');
        } else {
          commit('setApplicationErrorCode', (error.response.data as ErrorResponse).errorCode);
        }
      } else {
        console.error(error);
      }
    } finally {
      commit('setLoading', false);
    }
  },
  async removeContactRequest({ commit, state }, id: string): Promise<void> {
    try {
      await axios.delete(`api/v2/contacts/${id}`);
      state.requests.splice(state.requests.findIndex((i) => i.uuid === id), 1);
      commit('setTotalRows', state.totalRows - 1);
    } catch (error) {
      console.error(error);
    }
  },
  async forwardContactRequest({ commit }, id: string): Promise<void> { // TODO
    try {
      await axios.post(`api/v2/contacts/${id}/forward`);
    } catch (error) {
      console.error(error);
    }
  },
  async addRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      await axios.post(`api/v2/contacts/${state.currentRequest?.uuid}/recipients`, recipient);
      state.currentRequest?.recipients.push(recipient);
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
          commit('setModalErrorCode', (error.response.data as ErrorResponse).errorCode);
      } else {
        console.error(error);
      }
    }
  },
  async removeRecipient({ commit, state }, recipientId: String): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      await axios.delete(`api/v2/contacts/${state.currentRequest?.uuid}/recipients/${recipientId}`);
      if (state.currentRequest) {
        const recipients = state.currentRequest.recipients;
        state.currentRequest.recipients = state.currentRequest.recipients.filter(item => item.mail != recipientId);
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
          commit('setModalErrorCode', (error.response.data as ErrorResponse).errorCode);
      } else {
        console.error(error);
      }
    }
  },
  async updateRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit('setModalErrorCode', undefined);
    try {
      await axios.put(`api/v2/contacts/${state.currentRequest?.uuid}/recipients/${state.editRecipientId}`, recipient);
      if (state.currentRequest) {
        const recipients = state.currentRequest.recipients;
        state.currentRequest.recipients = recipients.filter(item => item.mail != state.editRecipientId);
        state.currentRequest.recipients.push(recipient);
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
          commit('setModalErrorCode', (error.response.data as ErrorResponse).errorCode);
      } else {
        console.error(error);
      }
    }
    commit('setEditRecipientId', undefined);
  },
  async showRequestModal({ commit, state }, id: string): Promise<void> {
    const currentRequest = this.getters.getRequestById(id);
    commit('setCurrentRequest', currentRequest);
    commit('setShowRequestModal', true);
  },
  async hideRequestModal({ commit }): Promise<void> {
    commit('setShowRequestModal', false);
    commit('setCurrentRequest', undefined);
    commit('setModalErrorCode', undefined);
  },
};
