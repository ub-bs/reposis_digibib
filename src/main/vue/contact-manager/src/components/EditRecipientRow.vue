<template>
  <tr>
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
          v-model="recipientSave.email" :class="v.email.$error ? 'is-invalid' : ''"/>
      <span v-else>
        {{ recipient.email }}
      </span>
    </td>
    <td class="col-1 align-middle text-center">
      <input v-if="editMode" type="checkbox" v-model="recipientSave.enabled"/>
      <input v-else type="checkbox" v-model="recipientSave.enabled" disabled/>
    </td>
    <td class="col-1 text-center align-middle">
      <EditToolbar :disabled='disabled' :editMode='editMode' @edit="handleEdit"
          @cancel="handleCancel" @update="handleUpdate" @remove="handleRemove" :remove="editable" />
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { Origin } from '../store/state';
import EditToolbar from './EditToolbar.vue';

const props = defineProps({
  recipient: {
    type: Object,
    required: true,
  },
});
const recipientSave = JSON.parse(JSON.stringify(props.recipient));
const store = useStore();
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
const editMode = ref(false);
const disabled = computed(() => store.state.editRecipientId !== undefined
    && store.state.editRecipientId !== props.recipient.uuid);
const editable = computed(() => props.recipient.origin === Origin.Manual);
const v = useVuelidate(rules, props.recipient);
const handleEdit = () => {
  store.commit('setEditRecipient', props.recipient.uuid);
  editMode.value = true;
};
const handleCancel = () => {
  store.commit('setEditRecipient', undefined);
  editMode.value = false;
};
const handleUpdate = () => {
  v.value.$validate();
  if (!v.value.$error) {
    store.dispatch('updateRecipient', recipientSave);
    editMode.value = false;
  }
};
const handleRemove = () => {
  store.dispatch('removeRecipient', props.recipient.uuid);
};
</script>
