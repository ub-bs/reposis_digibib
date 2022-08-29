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
        <option :value="Origin.Manual">manuel</option> <!-- TODO i18n -->
      </select>
    </td>
    <td class="col-4">
      <input class="form-control form-control-sm" type="text" v-model="recipient.email"
          :class="v.email.$error ? 'is-invalid' : ''" :disabled="disabled" />
    </td>
    <td class="col-1 align-middle text-center">
      <input type="checkbox" v-model="recipient.state" :disabled="disabled" />
    </td>
    <td class="col-1 text-center align-middle">
      <div class="btn-group">
        <button class="btn pr-1 pb-0 pt-0 border-0" @click="addRecipient" :disabled="disabled">
          <i class="fas fa-check"></i>
        </button>
        <button class="btn pl-1 pb-0 pt-0 border-0" @click="resetRecipient" :disabled="disabled">
          <i class="fas fa-ban"></i>
        </button>
      </div>
    </td>
  </tr>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import useVuelidate from '@vuelidate/core';
import { required, email } from '@vuelidate/validators';
import { Origin } from '../store/state';

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
const recipient = ref({});
const v = useVuelidate(rules, recipient);
const disabled = computed(() => store.state.editRecipientId !== undefined);
const addRecipient = () => {
  v.value.$validate();
  if (!v.value.$error) {
    store.dispatch('addRecipient', recipient.value);
  }
};
const resetRecipient = () => {
  v.value.$reset();
  recipient.value = {};
};
</script>
