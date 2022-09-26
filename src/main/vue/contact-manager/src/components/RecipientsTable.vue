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
      <template v-for="recipient in recipients" :key="recipient">
        <RecipientRow :recipient="recipient" :editUUID="editUUID"
          :isProcessed="request.state === RequestState.Processed
          || request.state === RequestState.Sending_Failed"
          :isSent="request.state === RequestState.Sent || request.state === RequestState.Confirmed"
          @delete="handleDelete" @edit="handleEdit" @update="handleUpdate" @mail="handleMail" />
      </template>
      <AddRecipientRow
        v-if="request.state === RequestState.Processed"
        :disabled="editUUID !== undefined" @add="handleAdd" />
    </tbody>
  </table>
  <ConfirmModal ref="confirmModal" />
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';
import AddRecipientRow from './AddRecipientRow.vue';
import ConfirmModal from './ConfirmModal.vue';
import RecipientRow from './RecipientRow.vue';
import { ActionTypes } from '../store/modal/action-types';
import { Recipient, RequestState } from '../utils';

const store = useStore();
const { t } = useI18n();
const confirmModal = ref(null);
const recipients = computed(() => store.getters['modal/getCurrentRecipients']);
const request = store.state.modal.currentRequest;
const editUUID = ref();
const handleEdit = (uuid: string) => {
  editUUID.value = uuid;
};
const handleAdd = (recipient: Recipient) => {
  store.dispatch(`modal/${ActionTypes.ADD_RECIPIENT}`, recipient);
};
const handleUpdate = (recipient: Recipient) => {
  store.dispatch(`modal/${ActionTypes.UPDATE_RECIPIENT}`, recipient);
  editUUID.value = undefined;
};
const handleDelete = async (recipientUUID: string) => {
  const ok = await confirmModal.value.show({
    title: t('digibib.contact.frontend.manager.confirm.deleteRecipient.title'),
    message: t('digibib.contact.frontend.manager.confirm.deleteRecipient.message', {
      mail: store.getters['modal/getRecipientByUUID'](recipientUUID).mail,
    }),
  });
  if (ok) {
    store.dispatch(`modal/${ActionTypes.REMOVE_RECIPIENT}`, recipientUUID);
  }
};
const handleMail = () => {
  console.log('TODO');
};
</script>
