<template>
  <table class="table table-striped" v-if="store.request">
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
          {{ tc('label.name') }}
        </th>
        <th>
          {{ tc('label.origin') }}
        </th>
        <th>
          {{ tc('label.email') }}
        </th>
        <th class="text-center">
          {{ tc('label.forward') }}
        </th>
        <th>
        </th>
      </tr>
    </thead>
    <tbody>
      <template v-for="recipient in recipients" :key="recipient">
        <RecipientRow :recipient="recipient" :editUUID="editUUID" @delete="removeRecipient"
          @edit="startEdit" @update="updateRecipient" @mail="mailRecipient" @cancel="cancelEdit" />
      </template>
      <AddRecipientRow
        v-if="store.request.state === RequestState.Processed"
        :disabled="editUUID !== undefined" @add="addRecipient" />
    </tbody>
  </table>
</template>
<script setup lang="ts">
import {
  computed,
  Component,
  getCurrentInstance,
  ref,
} from 'vue';
import { useI18n } from 'vue-i18n';
import { BvModal } from 'bootstrap-vue';
import { getI18nKey, Recipient, RequestState } from '@/utils';
import { useRequestStore } from '@/stores';
import AddRecipientRow from './AddRecipientRow.vue';
import RecipientRow from './RecipientRow.vue';

const instance: Component = getCurrentInstance();
const store = useRequestStore();
const emit = defineEmits(['error', 'info', 'actionStarted']);
const { t } = useI18n();
const tc = (value: string, obj?) => t(getI18nKey(value), obj);
const recipients = computed(() => ((store.request) ? store.request.recipients : []));
const editUUID = ref();
const startEdit = (uuid: string) => {
  editUUID.value = uuid;
};
const cancelEdit = () => {
  editUUID.value = undefined;
};
const addRecipient = async (recipient: Recipient) => {
  emit('actionStarted');
  try {
    await store.addRecipient(recipient);
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  }
};
const updateRecipient = async (recipient: Recipient) => {
  emit('actionStarted');
  try {
    await store.updateRecipient(recipient.uuid, recipient);
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  } finally {
    editUUID.value = undefined;
  }
};
const removeRecipient = async (recipientUUID: string) => {
  const bvModal = instance.ctx._bv__modal as BvModal;
  const value = await bvModal.msgBoxConfirm(tc('confirm.deleteRecipient.message', {
    mail: store.getRecipientById(recipientUUID).mail,
  }), {
    title: tc('confirm.deleteRecipient.title'),
  });
  if (value) {
    emit('actionStarted');
    try {
      await store.removeRecipient(recipientUUID);
    } catch (error) {
      emit('error', (error instanceof Error ? error.message : 'unknown'));
    }
  }
};
const mailRecipient = async (recipientUUID: string) => {
  emit('actionStarted');
  try {
    await store.forwardRequestToRecipient(recipientUUID);
    emit('info', 'forwardRecipient');
  } catch (error) {
    emit('error', (error instanceof Error ? error.message : 'unknown'));
  }
};
</script>
