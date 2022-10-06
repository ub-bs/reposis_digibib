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
  updateRequest,
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
  [ActionTypes.UPDATE_REQUEST]({ commit, state }: AugmentedActionContext, payload: Request): void,
}

export const actions: ActionTree<State, RootState> & Actions = {
  async [ActionTypes.FORWARD_REQUEST]({ commit, state }): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.request) {
        await forwardRequest(state.request.uuid);
        state.request.state = RequestState.Sending;
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
      if (state.request) {
        await forwardRequestToRecipient(state.request.uuid, id);
        // const recipient = getters.getRecipientByUUID(id);
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
      if (state.request) {
        const r = recipient;
        const response = await addRecipient(state.request.uuid, r);
        r.uuid = response.headers.location.split('/').pop();
        state.request.recipients.push(r);
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
      if (state.request && recipient.uuid) {
        await updateRecipient(state.request.uuid, recipient.uuid, recipient);
        const { recipients } = state.request;
        state.request.recipients = recipients
          .filter((item) => (item.uuid !== recipient.uuid));
        state.request.recipients.push(recipient);
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
      if (state.request) {
        await removeRecipient(state.request.uuid, recipientUUID);
        state.request.recipients = state.request.recipients
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
  async [ActionTypes.UPDATE_REQUEST]({ commit, state }, request: Request): Promise<void> {
    commit(MutationTypes.SET_ERROR_CODE, undefined);
    commit(MutationTypes.SET_INFO_CODE, undefined);
    try {
      if (state.request) {
        await updateRequest(state.request.uuid, request);
      }
    } catch (error) {
      if (error instanceof Error) {
        commit(MutationTypes.SET_ERROR_CODE, error.message);
      } else {
        commit(MutationTypes.SET_ERROR_CODE, 'unknown');
      }
    }
  },
};
