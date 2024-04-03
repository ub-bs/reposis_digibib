import axios from 'axios';
import { ErrorResponse, ContactPerson, Request } from '../utils';

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

const fetchRequests = (offset: number, limit: number) => axios.get(`api/v2/contact-requests?offset=${offset}&limit=${limit}`);
const removeRequest = (id: string) => axios.delete(`api/v2/contact-requests/${id}`);
const updateRequest = (id: string, request: Request) => axios.put(`api/v2/contact-requests/${id}`, {
  comment: request.comment,
});
const forwardRequest = (id: string) => axios.post(`api/v2/contact-requests/${id}/forward`);
const forwardRequestToRecipient = (id: string, recipientID: string) => axios.post(`api/v2/contact-requests/${id}/forward?recipient=${recipientID}`);
const addRecipient = (id: string, recipient: ContactPerson) => axios.post(`api/v2/contact-requests/${id}/recipients`, recipient);
const updateRecipient = (id: string, personId: string, recipient: ContactPerson) => {
  axios.put(`api/v2/contact-requests/${id}/recipients/${personId}`, {
    name: recipient.name,
    email: recipient.email,
    enabled: recipient.enabled,
    origin: recipient.origin,
    forwarding: recipient.forwarding,
  });
};
const removeRecipient = (id: string, personId: string) => axios.delete(`api/v2/contact-requests/${id}/recipients/${personId}`);
const getRequest = (id: string) => axios.get(`api/v2/contact-requests/${id}`);

export {
  fetchRequests,
  updateRequest,
  removeRequest,
  forwardRequest,
  getRequest,
  forwardRequestToRecipient,
  addRecipient,
  updateRecipient,
  removeRecipient,
};
