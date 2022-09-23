<template>
  <tr>
    <td class="col-3">
      <input class="form-control form-control-sm" type="text" v-model="recipient.name"
          :class="v.name.$error ? 'is-invalid' : ''" :disabled="disabled"/>
    </td>
    <td class="col-2">
      <select class="form-control form-control-sm" v-model="recipient.origin"
          :class="v.origin.$error ? 'is-invalid' : ''" :disabled="disabled">
        <option disabled value=""></option>
        <option :value="Origin.Manual">
          {{ $t('digibib.contact.frontend.manager.label.origin.manual') }}
        </option>
      </select>
    </td>
    <td class="col-4">
      <input class="form-control form-control-sm" type="text" v-model="recipient.mail"
          :class="v.mail.$error ? 'is-invalid' : ''" :disabled="disabled" />
    </td>
    <td class="col-1 align-middle text-center">
      <input type="checkbox" v-model="recipient.enabled" :disabled="disabled" />
    </td>
    <td class="col-1 text-center align-middle">
      <div class="btn-group">
        <button class="btn shadow-none pr-1 pb-0 pt-0 border-0" @click="addRecipient"
            :disabled="disabled">
          <i class="fas fa-check"></i>
        </button>
        <button class="btn shadow-none pl-1 pb-0 pt-0 border-0" @click="resetRecipient"
            :disabled="disabled">
          <i class="fas fa-ban"></i>
        </button>
      </div>
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { Origin } from '../utils';

defineProps({
  disabled: {
    type: Boolean,
    required: true,
  },
});
const emit = defineEmits(['add']);
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
const recipient = ref({});
const v = useVuelidate(rules, recipient);
const reset = () => {
  v.value.$reset();
  recipient.value = {};
};
const addRecipient = async () => {
  v.value.$validate();
  if (!v.value.$error) {
    emit('add', recipient.value);
    reset();
  }
};
</script>
