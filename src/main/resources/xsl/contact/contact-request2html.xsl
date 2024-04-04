<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="yes"/>
  
  <xsl:param name="CurrentLang"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <!-- TODO i18n -->
  <xsl:template match="ContactStatus">
	<div>
      <div>
	    <xsl:value-of select="concat('Die Anfrage ist aktuell: ', status/text(), '.')" />
	  </div>
	  <xsl:if test="status/text() = 'forwarded'">
	    <div>An folgende Entitäten wurde die Anfrage bereits weitergeleitet:</div>
	    <ul>
		  <xsl:for-each select="emails">
		    <li>
		      <xsl:value-of select="./text()"></xsl:value-of>
		    </li>
		  </xsl:for-each>
        </ul>	    
	  </xsl:if>
	</div>
  </xsl:template>

</xsl:stylesheet>
