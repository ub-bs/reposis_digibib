const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: "../../../../target/classes/META-INF/resources/contact-request-manager",
  publicPath: "./"
})
