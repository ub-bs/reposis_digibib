<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion"
    exclude-result-prefixes="mcrver">

  <xsl:import href="resource:xsl/layout/mir-common-layout.xsl" />
  <xsl:template name="mir.navigation">

    <div id="header_box" class="clearfix container">
      <div id="options_nav_box" class="mir-prop-nav">

        <!-- do not show on startpage -->
        <xsl:if test="not(//div/@class='jumbotwo')">
          <div class="searchfield_box">
            <form action="{$WebApplicationBaseURL}servlets/solr/find?q={0}" class="navbar-form navbar-left pull-right" role="search">
              <button type="submit" class="btn btn-primary"><i class="fa fa-search"></i></button>
              <div class="form-group">
                <input name="q" placeholder="Suche" class="form-control search-query" id="searchInput" type="text" />
              </div>
            </form>
          </div>
        </xsl:if>

        <nav>
          <ul class="nav navbar-nav pull-right">
            <xsl:call-template name="mir.loginMenu" />
          </ul>
        </nav>
        <a href="http://www.biblio.tu-bs.de/" id="bs-bibliothek">Universitätsbibliothek&#160;Braunschweig</a>
      </div>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="navbar navbar-default mir-main-nav">
      <div class="container">

        <div class="navbar-header">
          <button class="navbar-toggle" type="button" data-toggle="collapse" data-target=".mir-main-nav-entries">
            <span class="sr-only"> Toggle navigation </span>
            <span class="icon-bar">
            </span>
            <span class="icon-bar">
            </span>
            <span class="icon-bar">
            </span>
          </button>
          <div id="project_logo_box">
            <a class="navbar-brand" href="http://www.tu-braunschweig.de" title="zur Startseite der TU Braunschweig"><img src="{$WebApplicationBaseURL}images/siegel_rot.jpg" alt="Technische Universität Braunschweig" /></a>
          </div>
        </div>

        <nav class="collapse navbar-collapse mir-main-nav-entries">
          <ul class="nav navbar-nav pull-left">
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='brand']/*" />
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='search']" />
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='collections']" />
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='publish']" />
            <xsl:call-template name="mir.basketMenu" />
          </ul>
        </nav>

      </div><!-- /container -->
    </div>
  </xsl:template>

  <xsl:template name="mir.jumbotwo">
    <!-- do nothing special -->
  </xsl:template>

  <xsl:template name="mir.footer">
    <div class="container">
      <div class="row">
        <div class="col-md-12">
          <ul class="internal_links nav navbar-nav">
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='below']/*" />
          </ul>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="mir.powered_by">
    <xsl:variable name="mcr_version" select="concat('MyCoRe ',mcrver:getCompleteVersion())" />
    <div id="powered_by">
      <a href="http://www.mycore.de">
        <img src="{$WebApplicationBaseURL}mir-layout/images/mycore_logo_powered_120x30_blaue_schrift_frei.png" title="{$mcr_version}" alt="powered by MyCoRe" />
      </a>
    </div>
  </xsl:template>

</xsl:stylesheet>