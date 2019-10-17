package com.example.util.exposed.expr.postgres

import com.example.api.tweeter.db.TweetsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function as ExposedFunction
// see: https://github.com/JetBrains/Exposed/issues/500

/*
class Concat<T: String?>(val separator: String, vararg val expr: Expression<T>) : Function<T>(VarCharColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        currentDialect.functionProvider.concat(separator, queryBuilder, *expr)
    }
}

class Trim<T:String?>(val expr: Expression<T>): Function<T>(VarCharColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { append("TRIM(", expr,")") }
}
 */

//

fun customDistinctOn(vararg expressions: Expression<*>) = CustomStringFunction(
        "DISTINCT ON",
        *expressions
)

//class DistinctOn4<T: String?>(vararg val expr: Expression<T>) : ExposedFunction<T>(VarCharColumnType()) {
class DistinctOn4(vararg val expr: Expression<*>) : ExposedFunction<Int>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder)  = queryBuilder {
        //append("DISTINCT ON (", expr,")")
        append("DISTINCT ON (")
        append(*expr)
        append(")")
    }
}


class DistinctOn3<T: String?>(val separator: String, vararg val expr: Expression<T>) : ExposedFunction<T>(VarCharColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        distinctOn(separator, queryBuilder, *expr)
    }

    private fun <T:String?> distinctOn(separator: String, queryBuilder: QueryBuilder, vararg expr: Expression<T>) = queryBuilder {
        if (separator == "")
            append("DISTINCT ON(")
        else {
            append("DISTINCT ON(")
            append("'")
            append(separator)
            append("',")
        }
        expr.toList().appendTo { +it }
        append(")")
    }

    private fun <T:String?> concat(separator: String, queryBuilder: QueryBuilder, vararg expr: Expression<T>) = queryBuilder {
        if (separator == "")
            append("CONCAT(")
        else {
            append("CONCAT_WS(")
            append("'")
            append(separator)
            append("',")
        }
        expr.toList().appendTo { +it }
        append(")")
    }

}


fun foo() {
    /*
    val cols = TweetsTable.columns
    val fields = TweetsTable.fields
    cols.body()


    val a= TweetsTable
            .slice(TweetsTable.columns)
            .select {

            }.

     */

    val d = DistinctOn(TweetsTable.comment, TweetsTable.message)
    val dexp = Expression.build { d }
    val a= TweetsTable
           // .slice(TweetsTable.columns)
            .slice(d)
            .selectAll()

            .withDistinct()
}




//class DistinctOn(val a: Expression<*>, val b: Expression<*>) : CustomFunction<Int>(IntegerColumnType()) {
class DistinctOn(val a: Expression<*>, val b: Expression<*>) : Expression<Nothing>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
           // append("COALESCE(", expr, ", ", alternate, ")")

            val a_b=a.toQueryBuilder(queryBuilder)
            val a_s=a.toString()

            val out=append(
                    "DISTINCT ON ( ${a_s},  ${b.toString()} )"
            )
            val q_s=out.toString()
            println("q_s: $q_s")

            out
        }
    }

  // override fun toSQL(queryBuilder: QueryBuilder) = "DISTINCT ON (${expr.toSQL(queryBuilder)}) ${expr.toSQL(queryBuilder)}"
}

class DistinctOn2(val cols : List<Column<*>>) : Expression<Nothing>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            /*
            // append("COALESCE(", expr, ", ", alternate, ")")
            val cols2 = cols.columns
            cols2.forEach {
                it.descriptionDdl()
            }


            val a_b=a.toQueryBuilder(queryBuilder)
            val a_s=a.toString()

            cols.toString()
            */
            val out=append(
                    "DISTINCT ON ( Tweets.message,  Tweets.comment )"
            )
            val q_s=out.toString()
            println("q_s: $q_s")

            out
        }
    }

    // override fun toSQL(queryBuilder: QueryBuilder) = "DISTINCT ON (${expr.toSQL(queryBuilder)}) ${expr.toSQL(queryBuilder)}"
}
/*
class DistinctOn2(val a: Expression<*>, val b: Expression<*>) : org.jetbrains.exposed.sql.Function<String?>(StringColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder {
            // append("COALESCE(", expr, ", ", alternate, ")")

            val a_b=a.toQueryBuilder(queryBuilder)
            val a_s=a.toString()

            val out=append(
                    "DISTINCT ON ( ${a_s},  ${b.toString()} )"
            )
            val q_s=out.toString()
            println("q_s: $q_s")

            out
        }
    }

    // override fun toSQL(queryBuilder: QueryBuilder) = "DISTINCT ON (${expr.toSQL(queryBuilder)}) ${expr.toSQL(queryBuilder)}"
}

 */

/*
fun Column<*>.distinctOn(): Function<Int> = DistinctOn(this)



 */
