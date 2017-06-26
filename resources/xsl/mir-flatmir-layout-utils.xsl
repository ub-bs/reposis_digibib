<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion"
    exclude-result-prefixes="mcrver">

  <xsl:import href="resource:xsl/layout/mir-common-layout.xsl" />
  <xsl:param name="piwikID" select="'0'" />
  <xsl:param name="DIGIBIB.StartDate" select="'20161101'" />

  <xsl:template name="mir.navigation">

    <div id="header_box" class="clearfix container">
      <div id="options_nav_box" class="mir-prop-nav">

        <!-- do not show on startpage -->
        <xsl:if test="not(//div/@class='jumbotwo')">
          <div class="searchfield_box">
            <form action="{$WebApplicationBaseURL}servlets/solr/find?q={0}" class="navbar-form navbar-left pull-right" role="search">
              <div class="form-group">
                <input name="q" placeholder="Suche" class="form-control search-query" id="searchInput" type="text" />
              </div>
              <button type="submit" class="btn btn-primary"><i class="fa fa-search"></i></button>
            </form>
          </div>
        </xsl:if>

        <nav>
          <ul class="nav navbar-nav navbar-right">
            <xsl:call-template name="mir.loginMenu" />
            <xsl:call-template name="mir.languageMenu" />
          </ul>
        </nav>
        <br />
        <a href="https://ub.tu-braunschweig.de/" id="bs-bibliothek">Universitätsbibliothek Braunschweig</a>
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
            <a class="navbar-brand hidden-xs" href="http://www.tu-braunschweig.de" title="zur Startseite der TU Braunschweig"><img src="{$WebApplicationBaseURL}images/siegel_rot.png" alt="Technische Universität Braunschweig" /></a>
            <!-- a class="navbar-brand visible-xs-block" href="http://www.tu-braunschweig.de" title="zur Startseite der TU Braunschweig"><img src="{$WebApplicationBaseURL}images/siegel_rot_small.png" alt="Technische Universität Braunschweig" /></a -->
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

        <div id="digibib_feedback">
          <a href="mailto:digibib@tu-braunschweig.de">Feedback</a>
        </div>

      </div><!-- /container -->
    </div>
  </xsl:template>

  <xsl:template name="mir.jumbotwo">
    <!-- do nothing special -->
  </xsl:template>

  <xsl:template name="mir.footer">
    <div class="container">
      <div class="row">
        <div class="col-xs-6 col-sm-9">
          <ul class="internal_links nav navbar-nav">
            <xsl:apply-templates select="$loaded_navigation_xml/menu[@id='below']/*" />
          </ul>
        </div>
        <div class="col-xs-6 col-sm-3">
          <xsl:variable name="mcr_version" select="concat('MyCoRe ',mcrver:getCompleteVersion())" />
          <div id="powered_by">
            <a href="http://www.mycore.de">
              <img src="{$WebApplicationBaseURL}mir-layout/images/mycore_logo_small_invert.png" title="{$mcr_version}" alt="powered by MyCoRe" />
            </a>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="mir.powered_by">
    <xsl:if test="//site/@ID">
      <xsl:variable name="dateCreated" xmlns:encoder="xalan://java.net.URLEncoder"
                  select="document(concat('solr:q=',encoder:encode(concat('id:', //site/@ID)), '&amp;fl=created'))" />
      <xsl:variable name="dateCreatedInt" select="translate(substring-before($dateCreated//date[@name='created'],'T'), '-', '')" />
      <xsl:if test="$dateCreatedInt &lt; $DIGIBIB.StartDate">
        <div id="digibib_alert"
             class="alert alert-warning alert-dismissible text-center"
             role="alert">
          <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">×</span></button>
          Dieses Dokument wurde migriert. Falls Sie Probleme mit der Anzeige des Dokumentes haben, können Sie das Dokument auf dem
          <a href="http://digisrv-1.biblio.etc.tu-bs.de:8080/docportal/receive/DocPortal_document_{substring-after(//site/@ID,'dbbs_mods_')}" class="alert-link">alten Publikationsserver der
          TU Braunschweig</a> ansehen.
        </div>
      </xsl:if>
    </xsl:if>
    <!-- Piwik -->
    <xsl:if test="$piwikID &gt; 0">
      <script type="text/javascript">
            var _paq = _paq || [];
            _paq.push(['setDoNotTrack', true]);
            _paq.push(['trackPageView']);
            _paq.push(['enableLinkTracking']);
            (function() {
              var u="https://piwik.gbv.de/";
              var objectID = '<xsl:value-of select="//site/@ID" />';
              if(objectID != "") {
                _paq.push(["setCustomVariable",1, "object", objectID, "page"]);
              }
              _paq.push(['setTrackerUrl', u+'piwik.php']);
              _paq.push(['setSiteId', '<xsl:value-of select="$piwikID" />']);
              _paq.push(['setDownloadExtensions', '7z|aac|arc|arj|asf|asx|avi|bin|bz|bz2|csv|deb|dmg|doc|exe|flv|gif|gz|gzip|hqx|jar|jpg|jpeg|js|mp2|mp3|mp4|mpg|mpeg|mov|movie|msi|msp|odb|odf|odg|odp|ods|odt|ogg|ogv|pdf|phps|png|ppt|qt|qtm|ra|ram|rar|rpm|sea|sit|tar|tbz|tbz2|tgz|torrent|txt|wav|wma|wmv|wpd|z|zip']);
              var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
              g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
            })();
      </script>
      <noscript><p><img src="https://piwik.gbv.de/piwik.php?idsite={$piwikID}" style="border:0;" alt="" /></p></noscript>
    </xsl:if>
    <!-- End Piwik Code -->
  </xsl:template>

</xsl:stylesheet>