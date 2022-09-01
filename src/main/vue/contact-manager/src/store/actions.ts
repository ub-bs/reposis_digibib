/* eslint-disable */
import { ActionTree } from 'vuex';
import axios from 'axios';
import { Recipient, State } from './state';

export const actions: ActionTree<State, State> = {
  async fetchData({ commit }): Promise<void> {
    commit('setLoading', true);
    try {
      const response = await axios.get('api/v2/contacts');
      commit('setRequests', response.data);
      commit('setTotalRows', response.headers['x-total-count']);
    } catch (error) {
      console.error(error);
    }
    commit('setLoading', false);
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
  async forwardContactRequest({ commit }, id: string): Promise<void> {
    try {
      await axios.post(`api/v2/contacts/${id}/forward`);
    } catch (error) {
      console.error(error);
    }
  },
  async rejectContactRequest({ commit }, id: string): Promise<void> {
    try {
      await axios.post(`api/v2/contacts/${id}/reject`);
    } catch (error) {
      console.error(error);
    }
  },
  async addRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    try {
      await axios.post(`api/v2/contacts/${state.showRequestId}/recipients`, recipient);
    } catch (error) {
      console.error(error);
    }
  },
  async removeRecipient({ commit, state }, recipientId: String): Promise<void> {
    try {
      await axios.delete(`api/v2/recipients/${recipientId}`);
    } catch (error) {
      console.error(error);
    }
  },
  async updateRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    try {
      await axios.put(`api/v2/contacts/${state.showRequestId}/recipients/${state.editRecipientId}`, recipient);
      commit('setEditRecipientId', undefined);
    } catch (error) {
      console.error(error);
    }
  },
};
