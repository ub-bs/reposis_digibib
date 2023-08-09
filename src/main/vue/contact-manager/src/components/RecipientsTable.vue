<template>
  <table class="table table-striped">
    <colgroup>
      <col style="width: 40%">
      <col style="width: 5%">
      <col style="width: 40%">
      <col style="width: 10%">
      <col style="width: 5%">
    </colgroup>
    <thead>
      <tr>
        <th>
          {{ $t('digibib.contact.frontend.manager.label.name') }}
        </th>
        <th>
          {{ $t('digibib.contact.frontend.manager.label.origin') }}
        </th>
        <th>
          {{ $t('digibib.contact.frontend.manager.label.email') }}
        </th>
        <th class="text-center">
          {{ $t('digibib.contact.frontend.manager.label.forward') }}
        </th>
        <th>
        </th>
      </tr>
    </thead>
    <tbody>
      <template v-for="recipient in request.recipients" :key="recipient">
        <RecipientRow :recipient="recipient" :editUUID="editUUID"
          :isProcessed="request.state === RequestState.Processed
          || request.state === RequestState.Sending_Failed"
          :isSent="request.state === RequestState.Sent
          || request.state === RequestState.Confirmed"
          @delete="removeRecipient" @edit="startEdit" @update="updateRecipient"
          @mail="mailRecipient" @cancel="cancelEdit" />
      </template>
      <AddRecipientRow
        v-if="request.state === RequestState.Processed"
        :disabled="editUUID !== undefined" @add="addRecipient" />
    </tbody>
  </table>
</template>
<script setup lang="ts">
import { getCurrentInstance, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import { BvModal } from 'bootstrap-vue';
import AddRecipientRow from './AddRecipientRow.vue';
import RecipientRow from './RecipientRow.vue';
import { ActionTypes } from '../store/request/action-types';
import { Recipient, RequestState } from '../utils';

const props = defineProps({
  request: {
    type: Object,
    required: true,
  },
});
const store = useStore();
const { t } = useI18n();
const emit = defineEmits(['error', 'info', 'actionStarted']);
const editUUID = ref();
// eslint-disable-next-line
const instance = (getCurrentInstance() as any);
const startEdit = (uuid: string) => {
  editUUID.value = uuid;
};
const cancelEdit = () => {
  editUUID.value = undefined;
};
const addRecipient = async (recipient: Recipient) => {
  emit('actionStarted');
  try {
    await store.dispatch(`request/${ActionTypes.ADD_RECIPIENT}`, {
      slug: props.request.uuid,
      recipient,
    });
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  }
};
const updateRecipient = async (recipient: Recipient) => {
  emit('actionStarted');
  try {
    await store.dispatch(`request/${ActionTypes.UPDATE_RECIPIENT}`, {
      slug: props.request.uuid,
      recipientUUID: recipient.uuid,
      recipient,
    });
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  } finally {
    editUUID.value = undefined;
  }
};
const removeRecipient = async (recipientUUID: string) => {
  const bvModal = instance.ctx._bv__modal as BvModal;
  bvModal.msgBoxConfirm(t('digibib.contact.frontend.manager.confirm.deleteRecipient.message', {
    mail: store.getters['request/getRecipientByUUID'](props.request.uuid, recipientUUID).mail,
  }), {
    title: t('digibib.contact.frontend.manager.confirm.deleteRecipient.title'),
  }).then((value) => {
    if (value) {
      emit('actionStarted');
      store.dispatch(`request/${ActionTypes.REMOVE_RECIPIENT}`, {
        slug: props.request.uuid,
        recipientUUID,
      });
    }
  }).catch((error) => {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  });
};
const mailRecipient = async (recipientUUID: string) => {
  emit('actionStarted');
  try {
    await store.dispatch(`request/${ActionTypes.FORWARD_REQUEST_TO_RECIPIENT}`, {
      slug: props.request.uuid,
      recipientUUID,
    });
    emit('info', 'forwardRecipient');
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  }
};
</script>
