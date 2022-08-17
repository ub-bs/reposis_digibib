<template>
  <!-- {{ $t('digibib.contact.frontend.form.title') }} -->
  <modal @ok="emit('close')" title="TODO">
    <div>
      Die Anfrage wurde erfolgreich aufgenommen und wird nun an die Beteiligten weitergeleitet.
    </div>
    <div v-if="sendCopy">
      Sie erhalten in Kürze eine Bestätigungsmail.
    </div>
    <div>
      Sofern die Anfrage längst unbeantwortet bleibt, kontaktieren Sie bitte unseren Support.
    </div>
  </modal>
</template>

<script setup lang="ts">
import { defineEmits, defineProps } from 'vue';
import Modal from './Modal.vue';

defineProps({
  sendCopy: Boolean,
});
const emit = defineEmits(['close']);
const close = () => emit('close');
</script>
<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  -webkit-transform: scale(1.02);
  transform: scale(1.02);
}
</style>
