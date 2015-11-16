# validatom

A toyish implementation of a constraints engine enforced at transaction time in Datomic. This is the code for a lighting talk held at Datomic Conference, November 16 2015, Philadelphia.

The idea is that you can "decorate" schema attributes with references to database functions, which looks at the value of datoms with that particular attribute. Examples of use cases could be

`:employee/monthlySalary` should always be positive (or, maybe even above minimum salary)

`:user/firstname` should not have "bobby tables"-characters in them. Should not be empty string. Should be less than 200 characters.

`:user/email` should be an e-mail-address, even if you choose to store it as a string.

In resources/schema.edn there's `:attributes/validator` attribute, and attributes using it. There's also some (verbose) validators.

This currently works only with mem-db (add the transaction enforcing logic as either database functions or make the classes availiable for the transactor JVM-processes).

## Valid datoms

A datom is a five-tuple of `[Entity Attribute Value Tx added?]` and we here define a valid datom as one which, given the attribute, does not result in any errors reported in the validators assigned to the attribute.

This means that *undecorated attributes are always valid* (given they follow the datatypes/schema of Datomic of course).

All datom validation are currently "self contained" in that they don't need any database lookup to validate. Reference attributes could be supported but well, let's think about it some more...

## Constraints

To enforce a constraint in the database, there are some things which should "never" be stored in the database. This is made happening by doing a "with" database in the transaction, and see wheter the changed (added) datoms are adhering to the validators.

If they are not valid, an exception with an extensive report on what was broken is thrown and the transaction is cancelled.

## Installation

The `resources/schema.edn` cannot be transacted as-is to an empty database. In the `src/validatom/prepare_db` there's an example on a multipass transaction for getting everything into the database.

## Usage

See `src/validatom/example.clj`

## License

Copyright © 2015 Linus Ericsson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
