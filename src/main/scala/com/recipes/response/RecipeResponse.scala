package com.recipes.response

import io.circe.Encoder
import org.http4s.circe.jsonEncoderOf
import org.http4s.EntityEncoder
import com.recipes.model.Recipe

final case class RecipeResponse(
  id: Int,
  title: String,
  makingTime: String,
  serves: String,
  ingredients: String,
  cost: Int,
)

object RecipeResponse:
  given Encoder[RecipeResponse] = Encoder.AsObject.derived[RecipeResponse]

  given [F[_]]: EntityEncoder[F, RecipeResponse] = jsonEncoderOf

  def fromModel(recipe: Recipe) = RecipeResponse(
    id = recipe.id,
    title = recipe.title,
    makingTime = recipe.makingTime,
    serves = recipe.serves,
    ingredients = recipe.ingredients,
    cost = recipe.cost,
  )
