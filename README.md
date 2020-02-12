# spring-kotlin-exposed
playground for spring-boot 2.*, kotlin, jetbrains-exposed, postgres (jsonb + cube + earthdistance), flyway, docker

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
        val id = uuid("id")
        override val primaryKey: PrimaryKey = PrimaryKey(id, name = "author_pkey")
        val createdAt = instant("created_at")
        val modifiedAt = instant("updated_at")
        val version = integer("version")
        val name = text("name")
    }
                    
    object BookTable : Table("book") {
        val id = uuid("id")
        override val primaryKey: PrimaryKey = PrimaryKey(id, name = "book_pkey")
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

## runbook - how to run the examples ?

### quickstart: docker-compose "playground"

```
    # build db + app and start everything using docker-compose
       
    $ make -C rest-api playground.up
    $ open http://localhost:8080/swagger-ui.html
```


### build

```
    $ make -C rest-api help
    $ make -C rest-api app.build

```

### build + test

```
    $ make -C rest-api help
    $ make -C rest-api app.test
    
    # serve test reports ...
    $ make -C rest-api reports.serve.tests
    $ open http://127.0.0.1:20000
    

```

### run local db (docker)

```
    # db-local
    $ make -C rest-api db-local.up
    
    # db-ci (to be used for gradle test)
    $ make -C rest-api db-ci.up

```

### connect to a cloud hosted db (ssl enabled)

```
    # if your postgres is ssl enabled, you may need to add a few parameters to jdbc url ...
    e.g.: DB_URL: "my.postgres.example.com:5432/mydb?ssl=true&sslmode=prefer"

```

## exposed - examples & recipes

- bookstore api: 
    - crud-ish (joined tables: author, book)
- bookz api:
    - Mongo'ish, NoSQL'ish, ...
    - how to build a document store ?  
    - postgres jsonb data type     
- tweeter api: 
    - postgres enum data type, 
    - how to build your own spring-data-rest-like search-dsl
    - api response json post processing: jq, jsonpath ? JMESPATH.
- places api:
    - how to run geospatial queries
    - show all places within a radius of 5 km oder by distance ...
    - postgres cube + earthdistance extensions
    - postgres gist index     
     
### example api: bookstore
- api bookstore: crud-ish (joined tables: author, book)
```
# Highlights: postgres joins

sql ...

CREATE TABLE author (
  id         UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  name       TEXT                        NOT NULL
);

CREATE TABLE book (
  id         UUID                        NOT NULL,
  author_id  UUID                        NOT NULL,
  version    INTEGER                     NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  title      CHARACTER VARYING(255)      NOT NULL,
  status     CHARACTER VARYING(255)      NOT NULL,
  price      NUMERIC(15, 2)              NOT NULL
);
ALTER TABLE ONLY author
  ADD CONSTRAINT author_pkey PRIMARY KEY (id);
ALTER TABLE ONLY book
  ADD CONSTRAINT book_pkey PRIMARY KEY (id);
ALTER TABLE ONLY book
  ADD CONSTRAINT book_author_id_fkey FOREIGN KEY (author_id) REFERENCES author (id);

```

```
kotlin ...

object AuthorTable : Table("author") {
    val id = uuid("id")
    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "author_pkey")
    val createdAt = instant("created_at")
    val name = text("name")
    (...)
}

object BookTable : Table("book") {
    val id = uuid("id")
    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "book_pkey")
    val createdAt = instant("created_at")
    val authorId = (uuid("author_id") references AuthorTable.id)
    val title = varchar("title", 255)
    val status = enumerationByName("status", 255, BookStatus::class)
    val price = decimal("price", 15, 2)
}

enum class BookStatus { NEW, PUBLISHED; }

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

```
### api examples

# api: insert author into db
$ curl -X PUT "http://localhost:8080/api/bookstore/authors" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"name\": \"John Doe\"}"

# api: insert book into db - referencing author.author_id
$ curl -X PUT "http://localhost:8080/api/bookstore/books" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"authorId\": \"3c10f9bf-2056-4b93-b691-57128464e85e\", \"title\": \"John's way of life.\", \"status\": \"NEW\", \"price\": 0.29}"

# api: get all books from db inner join author
$ curl -X GET "http://localhost:8080/api/bookstore/books" -H "accept: */*"
```

### example api: bookz - Mongo'ish, NoSQL'ish, ...
- how to build a json document store ?

#### Highlights: postgres jsonb data type
```
# Highlights: postgres jsonb data type
 
sql ..
 
CREATE TABLE bookz (
  id         UUID                        NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  is_active BOOLEAN NOT NULL,
  data       JSONB                       NOT NULL
);
 
kotlin ...
 
object BookzTable : UUIDCrudTable("bookz") {
    val id = uuid("id")
    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "bookz_pkey")
    (...)
    val data = jsonb("data", BookzData::class.java, jacksonObjectMapper())
    (...)
}

