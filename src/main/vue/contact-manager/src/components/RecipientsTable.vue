<template>
  <table class="table table-striped table-bordered">
    <thead>
      <tr>
        <th class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.name') }}
        </th>
        <th class="col-3">
          {{ $t('digibib.contact.frontend.manager.label.source') }}
        </th>
        <th class="col-6">
          {{ $t('digibib.contact.frontend.manager.label.email') }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in recipients" :key="item">
        <td>
          {{ item.name }}
        </td>
        <td>
          {{ item.origin }}
        </td>
        <td>
          {{ item.email }}
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup lang="ts">
defineProps(['recipients']);
</script>
