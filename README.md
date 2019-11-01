# spring-kotlin-exposed
playground for spring-boot 2.*, kotlin, jetbrains-exposed, postgres, jsonb, flyway, docker

- Yes, we can talk to a sql db. It's easy. No rocket science.
- There is a life besides the "dark art of hibernate / jpa".
- Alternatives: JDBI, Requery, rxjava-jdbc, Jooq, Querydsl, Ktorm (https://ktorm.liuwj.me/) ...

## jetbrains exposed (overview)
- wiki: https://github.com/JetBrains/Exposed/wiki
- flavours: 
    - DSL Api: typesafe sql (immutable data structures)
    - DAO Api: ORM-/ActiveRecord-style (mutable entities)
- highlights: 
    - integrates with spring transaction manager
    - works with flyway db migrations
    - supports multiple datasources within one application
        (e.g.: source-db-1, source-db2, sink-db)
    - speeds up startup of spring-boot
    - low memory footprint (playground runs on 256M)
    - precise and typesafe sql queries (WYSISWG)
    - no runtime reflection magic  
    - writing add-ons is simple, e.g. custom db-column-types 
    
    
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
       
    $ make -C rest-api playground.up
    $ open http://localhost:8080/swagger-ui.html
```


## build

```
    $ make -C rest-api help
    $ make -C rest-api app.build

```

## build + test

```
    $ make -C rest-api help
    $ make -C rest-api app.test
    
    # serve test reports ...
    $ make -C rest-api reports.serve.tests
    $ open http://127.0.0.1:20000
    

```

## run local db (docker)

```
    # db-local
    $ make -C rest-api db-local.up
    
    # db-ci (to be used for gradle test)
    $ make -C rest-api db-ci.up

```

## connect to a cloud hosted db (ssl enabled)

```
    # if your postgres is ssl enabled, you may need to add a few parameters to jdbc url ...
    e.g.: DB_URL: "my.postgres.example.com:5432/mydb?ssl=true&sslmode=prefer"

```

## examples: api tweeter

- simple crud api endpoint (tables: tweet)
- api endpoint to insert some random data into db
- api endpoint to search in db ( poc for the spring-data fans)

```
# generate 50 records in table "tweet"
$ curl -X PUT http://localhost:8080/api/tweeter/bulk-generate/50

# search records in table "tweet"

POST "http://localhost:8080/api/tweeter/search"

payload:

{
  "limit": 10,
  "offset": 0,
  "match": {
    "message-LIKE": "fox",
    "comment-LIKE": "brown"
  },
  "filter": {
    "status-IN": [
      "DRAFT",
      "PUBLISHED"
    ]
  },
  "orderBy": [
    "createdAt-DESC"
  ]
}

$ curl -X POST "http://localhost:8080/api/tweeter/search" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"limit\": 10, \"offset\": 0, \"match\": { \"message-LIKE\": \"fox\", \"comment-LIKE\": \"brown\" }, \"filter\": { \"id-IN\": [ ], \"status-IN\": [ \"DRAFT\",\"PUBLISHED\" ] }, \"orderBy\": [ \"createdAt-DESC\" ]}"

```


```
# Highlights: postgres enum types
 
sql ..
 
CREATE TYPE TweetStatusType AS ENUM ('DRAFT', 'PENDING', 'PUBLISHED');

CREATE TABLE Tweet (
  (...)
  status TweetStatusType NOT NULL DEFAULT 'DRAFT'
);
 
kotlin ...
 
object TweetsTable : Table("tweet") {
    (...)
    val status = enumerationByNameAndSqlType(
            name = "status", sqlType = "TweetStatusType", klass = TweetStatus::class.java
    )
}

```
## examples: api bookstore, bookz, places

- api bookstore: crud-ish (joined tables: author, book)
- api bookz: jsonb examples (tables: bookz)
- api places: postgres geospatial query examples (postgres extensions: cube + earthdistance)

## This example project is based on ...
- https://github.com/making/spring-boot-db-samples
- https://github.com/sdeleuze/geospatial-messenger
- jsonb: https://www.compose.com/articles/faster-operations-with-the-jsonb-data-type-in-postgresql/
- jsonb: https://gist.github.com/quangIO/a623b5caa53c703e252d858f7a806919
- https://github.com/ilya40umov/KotLink

## Awesome Kotlin + Exposed

- Andrey Tarashevskiy : https://github.com/JetBrains/Exposed
- Sébastien Deleuze : 
    - https://spring.io/blog/2016/03/20/a-geospatial-messenger-with-kotlin-spring-boot-and-postgresql
    - https://github.com/sdeleuze/geospatial-messenger
- Piotr Jagielski: 
    - https://medium.com/@pjagielski/how-we-use-kotlin-with-exposed-at-touk-eacaae4565b5
    - https://github.com/TouK/kotlin-exposed-realworld
- Illia Sorokoumov: https://github.com/ilya40umov/KotLink

## Whats wrong with orm, jpa, hibernate and in-memory h2-db these days ?

There is no silver bullet. 
It's born in a world of single-instance big fat application servers.
It hardly fits into a modern world of:

- functional programming: e.g. immutable threadsafe pojos / data classes 
- CQRS and eventsourcing
- horizontal scaling of polyglot microservices

Make up your mind ...

- How hibernate ruined Monica's career: https://www.toptal.com/java/how-hibernate-ruined-my-career
- A guide to accessing databases in Java: https://www.marcobehler.com/guides/a-guide-to-accessing-databases-in-java/
- Why do I hate hibernate: https://de.slideshare.net/alimenkou/why-do-i-hate-hibernate-12998784
- ORM is an antipattern: http://seldo.com/weblog/2011/08/11/orm_is_an_antipattern
- Opinionated JPA: https://leanpub.com/opinionatedjpa/read
- Lightweight ORM, do it yourself: https://blog.philipphauer.de/do-it-yourself-orm-alternative-hibernate-drawbacks/
- Don't use H2 db for testing, use docker: https://blog.philipphauer.de/dont-use-in-memory-databases-tests-h2/
- Spring @Transactional - what you should know: https://www.reddit.com/r/java/comments/dp49m3/spring_transaction_management_an_unconventional/
