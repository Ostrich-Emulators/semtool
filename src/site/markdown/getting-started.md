## What Is It?
The OS-EM Semantic Toolkit is a group of functions for managing and exploring
data, and creating queries all wrapped up in an intuitive GUI. Currently, the
toolkit reads XLSX files and processes them into a graph database. It can also
create databases directly from RDF, Turtle (TTL), and N-Triples (NT) files.

The toolkit can be used in several different ways. Once you have a database,
you can run queries against it using the syntax-highlighting Query Panel. You
can create queries for future use via the Insight Manager. You can check the
consistency of your data and look for errors with the Quality Checker. The most
common use-case is using the Perspectives and Insights to visualize the data.

### The Database
The database is a triplestore of your data. Nothing happens without a database.
Luckily, OS-EM Semantic Toolkit can generate a triplestore from a variety of
sources. Check out [the data loading page](loading-data.html) for more information
on creating a triplestore. If you already have a triplestore, the Toolkit can
connect to a variety of popular engines.

The system requires every database it opens to have a defined "Base URI" that
uniquely identifies the data set. It can also contain (a very small set of )
optional metadata to make the GUI easier to use. Currently, the metadata
consists of:

* Title
* Summary (description)
* Organization
* Point of Contact
* Creation Date
* Last Update Date
* Reification Model (either OS-EM Semantic Toolkit style or Custom)


### Perspectives and Insights
Insights are essentially saved SPARQL queries. Unlike storing raw SPARQL, however,
an Insight allows a user to associate a label, description, and output display
with the query. So, a user can write the query, decide what to call it, and how
the results should be visualized.

Even better, Insights can have Parameters associated with them. Parameters allow
the user to define a generic query in the Insight, and then bind values to its
variables during before execution. The system automatically figures out which
bindings go with which variables. It's also smart enough to figure out which
variables are dependent on other variables, and act accordingly. So, for example,
the user might have one Parameter that lets the user choose what type of concept
to map, and then a dependent Parameter that allows them to select a given
instance of that type.

Perspectives are for organization. They have a name and description, and contain
zero or more Insights. Thus, the user can organize Insights arbitrarily. For
example, in a database containing population data, one Perspective might contain
Insights dealing with disease which another Perspective might have Insights
dealing with political divisions.

Perspectives and Insights can be stored in the database just like any other data.
However, the Toolkit supports private Perspectives and Insights that are separate
from the database. Private Perspectives and Insights are available only to their
author.