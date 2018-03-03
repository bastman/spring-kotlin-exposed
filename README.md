# spring-kotlin-exposed
playground for spring-boot 2.*, kotlin, jetbrains-exposed


Yes, we can talk to a sql db - without hibernate/jpa in the java spaceship enterprise ;)

It's easy. No ORM magic required.

```
    Example: the beauty of exposed-dsl ... it's typesafe
 
    fun findAllBooksJoinAuthor() =
            (AuthorTable innerJoin BookTable)
                    .selectAll()
                    .map { 
                        BookRecordJoinAuthorRecord(
                            bookRecord = it.toBookRecord(), 
                            authorRecord = it.toAuthorRecord()
                        ) 
                    }

```


## build

```
    $ make -C rest-api app.build

```

## run local db (docker)

```
    $ make -C rest-api db.local.up

```


## This example project is based on ...
- https://github.com/making/spring-boot-db-samples
- https://github.com/sdeleuze/geospatial-messenger

## Whats wrong with orm, jpa, hibernate and in-memory h2-db these days ?

There is no silver bullet. 
It's born in a world of single-instance big fat application servers.
It hardly fits into a modern world of:

- functional programming: e.g. immutable threadsafe pojos / data classes 
- CQRS and eventsourcing
- horizontal scaling of polyglot microservices

Make up your mind ...

- How hibernate ruined Monica's career: https://www.toptal.com/java/how-hibernate-ruined-my-career
- Why do I hate hibernate: https://de.slideshare.net/alimenkou/why-do-i-hate-hibernate-12998784
- ORM is an antipattern: http://seldo.com/weblog/2011/08/11/orm_is_an_antipattern
- Opinionated JPA: https://leanpub.com/opinionatedjpa/read
- Lightweight ORM, do it yourself: https://blog.philipphauer.de/do-it-yourself-orm-alternative-hibernate-drawbacks/
- Don't use H2 db for testing, use docker: https://blog.philipphauer.de/dont-use-in-memory-databases-tests-h2/

