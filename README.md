# spring-kotlin-exposed
playground for spring-boot 2.*, kotlin, jetbrains-exposed, postgres, jsonb, docker

- Yes, we can talk to a sql db. It's easy. No rocket science.
- There is a life besides the "dark art of hibernate / jpa".
- Alternatives: JDBI, Requery, rxjava-jdbc, Jooq, Querydsl, ...

## jetbrains exposed (overview)
- Wiki: https://github.com/JetBrains/Exposed/wiki
- flavours: 
    - DSL Api: typesafe sql (immutable data structures)
    - DAO Api: ORM-/ActiveRecord-style (mutable entities)
- highlights: 
    - integrates with spring transaction manager
    - supports multiple datasources within one application
        (e.g.: source-db-1, source-db2, sink-db)
    - writing custom db column types is simple
    - speeds up startup of spring-boot
    - low memory footprint   
    
    
## Exposed DSL Api: examples
- It's easy. No ORM magic. WYSIWYG.
- Ready for starship java "enterprise" deployments.

```
    # The beauty of exposed-dsl... Simple. Readable. Typesafe.
    
    # Example: query db
 
    fun findAllBooksJoinAuthor() =
            (AuthorTable innerJoin BookTable)
                    .selectAll()
                    .map { 
                        BookRecordJoinAuthorRecord(
                            bookRecord = it.toBookRecord(), 
                            authorRecord = it.toAuthorRecord()
                        ) 
                    }
    
    # Example: db schema 
    
    object AuthorTable : Table("author") {
        val id = uuid("id").primaryKey()
        val createdAt = instant("created_at")
        val modifiedAt = instant("updated_at")
        val version = integer("version")
        val name = text("name")
    }
                    
    object BookTable : Table("book") {
        val id = uuid("id").primaryKey()
        val createdAt = instant("created_at")
        val modifiedAt = instant("updated_at")
        val version = integer("version")
        val authorId = (uuid("author_id") references AuthorTable.id)
        val title = varchar("title", 255)
        val status = enumerationByName("status", 255, BookStatus::class.java)
        val price = decimal("price", 15, 2)
    } 
    
    # Example: Table Record Structures as immutable data classes
    
    data class AuthorRecord(
            val id: UUID,
            val createdAt: Instant,
            val modifiedAt: Instant,
            val version: Int,
            val name: String
    )   
    
    data class BookRecord(
            val id: UUID,
            val createdAt: Instant,
            val modifiedAt: Instant,
            val version: Int,
            val authorId: UUID,
            val title: String,
            val status: BookStatus,
            val price: BigDecimal
    )            

```

## playground

```
    # build db + app and start everything using docker-compose
     
    $ make -C rest-api playground up
    
    browse to: http://localhost:8080/swagger-ui.html
```


## build

```
    $ make -C rest-api app.build

```

## run local db (docker)

```
    $ make -C rest-api db-local.up

```

## examples:

- api tweeter: simple crud (tables: tweet)
- api bookstore: crud-ish (joined tables: author, book)
- api bookz: jsonb examples (tables: bookz)

## This example project is based on ...
- https://github.com/making/spring-boot-db-samples
- https://github.com/sdeleuze/geospatial-messenger
- jsonb: https://www.compose.com/articles/faster-operations-with-the-jsonb-data-type-in-postgresql/
- jsonb: https://gist.github.com/quangIO/a623b5caa53c703e252d858f7a806919

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

