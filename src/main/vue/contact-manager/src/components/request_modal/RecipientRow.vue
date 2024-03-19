<template>
  <tr :class="rowStyle" :title="title">
    <td class="col-3">
      <input v-if="isManualOrigin && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.name" :class="v.name.$error ? 'is-invalid' : ''" />
      <span v-else>
        {{ recipient.name }}
      </span>
    </td>
    <td class="col-2">
      <span>
        {{ recipient.origin }}
      </span>
    </td>
    <td class="col-4">
      <span>
        {{ recipient.email }}
      </span>
    </td>
    <td class="col-1 align-middle text-center">
      <input v-if="editMode" type="checkbox" v-model="recipientSave.enabled"/>
      <input v-else type="checkbox" :checked="recipient.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar :editMode="editMode"
          :edit="isUpdateable" :remove="removeable" :mail="isMailable" @edit="startEdit"
          @cancel="cancelEdit" @update="updateRecipient" @remove="removeRecipient"
          @mail="mailRecipient" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { PropType, computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
import { useI18n } from 'vue-i18n';
import { required, email } from '@vuelidate/validators';
import { ContactPerson, ORIGIN_MANUAL } from '@/utils';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  recipient: {
    type: Object as PropType<ContactPerson>,
    required: true,
  },
  editId: {
  },
  isProcessed: {
    type: Boolean,
    default: false,
  },
  isSent: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['cancel', 'delete', 'edit', 'mail', 'update']);
const rules = computed(() => ({
  name: {
    required,
  },
  email: {
    required,
    email,
  },
  origin: {
    required,
  },
}));
const { t } = useI18n();
const v = useVuelidate(rules, props.recipient);
const recipientSave = ref(JSON.parse(JSON.stringify(props.recipient)));
const editMode = computed(() => props.editId === props.recipient.email);
const disabled = computed(() => props.editId !== undefined
  && props.editId !== props.recipient.email);
const rowStyle = computed(() => {
  if (props.recipient.forwarding?.confirmed) {
    return 'table-success';
  }
  if (props.recipient.forwarding?.failed) {
    return 'table-danger';
  }
  if (props.recipient.forwarding?.date) {
    return 'table-warning';
  }
  return '';
});
const title = computed(() => {
  if (props.recipient.forwarding?.confirmed != null) {
    return t('digibib.contact.frontend.manager.info.confirmed', {
      date: new Date(props.recipient.forwarding?.confirmed).toLocaleString(),
    });
  }
  if (props.recipient.forwarding?.failed != null) {
    return t('digibib.contact.frontend.manager.info.failed', {
      date: new Date(props.recipient.forwarding?.failed).toLocaleString(),
    });
  }
  if (props.recipient.forwarding?.date != null) {
    return t('digibib.contact.frontend.manager.info.sent', {
      date: new Date(props.recipient.forwarding?.date).toLocaleString(),
    });
  }
  return '';
});
const isManualOrigin = computed(() => props.recipient.origin === ORIGIN_MANUAL);
const isUpdateable = computed(() => {
  if (disabled.value) {
    return false;
  }
  return props.isProcessed;
});
const removeable = computed(() => {
  if (!isUpdateable.value) {
    return false;
  }
  return isManualOrigin.value;
});
const isMailable = computed(() => {
  if (disabled.value) {
    return false;
  }
  if (!props.isSent) {
    return false;
  }
  if (props.recipient.forwarding?.confirmed != null) {
    return false;
  }
  return props.recipient.forwarding?.failed != null;
});
const startEdit = () => {
  emit('edit', props.recipient.email);
};
const cancelEdit = () => {
  recipientSave.value = JSON.parse(JSON.stringify(props.recipient));
  emit('cancel');
};
const updateRecipient = () => {
  v.value.$validate();
  if (!v.value.$error) {
    emit('update', recipientSave.value);
  }
};
const removeRecipient = () => {
  emit('delete', props.recipient.email);
};
const mailRecipient = () => {
  emit('mail', props.recipient.email);
};
</script>../../utils
