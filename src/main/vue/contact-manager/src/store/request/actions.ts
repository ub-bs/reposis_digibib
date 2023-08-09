import { ActionContext, ActionTree } from 'vuex';
import { RootState } from '../types';
import { State } from './state';
import { RequestState } from '../../utils';
import { ActionTypes } from './action-types';
import { MutationTypes } from './mutation-types';
import { Mutations } from './mutations';
import { Getters } from './getters';
import {
  addRecipient,
  fetchRequests,
  forwardRequest,
  forwardRequestToRecipient,
  removeRequest,
  removeRecipient,
  updateRecipient,
  updateRequest,
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
  [ActionTypes.FETCH]({ commit }: AugmentedActionContext, payload): void,
  [ActionTypes.FORWARD_REQUEST]({ commit }: AugmentedActionContext, payload: string): void,
  [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT](payload): void,
  [ActionTypes.ADD_RECIPIENT]({ commit }: AugmentedActionContext, payload): Promise<void>,
  [ActionTypes.UPDATE_RECIPIENT]({ commit }: AugmentedActionContext, payload): void,
  [ActionTypes.REMOVE_RECIPIENT]({ commit }: AugmentedActionContext, payload): void,
  [ActionTypes.UPDATE_REQUEST]({ commit }: AugmentedActionContext, payload): void,
  [ActionTypes.REMOVE_REQUEST]({ commit }: AugmentedActionContext, payload: string): void,
  [ActionTypes.SET_MODAL_DATA]({ commit }: AugmentedActionContext, payload): void,
}

export const actions: ActionTree<State, RootState> & Actions = {
  async [ActionTypes.FETCH]({ commit }, { offset, limit }): Promise<void> {
    const response = await fetchRequests(offset, limit);
    commit(MutationTypes.SET_REQUESTS, response.data);
    commit(MutationTypes.SET_TOTAL_COUNT, Number(response.headers['x-total-count']));
  },
  async [ActionTypes.FORWARD_REQUEST]({ commit }, slug: string): Promise<void> {
    await forwardRequest(slug);
    commit(MutationTypes.SET_STATE, { slug, state: RequestState.Sending });
  },
  async [ActionTypes.FORWARD_REQUEST_TO_RECIPIENT]({ slug, recipientUUID })
    : Promise<void> {
    await forwardRequestToRecipient(slug, recipientUUID);
    // TODO get recipient and reset sent and failed
  },
  async [ActionTypes.ADD_RECIPIENT]({ commit }, { slug, recipient }): Promise<void> {
    const r = recipient;
    const response = await addRecipient(slug, recipient);
    r.uuid = response.headers.location.split('/').pop();
    commit(MutationTypes.ADD_RECIPIENT, { slug, recipient: r });
  },
  async [ActionTypes.UPDATE_RECIPIENT]({ commit }, { slug, recipientUUID, recipient }):
    Promise<void> {
    await updateRecipient(slug, recipientUUID, recipient);
    commit(MutationTypes.UPDATE_RECIPIENT, { slug, recipientUUID, recipient });
  },
  async [ActionTypes.REMOVE_RECIPIENT]({ commit }, { slug, recipientUUID }): Promise<void> {
    await removeRecipient(slug, recipientUUID);
    commit(MutationTypes.REMOVE_RECIPIENT, { slug, recipientUUID });
  },
  async [ActionTypes.UPDATE_REQUEST]({ commit }, { slug, request }): Promise<void> {
    await updateRequest(slug, request);
    commit(MutationTypes.UPDATE_REQUEST, { slug, request });
  },
  async [ActionTypes.REMOVE_REQUEST]({ commit }, slug): Promise<void> {
    await removeRequest(slug);
    commit(MutationTypes.REMOVE_REQUEST, slug);
  },
  async [ActionTypes.SET_MODAL_DATA]({ commit }, slug): Promise<void> {
    commit(MutationTypes.SET_MODAL_DATA, slug);
  },
};
