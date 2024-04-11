import axios from 'axios';
import { ErrorResponse, Contact, Request } from '../utils';

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
  body: request.body,
  comment: request.comment,
  contacts: request.contacts,
  created: request.created,
  objectId: request.objectId,
  state: request.state,
});
const forwardRequest = (id: string, email: string) => axios.post(`api/v2/contact-requests/${id}/forward?recipient=${email}`);
const addContact = (id: string, contact: Contact) => axios.post(`api/v2/contact-requests/${id}/recipients`, contact);
const updateContact = (id: string, personId: string, contact: Contact) => {
  axios.put(`api/v2/contact-requests/${id}/recipients/${personId}`, contact);
};
const removeContactByEmail = (id: string, email: string) => axios.delete(`api/v2/contact-requests/${id}/recipients/${email}`);
const getRequest = (id: string) => axios.get(`api/v2/contact-requests/${id}`);

export {
  fetchRequests,
  updateRequest,
  removeRequest,
  getRequest,
  forwardRequest,
  addContact,
  updateContact,
  removeContactByEmail,
};
