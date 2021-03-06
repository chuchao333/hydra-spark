package hydra.spark.operations.transform

import hydra.spark.api._
import org.apache.spark.sql.{DataFrame, TypedColumn}

/**
  * Created by alexsilva on 1/25/17.
  */
case class ToJson(columns: Seq[String]) extends DFOperation {

  override def transform(df: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions._
    import df.sqlContext.implicits._
    ifNotEmpty(df) { df =>
      val typedCols: Seq[TypedColumn[Any, String]] = columns.map(c => to_json(col(c)).as[String])
      df.select(typedCols: _*)
    }
  }

  override def validate: ValidationResult = {
    if (columns.isEmpty)
      Invalid(ValidationError("select-columns", "Column list cannot be empty."))
    else Valid
  }
}

