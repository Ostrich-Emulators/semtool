# OS-EM Semantic Toolkit

[![Build Status](https://travis-ci.org/Ostrich-Emulators/semtool.svg?branch=master)](https://travis-ci.org/Ostrich-Emulators/semtool)

This is an almost complete re-write of SEMOSS project from https://github.com/semoss. That project appears dormant, though it may simply have moved to [SourceForge](https://sourceforge.net/projects/semoss).

The toolkit is a GUI application to explore semantic databases. It includes almost all the functionality of the original tool, including:

* Multiple output styles (graphs, tables, charts)
* Lots of graph functionality
  * Island, loop identification
  * Customizable icons, colors, text
  * Arbitrary graph traversal
  * Distance downstream, upstream
  * Multiple layout options
  * Minimum spanning tree
* Custom chart generation
* SPARQL editor
* Perspective/Insight query concept

And Adds:

* An updated technology stack (Java 8, BlazeGraph 2.1, JUNG 2.1)
* Standardized GUI elements and behaviors
* Standardized RDF predicates and querying
* Graphical Query Builder -- generate SPARQL via a graphical interface
* Insight Manager -- pre-define and organize queries, output styles
* Unit testing
* Error handling
* Multi-threaded UI, engine code
* More graph functions
  * Export to table
  * Condense
  * Full-text indexing/search
  * Export Edge List
* Automatic database metamodel generation
* Convert table to graph and vice versa
* Syntax-highlighting

## Code Organization
The source code is organized into several packages, and built with Maven.
* [Parent](http://ostrich-emulators.github.io/semtool/index.html) -- The meta-package to kick everything off [docs](http://ostrich-emulators.github.io/semtool/apidocs/index.html)
  * [Common](http://ostrich-emulators.github.io/semtool/common/index.html) -- Pieces of code that span multiple projects [docs](http://ostrich-emulators.github.io/semtool/common/apidocs/index.html)
  * [GUI](http://ostrich-emulators.github.io/semtool/gui/index.html) -- The main GUI application [docs](http://ostrich-emulators.github.io/semtool/gui/apidocs/index.html)
  * [Tools](http://ostrich-emulators.github.io/semtool/tools/index.html) -- A collection of tools like a legacy database upgrader and embedded webserver [docs](http://ostrich-emulators.github.io/semtool/tools/apidocs/index.html)
  * Web -- Not fully functional at the moment
