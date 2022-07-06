<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="fn mods">

  <xsl:import href="xslImport:modsmeta:metadata/metadata-extension.xsl" />

  <xsl:template name="print-field">
    <xsl:param name="i18n" />
    <xsl:param name="pre-value" />
    <xsl:param name="value" />
    <xsl:param name="url" />
    <dt>
      <xsl:value-of select="document(concat('i18n:', $i18n))" />
    </dt>
    <dd>
      <xsl:choose>
        <xsl:when test="$url">
          <a href="{$url}">
            <xsl:choose>
              <xsl:when test="$value">
                <xsl:value-of select="$value" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$url" />
              </xsl:otherwise>
            </xsl:choose>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$pre-value and $value">
              <strong>
                <xsl:value-of select="$pre-value" />
              </strong>
              <xsl:text> </xsl:text>
            </xsl:when>
            <xsl:when test="$pre-value">
              <xsl:value-of select="$pre-value" />
            </xsl:when>
          </xsl:choose>
          <xsl:if test="$value">
            <xsl:value-of select="$value" />
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </dd>
  </xsl:template>

  <xsl:template match="/">
    <xsl:variable name="mods" select="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods" />
      <div id="metadata-extension">
        <div class="mir_metadata" style="margin-top:-30px;">
          <dl>
            <xsl:if test="$mods/mods:extension[@displayLabel='codemeta-part']/fn:map">
              <xsl:call-template name="codemeta">
                <xsl:with-param name="codemeta" select="$mods/mods:extension[@displayLabel='codemeta-part']/fn:map" />
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="$mods/mods:extension[@displayLabel='advanced-part']/fn:map">
              <xsl:call-template name="advanced">
                <xsl:with-param name="advanced" select="$mods/mods:extension[@displayLabel='advanced-part']/fn:map" />
              </xsl:call-template>
            </xsl:if>
          </dl>
        </div>
      </div>
    <xsl:apply-imports />
  </xsl:template>

  <xsl:template name="get-classification-label">
    <xsl:param name="classification" />
    <xsl:value-of select="document(concat('classification:metadata:0:children:', $classification))//category/label[@xml:lang=$CurrentLang]/@text" />
  </xsl:template>

  <xsl:template name="advanced">
    <xsl:param name="advanced" />
    <xsl:if test="$advanced/fn:array[@key='type']/fn:map">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.type'" />
        <xsl:with-param name="pre-value">
          <xsl:call-template name="get-classification-label">
            <xsl:with-param name="classification" select="$advanced/fn:array[@key='type']/fn:map/fn:string[@key='type']" />
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="value" select="$advanced/fn:array[@key='type']/fn:map/fn:string[@key='description']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:array[@key='researchObject']/fn:map">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.researchObject'" />
        <xsl:with-param name="pre-value">
          <xsl:call-template name="get-classification-label">
            <xsl:with-param name="classification" select="$advanced/fn:array[@key='researchObject']/fn:map/fn:string[@key='type']" />
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="value" select="$advanced/fn:array[@key='researchObject']/fn:map/fn:string[@key='description']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:array[@key='dataOrigin']/fn:map">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.dataOrigin'" />
        <xsl:with-param name="pre-value">
          <xsl:call-template name="get-classification-label">
            <xsl:with-param name="classification" select="$advanced/fn:array[@key='dataOrigin']/fn:map/fn:string[@key='type']" />
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="value" select="$advanced/fn:array[@key='dataOrigin']/fn:map/fn:string[@key='description']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:array[@key='software_types']/fn:map">
      <xsl:variable name="name" select="$advanced/fn:array[@key='software_types']/fn:map/fn:string[@key='name']" />
      <xsl:variable name="version" select="$advanced/fn:array[@key='software_types']/fn:map/fn:string[@key='version']" />
      <xsl:variable name="fullname">
        <xsl:choose>
          <xsl:when test="$version">
            <xsl:value-of select="concat($name, ' (', $version, ')')" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$name" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.software'" />
        <xsl:with-param name="pre-value">
          <xsl:call-template name="get-classification-label">
            <xsl:with-param name="classification" select="$advanced/fn:array[@key='software_types']/fn:map/fn:string[@key='type']" />
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="value" select="$fullname" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:string[@key='methods']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.methods'" />
        <xsl:with-param name="value" select="$advanced/fn:string[@key='methods']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:string[@key='instruments']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.instruments'" />
        <xsl:with-param name="value" select="$advanced/fn:string[@key='instruments']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$advanced/fn:string[@key='processing']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.advanced.processing'" />
        <xsl:with-param name="value" select="$advanced/fn:string[@key='processing']" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="codemeta">
    <xsl:param name="codemeta" />
    <xsl:if test="$codemeta/fn:map[@key='developmentStatus']">
      <xsl:variable name="status" select="$codemeta/fn:map[@key='developmentStatus']/fn:string[@key='@value']" />
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.developmentStatus'" />
        <xsl:with-param name="value">
          <xsl:call-template name="get-classification-label">
            <xsl:with-param name="classification" select="$status" />
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='version']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.version'" />
        <xsl:with-param name="value" select="$codemeta/fn:string[@key='version']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='applicationCategory']">
      <xsl:for-each select="$codemeta/fn:array[@key='applicationCategory']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.applicationCategory'" />
          <xsl:with-param name="value" select="fn:string[@key='@value']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='applicationSubCategory']">
      <xsl:for-each select="$codemeta/fn:array[@key='applicationSubCategory']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.applicationSubCategory'" />
          <xsl:with-param name="value" select="fn:string[@key='@value']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='programmingLanguage']">
      <xsl:for-each select="$codemeta/fn:array[@key='programmingLanguage']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.programmingLanguage'" />
          <xsl:with-param name="value" select="fn:string[@key='name']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='operatingSystem']">
      <xsl:for-each select="$codemeta/fn:array[@key='operatingSystem']">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.operatingSystem'" />
          <xsl:with-param name="value" select="fn:string" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='processorRequirements']">
      <xsl:for-each select="$codemeta/fn:array[@key='processorRequirements']">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.processorRequirements'" />
          <xsl:with-param name="value" select="fn:string" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='memoryRequirements']">
      <xsl:for-each select="$codemeta/fn:array[@key='memoryRequirements']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.memoryRequirements'" />
          <xsl:with-param name="value" select="fn:string[@key='@value']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='storageRequirements']">
      <xsl:for-each select="$codemeta/fn:array[@key='storageRequirements']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.storageRequirements'" />
          <xsl:with-param name="value" select="fn:string[@key='@value']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='runtimePlatform']">
      <xsl:for-each select="$codemeta/fn:array[@key='runtimePlatform']">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.runtimePlatform'" />
          <xsl:with-param name="value" select="fn:string" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='softwareRequirements']">
      <xsl:for-each select="$codemeta/fn:array[@key='softwareRequirements']/fn:map">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.softwareRequirements'" />
          <xsl:with-param name="value" select="fn:string[@key='name']" />
          <xsl:with-param name="url" select="fn:string[@key='codeRepository']" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:array[@key='permissions']">
      <xsl:for-each select="$codemeta/fn:array[@key='permissions']">
        <xsl:call-template name="print-field">
          <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.permissions'" />
          <xsl:with-param name="value" select="fn:string" />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='codeRepository']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.codeRepo'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='codeRepository']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='buildInstructions']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.buildInstructions'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='buildInstructions']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='releaseNotes']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.releaseNotes'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='releaseNotes']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='contIntegration']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.contIntegration'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='contIntegration']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='issueTracker']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.issueTracker'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='issueTracker']" />
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$codemeta/fn:string[@key='readme']">
      <xsl:call-template name="print-field">
        <xsl:with-param name="i18n" select="'digibib.researchData.codeMeta.readme'" />
        <xsl:with-param name="url" select="$codemeta/fn:string[@key='readme']" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
