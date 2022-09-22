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
  Confirmed = 'CONFIRMED',
}

export type Recipient = {
  uuid: string | undefined;
  name: string;
  origin: Origin;
  mail: string;
  enabled: boolean;
  failed: Date;
  sent: Date;
  confirmed: Date;
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
