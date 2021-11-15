package com.colisweb.gdrive.client.bigquery

import com.google.cloud.bigquery.Field.Mode._
import com.google.cloud.bigquery.StandardSQLTypeName._
import com.google.cloud.bigquery.{Field, Schema, StandardSQLTypeName}
import com.google.common.base.CaseFormat.{LOWER_CAMEL, LOWER_UNDERSCORE}

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime}
import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe.{Symbol, Type, TypeTag, typeOf}

object BigQuerySchema {
  def schema[T: TypeTag]: Schema =
    Schema.of(typeOf[T].members.flatMap(BigQuerySchema(_).asField).toSeq.reverse.asJava)
}

case class BigQuerySchema(member: Symbol) {

  def asField: Option[Field] =
    if (member.isMethod)
      None
    else
      Some {
        val fieldName = LOWER_CAMEL.to(LOWER_UNDERSCORE, member.name.toString.strip)

        Field
          .newBuilder(fieldName, sqlTypeName, subFields: _*)
          .setMode(
            if (isRepeated) REPEATED
            else NULLABLE
          )
          .build()
      }

  private val typeSignature = member.typeSignature

  // returns type T for Option[T] or List[T]
  private val consideredType: Type =
    typeSignature.typeArgs match {
      case Nil             => typeSignature
      case List(innerType) => innerType
      case _ => throw new IllegalArgumentException(s"$typeSignature : cannot handle more than one parametered type")
    }

  private val isRepeated: Boolean  = typeSignature.erasure <:< typeOf[Seq[Any]]
  private val isCaseClass: Boolean = consideredType.typeSymbol.isClass && consideredType.typeSymbol.asClass.isCaseClass

  private val subFields: Seq[Field] =
    if (isCaseClass) consideredType.members.flatMap(BigQuerySchema(_).asField).toSeq.reverse
    else Nil

  private val sqlTypeName: StandardSQLTypeName =
    consideredType match {
      case t if t =:= typeOf[Boolean]       => BOOL
      case t if t =:= typeOf[Int]           => INT64
      case t if t =:= typeOf[Double]        => NUMERIC
      case t if t =:= typeOf[Instant]       => TIMESTAMP
      case t if t =:= typeOf[LocalDate]     => DATE
      case t if t =:= typeOf[LocalTime]     => TIME
      case t if t =:= typeOf[LocalDateTime] => DATETIME
      case _ if isCaseClass                 => STRUCT
      case _                                => STRING
    }
}
