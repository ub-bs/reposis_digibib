<template>
  <transition name="modal" appear>
    <div class="modal-mask">
      <div class="modal-wrapper" @click="close">
        <div class="modal-dialog modal-lg" role="document" @click.stop="">
          <div class="modal-content">
            <div class="modal-header">
              <slot name="title">
                <h5 class="modal-title">
                  {{ title }}
                </h5>
                <button v-if="!okOnly" type="button" class="close" aria-label="Close">
                  <span aria-hidden="true" @click="close">&times;</span>
                </button>
              </slot>
            </div>
            <div class="modal-body">
              <slot />
            </div>
            <div class="modal-footer">
              <slot name="footer">
                <button type="button" class="btn btn-primary" @click="emit('ok')">
                  {{ okTitle }}
                </button>
              </slot>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>
<script setup lang="ts">
import { defineEmits, defineProps } from 'vue';

defineProps({
  title: String,
  okTitle: {
    type: String,
    default: 'OK',
  },
  okOnly: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['close', 'ok']);
const close = () => {
  emit('close');
};
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
