<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>
 

 <!-- FIXME (JJP):  bugzilla is hardwired -->
 <xsl:variable name="bugzilla" select="'http://nagoya.apache.org/bugzilla/buglist.cgi?bug_id='"/>

 <xsl:param name="bugtracking-url" select="$bugzilla"/>

 <xsl:template match="/">
  <xsl:apply-templates select="//changes"/>
 </xsl:template>
 
 <xsl:template match="changes">
  <document>
   <header>
    <title>History of Changes</title>
   </header>
   <body>
    <p><link href="changes.rss"><img border="0" src="images/rss.png" alt="RSS"/></link></p>    
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="release">
  <section id="{@version}">
   <title>Version <xsl:value-of select="@version"/> (<xsl:value-of select="@date"/>)</title>
   <table>
    <xsl:apply-templates/>
   </table>
  </section>
 </xsl:template>

 <xsl:template match="action">
  <tr>
   <td>   <icon src="images/{@type}.jpg" alt="{@type}"/> </td>
   <td> <xsl:apply-templates/> </td>
   
   <td><xsl:value-of select="@dev"/></td>

   <td>
   <xsl:if test="@due-to and @due-to!=''">
    <xsl:text> Thanks to </xsl:text>
    <xsl:choose>
     <xsl:when test="@due-to-email and @due-to-email!=''">
      <link href="mailto:{@due-to-email}">
       <xsl:value-of select="@due-to"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="@due-to"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:text>.</xsl:text>
   </xsl:if>

   <xsl:if test="@fixes-bug">
     <xsl:text> Fixes </xsl:text>
     <xsl:call-template name="print-bugs">
       <xsl:with-param name="buglist" select="translate(normalize-space(@fixes-bug),' ','')"/>
     </xsl:call-template>
     <!--
     <xsl:choose>
       <xsl:when test="contains(@fixes-bug, ',')">
         <!-<link href="{$bugtracking-url}{translate(normalize-space(@fixes-bug),' ','')}">->
           <link href="{$bugtracking-url}">
             <xsl:text>bugs </xsl:text><xsl:value-of select="normalize-space(@fixes-bug)"/>
           </link>
         </xsl:when>
         <xsl:otherwise>
           <link href="{$bugtracking-url}{@fixes-bug}">
             <xsl:text>bug </xsl:text><xsl:value-of select="@fixes-bug"/>
           </link>
         </xsl:otherwise>
       </xsl:choose>
       -->
       <xsl:text>.</xsl:text>
     </xsl:if>
     </td>
   </tr>
 </xsl:template>

 <!-- Print each bug id in a comma-separated list -->
 <xsl:template name="print-bugs">
   <xsl:param name="buglist"/>
   <xsl:choose>
     <xsl:when test="contains($buglist, ',')">
       <xsl:variable name="current" select="substring-before($buglist, ',')"/>
       <link href="{concat($bugtracking-url, $current)}">
         <xsl:value-of select="$current"/>
       </link>
       <xsl:text>, </xsl:text>
       <xsl:call-template name="print-bugs">
         <xsl:with-param name="buglist" select="substring-after($buglist, ',')"/>
       </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
       <link href="{concat($bugtracking-url, $buglist)}"><xsl:value-of select="$buglist"/></link>
     </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

</xsl:stylesheet>
