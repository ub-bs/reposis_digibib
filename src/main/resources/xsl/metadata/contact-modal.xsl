<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="xslImport:modsmeta:metadata/contact-modal.xsl" />
  <xsl:param name="Digibib.ContactService.Genres.Enabled" />

  <xsl:template match="/">
    <xsl:if test="contains($Digibib.ContactService.Genres.Enabled, $mods-type)">
      <div id="contactModalDiv">
        <xsl:call-template name="print-contact-modal" />
        <xsl:call-template name="print-contact-btn" />
      </div>
    </xsl:if>
    <xsl:apply-imports />
  </xsl:template>

  <xsl:template name="print-contact-modal">
    <script src="{$WebApplicationBaseURL}vue/vue.global.prod.js" />
    <script src="{$WebApplicationBaseURL}vue/vue-i18n.global.prod.js" />
    <script src="{$WebApplicationBaseURL}vue/contactForm/contactForm.umd.js" />
    <link rel="stylesheet" href="{$WebApplicationBaseURL}vue/contactForm/contactForm.css" />

    <div id="contact-form-mount">
      <cf base-url="{$WebApplicationBaseURL}" object-id="{$id}"></cf>
    </div>
    <script>
    function initialize(i18nData) {
        const i18n = VueI18n.createI18n({
        locale: '_',
        messages: {
          _: i18nData,
        },
      });
      const app = Vue.createApp({
        components: {
          cf: contactForm
        },
      });
      app.use(i18n);
      app.mount('#contact-form-mount');
    }
    fetch(webApplicationBaseURL + 'rsc/locale/translate/digibib.contact.frontend.*')
      .then(response => response.json())
      .then((data) => initialize(data))
      .catch((error) => initialize({}));
    </script>
  </xsl:template>

  <xsl:template name="print-contact-btn">
    <li>
      <div id="btnContact" />
    </li>
    <script>
    $(document).ready(() => {
      actionMenu = $("div#mir-edit-div").find("ul.dropdown-menu");
      actionMenu.append($("#btnContact").parent());
    });
    </script>
  </xsl:template>
</xsl:stylesheet>
