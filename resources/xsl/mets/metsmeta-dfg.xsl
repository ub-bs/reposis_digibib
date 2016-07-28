<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:mets="http://www.loc.gov/METS/"
  xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xalan="http://xml.apache.org/xalan" xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
  xmlns:mcr="http://www.mycore.org/" xmlns:archcom="xalan://de.uni_jena.thulb.archive.frontend.cli.command.MCRCommandCommons" xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:gndo="http://d-nb.info/standards/elementset/gnd#" xmlns:geo="http://www.opengis.net/ont/geosparql#"
  xmlns:mcrgeo="org.mycore.common.MCRGeoUtilities" xmlns:zs="http://www.loc.gov/zing/srw/" xmlns:info="info:srw/schema/5/picaXML-v1.0"
  xmlns:urn="xalan://org.mycore.urn.MCRXMLFunctions" xmlns:MCRArchiveUtils="xalan://de.uni_jena.thulb.archive.tools.MCRArchiveUtils"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" exclude-result-prefixes="xalan mcrxml encoder archcom mcr rdf gndo geo mcrgeo zs info urn MCRArchiveUtils i18n"
  version="1.0">
  <xsl:output method="xml" encoding="utf-8" />

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="MCR.OPAC.CATALOG" />

  <xsl:variable name="ACTUAL.OPAC.CATALOG">
    <xsl:choose>
      <xsl:when test="not($MCR.OPAC.CATALOG)">
        <xsl:value-of select="'http://gso.gbv.de/DB=2.1/'" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$MCR.OPAC.CATALOG" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="mycoreobject" priority="1" mode="ownerEntity">
    <xsl:variable name="entityLink" select="metadata/def.entitylink/entitylink[@type='corporation']" />
    <xsl:choose>
      <xsl:when test="$entityLink">
        <xsl:apply-templates select="document(concat('mcrobject:',$entityLink[1]/@xlink:href))/mycoreobject" mode="entity">
          <xsl:with-param name="type" select="'owner'" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="fallbackEntity" />
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template match="mycoreobject" priority="1" mode="sponsorEntity">
    <xsl:variable name="entityLink" select="metadata/def.entitylink/entitylink[@type='patron']" />
    <xsl:choose>
      <xsl:when test="$entityLink">
        <xsl:apply-templates select="document(concat('mcrobject:',$entityLink[1]/@xlink:href))/mycoreobject" mode="entity">
          <xsl:with-param name="type" select="'sponsor'" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>
          no sponsor defined
        </xsl:comment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="1" mode="partnerEntity">
    <xsl:variable name="entityLink" select="metadata/def.entitylink/entitylink[@type='partner']" />
    <xsl:choose>
      <xsl:when test="$entityLink">
        <xsl:apply-templates select="document(concat('mcrobject:',$entityLink[1]/@xlink:href))/mycoreobject" mode="entity">
          <xsl:with-param name="type" select="'sponsor'" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>
          no sponsor defined
        </xsl:comment>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="1" mode="language">
    <xsl:variable name="ppn" select="./metadata/def.identifier/identifier[@type='ppn']" />
    <xsl:variable name="catalog">
      <xsl:choose>
        <xsl:when test="starts-with(./metadata/def.typeOfUnit/typeOfUnit/@categid, 3)">
          <xsl:value-of select="'ikar'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'gbvcat'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:if test="$ppn">
      <xsl:variable name="catalogData"
        select="document(concat('http://sru.gbv.de/', $catalog, '?query=pica.ppn=', $ppn, '&amp;version=1.1&amp;operation=searchRetrieve&amp;recordSchema=picaxml&amp;recordPacking=xml&amp;maximumRecords=1&amp;startRecord=1'))" />
      <xsl:variable name="languageValue"
        select="$catalogData/zs:searchRetrieveResponse/zs:records/zs:record/zs:recordData/*[1]/info:datafield[@tag='010@']/info:subfield[@code='a']" />

      <mods:language usage="primary">
        <mods:languageTerm type="code" authority="iso639-2b">
          <xsl:choose>
            <xsl:when test="$languageValue">
              <xsl:value-of select="$languageValue" />
            </xsl:when>
            <xsl:otherwise>
              <!-- the default language value -->
              <xsl:value-of select="'ger'" />
            </xsl:otherwise>
          </xsl:choose>
        </mods:languageTerm>
      </mods:language>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mycoreobject" priority="1" mode="originInfo">
    <mods:originInfo>
      <mods:publisher>
        <xsl:choose>
          <xsl:when test="./metadata/def.entitylink/entitylink[@type='corporation']">
            <xsl:variable name="owner"
              select="document(concat('mcrobject:',./metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href))/mycoreobject" />

            <xsl:if test="$owner/metadata/def.identifier/identifier[@type='isil']">
              <xsl:value-of select="concat($owner/metadata/def.identifier/identifier[@type='isil'], ' - ')" />
            </xsl:if>

            <xsl:call-template name="printNamesCorp">
              <xsl:with-param name="unittitleProper" select="$owner/metadata/def.unittitle/unittitle[@type='proper']" />
              <xsl:with-param name="unittitleFormal" select="$owner/metadata/def.unittitle/unittitle[@type='formal']" />
              <xsl:with-param name="firstName" select="$owner/metadata/def.heading/heading/firstName" />
              <xsl:with-param name="lastName" select="$owner/metadata/def.heading/heading/lastName" />
              <xsl:with-param name="personalName" select="$owner/metadata/def.heading/heading/personalName" />
              <xsl:with-param name="collocation" select="$owner/metadata/def.heading/heading/collocation" />
              <xsl:with-param name="nameAffix" select="$owner/metadata/def.heading/heading/nameAffix" />
              <xsl:with-param name="departments" select="$owner/metadata/def.heading/heading/departments" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'DE-27 - Thüringer Universitäts- und Landesbibliothek'" />
          </xsl:otherwise>
        </xsl:choose>
      </mods:publisher>

      <mods:dateCaptured encoding="w3cdtf">
        <xsl:value-of select="./service/servdates/servdate[@type='createdate']" />
      </mods:dateCaptured>
      <mods:edition>[Electronic ed.]</mods:edition>
    </mods:originInfo>
  </xsl:template>


  <xsl:template match="mycoreobject[contains(@ID,'_corporation_')]" mode="entity" xmlns:urmel="http://www.urmel-dl.de/ns/mods-entities">
    <xsl:param name="type" />
    <xsl:variable name="title">
      <xsl:call-template name="printNamesCorp">
        <xsl:with-param name="firstName" select="./metadata/def.heading/heading/firstName" />
        <xsl:with-param name="lastName" select="./metadata/def.heading/heading/lastName" />
        <xsl:with-param name="personalName" select="./metadata/def.heading/heading/personalName" />
        <xsl:with-param name="collocation" select="./metadata/def.heading/heading/collocation" />
        <xsl:with-param name="nameAffix" select="./metadata/def.heading/heading/nameAffix" />
        <xsl:with-param name="departments" select="./metadata/def.heading/heading/departments" />
      </xsl:call-template>
    </xsl:variable>
    <urmel:entity type="{$type}" xlink:type="extended" xlink:title="{$title}">
      <xsl:if test="metadata/def.siteURL/siteURL">
        <urmel:site xlink:type="locator" xlink:href="{metadata/def.siteURL/siteURL}" />
      </xsl:if>
      <xsl:if test="metadata/def.logoURL/logoURL[@type='small']">
        <urmel:logo xlink:type="resource" xlink:href="{metadata/def.logoURL/logoURL[@type='small']}" />
      </xsl:if>
      <xsl:if test="metadata/def.logoURL/logoURL[@type='normal']">
        <urmel:full-logo xlink:type="resource" xlink:href="{metadata/def.logoURL/logoURL[@type='normal']}" />
      </xsl:if>
    </urmel:entity>
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_cbu_')]" priority="1" mode="metsmeta">
    <xsl:if test="./metadata/def.shelfmark/shelfmark">
      <mods:location>
        <mods:shelfLocator>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark[1]" />
        </mods:shelfLocator>


        <mods:physicalLocation>
          <xsl:choose>
            <xsl:when test="./metadata/def.entitylink/entitylink[@type='corporation']">
              <xsl:variable name="owner"
                select="document(concat('mcrobject:',./metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href))/mycoreobject" />

              <xsl:if test="$owner/metadata/def.identifier/identifier[@type='isil']">
                <xsl:attribute name="valueURI">
                  <xsl:value-of
                  select="concat('http://ld.zdb-services.de/resource/organisations/', $owner/metadata/def.identifier/identifier[@type='isil'])" />
                </xsl:attribute>
              </xsl:if>

              <xsl:call-template name="printNamesCorp">
                <xsl:with-param name="unittitleProper" select="$owner/metadata/def.unittitle/unittitle[@type='proper']" />
                <xsl:with-param name="unittitleFormal" select="$owner/metadata/def.unittitle/unittitle[@type='formal']" />
                <xsl:with-param name="firstName" select="$owner/metadata/def.heading/heading/firstName" />
                <xsl:with-param name="lastName" select="$owner/metadata/def.heading/heading/lastName" />
                <xsl:with-param name="personalName" select="$owner/metadata/def.heading/heading/personalName" />
                <xsl:with-param name="collocation" select="$owner/metadata/def.heading/heading/collocation" />
                <xsl:with-param name="nameAffix" select="$owner/metadata/def.heading/heading/nameAffix" />
                <xsl:with-param name="departments" select="$owner/metadata/def.heading/heading/departments" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="archcom:getComponentHumanReadablePath(@ID)" />
            </xsl:otherwise>
          </xsl:choose>
        </mods:physicalLocation>

        <xsl:call-template name="holdingExternal">
          <xsl:with-param name="entityLinkId" select="./metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href" />
        </xsl:call-template>

      </mods:location>
    </xsl:if>

    <xsl:variable name="sourceCatalog">
      <xsl:choose>
        <xsl:when test="../../def.source/source[@type='catalog']">
          <xsl:value-of select="../../def.source/source[@type='catalog']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$ACTUAL.OPAC.CATALOG" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="titleCount" select="./metadata/def.title/title" />
    <xsl:for-each select="./metadata/def.title/title">
      <mods:titleInfo>
        <xsl:attribute name="type">
          <xsl:choose>
            <xsl:when test="@type ='main_title'">
              <xsl:value-of select="'uniform'" />
            </xsl:when>
            <xsl:when test="@type ='title_de'">
              <xsl:value-of select="'translated'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'alternative'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="metadata/def.identifier/identifier[@type='ppn']">
          <xsl:attribute name="authority">
            <xsl:value-of select="'gbv'" />
          </xsl:attribute>
          <xsl:if test="string-length($sourceCatalog)>5">
            <xsl:attribute name="authorityURI">
              <xsl:value-of select="$sourceCatalog" />
            </xsl:attribute>
            <xsl:attribute name="valueURI">
              <xsl:call-template name="generateCatalogURL">
                <xsl:with-param name="catalog" select="$sourceCatalog" />
                <xsl:with-param name="ppn" select="metadata/def.identifier/identifier[@type='ppn']" />
              </xsl:call-template>
            </xsl:attribute>
          </xsl:if>
        </xsl:if>

        <mods:title>
          <!-- remove '@' characters -->
          <xsl:value-of select="translate(.,'@','')" />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>

    <xsl:if
      test="./metadata/def.place/place[@type='printing_original_item'] or ./metadata/def.place/place[@type='pointOfOrigin'] or ./metadata/def.date/date">
      <mods:originInfo>
        <xsl:if test="./metadata/def.place/place[@type='printing_original_item'] or ./metadata/def.place/place[@type='pointOfOrigin']">
          <mods:place>
            <mods:placeTerm>
              <xsl:value-of select="./metadata/def.place/place[@type='printing_original_item']" />
              <xsl:value-of select="./metadata/def.place/place[@type='pointOfOrigin']" />
            </mods:placeTerm>
          </mods:place>
        </xsl:if>
        <xsl:if test="./metadata/def.date/date">
          <mods:dateIssued keyDate="yes">
            <xsl:value-of select="./metadata/def.date/date" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
      <xsl:if test="./metadata/def.extent/extent">
        <mods:extent>
          <xsl:value-of select="./metadata/def.extent/extent" />
        </mods:extent>
      </xsl:if>
      <xsl:if test="./metadata/def.format/format">
        <mods:extent>
          <xsl:value-of select="./metadata/def.format/format" />
        </mods:extent>
      </xsl:if>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <!-- coordinates -->
    <xsl:if test="./metadata/def.coordinates/coordinates">
      <xsl:variable name="north" select="mcrgeo:toDecimalDegrees(./metadata/def.coordinates/coordinates[@type='north'])" />
      <xsl:variable name="south" select="mcrgeo:toDecimalDegrees(./metadata/def.coordinates/coordinates[@type='south'])" />
      <xsl:variable name="west" select="mcrgeo:toDecimalDegrees(./metadata/def.coordinates/coordinates[@type='west'])" />
      <xsl:variable name="east" select="mcrgeo:toDecimalDegrees(./metadata/def.coordinates/coordinates[@type='east'])" />
      <mods:subject>
        <mods:cartographics>
          <mods:coordinates>
            <xsl:value-of select="concat($north, ',', $west, ',', $south, ',', $east)" />
          </mods:coordinates>
        </mods:cartographics>
      </mods:subject>
    </xsl:if>

    <xsl:apply-templates select="metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />

    <xsl:if test="./structure/parents/parent">
      <mods:relatedItem type="host">
        <xsl:variable name="parent" select="document(concat('mcrobject:',./structure/parents/parent/@xlink:href))/mycoreobject" />
        <xsl:apply-templates select="$parent" mode="metsmeta" />
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_certificate_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.shelfmark/shelfmark">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>

    <mods:location>
      <xsl:if test="./metadata/def.shelfmark/shelfmark">
        <mods:shelfLocator>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark" />
        </mods:shelfLocator>
        <mods:physicalLocation>
          <xsl:value-of select="archcom:getComponentHumanReadablePath(@ID)" />
        </mods:physicalLocation>
      </xsl:if>

      <xsl:call-template name="holdingExternal">
        <xsl:with-param name="entityLinkId" select="./metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href" />
      </xsl:call-template>
    </mods:location>

    <xsl:if test="./metadata/def.register/register">
      <mods:titleInfo>
        <xsl:attribute name="type">
           <xsl:value-of select="'uniform'" />
        </xsl:attribute>

        <mods:title>
          <xsl:value-of select="./metadata/def.register/register" />
        </mods:title>
      </mods:titleInfo>
      <xsl:if test="./metadata/def.transcription/transcription">
        <mods:titleInfo type="'translated'">
          <xsl:value-of select="./metadata/def.transcription/transcription" />
        </mods:titleInfo>
      </xsl:if>
    </xsl:if>

    <xsl:if
      test="./metadata/def.component/component or ./metadata/def.date/date or ./metadata/def.datequote/datequote or ./metadata/def.editionStatement/editionStatement">
      <mods:originInfo>
        <xsl:if test="./metadata/def.component/component">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.component/component" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
              <xsl:with-param name="separator" select="'#'" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>

        <xsl:if test="./metadata/def.date/date">
          <mods:dateIssued keyDate="yes">
            <xsl:value-of select="./metadata/def.date/date" />
          </mods:dateIssued>
          <xsl:if test="./metadata/def.datequote/datequote">
            <mods:dateIssued keyDate="no">
              <xsl:value-of select="./metadata/def.datequote/datequote" />
            </mods:dateIssued>
          </xsl:if>

          <xsl:if test="./metadata/def.periode/periode[@type='from']">
            <mods:dateValid encoding="iso8601" point="start">
              <xsl:value-of select="./metadata/def.periode/periode[@type='from']" />
            </mods:dateValid>
          </xsl:if>

          <xsl:if test="./metadata/def.periode/periode[@type='to']">
            <mods:dateValid encoding="iso8601" point="end">
              <xsl:value-of select="./metadata/def.periode/periode[@type='to']" />
            </mods:dateValid>
          </xsl:if>

          <xsl:if test="./metadata/def.editionStatement/editionStatement">
            <mods:edition>
              <xsl:value-of select="./metadata/def.editionStatement/editionStatement" />
            </mods:edition>
          </xsl:if>
          <mods:issuance>
            <xsl:value-of select="'single unit'" />
          </mods:issuance>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <xsl:if test="./metadata/def.writingMaterial/writingMaterial">
        <mods:extent>
          <xsl:value-of select="archcom:getClassificationHumanReadable(@ID,'writingMaterial')" />
        </mods:extent>
      </xsl:if>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note or ./metadata/def.seal/seal">
      <xsl:if test="./metadata/def.note/note">
        <mods:note type="source characteristics">
          <xsl:value-of select="./metadata/def.note/note" />
        </mods:note>
      </xsl:if>
      <xsl:if test="./metadata/def.seal/seal">
        <mods:note type="source characteristics">
          <xsl:value-of select="concat('Siegel:', ./metadata/def.seal/seal)" />
        </mods:note>
      </xsl:if>
    </xsl:if>
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_file_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.unitid/unitid">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.unitid/unitid" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <mods:titleInfo>
      <xsl:attribute name="type">
           <xsl:value-of select="'uniform'" />
        </xsl:attribute>
      <mods:title>
        <xsl:value-of select="./metadata/def.unittitle/unittitle[@type='proper']" />
      </mods:title>
    </mods:titleInfo>

    <xsl:if test="./metadata/def.provenance/provenance or ./metadata/def.unitdate/unitdate or ./metadata/def.component/component">
      <mods:originInfo>
        <xsl:if test="./metadata/def.component/component">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.component/component" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
              <xsl:with-param name="separator" select="'#'" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>

        <xsl:if test="./metadata/def.unitdate/unitdate">
          <mods:dateIssued keyDate="yes" encoding="iso8601">
            <xsl:value-of select="./metadata/def.unitdate/unitdate" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:apply-templates select="./metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_imgitem_')]" priority="1" mode="metsmeta">
    <xsl:comment>
      Start - mycoreobject[contains(@ID,'_imgitem_')] (metsmeta-dfg.xsl)
    </xsl:comment>
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.shelfmark/shelfmark">
          <xsl:attribute name="type">shelfmark</xsl:attribute>
          <xsl:value-of select="./metadata/def.shelfmark/shelfmark[1]" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>
    <xsl:for-each select="./metadata/def.unittitle/unittitle">
      <mods:titleInfo>
        <xsl:attribute name="type">
          <xsl:choose>
            <xsl:when test="@type ='proper'">
              <xsl:value-of select="'uniform'" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'alternative'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <mods:title>
          <xsl:value-of select="." />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>

    <xsl:if test="./metadata/def.provenance/provenance or ./metadata/def.date/date">
      <mods:originInfo>
        <xsl:if test="./metadata/def.provenance/provenance">
          <mods:place>
            <mods:placeTerm type="text">
              <xsl:value-of select="./metadata/def.provenance/provenance" />
            </mods:placeTerm>
          </mods:place>
        </xsl:if>
        <xsl:if test="./metadata/def.date/date">
          <mods:dateIssued keyDate="yes" encoding="iso8601">
            <xsl:value-of select="./metadata/def.date/date" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
      <xsl:if test="./metadata/def.dimensions/dimensions">
        <mods:extent>
          <xsl:value-of select="./metadata/def.dimensions/dimensions" />
        </mods:extent>
      </xsl:if>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.iconography/iconography">
      <mods:abstract>
        <xsl:value-of select="./metadata/def.iconography/iconography" />
      </mods:abstract>
    </xsl:if>
    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:apply-templates select="./metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />

    <xsl:if test="./structure/parents/parent">
      <mods:relatedItem type="host">
        <xsl:variable name="parent" select="document(concat('mcrobject:',./structure/parents/parent/@xlink:href))/mycoreobject" />
        <xsl:apply-templates select="$parent" mode="metsmeta" />
      </mods:relatedItem>
    </xsl:if>
    <xsl:comment>
      End - mycoreobject[contains(@ID,'_imgitem_')] (metsmeta-dfg.xsl)
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_performance_')]" priority="1" mode="metsmeta">
    <mods:identifier type="urn">
      <xsl:choose>
        <xsl:when test="./metadata/def.identifier/identifier[@type='urn']">
          <xsl:attribute name="type">urn</xsl:attribute>
          <xsl:value-of select="./metadata/def.identifier/identifier[@type='urn']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>

    <xsl:if test="./metadata/def.unittitle/unittitle">
      <mods:titleInfo>
        <xsl:attribute name="type">
           <xsl:value-of select="'uniform'" />
        </xsl:attribute>
        <mods:title>
          <xsl:value-of select="./metadata/def.unittitle/unittitle" />
        </mods:title>
      </mods:titleInfo>
    </xsl:if>

    <!-- the place where the performance was performed -->
    <xsl:if test="./metadata/def.site/site or ./metadata/def.date/date">
      <mods:originInfo>
        <xsl:if test="./metadata/def.site/site">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.site/site" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>
        <xsl:if test="./metadata/def.date/date">
          <mods:dateIssued keyDate="yes" encoding="iso8601">
            <xsl:value-of select="./metadata/def.date/date" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:if test="./metadata/def.bibliography/bibliography">
      <mods:note type="bibliography">
        <xsl:value-of select="./metadata/def.bibliography/bibliography" />
      </mods:note>
    </xsl:if>

    <xsl:if test="./metadata/def.footnote/footnote">
      <mods:note type="content">
        <xsl:value-of select="./metadata/def.footnote/footnote" />
      </mods:note>
    </xsl:if>

    <xsl:if test="./metadata/def.review/review">
      <mods:note type="publications">
        <xsl:value-of select="./metadata/def.review/review" />
      </mods:note>
    </xsl:if>


    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <xsl:apply-templates select="./metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />

    <xsl:if test="./structure/parents/parent">
      <mods:relatedItem type="host">
        <xsl:variable name="parent" select="document(concat('mcrobject:',./structure/parents/parent/@xlink:href))/mycoreobject" />
        <xsl:apply-templates select="$parent" mode="metsmeta" />
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>


  <!-- ARCHIVESOURCE -->
  <xsl:template match="mycoreobject[contains(@ID,'_archivesource_')]" priority="1" mode="metsmeta">

    <!-- the performance associated with the archivesource -->
    <xsl:variable name="performanceMeta" select="document(concat('mcrobject:', MCRArchiveUtils:getPerformance(@ID)))/mycoreobject" />

    <xsl:if test="$performanceMeta/metadata/def.unittitle/unittitle">
      <mods:titleInfo type="uniform">
        <mods:title>
          <xsl:value-of select="$performanceMeta/metadata/def.unittitle/unittitle[@type='source']" />
        </mods:title>
      </mods:titleInfo>
    </xsl:if>

    <!-- identifier is the unitid -->
    <xsl:if test="./metadata/def.unitid/unitid">
      <mods:identifier>
        <xsl:attribute name="type">unitid</xsl:attribute>
        <xsl:value-of select="./metadata/def.unitid/unitid[1]" />
      </mods:identifier>
    </xsl:if>

    <!-- the place is the resolved label of the component -->
    <xsl:if test="./metadata/def.component/component">
      <mods:location>
        <xsl:variable name="nodes" select="./metadata/def.component/component" />
        <xsl:variable name="compFull">
          <xsl:call-template name="resolveLabel">
            <xsl:with-param name="nodes" select="$nodes" />
            <xsl:with-param name="separator" select="'#'" />
          </xsl:call-template>
        </xsl:variable>

        <mods:physicalLocation>
          <xsl:value-of select="substring-before($compFull, '#')" />
        </mods:physicalLocation>

      </mods:location>
    </xsl:if>

    <xsl:if test="./metadata/def.page/page">
      <mods:physicalDescription>
        <mods:extent>
          <xsl:value-of select="./metadata/def.page/page" />
        </mods:extent>
      </mods:physicalDescription>
    </xsl:if>

    <!-- <xsl:apply-templates select="." mode="originInfo" /> -->
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>

    <xsl:if test="./metadata/def.type/type">
      <mods:genre>
        <xsl:variable name="nodes" select="./metadata/def.type/type" />
        <xsl:call-template name="resolveLabel">
          <xsl:with-param name="nodes" select="$nodes" />
        </xsl:call-template>
      </mods:genre>
    </xsl:if>


    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:if test="./metadata/def.bibliography/bibliography">
      <mods:note type="bibliography">
        <xsl:value-of select="./metadata/def.bibliography/bibliography" />
      </mods:note>
    </xsl:if>

    <xsl:if test="./metadata/def.footnote/footnote">
      <mods:note type="content">
        <xsl:value-of select="./metadata/def.footnote/footnote" />
      </mods:note>
    </xsl:if>

    <!-- the mods for the linked performance -->
    <xsl:variable name="modsPerformance">
      <xsl:apply-templates select="$performanceMeta" mode="metsmeta" />
    </xsl:variable>

    <xsl:variable name="modsPerformanceNodeSet" select="xalan:nodeset($modsPerformance)" />
    <xsl:copy-of select="$modsPerformanceNodeSet/mods:note" />
    <xsl:copy-of select="$modsPerformanceNodeSet/mods:name" />
    <xsl:copy-of select="$modsPerformanceNodeSet/mods:originInfo[mods:dateIssued]" />

    <xsl:apply-templates select="." mode="recordIdentifier" />
    <xsl:apply-templates select="." mode="originInfo" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_movie_')]" priority="1" mode="metsmeta">
    <!-- title -->
    <xsl:if test="./metadata/def.title/title">
      <mods:titleInfo type="uniform">
        <mods:title>
          <xsl:value-of select="./metadata/def.title/title" />
        </mods:title>
      </mods:titleInfo>
    </xsl:if>

    <mods:typeOfRessource>
      <xsl:value-of select="'moving image'" />
    </mods:typeOfRessource>
    <!-- provenance and yearOfProducction -->
    <xsl:if test="./metadata/def.provenance/provenance or ./metadata/def.yearOfProduction/yearOfProduction">
      <mods:originInfo>
        <xsl:if test="./metadata/def.provenance/provenance">
          <mods:place>
            <xsl:value-of select="./metadata/def.provenance/provenance" />
          </mods:place>
        </xsl:if>
        <xsl:if test="./metadata/def.yearOfProduction/yearOfProduction">
          <mods:dateCaptured>
            <xsl:value-of select="./metadata/def.yearOfProduction/yearOfProduction" />
          </mods:dateCaptured>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <!-- Nodes -->
    <mods:language>
      <xsl:variable name="nodes" select="./metadata/def.language/language" />
      <xsl:call-template name="resolveLabel">
        <xsl:with-param name="nodes" select="$nodes" />
      </xsl:call-template>
    </mods:language>
    <!-- Physical description -->
    <xsl:if
      test="./metadata/def.format/format | ./metadata/def.color/color | ./metadata/def.sound/sound | ./metadata/def.plot/plot | ./metadata/def.note/note">
      <mods:physicalDescription>
        <xsl:if test="./metadata/def.format/format">
          <mods:form type="format">
            <xsl:variable name="nodes" select="./metadata/def.format/format" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:form>
        </xsl:if>
        <xsl:if test="./metadata/def.color/color">
          <mods:form type="coloring">
            <xsl:variable name="nodes" select="./metadata/def.color/color" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:form>
        </xsl:if>
        <xsl:if test="./metadata/def.sound/sound">
          <mods:form type="sound">
            <xsl:variable name="nodes" select="./metadata/def.sound/sound" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:form>
        </xsl:if>
        <xsl:if test="./metadata/def.plot/plot">
          <mods:note type="content">
            <xsl:value-of select="./metadata/def.plot/plot" />
          </mods:note>
        </xsl:if>

        <xsl:if test="./metadata/def.note/note">
          <mods:note type="source characteristics">
            <xsl:value-of select="./metadata/def.note/note" />
          </mods:note>
        </xsl:if>

      </mods:physicalDescription>
    </xsl:if>
    <!-- Genre -->
    <xsl:if test="./metadata/def.genre/genre">
      <mods:subject>
        <mods:genre>
          <xsl:variable name="nodes" select="./metadata/def.genre/genre" />
          <xsl:call-template name="resolveLabel">
            <xsl:with-param name="nodes" select="$nodes" />
          </xsl:call-template>
        </mods:genre>
      </mods:subject>
    </xsl:if>
  </xsl:template>


  <xsl:template match="mycoreobject[contains(@ID,'_docitem_')]" priority="1" mode="metsmeta">

    <xsl:for-each select="./metadata/def.unittitle/unittitle">
      <mods:titleInfo>
        <xsl:if test="@type = 'formal'">
          <xsl:attribute name="type">
					<xsl:value-of select="'alternative'" />
				</xsl:attribute>
        </xsl:if>
        <mods:title>
          <xsl:value-of select="." />
        </mods:title>
      </mods:titleInfo>
    </xsl:for-each>

    <mods:typeOfRessource>
      <xsl:value-of select="'text'" />
    </mods:typeOfRessource>

    <!-- Names -->
    <xsl:for-each select="./metadata/def.entity/entity">
      <mods:name type="personal">
        <mods:namePart>
          <xsl:value-of select="." />
        </mods:namePart>
        <mods:role>
          <mods:roleTerm type="text">
            <xsl:value-of select="@type" />
          </mods:roleTerm>
        </mods:role>
      </mods:name>
    </xsl:for-each>

    <xsl:if test="./metadata/def.date/date or ./metadata/def.component/component">
      <mods:originInfo>
        <xsl:if test="./metadata/def.date/date">
          <mods:dateCreated>
            <xsl:value-of select="./metadata/def.date/date" />
          </mods:dateCreated>
        </xsl:if>
        <xsl:if test="./metadata/def.component/component">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.component/component" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <xsl:if test="./metadata/def.language/language">
      <mods:language>
        <xsl:variable name="nodes" select="./metadata/def.language/language" />
        <xsl:call-template name="resolveLabel">
          <xsl:with-param name="nodes" select="$nodes" />
        </xsl:call-template>
      </mods:language>
    </xsl:if>

    <xsl:if test="./metadata/def.note/note | ./metadata/def.form/form">
      <mods:physicalDescription>
        <xsl:if test="./metadata/def.note/note">
          <mods:note type="source characteristics">
            <xsl:value-of select="./metadata/def.note/note" />
          </mods:note>
        </xsl:if>
        <xsl:if test="./metadata/def.form/form">
          <mods:form>
            <xsl:variable name="nodes" select="./metadata/def.form/form" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
            </xsl:call-template>
          </mods:form>
        </xsl:if>
      </mods:physicalDescription>
    </xsl:if>

    <xsl:if test="./structure/parents/parent">
      <mods:relatedItem type="host">
        <xsl:variable name="parent" select="document(concat('mcrobject:',./structure/parents/parent/@xlink:href))/mycoreobject" />
        <xsl:apply-templates select="$parent" mode="metsmeta" />
      </mods:relatedItem>
    </xsl:if>

  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_visitation_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.unitid/unitid">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.unitid/unitid" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>

    <mods:titleInfo>
      <xsl:attribute name="type">
           <xsl:value-of select="'uniform'" />
        </xsl:attribute>
      <mods:title>
        <xsl:value-of select="./metadata/def.title/title[@type='visitation']" />
        <xsl:if test="./metadata/def.leafSuccession/leafSuccession">
          <xsl:value-of select="concat(' (Seitenfolge: ', ./metadata/def.leafSuccession/leafSuccession, ')')" />
        </xsl:if>
      </mods:title>
    </mods:titleInfo>

    <xsl:if
      test="./metadata/def.provenance/provenance or ./metadata/def.unitdate/unitdate or ./metadata/def.component/component or ./metadata/def.date/date">
      <mods:originInfo>
        <xsl:if test="./metadata/def.component/component">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.component/component" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
              <xsl:with-param name="separator" select="'#'" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>

        <xsl:if test="./metadata/def.date/date/von">
          <mods:dateIssued keyDate="yes" encoding="iso8601">
            <xsl:value-of select="substring(./metadata/def.date/date/von, 0, 5)" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:if test="./metadata/def.country/country">
      <mods:subject>
        <xsl:for-each select="./metadata/def.country/country">
          <mods:geographic xml:lang="de" valueURI="{concat($WebApplicationBaseURL, 'rsc/classifications/export/visitationcountry')}">
            <xsl:value-of select="mcrxml:getDisplayName('visitationcountry', @categid)" />
          </mods:geographic>
        </xsl:for-each>
      </mods:subject>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:apply-templates select="./metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_showcase_')]" priority="1" mode="metsmeta">
    <mods:identifier>
      <xsl:choose>
        <xsl:when test="./metadata/def.unitid/unitid">
          <xsl:attribute name="type">unitid</xsl:attribute>
          <xsl:value-of select="./metadata/def.unitid/unitid" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">mcrid</xsl:attribute>
          <xsl:value-of select="@ID" />
        </xsl:otherwise>
      </xsl:choose>
    </mods:identifier>

    <mods:titleInfo>
      <xsl:attribute name="type">
           <xsl:value-of select="'uniform'" />
        </xsl:attribute>
      <mods:title>
        <xsl:value-of select="./metadata/def.title/title" />
        <xsl:if test="./metadata/def.leaf/leaf">
          <xsl:value-of select="concat(' (Seiten: ', ./metadata/def.leaf/leaf, ')')" />
        </xsl:if>
      </mods:title>
    </mods:titleInfo>

    <xsl:if
      test="./metadata/def.provenance/provenance or ./metadata/def.unitdate/unitdate or ./metadata/def.component/component or ./metadata/def.date/date">
      <mods:originInfo>
        <xsl:if test="./metadata/def.component/component">
          <mods:place>
            <xsl:variable name="nodes" select="./metadata/def.component/component" />
            <xsl:call-template name="resolveLabel">
              <xsl:with-param name="nodes" select="$nodes" />
              <xsl:with-param name="separator" select="'#'" />
            </xsl:call-template>
          </mods:place>
        </xsl:if>

        <xsl:if test="./metadata/def.date/date/von">
          <mods:dateIssued keyDate="yes" encoding="iso8601">
            <xsl:value-of select="substring(./metadata/def.date/date/von, 0, 5)" />
          </mods:dateIssued>
        </xsl:if>
      </mods:originInfo>
    </xsl:if>

    <xsl:if test="./metadata/def.country/country or ./metadata/def.place/place[@type='significant']">
      <mods:subject>
        <xsl:for-each select="./metadata/def.country/country">
          <mods:geographic xml:lang="de" valueURI="{concat($WebApplicationBaseURL, 'rsc/classifications/export/visitationcountry')}">
            <xsl:value-of select="mcrxml:getDisplayName('visitationcountry', @categid)" />
          </mods:geographic>
        </xsl:for-each>

        <xsl:for-each select="./metadata/def.place/place[@type='significant']">
          <mods:geographic xml:lang="de" authorityURI="http://d-nb.info/gnd/" valueURI="{concat('http://d-nb.info/gnd/', . , '/about/rdf')}">
            <xsl:variable name="baseURL" select="concat('http://d-nb.info/gnd/', . , '/about/')" />
            <xsl:variable name="rdf" select="document(concat($baseURL, 'rdf'))" />
            <xsl:value-of select="$rdf/rdf:RDF/rdf:Description/gndo:preferredNameForThePlaceOrGeographicName[1]" />
          </mods:geographic>
        </xsl:for-each>
      </mods:subject>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.note/note">
      <mods:note type="source characteristics">
        <xsl:value-of select="./metadata/def.note/note" />
      </mods:note>
    </xsl:if>

    <xsl:apply-templates select="./metadata/def.entitylink/entitylink" mode="personal" />
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="mycoreobject" mode="recordIdentifier">
    <mods:location>
      <mods:url access="object in context">
        <xsl:choose>
          <xsl:when test="contains(@ID, '_archivesource_')">
            <xsl:value-of select="concat('http://theaterzettel-weimar.de/item/', @ID)" />
          </xsl:when>

          <xsl:when
            test="(contains(@ID, '_file_') and structure/children/child[contains(@xlink:href, '_visitation_')]) or  contains(@ID, '_visitation_')  or contains(@ID, '_showcase_') or contains(@ID, '_page_')">
            <xsl:value-of select="concat('http://www.reformationsportal.de/item/', @ID)" />
          </xsl:when>

          <xsl:otherwise>
            <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',@ID)" />
          </xsl:otherwise>
        </xsl:choose>
      </mods:url>

      <xsl:if test="contains(@ID, '_archivesource_')">
        <mods:physicalLocation>
          <xsl:value-of select="archcom:getComponentHumanReadablePath(@ID)" />
        </mods:physicalLocation>
      </xsl:if>
    </mods:location>

    <mods:identifier>
      <xsl:attribute name="type">uri</xsl:attribute>
      <xsl:value-of select="concat($WebApplicationBaseURL,'receive/',@ID)" />
    </mods:identifier>

    <mods:identifier>
      <xsl:attribute name="type">mcrid</xsl:attribute>
      <xsl:value-of select="@ID" />
    </mods:identifier>

    <xsl:variable name="derivateId" select="structure/derobjects/derobject[1]/@xlink:href" />

    <xsl:if test="urn:hasURNDefined($derivateId)">
      <xsl:variable name="derivate" select="document(concat('mcrobject:', $derivateId))/mycorederivate" />
      <mods:identifier>
        <xsl:attribute name="type">urn</xsl:attribute>
        <xsl:value-of select="$derivate/derivate/fileset/file[1]/urn" />
      </mods:identifier>
    </xsl:if>

    <xsl:for-each select="./metadata/def.identifier/identifier">
      <mods:identifier>
        <xsl:attribute name="type">
          <xsl:value-of select="@type" />
        </xsl:attribute>
        <xsl:value-of select="." />
      </mods:identifier>
    </xsl:for-each>

    <mods:recordInfo>
      <mods:languageOfCataloging usage="primary">
        <mods:languageTerm type="code" authority="iso639-2b">ger</mods:languageTerm>
      </mods:languageOfCataloging>

      <xsl:variable name="sourceAttrValue">
        <xsl:choose>
          <xsl:when test="./metadata/def.entitylink/entitylink[@type='corporation']">
            <xsl:variable name="owner"
              select="document(concat('mcrobject:',./metadata/def.entitylink/entitylink[@type='corporation']/@xlink:href))/mycoreobject" />
            <xsl:call-template name="printNamesCorp">
              <xsl:with-param name="unittitleProper" select="$owner/metadata/def.unittitle/unittitle[@type='proper']" />
              <xsl:with-param name="unittitleFormal" select="$owner/metadata/def.unittitle/unittitle[@type='formal']" />
              <xsl:with-param name="firstName" select="$owner/metadata/def.heading/heading/firstName" />
              <xsl:with-param name="lastName" select="$owner/metadata/def.heading/heading/lastName" />
              <xsl:with-param name="personalName" select="$owner/metadata/def.heading/heading/personalName" />
              <xsl:with-param name="collocation" select="$owner/metadata/def.heading/heading/collocation" />
              <xsl:with-param name="nameAffix" select="$owner/metadata/def.heading/heading/nameAffix" />
              <xsl:with-param name="departments" select="$owner/metadata/def.heading/heading/departments" />
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'DE-27 (Thüringer Universitäts- und Landesbibliothek)'" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <mods:recordIdentifier source="{$sourceAttrValue}">
        <xsl:value-of select="@ID" />
      </mods:recordIdentifier>
      <mods:recordChangeDate>
        <xsl:value-of select="./service/servdates/servdate[@type='modifydate']" />
      </mods:recordChangeDate>
      <mods:recordCreationDate>
        <xsl:value-of select="./service/servdates/servdate[@type='createdate']" />
      </mods:recordCreationDate>
    </mods:recordInfo>
    <xsl:if test="parents/parent/@xlink:href">
      <mods:relatedItem type="host">
        <mods:recordInfo>
          <mods:recordIdentifier type="mcrid">
            <xsl:copy-of select="parents/parent/@xlink:href" />
          </mods:recordIdentifier>
        </mods:recordInfo>
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <xsl:template match="entitylink" mode="personal">
    <xsl:variable name="sourceCatalog">
      <xsl:choose>
        <xsl:when test="../../def.source/source[@type='catalog']">
          <xsl:value-of select="../../def.source/source[@type='catalog']" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$ACTUAL.OPAC.CATALOG" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <mods:name>

      <xsl:attribute name="type">
        <xsl:choose>
        <xsl:when test="contains(@xlink:href,'_corporation_')">
          <xsl:value-of select="'corporate'" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'personal'" />
        </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:variable name="personObj" select="document(concat('mcrobject:',@xlink:href))" />

      <xsl:choose>

        <xsl:when test="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='gnd']">
          <xsl:attribute name="authority">
            <xsl:value-of select="'gnd'" />
          </xsl:attribute>
          <xsl:attribute name="authorityURI">
            <xsl:value-of select="'http://d-nb.info/gnd/'" />
          </xsl:attribute>
          <xsl:attribute name="valueURI">
            <xsl:value-of select="concat('http://d-nb.info/gnd/',$personObj/mycoreobject/metadata/def.identifier/identifier[@type='gnd'])" />
          </xsl:attribute>
        </xsl:when>

        <xsl:when test="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='pnd']">
          <xsl:attribute name="authority">
            <xsl:value-of select="'pnd'" />
          </xsl:attribute>
          <xsl:attribute name="authorityURI">
            <xsl:value-of select="'http://d-nb.info/gnd/'" />
          </xsl:attribute>
          <xsl:attribute name="valueURI">
            <xsl:value-of select="concat('http://d-nb.info/gnd/',$personObj/mycoreobject/metadata/def.identifier/identifier[@type='pnd'])" />
          </xsl:attribute>
        </xsl:when>

        <xsl:when test="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='ppn']">
          <xsl:attribute name="authority">
            <xsl:value-of select="'gvk-ppn'" />
          </xsl:attribute>
          <xsl:if test="string-length($sourceCatalog)>5">
            <xsl:attribute name="authorityURI">
              <xsl:value-of select="$sourceCatalog" />
            </xsl:attribute>
            <xsl:attribute name="valueURI">
              <xsl:call-template name="generateCatalogURL">
                <xsl:with-param name="catalog" select="$sourceCatalog" />
                <xsl:with-param name="ppn" select="$personObj/mycoreobject/metadata/def.identifier/identifier[@type='ppn']" />
              </xsl:call-template>
            </xsl:attribute>
          </xsl:if>
        </xsl:when>

      </xsl:choose>

      <xsl:if test="@type">
        <mods:role>
          <mods:roleTerm type="code" authority="marcrelator">
            <xsl:apply-templates select="@type" mode="marcrelator" />
          </mods:roleTerm>
        </mods:role>
      </xsl:if>
      <xsl:variable name="personalName" select="$personObj/mycoreobject/metadata/def.heading/heading/personalName" />
      <xsl:variable name="collocation" select="$personObj/mycoreobject/metadata/def.heading/heading/collocation" />
      <xsl:choose>
        <xsl:when test="$personalName">
          <mods:namePart type="given">
            <xsl:value-of select="$personalName" />
          </mods:namePart>

          <xsl:if test="$collocation">
            <xsl:choose>

              <xsl:when test="contains($personObj/mycoreobject/@ID, '_corporation_')">
                <mods:namePart type="affiliation">
                  <xsl:value-of select="$collocation" />
                </mods:namePart>
              </xsl:when>
              <xsl:otherwise>
                <mods:namePart type="termsOfAddress">
                  <xsl:value-of select="$collocation" />
                </mods:namePart>
              </xsl:otherwise>
            </xsl:choose>

            <mods:displayForm>
              <xsl:variable name="collocationDisplay">
                <xsl:choose>
                  <xsl:when test="$collocation">
                    <xsl:value-of select="concat('&lt;', $collocation, '&gt;')" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="''" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

              <xsl:value-of select="concat($personalName, ' ', $collocationDisplay)" />
            </mods:displayForm>
          </xsl:if>
        </xsl:when>

        <xsl:when test="$personObj/mycoreobject/metadata/def.unittitle/unittitle">
          <mods:displayForm>
            <xsl:value-of select="$personObj/mycoreobject/metadata/def.unittitle/unittitle" />
          </mods:displayForm>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="lastName" select="$personObj/mycoreobject/metadata/def.heading/heading/lastName" />
          <xsl:variable name="firstName" select="$personObj/mycoreobject/metadata/def.heading/heading/firstName" />
          <xsl:variable name="nameAffix" select="$personObj/mycoreobject/metadata/def.heading/heading/nameAffix" />
          <xsl:if test="$lastName">
            <mods:namePart type="family">
              <xsl:value-of select="$lastName" />
            </mods:namePart>
          </xsl:if>
          <xsl:if test="$firstName">
            <mods:namePart type="given">
              <xsl:value-of select="$firstName" />
            </mods:namePart>
          </xsl:if>
          <mods:displayForm>
            <xsl:value-of select="concat($lastName,', ',$firstName,' ', $nameAffix)" />
          </mods:displayForm>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:variable name="dateOfBirth" select="$personObj/mycoreobject/metadata/def.dateOfBirth/dateOfBirth" />
      <xsl:variable name="dateOfDeath" select="$personObj/mycoreobject/metadata/def.dateOfDeath/dateOfDeath" />
      <xsl:if test="$dateOfBirth | $dateOfDeath">
        <mods:namePart type="date">
          <xsl:value-of select="concat($dateOfBirth,' - ',$dateOfDeath)" />
        </mods:namePart>
      </xsl:if>
    </mods:name>
  </xsl:template>

  <xsl:template match="mycoreobject[contains(@ID,'_digicult_')]" priority="1" mode="metsmeta">
    <mods:titleInfo>
      <xsl:attribute name="type">
        <xsl:value-of select="'uniform'" />
      </xsl:attribute>
      <mods:title>
        <xsl:choose>
          <xsl:when test="./metadata/def.title/title">
            <xsl:value-of select="./metadata/def.title/title" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="./metadata/def.subject/subject" />
          </xsl:otherwise>
        </xsl:choose>
      </mods:title>
    </mods:titleInfo>

    <xsl:if test="./metadata/def.date/date">
      <mods:originInfo>
        <mods:dateIssued keyDate="yes" encoding="iso8601">
          <xsl:value-of select="./metadata/def.date/date" />
        </mods:dateIssued>
      </mods:originInfo>
    </xsl:if>

    <xsl:apply-templates select="." mode="originInfo" />
    <xsl:apply-templates select="." mode="language" />

    <mods:physicalDescription>
      <mods:digitalOrigin>reformatted digital</mods:digitalOrigin>
    </mods:physicalDescription>
    <xsl:if test="./metadata/def.description/description">
      <mods:note type="content">
        <xsl:value-of select="./metadata/def.description/description" />
      </mods:note>
    </xsl:if>
    <xsl:apply-templates select="." mode="recordIdentifier" />
  </xsl:template>

  <xsl:template match="entitylink/@type" mode="marcrelator">
    <!-- http://www.loc.gov/marc/relators/relacode.html -->
    <xsl:choose>
      <xsl:when test=". = 'pictured'">
        <xsl:value-of select="'dpc'" />
      </xsl:when>
      <xsl:when test=". = 'illustrator'">
        <xsl:value-of select="'ill'" />
      </xsl:when>
      <xsl:when test=". = 'author'">
        <xsl:value-of select="'aut'" />
      </xsl:when>
      <xsl:when test=". = 'printer'">
        <xsl:value-of select="'prt'" />
      </xsl:when>
      <xsl:when test=". = 'other'">
        <xsl:value-of select="'oth'" />
      </xsl:when>
      <xsl:when test=". = 'employer'">
        <!-- Auftraggeber -> Applicant -->
        <xsl:value-of select="'app'" />
      </xsl:when>
      <xsl:when test=". = 'person_charge'">
        <!-- Bearbeiter -> Editor -->
        <xsl:value-of select="'edt'" />
      </xsl:when>
      <xsl:when test=". = 'owner'">
        <!-- Besitzer -> owner -->
        <xsl:value-of select="'own'" />
      </xsl:when>
      <xsl:when test=". = 'previous_owner'">
        <!-- Besitzer -> former owner -->
        <xsl:value-of select="'fmo'" />
      </xsl:when>
      <xsl:when test=". = 'recipient'">
        <!-- Empfänger -> recipient -->
        <xsl:value-of select="'rcp'" />
      </xsl:when>
      <xsl:when test=". = 'artist'">
        <!-- Künstler -> artist -->
        <xsl:value-of select="'art'" />
      </xsl:when>
      <xsl:when test=". = 'writer'">
        <!-- Schreiber -> inscriber -->
        <xsl:value-of select="'ins'" />
      </xsl:when>
      <xsl:when test=". = 'translator'">
        <!-- Übersetzer -> translator -->
        <xsl:value-of select="'trl'" />
      </xsl:when>
      <xsl:when test=". = 'corporation'">
        <!-- Institution -> owner -->
        <xsl:value-of select="'own'" />
      </xsl:when>
      <xsl:when test=". = 'previous_organisation'">
        <!-- Vorbesitzende Institution -> former owner -->
        <xsl:value-of select="'fmo'" />
      </xsl:when>
      <xsl:when test=". = 'patron'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'pat'" />
      </xsl:when>

      <xsl:when test=". = 'editor'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'edt'" />
      </xsl:when>

      <xsl:when test=". = 'conductor'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'cnd'" />
      </xsl:when>

      <xsl:when test=". = 'director'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'drt'" />
      </xsl:when>

      <xsl:when test=". = 'choreographer'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'chr'" />
      </xsl:when>

      <xsl:when test=". = 'other'">
        <!-- Förderer -> parton -->
        <xsl:value-of select="'oth'" />
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select="'asn'" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="generateCatalogURL">
    <xsl:param name="catalog" select="$ACTUAL.OPAC.CATALOG" />
    <xsl:param name="ppn" />
    <xsl:if test="string-length($catalog) &gt; 5">
      <xsl:value-of select="concat($catalog,'PPN?PPN=',$ppn)" />
    </xsl:if>
  </xsl:template>

  <xsl:template name="resolveLabel">
    <xsl:param name="nodes" />
    <xsl:param name="separator" select="'&#x00BB;'" />

    <xsl:for-each select="$nodes">
      <!-- for the moment only local classification supported -->
      <xsl:variable name="classLink" select="concat('classification:metadata:0:parents:',@classid,':',@categid)" />
      <xsl:variable name="classInfo" select="document($classLink)" />
      <xsl:for-each select="$classInfo/descendant::category">
        <xsl:choose>
          <xsl:when test="label/@description">
            <xsl:value-of select="label/@description" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="label/@text" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="position() != last()">
          <xsl:value-of select="$separator" />
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="holdingExternal">
    <xsl:param name="entityLinkId" />

    <!-- use gnd given by entity or use gnd of Jena by default -->
    <xsl:variable name="placeByGND">
      <xsl:choose>
        <xsl:when test="$entityLinkId">
          <xsl:variable name="owner" select="document(concat('mcrobject:',$entityLinkId))/mycoreobject" />
          <xsl:choose>
            <xsl:when test="$owner/metadata/def.place/place[@type='gnd']">
              <xsl:value-of select="$owner/metadata/def.place/place[@type='gnd']" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'4028557-1'" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'4028557-1'" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="placeData" select="document(concat('http://d-nb.info/gnd/', $placeByGND , '/about/rdf'))" />
    <xsl:variable name="coordinates" select="$placeData/rdf:RDF/rdf:Description/geo:hasGeometry/geo:asWKT" />
    <xsl:variable name="lat" select="substring-before(substring-after($coordinates, 'Point ( '), ' ')" />
    <xsl:variable name="long" select="substring-before(substring-after(substring-after($coordinates, 'Point ( '), ' '), ' )')" />

    <mods:holdingExternal xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:ns3="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#">
      <edm:currentLocation ns3:resource="{concat('http://d-nb.info/gnd/', $placeByGND, '/about/')}">
        <xsl:value-of select="$placeData/rdf:RDF/rdf:Description/gndo:preferredNameForThePlaceOrGeographicName[1]" />
      </edm:currentLocation>
      <wgs84_pos:lat>
        <xsl:value-of select="$lat" />
      </wgs84_pos:lat>
      <wgs84_pos:long>
        <xsl:value-of select="$long" />
      </wgs84_pos:long>
    </mods:holdingExternal>

  </xsl:template>

</xsl:stylesheet>