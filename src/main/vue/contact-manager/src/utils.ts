export enum Origin {
  Manual = 'MANUAL',
  Fallback = 'FALLBACK',
}

export enum RequestState {
  Received = 'RECEIVED',
  Processing = 'PROCESSING',
  Processing_Failed = 'PROCESSING_FAILED',
  Processed = 'PROCESSED',
  Sending = 'SENDING',
  Sending_Failed = 'SENDING_FAILED',
  Sent = 'SENT',
  Rejected = 'REJECTED',
  Accepted = 'ACCEPTED',
}

export type Recipient = {
  name: string;
  origin: Origin;
  email: string;
  enabled: boolean;
  failed: boolean;
}

export type Request = {
  uuid: string;
  name: string;
  email: string;
  orcid?: string;
  created: Date;
  state: RequestState;
  message: string;
  objectID: string;
  recipients: Recipient[];
}

export type ErrorResponse = {
  errorCode: string;
}

export function isWarmState(state: RequestState): boolean {
  return RequestState.Processed === state || RequestState.Received === state;
}
