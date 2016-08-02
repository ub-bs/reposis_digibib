<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcr" version="1.0">
  <xsl:include href="mets-iview.xsl" />
  <xsl:include href="mets-amd.xsl" />
  <xsl:include href="mods2mods.xsl" />

  <xsl:output method="xml" encoding="utf-8" />
  <xsl:param name="MCR.Module-iview2.SupportedContentTypes" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="derivateID" />
  <xsl:param name="objectID" />

  <xsl:variable name="sourcedoc" select="document(concat('mcrobject:',$objectID))" />

  <xsl:template match="/mycoreobject" priority="0" mode="metsmeta" xmlns:mods="http://www.loc.gov/mods/v3">
    <mets:mdWrap MDTYPE="MODS">
      <mets:xmlData>
        <xsl:apply-templates mode="mods2mods" />
      </mets:xmlData>
    </mets:mdWrap>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="fallBackEntity"
                xmlns:tubs="http://www.urmel-dl.de/ns/mods-entities">
                <!-- TODO: add content of TU BS
    <urmel:entity type="owner" xlink:type="extended" xlink:title="Technische Universität Braunschweig">
      <urmel:site xlink:type="locator" xlink:href="http://www.tu-braunschweig.de/" />
      <urmel:logo xlink:type="resource" xlink:href="http://reposis-test.gbv.de/digibib/images/siegel_rot.jpg" />
      <urmel:full-logo xlink:type="resource" xlink:href="http://reposis-test.gbv.de/digibib/images/siegel_rot.jpg" />
    </urmel:entity> -->
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="ownerEntity">
    <xsl:apply-templates select="." mode="fallbackEntity" />
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="sponsorEntity">
    <xsl:comment>
      no sponsor defined
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="partnerEntity">
    <xsl:comment>
      no partner defined
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="0" mode="entities">
    <xsl:apply-templates mode="ownerEntity" select="." />
    <xsl:apply-templates mode="sponsorEntity" select="." />
    <xsl:apply-templates mode="partnerEntity" select="." />
  </xsl:template>

  <xsl:template match="mets:mets">
    <mets:mets>
      <xsl:if test="not(mets:amdSec)">
        <xsl:variable name="emptryAMDSec">
          <mets:amdSec />
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($emptryAMDSec)" />
      </xsl:if>
      <xsl:if test="not(mets:dmdSec)">
        <xsl:variable name="emptyDMDSec">
          <mets:dmdSec ID="dmd_{$derivateID}"/>
        </xsl:variable>
        <xsl:apply-templates select="xalan:nodeset($emptyDMDSec)" />
      </xsl:if>
      <xsl:apply-templates />
    </mets:mets>
  </xsl:template>

  <xsl:template match="mets:dmdSec">
    <mets:dmdSec ID="dmd_{$derivateID}">
      <mets:mdWrap MDTYPE="MODS">
        <mets:xmlData>
          <mods:mods>
            <xsl:apply-templates mode="metsmeta" select="$sourcedoc/mycoreobject" />
            <mods:extension>
              <urmel:entities xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
                <urmel:entity type="operator" xlink:type="extended"
                              xlink:title="Thüringer Universitäts- und Landesbibliothek Jena">
                  <urmel:site xlink:type="locator" xlink:href="http://www.thulb.uni-jena.de" />
                  <urmel:logo xlink:type="resource" xlink:href="{$logoBaseUrl}thulb.svg" />
                  <urmel:full-logo xlink:type="resource" xlink:href="{$logoBaseUrl}thulb+text.svg" />
                </urmel:entity>
                <xsl:apply-templates mode="entities" select="$sourcedoc/mycoreobject" />
              </urmel:entities>
            </mods:extension>
          </mods:mods>
        </mets:xmlData>
      </mets:mdWrap>
    </mets:dmdSec>
  </xsl:template>

  <xsl:template match="mets:amdSec">
    <xsl:call-template name="amdSec">
      <xsl:with-param name="derobject" select="$derivateID" />
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
