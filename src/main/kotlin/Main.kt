import org.hibernate.cfg.Configuration
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


@Entity
data class Drug(

    @Id
    @Column(nullable = false)
    val ndc: String,

    @Column(nullable = true)
    val generic: String?
) {
    override fun toString() = "$ndc: $generic"
}

fun main() {
    val url = "jdbc:snowflake://ar66627.eu-west-1.snowflakecomputing.com/"
    val prop = Properties()
    prop["user"] = "jabm@lundbeck.com"
    prop["db"] = "dev_raw_iqvia_pharmetrics_db"
    prop["schema"] = "dbt_tron"
    prop["warehouse"] = "transforming_wh"
    prop["role"] = "data science"
    prop["authenticator"] = "externalbrowser"

    //Configure SQLite:
    /*
    hibernate.connection.driver_class=org.sqlite.JDBC
    hibernate.connection.url=jdbc:sqlite:test.db
    hibernate.dialect = org.sqlite.hibernate.dialect.SQLiteDialect
    hibernate.hbm2ddl.auto=create*/

    val sessionFactory = Configuration()
        .addAnnotatedClass(Drug::class.java)
        .setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC")
        .setProperty("hibernate.connection.url", "jdbc:sqlite:test.sqlite")
        .setProperty("hibernate.dialect", "org.sqlite.hibernate.dialect.SQLiteDialect")
        .setProperty("hibernate.show_sql", "false")
        .setProperty("hibernate.hbm2ddl.auto", "create")
        .buildSessionFactory()

    val session = sessionFactory
        .openSession()


    session.beginTransaction()
    DriverManager.getConnection(url, prop).use {

        val stat: Statement = it.createStatement()
        val res: ResultSet = stat.executeQuery(
            """select ndc, generic_name
                from dev_raw_iqvia_pharmetrics_db.dbt_tron.stg_pp_rx_lookup limit 1000""".trimIndent()
        )
        println("Number of columns: ${res.metaData.columnCount}\n")
        print("rownumber")

        //print column-names
        (1..res.metaData.columnCount).forEach {it2 ->
            print(
                " ${res.metaData.getColumnName(it2)}(${
                    res.metaData.getColumnTypeName(
                        it2
                    )
                })"
            )
        }
        println()

//res.metaData.getColumnType()
        //  java.sql.Types.
        //traverse through the resultset
        var i = 0
        var generic: String
        while (res.next()) {
            generic = res.getString(2)?:"NULL"
          //  println("${res.row} ${res.getString(1)} ${res.getString(2)}")

            session.save(Drug(res.getString(1),generic))
            i++
            if ( i % 20 == 0 || res.isLast) { //20, same as the JDBC batch size
                //flush a batch of inserts and release memory:
                session.flush()
                session.clear()
            }

        }

    }

    session.transaction.commit()


   println(session.statistics.toString())


    val data2 = session.get(Drug::class.java, "00585118403")
    println("look for 00585118403: $data2")



    session
        .createNativeQuery("select ndc, generic from Drug where generic like 'OLANZ%'")
        .setMaxResults(2)
        .addEntity(Drug::class.java)
        .list()
        .forEach {println(it)}


    val cb = session.criteriaBuilder
    val cq: CriteriaQuery<Drug> = cb.createQuery(Drug::class.java)
    val rootEntry: Root<Drug> = cq.from(Drug::class.java)
    val all: CriteriaQuery<Drug> = cq.select(rootEntry)
   // println(session.metamodel.)

    val all2=session.criteriaBuilder
        .createQuery(Drug::class.java)
        .select(session.criteriaBuilder.createQuery(Drug::class.java).from(Drug::class.java))

    val allQuery: TypedQuery<Drug> = session.createQuery(all).setMaxResults(5)
    allQuery.resultList.forEach {println(it)}
    /*
    val allQuery2 = session.createNativeQuery("select * from Drug where generic like 'OLANZ%'").setMaxResults(5)
    val mylist = allQuery.list()
    println(mylist[0].toString())*/
}