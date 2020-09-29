# Twirl RDF

Twirl is a [SPIN](https://spinrdf.org/spin.html) constraint validator that is meant to be a drop-in replacement for the [SPIN RDF](https://github.com/spinrdf/spinrdf/).

Twirl implementation is based on plain SPARQL execution and does not depend on Jena's internals, which leads to issues in SPIN RDF:
* [Behavior change between Jena 3.0.1 and 3.16.0-SNAPSHOT](https://github.com/spinrdf/spinrdf/issues/22)
* [SPIN constraint validation differs on 3.0.1 and 3.16.0-SNAPSHOT](https://www.mail-archive.com/users@jena.apache.org/msg17304.html)

Twirl has the following limitations:
* `ASK` constraints are not supported (only `CONSTRUCT`)
* constraint violation fixes are not supported
* SPIN functions are not supported
* SPIN rules are not supported

Note that Twirl uses a different version of the [SPL vocabulary](etc/spl.spin.ttl).