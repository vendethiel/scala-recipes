package com.recipes.model

import cats.effect.Concurrent
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Recipe(
  id: Int,
  title: String,
  makingTime: String,
  serves: String,
  ingredients: String,
  cost: Int,
  createdAt: String,
  updatedAt: String,
)

object Recipe:
  given Decoder[Recipe] = Decoder.derived[Recipe]

  given [F[_]: Concurrent]: EntityDecoder[F, Recipe] = jsonOf

  given Encoder[Recipe] = Encoder.AsObject.derived[Recipe]

  given [F[_]]: EntityEncoder[F, Recipe] = jsonEncoderOf
