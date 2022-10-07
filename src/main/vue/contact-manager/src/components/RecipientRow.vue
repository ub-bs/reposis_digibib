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
      <select v-if="isManualOrigin && editMode" class="form-control form-control-sm"
          v-model="recipientSave.origin" :class="v.origin.$error ? 'is-invalid' : ''">
        <option :value="Origin.Manual">
          {{ $t('digibib.contact.frontend.manager.label.origin.manual') }}
        </option>
      </select>
      <span v-else>
        {{ recipient.origin }}
      </span>
    </td>
    <td class="col-4">
      <input v-if="isManualOrigin && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.mail" :class="v.mail.$error ? 'is-invalid' : ''"/>
      <span v-else>
        {{ recipient.mail }}
      </span>
    </td>
    <td class="col-1 align-middle text-center">
      <input v-if="editMode" type="checkbox" v-model="recipientSave.enabled"/>
      <input v-else type="checkbox" :checked="recipient.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar :editMode='editUUID === recipient.uuid'
          :edit="updateable" :remove="removeable" :mail="mailable" @edit="startEdit"
          @cancel="cancelEdit" @update="updateRecipient" @remove="removeRecipient"
          @mail="mailRecipient" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
import { useI18n } from 'vue-i18n';
import { required, email } from '@vuelidate/validators';
import { Origin } from '../utils';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  recipient: {
    type: Object,
    required: true,
  },
  editUUID: {
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
  mail: {
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
const editMode = computed(() => props.editUUID === props.recipient.uuid);
const disabled = computed(() => props.editUUID !== undefined
  && props.editUUID !== props.recipient.uuid);
const rowStyle = computed(() => {
  if (props.recipient.confirmed) {
    return 'table-success';
  }
  if (props.recipient.failed) {
    return 'table-danger';
  }
  if (props.recipient.sent) {
    return 'table-warning';
  }
  return '';
});
const title = computed(() => {
  if (props.recipient.confirmed != null) {
    return t('digibib.contact.frontend.manager.info.confirmed', {
      date: new Date(props.recipient.confirmed).toLocaleString(),
    });
  }
  if (props.recipient.failed != null) {
    return t('digibib.contact.frontend.manager.info.failed', {
      date: new Date(props.recipient.failed).toLocaleString(),
    });
  }
  if (props.recipient.sent != null) {
    return t('digibib.contact.frontend.manager.info.sent', {
      date: new Date(props.recipient.sent).toLocaleString(),
    });
  }
  return '';
});
const isManualOrigin = computed(() => props.recipient.origin === Origin.Manual);
const updateable = computed(() => {
  if (disabled.value) {
    return false;
  }
  return props.isProcessed;
});
const removeable = computed(() => {
  if (!updateable.value) {
    return false;
  }
  return isManualOrigin.value;
});
const mailable = computed(() => {
  if (disabled.value) {
    return false;
  }
  if (!props.isSent) {
    return false;
  }
  if (props.recipient.confirmed != null) {
    return false;
  }
  return props.recipient.failed != null;
});
const startEdit = () => {
  emit('edit', props.recipient.uuid);
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
  emit('delete', props.recipient.uuid);
};
const mailRecipient = () => {
  emit('mail', props.recipient.uuid);
};
</script>
