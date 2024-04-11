<template>
  <tr :class="rowStyle" :title="title">
    <td class="col-4">
      <input v-if="isManualOrigin && editMode" class="form-control form-control-sm" type="text"
          v-model="contactSave.name" :class="v.name.$error ? 'is-invalid' : ''" />
      <span v-else>
        {{ contact.name }}
      </span>
    </td>
    <td class="col-2">
      <span>
        {{ contact.origin }}
      </span>
    </td>
    <td class="col-4">
      <span>
        {{ contact.email }}
      </span>
    </td>
    <td class="col-2 text-center align-middle">
      <EditToolbar :editMode="editMode" :editable="contact.origin === ORIGIN_MANUAL"
          :edit="isUpdateable" :remove="removeable" :mail="isMailable" @edit="startEdit"
          @cancel="cancelEdit" @update="updateContact" @remove="removeContact"
          @mail="mailContact" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { PropType, computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
import { useI18n } from 'vue-i18n';
import { required, email } from '@vuelidate/validators';
import {
  Contact,
  ORIGIN_MANUAL,
  ContactEvent,
  ContactEventType,
  compareEvents,
} from '@/utils';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  contact: {
    type: Object as PropType<Contact>,
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
const v = useVuelidate(rules, props.contact);
const contactSave = ref(JSON.parse(JSON.stringify(props.contact)));
const editMode = computed(() => props.editId === props.contact.email);
const disabled = computed(() => props.editId !== undefined
  && props.editId !== props.contact.email);
const lastEvent = computed((): ContactEvent | null => {
  if (props.contact.events.length === 0) {
    return null;
  }
  const { events } = props.contact;
  return events.sort(compareEvents).reverse()[0];
});
const rowStyle = computed(() => {
  if (lastEvent.value?.type === ContactEventType.CONFIRMED) {
    return 'table-success';
  }
  if (lastEvent.value?.type === ContactEventType.SENT_FAILED) {
    return 'table-danger';
  }
  if (lastEvent.value?.type === ContactEventType.SENT) {
    return 'table-warning';
  }
  return '';
});
const title = computed(() => {
  if (lastEvent.value?.type === ContactEventType.CONFIRMED) {
    return t('digibib.contact.frontend.manager.info.confirmed', {
      date: new Date(lastEvent.value.date).toLocaleString(),
    });
  }
  if (lastEvent.value?.type === ContactEventType.SENT_FAILED) {
    return t('digibib.contact.frontend.manager.info.failed', {
      date: new Date(lastEvent.value.date).toLocaleString(),
    });
  }
  if (lastEvent.value?.type === ContactEventType.SENT) {
    return t('digibib.contact.frontend.manager.info.sent', {
      date: new Date(lastEvent.value?.date).toLocaleString(),
    });
  }
  return '';
});
const isManualOrigin = computed(() => props.contact.origin === ORIGIN_MANUAL);
const isUpdateable = computed(() => {
  if (disabled.value) {
    return false;
  }
  if (lastEvent.value?.type === ContactEventType.SENT) {
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
  return lastEvent.value?.type === ContactEventType.SENT_FAILED;
});
const startEdit = () => {
  emit('edit', props.contact.email);
};
const cancelEdit = () => {
  contactSave.value = JSON.parse(JSON.stringify(props.contact));
  emit('cancel');
};
const updateContact = () => {
  v.value.$validate();
  if (!v.value.$error) {
    emit('update', contactSave.value);
  }
};
const removeContact = () => {
  emit('delete', props.contact.email);
};
const mailContact = () => {
  emit('mail', props.contact.email);
};
</script>../../utils
