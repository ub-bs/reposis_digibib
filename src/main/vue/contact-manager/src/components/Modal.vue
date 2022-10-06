<template>
  <Transition name="modal-fade">
    <div class="modal-backdrop" @click="close">
      <div class="modal-dialog" :class="style" role="document" @click.stop="">
        <div class="modal-content">
          <div class="modal-header">
            <slot name="title">
              <h5 class="modal-title">
                {{ title }}
              </h5>
              <button v-if="!hideHeaderClose" type="button" class="close" aria-label="Close">
                <span aria-hidden="true" @click="close">&times;</span>
              </button>
            </slot>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div class="modal-footer">
            <slot name="footer">
              <button v-if="!okOnly" type="button" class="btn btn-secondary" @click="cancel">
                {{ cancelTitle }}
              </button>
              <button type="button" class="btn btn-primary" @click="ok">
                {{ okTitle }}
              </button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>
<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  title: String,
  okTitle: {
    type: String,
    default: 'OK',
  },
  cancelTitle: {
    type: String,
    default: 'Cancel',
  },
  okOnly: {
    type: Boolean,
    default: false,
  },
  busy: {
    type: Boolean,
    default: false,
  },
  size: {
    type: String,
    default: 'md',
  },
  scrollable: {
    type: Boolean,
    default: false,
  },
  hideHeaderClose: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['close', 'ok', 'cancel']);
const close = () => {
  if (!props.busy) {
    emit('close');
  }
};
const ok = () => {
  if (!props.busy) {
    emit('ok');
  }
};
const cancel = () => {
  if (!props.busy) {
    emit('cancel');
  }
};
const style = computed(() => {
  let result = `modal-${props.size}`;
  if (props.scrollable) {
    result += ' modal-dialog-scrollable';
  }
  return result;
});
</script>
<style>
.modal-backdrop {
  z-index: 9998;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-fade-enter-from {
  opacity: 0;
}
.modal-fade-enter-active,
.modal-fade-leave-active,
.modal-fade-leave-from,
.modal-fade-leave-to {
  transition: all 0.3s ease-out;
}
</style>
