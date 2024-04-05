<template>
  <Modal :title="request.id" size="xl" ok-only scrollable @close="emit('close')">
    <div class="container-fluid">
      <div v-if="errorCode" class="alert alert-danger" role="alert">
        {{ $t(`digibib.contact.frontend.manager.error.${errorCode}`) }}
      </div>
      <div v-if="infoCode" class="alert alert-success" role="alert">
        {{ $t(`digibib.contact.frontend.manager.info.${infoCode}`) }}
      </div>
      <div v-if="request.state === RequestState.FORWARDING" class="alert alert-success"
        role="alert">
        {{ $t('digibib.contact.frontend.manager.info.forward') }}
      </div>
      <div v-if="request.state < RequestState.PROCESSED" class="alert alert-warning" role="alert">
        {{ $t(`digibib.contact.frontend.manager.warning.requestNotProcessed`) }}
      </div>
      <request-body-view :body="request.body" />
      <hr class="my-4" />
      <comment-edit :comment="request.comment" :enabled="isEditable" @save="handleCommentSave" />
      <hr class="my-4" />
      <h6 class="mb-2">{{ $t('digibib.contact.frontend.manager.label.recipients') }}</h6>
      <p>{{ $t('digibib.contact.frontend.manager.info.recipients') }}</p>
      <table class="table table-striped">
        <colgroup>
          <col style="width: 40%">
          <col style="width: 10%">
          <col style="width: 40%">
          <col style="width: 10%">
        </colgroup>
        <thead>
          <tr>
            <th>{{ $t('digibib.contact.frontend.manager.label.name') }}</th>
            <th>{{ $t('digibib.contact.frontend.manager.label.origin') }}</th>
            <th>{{ $t('digibib.contact.frontend.manager.label.email') }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <template v-for="recipient in request.contactPersons" :key="recipient">
            <RecipientRow :recipient="recipient" :editId="editId"
              :isProcessed="request.state === RequestState.PROCESSED"
              @delete="doRemoveRecipient" @edit="startEdit" @update="doUpdateRecipient"
              @mail="doForwardRequestToRecipient" @cancel="cancelEdit" />
          </template>
          <AddRecipientRow
            v-if="request.state === RequestState.PROCESSED"
            :disabled="editId !== undefined" @add="doAddRecipient" />
        </tbody>
      </table>
    </div>
  </Modal>
  <ConfirmModal ref="confirmModal" />
</template>
<script setup lang="ts">
import { computed, ref, PropType } from 'vue';
import { useI18n } from 'vue-i18n';
import ConfirmModal from '@/components/ConfirmModal.vue';
import Modal from '@/components/Modal.vue';
import { ContactPerson, Request, RequestState } from '@/utils';
import {
  updateRequest,
  forwardRequest,
  addRecipient,
  updateRecipient,
  removeRecipient,
  forwardRequestToRecipient,
  getRequest,
} from '@/api/service';
import AddRecipientRow from './AddRecipientRow.vue';
import RequestBodyView from './RequestBodyView.vue';
import CommentEdit from './CommentEdit.vue';
import RecipientRow from './RecipientRow.vue';

const props = defineProps({
  request: {
    type: Object as PropType<Request>,
    required: true,
  },
});

const emit = defineEmits(['request-updated', 'recipients-updated', 'close']);
const { t } = useI18n();
const confirmModal = ref();
const errorCode = ref();
const infoCode = ref();
const editId = ref();

const startEdit = (id: string): void => {
  editId.value = id;
};
const cancelEdit = (): void => {
  editId.value = undefined;
};
const resetInfoError = (): void => {
  errorCode.value = null;
  infoCode.value = null;
};
const handleInfo = (code: string): void => {
  infoCode.value = code;
};
const handleError = (code: string): void => {
  errorCode.value = code;
};
const isEditable = computed((): boolean => {
  if (props.request.state === RequestState.PROCESSED) {
    return true;
  }
  return false;
});
const handleCommentSave = async (comment: string) => {
  resetInfoError();
  try {
    const { request } = props;
    request.comment = comment;
    await updateRequest(props.request.id, request);
    const response = await getRequest(props.request.id);
    emit('request-updated', response.data);
  } catch (error) {
    handleError(error instanceof Error ? error.message : 'unknown');
  }
};
const doAddRecipient = async (recipient: ContactPerson): Promise<void> => {
  resetInfoError();
  try {
    await addRecipient(props.request.id, recipient);
    const recipients = props.request.contactPersons;
    const newRecipient = recipient; // TODO fetch recipient
    newRecipient.events = [];
    recipients.push(newRecipient);
    emit('recipients-updated', props.request.id, recipients);
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  }
};
const doUpdateRecipient = async (recipient: ContactPerson): Promise<void> => {
  resetInfoError();
  try {
    await updateRecipient(props.request.id, recipient.email, recipient);
    const recipientIndex = props.request.contactPersons
      .findIndex((c: ContactPerson) => recipient.email === c.email);
    const recipients = props.request.contactPersons;
    recipients[recipientIndex] = recipient;
    emit('recipients-updated', props.request.id, recipients);
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  } finally {
    editId.value = undefined;
  }
};
const doRemoveRecipient = async (mail: string): Promise<void> => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirm.deleteRecipient.title'),
    message: t('digibib.contact.frontend.manager.confirm.deleteRecipient.message', {
      mail,
    }),
  });
  if (ok) {
    resetInfoError();
    try {
      await removeRecipient(props.request.id, mail);
      const recipients = props.request.contactPersons
        .filter((c: ContactPerson) => mail !== c.email);
      emit('recipients-updated', props.request.id, recipients);
    } catch (error) {
      handleError((error instanceof Error ? error.message : 'unknown'));
    }
  }
};
const doForwardRequestToRecipient = async (recipientId: string): Promise<void> => {
  resetInfoError();
  try {
    await forwardRequestToRecipient(props.request.id, recipientId);
    // TODO fetch and update recipient
    handleInfo('forwardRecipient');
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  }
};
</script>
