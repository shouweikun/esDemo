import java.net.InetAddress

import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders.matchQuery
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder
import org.elasticsearch.index.query.{BoolQueryBuilder, MatchAllQueryBuilder, QueryBuilder, QueryBuilders}
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.{exponentialDecayFunction, fieldValueFactorFunction}
import org.elasticsearch.transport.client.PreBuiltTransportClient

/**
  * Created by john_liu on 2018/4/12.
  */
object EsClientDemo {
  def main(args: Array[String]): Unit = {

    val settings = Settings.builder.put("cluster.name", "elasticsearchTest").build
    val client = new PreBuiltTransportClient(settings)
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.16.102"), 9300))
    val a =client.prepareGet("shopdata04121401","shoptest04121401","10000000000").setOperationThreaded(false)
      .get();
    println(a.toString)
    val functions = Array(new FunctionScoreQueryBuilder.FilterFunctionBuilder(matchQuery("name", "kimchy"), fieldValueFactorFunction("name").modifier(Modifier.LN).factor(2)), new FunctionScoreQueryBuilder.FilterFunctionBuilder(exponentialDecayFunction("age", 0L, 1L)))
    val qb: QueryBuilder = QueryBuilders.functionScoreQuery(functions)

    val b =  QueryBuilders.functionScoreQuery(new BoolQueryBuilder(),fieldValueFactorFunction("name").modifier(Modifier.LN).factor(2))
   val c =  client.prepareSearch().setQuery(new MatchAllQueryBuilder()).get
    println(c)

  }
}