data class BookzData(val title: String, val genres: List<String>, val published: Boolean)


```

```
# api: insert some sample data into db ...
$ curl -X POST "http://localhost:8080/api/bookz-jsonb/books/bulk-save" -H "accept: */*"

# api: insert a new bookz into db
$ curl -X PUT "http://localhost:8080/api/bookz-jsonb/books" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"data\": { \"genres\": [ \"programming\",\"enterprise\",\"bingo\" ], \"published\": true, \"title\": \"the book\" }}"

# api: get all bookz ...
$ curl -X GET "http://localhost:8080/api/bookz-jsonb/books" -H "accept: */*"
```

### example api: tweeter

- postgres enum types
- how to create your own spring-data-rest-like search dsl ?
- jq, jsonpath, ... ? JMESPath . How to post process api responses using a json query-language ? 
#### highlights: postgres enum types
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

#### highlights: REST'ish search-dsl

- simple crud api endpoint (tables: tweet)
- api endpoint to insert some random data into db
- api endpoint to search in db 
- how to create your own spring-data-rest clone ?

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

#### highlights: JMESPath - json query language

- jq, jsonpath, ... ? JMESPath .
- JMESPath json query language . see: http://jmespath.org/tutorial.html

```
how to ...

    POST /api/search(q=...) | jmespath(q="items[].{id:id, createdAt:createdAt}")

... on REST-api level?

```


```
# generate 50 records in table "tweet"
$ curl -X PUT http://localhost:8080/api/tweeter/bulk-generate/50

# search records in table "tweet" 
# and apply JMESPath query to the reponse ...
# example: "items[].{id:id, createdAt:createdAt}"  
# ^^ we just want attributes "id", "createdAt" on item level


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
  ],
  "jmesPath":"items[].{id:id, createdAt:createdAt}"
}

$ curl -X POST "http://localhost:8080/api/tweeter/search/jmespath" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"limit\": 10, \"offset\": 0, \"match\": { \"message-LIKE\": \"fox\", \"comment-LIKE\": \"brown\" }, \"filter\": { \"status-IN\": [ \"DRAFT\", \"PUBLISHED\" ] }, \"orderBy\": [ \"createdAt-DESC\" ],\"jmesPath\":\"items[].{id:id, createdAt:createdAt}\"}"

```



### examples api: places - how to run geospatial queries ?
- show all places within a radius of 5 km oder by distance ...
- solution: postgres: cube + earthdistance extensions and gist index
- alternatives: you may want to have a look into PostGIS as alternative to cube + earthdistance (see: https://github.com/sdeleuze/geospatial-messenger)
```
sql ...

CREATE TABLE place
(
    place_id                     uuid               NOT NULL,
    created_at                   timestamp          NOT NULL,
    modified_at                  timestamp          NOT NULL,
    deleted_at                   timestamp          NULL,
    active                       bool               NOT NULL,
    place_name                   varchar(2048)      NOT NULL,
    country_name                 varchar(2048)      NOT NULL,
    city_name                    varchar(2048)      NOT NULL,
    postal_code                  varchar(2048)      NOT NULL,
    street_address               varchar(2048)      NOT NULL,
    formatted_address            varchar(2048)      NOT NULL,
    latitude                     numeric(10, 6)     NOT NULL,
    longitude                    numeric(10, 6)     NOT NULL,

    CONSTRAINT place_pkey PRIMARY KEY (place_id)
);

CREATE INDEX place_geosearch_index ON place USING gist (ll_to_earth(latitude, longitude));
```

```
kotlin ...

