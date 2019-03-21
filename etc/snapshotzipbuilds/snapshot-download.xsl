<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:s3="https://s3.amazonaws.com/doc/2006-03-01/"
		version="1.0">
	<xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">
		<head>
			<style type="text/css" media="all">@import "./dist-download.css";</style>
		</head>
		<body>
			<xsl:apply-templates select="s3:ListBucketResult"/>
		</body>
	</xsl:template>
	
	<xsl:template match="s3:ListBucketResult">
		<xsl:variable name="bucket-name" select="s3:Name"/>
		<xsl:variable name="prefix" select="substring(s3:Prefix,1)"/>
		<table>
			<tr>
				<th class="name">Spring LDAP Project Snapshots</th>
				<th class="size">Size</th>
			</tr>
			<xsl:for-each select="s3:Contents[substring(s3:Key, (string-length(s3:Key) - 2)) = 'zip']">
				<tr>
					<td class="name">
						<a class="name" href="https://s3.amazonaws.com/{$bucket-name}/{s3:Key}">
							<xsl:value-of select="substring-after(s3:Key,$prefix)"/><br/>
						</a>
					</td>
					<td class="size"><xsl:value-of select="format-number(s3:Size div 1048576, '###,##0.0')"/> MB</td>
				</tr>				
			</xsl:for-each>
		</table>
	</xsl:template>

</xsl:stylesheet>
