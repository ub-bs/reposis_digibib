/* eslint-disable */
import { ActionTree } from 'vuex';
import axios, { AxiosError } from 'axios';
import { State } from './state';
import { Recipient, RequestState, ErrorResponse } from '../../utils';

export const actions: ActionTree<State, State> = {
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
  async showRequestModal({ commit, state, rootGetters }, id: string): Promise<void> {
    const currentRequest = rootGetters['getRequestById'](id);
    commit('setCurrentRequest', currentRequest);
    commit('setShowRequestModal', true);
  },
  async hideRequestModal({ commit }): Promise<void> {
    commit('setShowRequestModal', false);
    commit('setCurrentRequest', undefined);
    commit('setModalErrorCode', undefined);
  },
};
