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
      <h6 class="mb-2">{{ $t('digibib.contact.frontend.manager.label.contacts') }}</h6>
      <p>{{ $t('digibib.contact.frontend.manager.info.contacts') }}</p>
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
          <template v-for="contact in request.contacts" :key="contact">
            <contact-row :contact="contact" :editId="editId"
              :isProcessed="request.state === RequestState.PROCESSED"
              @delete="doRemoveContact" @edit="startEdit" @update="doUpdateContact"
              @mail="doForwardRequest" @cancel="cancelEdit" />
          </template>
          <add-contact-row v-if="request.state === RequestState.PROCESSED"
            :disabled="editId !== undefined" @add="doAddContact" />
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
import { Contact, Request, RequestState } from '@/utils';
import {
  updateRequest,
  addContact,
  updateContact,
  removeContactByEmail,
  forwardRequest,
  getRequest,
} from '@/api/service';
import AddContactRow from './AddContactRow.vue';
import RequestBodyView from './RequestBodyView.vue';
import CommentEdit from './CommentEdit.vue';
import ContactRow from './ContactRow.vue';

const props = defineProps({
  request: {
    type: Object as PropType<Request>,
    required: true,
  },
});

const emit = defineEmits(['request-updated', 'contacts-updated', 'close']);
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
const doAddContact = async (contact: Contact): Promise<void> => {
  resetInfoError();
  try {
    await addContact(props.request.id, contact);
    const { contacts } = props.request;
    const newContact = contact; // TODO fetch contact
    newContact.events = [];
    contacts.push(newContact);
    emit('contacts-updated', props.request.id, contacts);
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  }
};
const doUpdateContact = async (contact: Contact): Promise<void> => {
  resetInfoError();
  try {
    await updateContact(props.request.id, contact.email, contact);
    const contactIndex = props.request.contacts
      .findIndex((c: Contact) => contact.email === c.email);
    const { contacts } = props.request;
    contacts[contactIndex] = contact;
    emit('contacts-updated', props.request.id, contacts);
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  } finally {
    editId.value = undefined;
  }
};
const doRemoveContact = async (email: string): Promise<void> => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirm.deleteContact.title'),
    message: t('digibib.contact.frontend.manager.confirm.deleteContact.message', {
      email,
    }),
  });
  if (ok) {
    resetInfoError();
    try {
      await removeContactByEmail(props.request.id, email);
      const contacts = props.request.contacts.filter((c: Contact) => email !== c.email);
      emit('contacts-updated', props.request.id, contacts);
    } catch (error) {
      handleError((error instanceof Error ? error.message : 'unknown'));
    }
  }
};
const doForwardRequest = async (email: string): Promise<void> => {
  resetInfoError();
  try {
    await forwardRequest(props.request.id, email);
    // TODO fetch and update contact
    handleInfo('forwardContact');
  } catch (error) {
    handleError((error instanceof Error ? error.message : 'unknown'));
  }
};
</script>
