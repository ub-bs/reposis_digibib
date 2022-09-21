import axios from 'axios';
import { ErrorResponse, Recipient } from '../utils';

// TODO own instance

axios.interceptors.response.use((response) => response, (error) => {
  if (axios.isAxiosError(error) && error.response) {
    if (error.response.status === 401 || error.response.status === 403) {
      return Promise.reject(new Error('unauthorizedError'));
    }
    const { errorCode } = (error.response.data as ErrorResponse);
    if (errorCode && errorCode !== 'UNKNOWN') {
      return Promise.reject(new Error(errorCode));
    }
  }
  return Promise.reject(new Error('unknown'));
});

const getRequests = (offset: number, limit: number) => axios.get(`api/v2/contacts?offset=${offset}&limit=${limit}`);
const removeRequest = (id: string) => axios.delete(`api/v2/contacts/${id}`);
const forwardRequest = (id: string) => axios.post(`api/v2/contacts/${id}/forward`);
const addRecipient = (id: string, recipient: Recipient) => axios.post(`api/v2/contacts/${id}/recipients`, recipient);
const updateRecipient = (id: string, recipientID: string, recipient: Recipient) => axios.put(`api/v2/contacts/${id}/recipients/${recipientID}`, recipient);
const removeRecipient = (id: string, recipientID: string) => axios.delete(`api/v2/contacts/${id}/recipients/${recipientID}`);

export {
  getRequests,
  removeRequest,
  forwardRequest,
  addRecipient,
  updateRecipient,
  removeRecipient,
};
