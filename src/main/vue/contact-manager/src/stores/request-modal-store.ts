import { defineStore } from 'pinia';
import {
  addRecipient,
  forwardRequest,
  forwardRequestToRecipient,
  removeRecipient,
  updateRecipient,
  updateRequest,
} from '@/api/service';
import { Request, RequestState, Recipient } from '@/utils';

interface State {
  showModal: boolean
  request?: Request
}

export const useRequestStore = defineStore('request', {
  state: (): State => ({
    showModal: false,
  }),
  getters: {
    getRecipientById(state) {
      return (id) => {
        if (state.request) {
          return state.request.recipients.find((r) => r.uuid === id);
        }
        return undefined;
      };
    },
    isEditable: (state): boolean => {
      if (state.request) {
        if (state.request.state < RequestState.Sending) {
          return true;
        }
        return state.request.state === RequestState.Sending_Failed;
      }
      return false;
    },
    isRequestProcessed: (state): boolean => {
      if (state.request) {
        return state.request.state === RequestState.Processed
          || state.request.state === RequestState.Sending_Failed;
      }
      return false;
    },
    isRequestSent: (state): boolean => {
      if (state.request) {
        return state.request.state === RequestState.Sent
          || state.request.state === RequestState.Confirmed;
      }
      return false;
    },
    isReadyToForward: (state): boolean => {
      if (state.request) {
        if (state.request.state === RequestState.Sending_Failed) {
          return false;
        }
        if (state.request.state !== RequestState.Processed) {
          return false;
        }
        return (state.request.recipients.filter((r) => r.enabled === true).length > 0);
      }
      return false;
    },
  },
  actions: {
    async forwardRequest(): Promise<void> {
      if (this.request) {
        await forwardRequest(this.request.uuid);
        this.request.state = RequestState.Sending;
      }
    },
    async forwardRequestToRecipient(id: string): Promise<void> {
      if (this.request) {
        await forwardRequestToRecipient(this.request.uuid, id);
        const index = this.request.recipients.findIndex((r) => r.uuid === id);
        if (index > -1) {
          this.request.recipients[index].failed = undefined;
          this.request.recipients[index].sent = new Date();
        } else {
          throw new Error();
        }
      }
    },
    async addRecipient(recipient: Recipient): Promise<void> {
      if (this.request) {
        const tmp = recipient;
        tmp.uuid = await addRecipient(this.request.uuid, recipient);
        this.request.recipients.push(tmp);
      }
    },
    async updateRecipient(id: string, recipient: Recipient): Promise<void> {
      if (this.request) {
        await updateRecipient(this.request.uuid, id, recipient);
        const index = this.request.recipients.findIndex((r) => r.uuid === recipient.uuid);
        if (index > -1) {
          this.request.recipients[index] = recipient;
        } else {
          throw new Error();
        }
      }
    },
    async removeRecipient(id: string): Promise<void> {
      if (this.request) {
        await removeRecipient(this.request.uuid, id);
        this.request.recipients = this.request.recipients.filter((r) => r.uuid !== id);
      }
    },
    async updateRequest(): Promise<void> {
      if (this.request) {
        await updateRequest(this.request.uuid, this.request);
      }
    },
  },
});
