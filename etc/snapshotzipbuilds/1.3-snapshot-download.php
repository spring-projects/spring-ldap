<?php
$url = "http://s3.amazonaws.com/maven.springframework.org?prefix=snapshot/org/springframework/ldap/spring-ldap/1.3.2.CI-SNAPSHOT/";
$xml = file_get_contents($url);

header('Content-Type: text/xml; charset=UTF-8');
echo '<?xml version="1.0" encoding="UTF-8"?>';
echo '<?xml-stylesheet type="text/xsl" href="./snapshot-download.xsl"?>';
echo substr($xml, 39);
?>
