<?xml version="1.0" encoding="UTF-8"?>
<!-- ============================================== -->
<!-- $Revision$ $Date$ -->
<!-- ============================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:mcr="http://www.mycore.org/" xmlns:acl="xalan://org.mycore.access.MCRAccessManager" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions" exclude-result-prefixes="mcr xalan i18n acl mcrxml">

  <xsl:include href="entitylink-common.xsl" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="MCR.OPAC.CATALOG" />
  <xsl:param name="objectID" />
  <xsl:param name="license" select="'CC-BY-NC-SA'" />

  <xsl:variable name="ACTUAL.OPAC.CATALOG">
    <xsl:choose>
      <xsl:when test="$MCR.OPAC.CATALOG = '%MCROpacCatalog%'">
        <xsl:value-of select="'http://gso.gbv.de/DB=2.1/'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$MCR.OPAC.CATALOG" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template name="amdSec">
    <xsl:param name="mcrobject" />
    <xsl:param name="derobject" />

    <xsl:variable name="sectionID">
      <xsl:choose>
        <xsl:when test="$mcrobject">
          <xsl:value-of select="$mcrobject" />
        </xsl:when>
        <xsl:when test="$derobject">
          <xsl:value-of select="$derobject" />
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="derivateOwnerId">
      <xsl:choose>
        <!-- it is derivate -->
        <xsl:when test="$derobject">
          <xsl:value-of select="mcrxml:getMCRObjectID($derobject)" />
        </xsl:when>
        <xsl:otherwise>
          <!--it is a regular mcrobj -->
          <xsl:value-of select="$mcrobject" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="derivateOwnerObj" select="document(concat('mcrobject:', $derivateOwnerId))" />
    <xsl:variable name="ownerEntityLinkId" select="$derivateOwnerObj/mycoreobject/metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href" />

    <xsl:comment>
      Start amdSec - mets-amd.xsl
    </xsl:comment>
    <mets:amdSec ID="amd_{$sectionID}">
      <mets:rightsMD ID="rightsMD_263566811">
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVRIGHTS">
          <mets:xmlData>
            <dv:rights xmlns:dv="http://dfg-viewer.de/">
              <xsl:choose>
                <xsl:when test="$ownerEntityLinkId">
                  <xsl:variable name="entity" select="document(concat('mcrobject:', $ownerEntityLinkId))" />
                  <!-- owner name -->
                  <dv:owner>
                    <xsl:call-template name="printNamesCorp">
                      <xsl:with-param name="unittitleProper" select="$entity/mycoreobject/metadata/def.unittitle/unittitle[@type='proper']" />
                      <xsl:with-param name="unittitleFormal" select="$entity/mycoreobject/metadata/def.unittitle/unittitle[@type='formal']" />
                      <xsl:with-param name="firstName" select="$entity/mycoreobject/metadata/def.heading/heading/firstName" />
                      <xsl:with-param name="lastName" select="$entity/mycoreobject/metadata/def.heading/heading/lastName" />
                      <xsl:with-param name="personalName" select="$entity/mycoreobject/metadata/def.heading/heading/personalName" />
                      <xsl:with-param name="collocation" select="$entity/mycoreobject/metadata/def.heading/heading/collocation" />
                      <xsl:with-param name="nameAffix" select="$entity/mycoreobject/metadata/def.heading/heading/nameAffix" />
                      <xsl:with-param name="name" select="$entity/mycoreobject/metadata/def.heading/heading/name" />
                      <xsl:with-param name="departments" select="$entity/mycoreobject/metadata/def.departments" />
                    </xsl:call-template>
                  </dv:owner>

                  <!-- contact -->
                  <xsl:choose>
                    <xsl:when test="$entity/mycoreobject/metadata/def.contact/contact">
                      <dv:ownerContact>
                        <xsl:value-of select="$entity/mycoreobject/metadata/def.contact/contact" />
                      </dv:ownerContact>
                    </xsl:when>
                    <xsl:otherwise>
                      <dv:ownerContact>mailto:thulb_ims@thulb.uni-jena.de</dv:ownerContact>
                    </xsl:otherwise>
                  </xsl:choose>

                  <!-- owner logo -->
                  <xsl:choose>
                    <xsl:when test="$entity/mycoreobject/metadata/def.logoURL/logoURL[@type='dfg-viewer']">
                      <dv:ownerLogo>
                        <xsl:value-of select="$entity/mycoreobject/metadata/def.logoURL/logoURL[@type='dfg-viewer']" />
                      </dv:ownerLogo>
                    </xsl:when>
                    <xsl:otherwise>
                      <dv:ownerLogo>
                        <xsl:value-of select="concat($WebApplicationBaseURL,'images/logo-dfg-viewer.png')" />
                      </dv:ownerLogo>
                    </xsl:otherwise>
                  </xsl:choose>

                  <!-- owner site url -->
                  <xsl:choose>
                    <xsl:when test="$entity/mycoreobject/metadata/def.siteURL/siteURL">
                      <dv:ownerSiteURL>
                        <xsl:value-of select="$entity/mycoreobject/metadata/def.siteURL/siteURL" />
                      </dv:ownerSiteURL>
                    </xsl:when>
                    <xsl:otherwise>
                      <dv:ownerSiteURL>http://www.thulb.uni-jena.de/</dv:ownerSiteURL>
                    </xsl:otherwise>
                  </xsl:choose>

                  <dv:license>
                    <xsl:choose>
                      <xsl:when test="$entity/mycoreobject/metadata/def.license/license">
                        <xsl:variable name="licenseClass" select="$entity/mycoreobject/metadata/def.license/license/@classid" />
                        <xsl:variable name="licenseId" select="$entity/mycoreobject/metadata/def.license/license/@categid" />
                        <xsl:value-of select="mcrxml:getDisplayName($licenseClass, $licenseId)" />
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$license" />
                      </xsl:otherwise>
                    </xsl:choose>
                  </dv:license>

                </xsl:when>
                <!-- fallback behaviour -->
                <xsl:otherwise>
                  <dv:owner>Thüringer Universitäts- und Landesbibliothek</dv:owner>
                  <dv:ownerContact>mailto:thulb_ims@thulb.uni-jena.de</dv:ownerContact>
                  <dv:ownerLogo>
                    <xsl:value-of select="concat($WebApplicationBaseURL,'images/logo-dfg-viewer.png')" />
                  </dv:ownerLogo>
                  <dv:ownerSiteURL>http://www.thulb.uni-jena.de/</dv:ownerSiteURL>
                  <dv:license>
                    <xsl:value-of select="$license" />
                  </dv:license>
                </xsl:otherwise>
              </xsl:choose>
            </dv:rights>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:rightsMD>
      <mets:digiprovMD>
        <xsl:attribute name="ID">
            <xsl:value-of select="concat('digiprovMD',$sectionID)" />          
          </xsl:attribute>
        <mets:mdWrap MIMETYPE="text/xml" MDTYPE="OTHER" OTHERMDTYPE="DVLINKS">
          <mets:xmlData>
            <dv:links xmlns:dv="http://dfg-viewer.de/">
              <xsl:variable name="ppn" select="metadata/def.identifier/identifier[@type='ppn']" />
              <xsl:if test="$ppn">
                <dv:reference>
                  <xsl:value-of select="concat($ACTUAL.OPAC.CATALOG,'PPN?PPN=',$ppn)" />
                </dv:reference>
              </xsl:if>
              <dv:presentation>
                <xsl:choose>
                  <xsl:when test="$mcrobject">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',$mcrobject)" />
                  </xsl:when>
                  <xsl:when test="$derobject">
                    <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',mcrxml:getMCRObjectID($derobject))" />
                  </xsl:when>
                </xsl:choose>
              </dv:presentation>
            </dv:links>
          </mets:xmlData>
        </mets:mdWrap>
      </mets:digiprovMD>
    </mets:amdSec>
    <xsl:comment>
      End amdSec - mets-amd.xsl
    </xsl:comment>
  </xsl:template>

</xsl:stylesheet>