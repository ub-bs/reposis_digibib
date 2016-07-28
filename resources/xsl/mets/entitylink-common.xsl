<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision: 2256 $ $Date: 2009-09-30 17:11:00 +0200 (Mi, 30 Sep 2009) $ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="mcr xalan i18n acl">
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template name="printNamesCorp">
    <xsl:param name="unittitleProper" />
    <xsl:param name="unittitleFormal" />
    <xsl:param name="firstName" />
    <xsl:param name="lastName" />
    <xsl:param name="personalName" />
    <xsl:param name="collocation" />
    <xsl:param name="nameAffix" />
    <xsl:param name="nameAsInOpac" />
    <xsl:param name="name" />
    <xsl:param name="departments" />

    <xsl:choose>
      <xsl:when test="$unittitleProper">
        <xsl:value-of select="$unittitleProper" />
      </xsl:when>

      <xsl:when test="$unittitleFormal">
        <xsl:value-of select="$unittitleFormal" />
      </xsl:when>

      <xsl:when test="$personalName">
        <xsl:value-of select="$personalName" />
        <xsl:if test="$collocation">
          <xsl:value-of select="concat(' &lt;',$collocation,'&gt;')" />
        </xsl:if>
      </xsl:when>

      <xsl:when test="$firstName and $lastName and $collocation">
        <xsl:value-of select="concat($lastName,', ',$firstName,' &lt;',$collocation,'&gt;')" />
      </xsl:when>

      <xsl:when test="$name">
        <xsl:value-of select="$name" />
      </xsl:when>

      <xsl:otherwise>
        <xsl:if test="$firstName and $lastName and $nameAffix">
          <xsl:value-of select="concat($lastName,', ',$firstName,' ',$nameAffix)" />
        </xsl:if>

        <xsl:if test="$firstName and $lastName and not($nameAffix)">
          <xsl:value-of select="concat($lastName,', ',$firstName)" />
        </xsl:if>

        <xsl:if test="$firstName and not($lastName or $nameAffix)">
          <xsl:value-of select="firstName" />
        </xsl:if>

        <xsl:if test="not ($firstName) and $lastName">
          <xsl:value-of select="$lastName" />
        </xsl:if>

        <xsl:if test="$firstName and not($lastName)">
          <xsl:value-of select="$firstName" />
        </xsl:if>

      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$departments">
      <xsl:for-each select="$departments/department">
        <xsl:value-of select="concat(' / ',.)" />
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="printNames">
    <xsl:param name="firstName" />
    <xsl:param name="lastName" />
    <xsl:param name="personalName" />
    <xsl:param name="collocation" />
    <xsl:param name="nameAffix" />
    <xsl:param name="nameAsInOpac" />
    <xsl:param name="name" />

    <xsl:choose>
      <xsl:when test="$personalName">
        <xsl:value-of select="$personalName" />
        <xsl:if test="$collocation">
          <xsl:value-of select="concat(' &lt;',$collocation,'&gt;')" />
        </xsl:if>
      </xsl:when>

      <xsl:when test="$firstName and $lastName and $collocation">
        <xsl:value-of select="concat($lastName,', ',$firstName,' &lt;',$collocation,'&gt;')" />
      </xsl:when>

      <xsl:when test="$name">
        <xsl:value-of select="$name" />
      </xsl:when>

      <xsl:otherwise>
        <xsl:if test="$firstName and $lastName and $nameAffix">
          <xsl:value-of select="concat($lastName,', ',$firstName,' ',$nameAffix)" />
        </xsl:if>

        <xsl:if test="$firstName and $lastName and not($nameAffix)">
          <xsl:value-of select="concat($lastName,', ',$firstName)" />
        </xsl:if>

        <xsl:if test="$firstName and not($lastName or $nameAffix)">
          <xsl:value-of select="firstName" />
        </xsl:if>

        <xsl:if test="not ($firstName) and $lastName">
          <xsl:value-of select="$lastName" />
        </xsl:if>

        <xsl:if test="$firstName and not($lastName)">
          <xsl:value-of select="$firstName" />
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="printNamesById">
    <xsl:param name="identifier" />
    <xsl:variable name="object" select="document(concat('mcrobject:', $identifier))/mycoreobject" />

    <xsl:choose>
      <xsl:when test="$object/metadata/def.unittitle/unittitle[@type='proper']">
        <xsl:value-of select="$object/metadata/def.unittitle/unittitle[@type='proper']" />

        <xsl:if test="$object/metadata/def.location">
          <xsl:value-of select="concat(', ',$object/metadata/def.location/location)" />
        </xsl:if>

        <xsl:if test="$object/metadata/def.validity">
          <xsl:value-of select="concat(' (',$object/metadata/def.validity/validity,')')" />
        </xsl:if>

      </xsl:when>

      <xsl:when test="$object/metadata/def.heading/heading">
        <xsl:call-template name="printNamesCorp">
          <xsl:with-param name="firstName" select="$object/metadata/def.heading/heading/firstName" />
          <xsl:with-param name="lastName" select="$object/metadata/def.heading/heading/lastName" />
          <xsl:with-param name="personalName" select="$object/metadata/def.heading/heading/personalName" />
          <xsl:with-param name="collocation" select="$object/metadata/def.heading/heading/collocation" />
          <xsl:with-param name="nameAffix" select="$object/metadata/def.heading/heading/nameAffix" />
          <xsl:with-param name="departments" select="$object/metadata/def.heading/heading/departments" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@ID" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>