import { ActionTree } from 'vuex';
import axios from 'axios';
import { RootState } from '../types';
import { State } from './state';
import { Recipient, RequestState } from '../../utils';
import { MutationTypes } from './mutation-types';

export const actions: ActionTree<State, RootState> = {
  async forwardCurrentRequest({ commit, state }): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await axios.post(`api/v2/contacts/${state.currentRequest.uuid}/forward`);
        state.currentRequest.state = RequestState.Sending;
        commit(MutationTypes.SET_INFO_CODE, 'forward');
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async addRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await axios.post(`api/v2/contacts/${state.currentRequest.uuid}/recipients`, recipient);
        state.currentRequest.recipients.push(recipient);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async removeRecipient({ commit, state }, recipientId: string): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await axios.delete(`api/v2/contacts/${state.currentRequest.uuid}/recipients/${recipientId}`);
        state.currentRequest.recipients = state.currentRequest.recipients
          .filter((item) => (item.mail !== recipientId));
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async updateRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await axios.put(`api/v2/contacts/${state.currentRequest.uuid}/recipients/${state.editRecipientId}`, recipient);
        const { recipients } = state.currentRequest;
        state.currentRequest.recipients = recipients
          .filter((item) => (item.mail !== state.editRecipientId));
        state.currentRequest.recipients.push(recipient);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    } finally {
      commit(MutationTypes.SET_EDIT_RECIPIENT_ID, undefined);
    }
  },
  showRequestModal({ commit }, request: Request): void {
    commit(MutationTypes.SET_CURRENT_REQUEST, request);
    commit(MutationTypes.SET_SHOW_REQUEST_MODAL, true);
  },
  hideRequestModal({ commit }): void {
    commit(MutationTypes.SET_SHOW_REQUEST_MODAL, false);
    commit(MutationTypes.SET_CURRENT_REQUEST, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    commit(MutationTypes.SET_ERROR_CODE, undefined);
  },
};
