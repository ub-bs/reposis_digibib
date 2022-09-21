import { ActionTree } from 'vuex';
import { RootState } from '../types';
import { State } from './state';
import { Recipient, RequestState } from '../../utils';
import { MutationTypes } from './mutation-types';
import {
  forwardRequest,
  addRecipient,
  updateRecipient,
  removeRecipient,
} from '../../api/service';

export const actions: ActionTree<State, RootState> = {
  async forwardCurrentRequest({ commit, state }): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await forwardRequest(state.currentRequest.uuid);
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
        await addRecipient(state.currentRequest.uuid, recipient);
        // recipient.id = response.headers.location.split('/').pop();
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
  async updateRecipient({ commit, state }, recipient: Recipient): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest && state.editRecipientId) {
        await updateRecipient(state.currentRequest.uuid, state.editRecipientId, recipient);
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
  async removeRecipient({ commit, state }, recipientId: string): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await removeRecipient(state.currentRequest.uuid, recipientId);
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
