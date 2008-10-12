This project uses the 'DocBook XSL distribution' for HTML and PDF
generation of project upgrade documentation.

This project's build.xml file contains targets to generate the
project upgrade documentation.

To generate project documentation, execute one of the following
build targets:

* upgrade-all        - generate documentation in all supported formats
* upgrade-pdf        - generate the PDF documentation
* upgrade-html       - generate the HTML documentation
* upgrade-clean      - clean any output directories for docs

For generation to complete successfully, you must have first extracted
the .jar libraries contained in this archive:
    - http://static.springframework.org/spring/files/docbook-reference-libs.zip
... to ${basedir}/docs/reference.  If you have not yet done so, download this file
and unzip the contents of the archive into ${basedir}/docs/reference.
