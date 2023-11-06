import { Recipient, Request } from '@/utils';
import instance from './axios';

export const fetchRequests = async (offset: number, limit: number) => instance.get<Request[]>(`api/v2/contacts?offset=${offset}&limit=${limit}`);
export const removeRequest = async (id: string): Promise<void> => instance.delete(`api/v2/contacts/${id}`);
export const updateRequest = async (id: string, request: Request): Promise<void> => instance.put(`api/v2/contacts/${id}`, request);
export const forwardRequest = async (id: string): Promise<void> => instance.post(`api/v2/contacts/${id}/forward`);
export const forwardRequestToRecipient = async (id: string, recipientID: string): Promise<void> => instance.post(`api/v2/contacts/${id}/forward?recipient=${recipientID}`);
export const addRecipient = async (id: string, recipient: Recipient): Promise<string> => (await instance.post(`api/v2/contacts/${id}/recipients`, recipient))
  .headers.location.split('/').pop() as string;
export const updateRecipient = async (id: string, recipientID: string, recipient: Recipient): Promise<void> => instance.put(`api/v2/contacts/${id}/recipients/${recipientID}`, recipient);
export const removeRecipient = async (id: string, recipientID: string): Promise<void> => instance.delete(`api/v2/contacts/${id}/recipients/${recipientID}`);
