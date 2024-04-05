<template>
  <tr :class="rowStyle" :title="title">
    <td class="col-4">
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
    <td class="col-2 text-center align-middle">
      <EditToolbar :editMode="editMode" :editable="recipient.origin === ORIGIN_MANUAL"
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
import {
  ContactPerson,
  ORIGIN_MANUAL,
  PersonEvent,
  PersonEventType,
  compareEvents,
} from '@/utils';
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
const lastEvent = computed((): PersonEvent | null => {
  if (props.recipient.events.length === 0) {
    return null;
  }
  const { events } = props.recipient;
  return events.sort(compareEvents)[0];
});
const rowStyle = computed(() => {
  if (lastEvent.value?.type === PersonEventType.CONFIRMED) {
    return 'table-success';
  }
  if (lastEvent.value?.type === PersonEventType.SENT_FAILED) {
    return 'table-danger';
  }
  if (lastEvent.value?.type === PersonEventType.SENT) {
    return 'table-warning';
  }
  return '';
});
const title = computed(() => {
  if (lastEvent.value?.type === PersonEventType.CONFIRMED) {
    return t('digibib.contact.frontend.manager.info.confirmed', {
      date: new Date(lastEvent.value.date).toLocaleString(),
    });
  }
  if (lastEvent.value?.type === PersonEventType.SENT_FAILED) {
    return t('digibib.contact.frontend.manager.info.failed', {
      date: new Date(lastEvent.value.date).toLocaleString(),
    });
  }
  if (lastEvent.value?.type === PersonEventType.SENT) {
    return t('digibib.contact.frontend.manager.info.sent', {
      date: new Date(lastEvent.value?.date).toLocaleString(),
    });
  }
  return '';
});
const isManualOrigin = computed(() => props.recipient.origin === ORIGIN_MANUAL);
const isUpdateable = computed(() => {
  if (disabled.value) {
    return false;
  }
  if (lastEvent.value?.type === PersonEventType.SENT) {
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
  if (!lastEvent.value) {
    return true;
  }
  return lastEvent.value?.type === PersonEventType.SENT_FAILED;
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
