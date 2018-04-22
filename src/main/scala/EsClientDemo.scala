import java.net.InetAddress

import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders.matchQuery
import org.elasticsearch.index.query.functionscore.{FunctionScoreQueryBuilder, ScoreFunctionBuilder, ScoreFunctionBuilders, ScriptScoreFunctionBuilder}
import org.elasticsearch.index.query.{BoolQueryBuilder, MatchAllQueryBuilder, QueryBuilder, QueryBuilders}
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.{exponentialDecayFunction, fieldValueFactorFunction}
import org.elasticsearch.script.{Script, ScriptType}
import org.elasticsearch.transport.client.PreBuiltTransportClient

/**
  * Created by john_liu on 2018/4/12.
  */
object EsClientDemo {
  def main(args: Array[String]): Unit = {

    val settings = Settings.builder.put("cluster.name", "elasticsearchTest").build
    val client = new PreBuiltTransportClient(settings)
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.16.102"), 9300))
    val a = client.prepareGet("shopdata04121401", "shoptest04121401", "10000000000").setOperationThreaded(false)
      .get();
    println(a)
    val sxx = client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(new QueryDemo().mallQueryDemo).get()
    println(sxx)

    val functions = Array(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.boolQuery(), fieldValueFactorFunction("name").modifier(Modifier.LN).factor(2)), new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction("name").modifier(Modifier.LN).factor(2)))

    val qb: QueryBuilder = QueryBuilders.functionScoreQuery(functions)

    println(qb)
    val b = QueryBuilders.functionScoreQuery(new BoolQueryBuilder(), fieldValueFactorFunction("name").modifier(Modifier.LN).factor(2))

    val c = client.prepareSearch().setQuery(new MatchAllQueryBuilder())

    println(b)
    println(c)
    val dfdf = s"""_score*0.5+0.3*Math.log(doc['goodRate'].value)+0.2*Math.log(doc['sales'].value);"""

    val d = QueryBuilders.wrapperQuery("[]")
    println(d)
    val e = QueryBuilders.functionScoreQuery(new BoolQueryBuilder().should(QueryBuilders.matchPhraseQuery("name", "小米").slop(3)).should(QueryBuilders.matchPhraseQuery("subtitle", "小米").slop(3)), ScoreFunctionBuilders.scriptFunction(dfdf)).boostMode(CombineFunction.SUM)
    val ccc = new BoolQueryBuilder().should(QueryBuilders.matchPhraseQuery("name", "小米")).should(QueryBuilders.matchPhraseQuery("subtitle", "小米"))
    println(client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(ccc).get())
      println (client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(e).get())
      println (client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(e).get())
    val f = QueryBuilders.matchQuery("name", "华")
    println(client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(f).get())
    val g = QueryBuilders.matchAllQuery()
    println(client.prepareSearch("shopdata04121401", "shoptest04121401").setQuery(g).get())
  }
}
