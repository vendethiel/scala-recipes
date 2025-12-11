package com.recipes.spec

import cats.effect.Concurrent
import io.circe.Decoder
import org.http4s.circe.jsonOf
import org.http4s.EntityDecoder
import com.recipes.model.Recipe

final case class RecipeSpec(
  title: String,
  makingTime: String,
  serves: String,
  ingredients: String,
  cost: Int,
) {
  def asModel(id: Int): Recipe =
    val now = java.time.LocalDateTime.now.toString
    Recipe(
      id = id,
      title = title,
      makingTime = makingTime,
      serves = serves,
      ingredients = ingredients,
      cost = cost,
      createdAt = now,
      updatedAt = now,
    )
}

object RecipeSpec:
  given Decoder[RecipeSpec] = Decoder.instance[RecipeSpec] { o =>
    for {
      title <- o.get[String]("title")
      makingTime <- o.get[String]("making_time")
      serves <- o.get[String]("serves")
      ingredients <- o.get[String]("ingredients")
      cost <- o.get[Int]("cost")
    } yield RecipeSpec(title, makingTime, serves, ingredients, cost)
  }

  given [F[_]: Concurrent]: EntityDecoder[F, RecipeSpec] = jsonOf