object PlaceTable : Table("place") {
    val place_id = uuid("place_id")
    override val primaryKey: PrimaryKey = PrimaryKey(place_id, name = "place_pkey")
    (...)
    // custom
    val streetAddress = varchar(name = "street_address", length = 2048)
    val latitude = decimal(name = "latitude", precision = 10, scale = 6)
    val longitude = decimal(name = "longitude", precision = 10, scale = 6)
}
```
```
flavour: native query ...

        val sql: String = """
                    SELECT
                        ${selectFields.joinToString(" , ")},

                        earth_distance(
                            ll_to_earth( ${req.latitude} , ${req.longitude} ),
                            ll_to_earth( ${PLACE.latitude.qName}, ${PLACE.longitude.qName} )
                        ) as $FIELD_DISTANCE

                    FROM
                        ${PLACE.qTableName}

                    WHERE
                        earth_box(
                            ll_to_earth( ${req.latitude} , ${req.longitude} ), ${req.radiusInMeter}
                        ) @> ll_to_earth( ${PLACE.latitude.qName} , ${PLACE.longitude.qName} )

                        AND
                            earth_distance(
                                ll_to_earth( ${req.latitude} , ${req.longitude} ),
                                ll_to_earth( ${PLACE.latitude.qName}, ${PLACE.longitude.qName} )
                            ) <= ${req.radiusInMeter}

                    ORDER BY
                        $FIELD_DISTANCE ASC,
                        ${PLACE.createdAt.qName} ASC,
                        ${PLACE.place_id.qName} ASC

                    LIMIT ${req.limit}
                    OFFSET ${req.offset}

                    ;
        """.trimIndent()
```
```
flavour: custom dsl query ...

fun search(req:Request):Response {
        val geoSearchQuery: GeoSearchQuery = buildGeoSearchQuery(
                fromLatitude = req.payload.latitude,
                fromLongitude = req.payload.longitude,
                searchRadiusInMeter = req.payload.radiusInMeter,
                toLatitudeColumn = PLACE.latitude,
                toLongitudeColumn = PLACE.longitude,
                returnDistanceAsAlias = "distance_from_current_location"
        )

        return PLACE
                .slice(
                        geoSearchQuery.sliceDistanceAlias,
                        *PLACE.columns.toTypedArray()
                )
                .select {
                    (PLACE.active eq true)
                            .and(geoSearchQuery.whereDistanceLessEqRadius)
                            .and(geoSearchQuery.whereEarthBoxContainsLocation)
                }
                .orderBy(
                        Pair(geoSearchQuery.orderByDistance, SortOrder.ASC),
                        Pair(PLACE.createdAt, SortOrder.ASC),
                        Pair(PLACE.place_id, SortOrder.ASC)
                )
                .limit(n = req.payload.limit, offset = req.payload.offset)
                .map {
                    (...)
                }
}

fun buildGeoSearchQuery(
        fromLatitude: Number,
        fromLongitude: Number,
        searchRadiusInMeter: Number,
        toLatitudeColumn: Column<out Number>,
        toLongitudeColumn: Column<out Number>,
        returnDistanceAsAlias: String
): GeoSearchQuery {
    val reqEarthExpr: CustomFunction<PGEarthPointLocation> = ll_to_earth(
            latitude = fromLatitude, longitude = fromLongitude
    )
    val dbEarthExpr: CustomFunction<PGEarthPointLocation> = ll_to_earth(
            latitude = toLatitudeColumn, longitude = toLongitudeColumn
    )
    val earthDistanceExpr: CustomFunction<Double> = earth_distance(
            fromEarth = reqEarthExpr, toEarth = dbEarthExpr
    )
    val earthDistanceExprAlias: ExpressionAlias<Double> = ExpressionAlias(
            earthDistanceExpr, returnDistanceAsAlias
    )
    val reqEarthBoxExpr: CustomFunction<PGEarthBox> = earth_box(
            fromLocation = reqEarthExpr,
            greatCircleRadiusInMeter = intParam(searchRadiusInMeter.toInt())
    )

    return GeoSearchQuery(
            sliceDistanceAlias = earthDistanceExprAlias,
            whereDistanceLessEqRadius = (earthDistanceExpr lessEq searchRadiusInMeter.toDouble()),
            whereEarthBoxContainsLocation = (reqEarthBoxExpr rangeContains dbEarthExpr),
            orderByDistance = earthDistanceExpr
    )
}

```
```
api example: find all places within radiusnof 5000 metres from "latitude": 10.0, "longitude": 20.0 order by distance
$ curl -X POST "http://localhost:8080/api/places/geosearch/dsl" -H "accept: */*" -H "Content-Type: application/json" -d "{ \"latitude\": 10.0, \"longitude\": 20.0, \"radiusInMeter\": 50000, \"limit\": 10, \"offset\": 0}"
```

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
    - https://medium.com/@pjagielski/announcing-krush-idiomatic-persistence-layer-for-kotlin-based-on-exposed-ab8e5c6de72d
    - https://github.com/TouK/kotlin-exposed-realworld
    - https://github.com/TouK/krush
- Illia Sorokoumov: https://github.com/ilya40umov/KotLink
- krush : https://github.com/TouK/krush
        It’s based on a compile-time JPA annotation processor that generates Exposed DSL table and objects mappings for you.


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
