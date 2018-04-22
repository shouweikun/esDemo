import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier
import org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery.ScoreMode
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.index.query.functionscore.{FunctionScoreQueryBuilder, ScoreFunctionBuilders}
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.fieldValueFactorFunction

/**
  * Created by john_liu on 2018/4/21.
  */
class QueryDemo {

  val mallQueryDemo = {

    val priceField = "price"
    val nameField = "name"
    val goodRateField = "goodRate"
    val salesField = "sales"
    val searchKeyWords = "小米"
    val fromPrice = 10
    val toPrice = 30
    val priceRangeSearch = QueryBuilders.rangeQuery(priceField)
    //.from(fromPrice).to(toPrice)
    val keyWordsMatchQuery = QueryBuilders.matchPhraseQuery(nameField, searchKeyWords).slop(3)
    val baseSearch = QueryBuilders.boolQuery()
      // .must(priceRangeSearch)
      .should(keyWordsMatchQuery).boost(50)
    val scoreFunctions = Array(new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction(salesField).modifier(Modifier.LOG1P).factor(1)), new FunctionScoreQueryBuilder.FilterFunctionBuilder(fieldValueFactorFunction(goodRateField).modifier(Modifier.LN1P).factor(2)))

    val finalFunctionScoreSearch = QueryBuilders.functionScoreQuery(baseSearch, scoreFunctions).scoreMode(ScoreMode.SUM)
    finalFunctionScoreSearch
  }

  val scriptQueryDemo = {
    val script = s"""_score*0.5+0.3*Math.log(doc['goodRate'].value)+0.2*Math.log(doc['sales'].value);"""
    val e = QueryBuilders.functionScoreQuery(new BoolQueryBuilder().should(QueryBuilders.matchPhraseQuery("name", "小米").slop(3)).should(QueryBuilders.matchPhraseQuery("subtitle", "小米").slop(3)).filter(QueryBuilders.matchQuery("name", "小米").fuzziness(Fuzziness.AUTO)).minimumShouldMatch(30), ScoreFunctionBuilders.scriptFunction(script)).boostMode(CombineFunction.SUM)
  }
}

object QueryDemo extends App {
  val a = new QueryDemo
  println(a.mallQueryDemo)
}
