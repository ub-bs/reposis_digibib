<template>
  <tr :class="rowStyle">
    <td class="col-3">
      <input v-if="editable && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.name" :class="v.name.$error ? 'is-invalid' : ''" />
      <span v-else>
        {{ recipient.name }}
      </span>
    </td>
    <td class="col-2">
      <select v-if="editable && editMode" class="form-control form-control-sm"
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
      <input v-if="editable && editMode" class="form-control form-control-sm" type="text"
          v-model="recipientSave.mail" :class="v.mail.$error ? 'is-invalid' : ''"/>
      <span v-else>
        {{ recipient.mail }}
      </span>
    </td>
    <td class="col-1 align-middle text-center">
      <input v-if="editMode" type="checkbox" v-model="recipientSave.enabled"/>
      <input v-else type="checkbox" v-model="recipientSave.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar :disabled='disabled || readOnly' :editMode='editUUID === recipient.uuid'
          @edit="handleEdit" @cancel="handleCancel" @update="handleUpdate" @remove="handleRemove"
          :remove="editable" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
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
  readOnly: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['edit', 'delete', 'update']);
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
const v = useVuelidate(rules, props.recipient);
const recipientSave = ref(JSON.parse(JSON.stringify(props.recipient)));
const editMode = computed(() => props.editUUID === props.recipient.uuid);
const disabled = computed(() => props.editUUID !== undefined
  && props.editUUID !== props.recipient.uuid);
const editable = computed(() => props.recipient.origin === Origin.Manual);
const rowStyle = computed(() => {
  if (props.recipient.success) {
    return 'table-success';
  }
  if (props.recipient.failed) {
    return 'table-failed';
  }
  if (props.recipient.sent) {
    return 'table-warning';
  }
  return '';
});
const handleEdit = () => {
  emit('edit', props.recipient.uuid);
};
const handleCancel = () => {
  emit('edit', undefined);
};
const handleUpdate = async () => {
  v.value.$validate();
  if (!v.value.$error) {
    emit('update', recipientSave.value);
  }
};
const handleRemove = () => {
  emit('delete', props.recipient.uuid);
};
</script>
