<template>
  <table class="table">
    <thead>
      <tr>
        <th v-for="field in fields" :key="field.key" :class="field.class">
          {{ field.label }}
        </th>
      </tr>
    </thead>
    <tbody v-if="requests">
      <template v-for="(item, index) in requests" :key="item">
        <tr class="col-1" @click="toggleExpanded(index)">
          <td v-for="field in fields" :key="field.key" :class="field.class">
            {{ item[field.key] }}
          </td>
        </tr>
        <tr v-if="expanded.includes(index)" class="bg-light-grey">
          <td :colspan="fields.length">
            <div class="row justify-content-md-center">
              <div class="col-10">
                <div class="row">
                  <div class="col-12">
                    <textarea rows="5" class="form-control" readonly v-model="item.message" />
                  </div>
                </div>
                <div class="row mt-2 text-right">
                  <div class="col">
                    <button type="button" class="btn btn-danger mr-1">
                      {{ $t('digibib.contact.frontend.manager.button.reject') }}
                    </button>
                    <button type="button" class="btn btn-success">
                      {{ $t('digibib.contact.frontend.manager.button.accept') }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useStore } from 'vuex';
import { useI18n } from 'vue-i18n';

const store = useStore();
const requests = computed(() => store.state.requests);
const { t } = useI18n();
const expanded = ref([]) as number[];
const fields = [
  {
    key: 'created',
    label: t('digibib.contact.frontend.manager.label.created'),
    class: 'col-2',
  },
  {
    key: 'objectID',
    label: t('digibib.contact.frontend.manager.label.objectID'),
    class: 'col-2',
  },
  {
    key: 'name',
    label: t('digibib.contact.frontend.manager.label.name'),
    class: 'col-3',
  },
  {
    key: t('email'),
    label: t('digibib.contact.frontend.manager.label.email'),
    class: 'col-2',
  },
  {
    key: 'orcid',
    label: t('digibib.contact.frontend.manager.label.orcid'),
    class: 'col-2',
  },
  {
    key: 'state',
    label: t('digibib.contact.frontend.manager.label.state'),
    class: 'col-1',
  },
];
function toggleExpanded(id: number) {
  const index = this.expanded.indexOf(id);
  if (index > -1) {
    this.expanded.splice(index, 1);
  } else {
    this.expanded.push(id);
  }
}
</script>
