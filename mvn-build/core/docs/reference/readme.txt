This project uses the 'DocBook XSL distribution' for HTML and PDF
generation of project reference documentation.

This project's build.xml file contains targets to generate the
project reference documentation.

To generate project documentation, execute one of the following
build targets:

* doc-all        - generate documentation in all supported formats
* doc-pdf        - generate the PDF documentation
* doc-html       - generate the HTML documentation
* doc-htmlsingle - generate single page HTML documentation
* doc-clean      - clean any output directories for docs

For generation to complete successfully, you must have first extracted
the .jar libraries contained in this archive:
    - http://static.springframework.org/spring/files/docbook-reference-libs.zip
... to ${basedir}/docs/reference.  If you have not yet done so, download this file
and unzip the contents of the archive into ${basedir}/docs/reference.