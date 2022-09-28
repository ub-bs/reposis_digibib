import { ActionContext, ActionTree } from 'vuex';
import { RootState } from '../types';
import { State } from './state';
import { Request, Recipient, RequestState } from '../../utils';
import { ActionTypes } from './action-types';
import { MutationTypes } from './mutation-types';
import { Mutations } from './mutations';
import { Getters } from './getters';
import {
  forwardRequest,
  forwardRequestToRecipient,
  addRecipient,
  updateRecipient,
  removeRecipient,
} from '../../api/service';

type AugmentedActionContext = {
  commit<K extends keyof Mutations>(
    key: K,
    payload: Parameters<Mutations[K]>[1],
  ): ReturnType<Mutations[K]>
  getters<K extends keyof Getters>(
    key: K,
    payload: Parameters<Getters[K]>[1],
  ): ReturnType<Getters[K]>
} & Omit<ActionContext<State, RootState>, 'commit'>

export interface Actions {
  [ActionTypes.FORWARD_REQUEST]({ commit, state }: AugmentedActionContext): void,
  [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT]({ commit, state }:
    AugmentedActionContext, payload: string): void,
  [ActionTypes.ADD_RECIPIENT]({ commit, state }:
    AugmentedActionContext, payload: Recipient): Promise<void>,
  [ActionTypes.UPDATE_RECIPIENT]({ commit, state }:
    AugmentedActionContext, payload: Recipient): void,
  [ActionTypes.REMOVE_RECIPIENT]({ commit, state }: AugmentedActionContext, payload: string): void,
  [ActionTypes.SHOW_REQUEST]({ commit }: AugmentedActionContext, payload: Request): void,
  [ActionTypes.HIDE_REQUEST]({ commit }: AugmentedActionContext): void,
}

export const actions: ActionTree<State, RootState> & Actions = {
  async [ActionTypes.FORWARD_REQUEST]({ commit, state }): Promise<void> {
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
  async [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT]({ commit, state }, id: string): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await forwardRequestToRecipient(state.currentRequest.uuid, id);
        // TODO get recipient and reset sent and failed
        commit(MutationTypes.SET_INFO_CODE, 'forwardRecipient');
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async [ActionTypes.ADD_RECIPIENT]({ commit, state }, recipient: Recipient): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        const r = recipient;
        const response = await addRecipient(state.currentRequest.uuid, r);
        r.uuid = response.headers.location.split('/').pop();
        state.currentRequest.recipients.push(r);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  async [ActionTypes.UPDATE_RECIPIENT]({ commit, state }, recipient: Recipient): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest && recipient.uuid) {
        await updateRecipient(state.currentRequest.uuid, recipient.uuid, recipient);
        const { recipients } = state.currentRequest;
        state.currentRequest.recipients = recipients
          .filter((item) => (item.uuid !== recipient.uuid));
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
  async [ActionTypes.REMOVE_RECIPIENT]({ commit, state }, recipientUUID: string): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.currentRequest) {
        await removeRecipient(state.currentRequest.uuid, recipientUUID);
        state.currentRequest.recipients = state.currentRequest.recipients
          .filter((item) => (item.uuid !== recipientUUID));
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
  [ActionTypes.SHOW_REQUEST]({ commit }, request: Request): void {
    commit(MutationTypes.SET_CURRENT_REQUEST, request);
    commit(MutationTypes.SET_SHOW_REQUEST_MODAL, true);
  },
  [ActionTypes.HIDE_REQUEST]({ commit }): void {
    commit(MutationTypes.SET_SHOW_REQUEST_MODAL, false);
    commit(MutationTypes.SET_CURRENT_REQUEST, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    commit(MutationTypes.SET_ERROR_CODE, undefined);
  },
};
