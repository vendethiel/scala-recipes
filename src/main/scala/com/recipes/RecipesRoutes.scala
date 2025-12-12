package com.recipes

import io.circe.syntax.*
import com.recipes.repository.RecipeNotFoundError
import com.recipes.response.RecipeResponse
import com.recipes.spec.RecipeSpec
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import cats.implicits.*
import cats.effect.Concurrent

object RecipesRoutes:

  def recipeRoutes[F[_]: Concurrent](J: Recipes[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "recipes" =>
        for {
          recipes <- J.list
          resp <- Ok(Map("recipes" -> recipes))
        } yield resp

      case GET -> Root / "recipes" / IntVar(id) =>
        for {
          recipe <- J.get(id)
          resp <- recipe match {
            case Left(RecipeNotFoundError) => NotFound()
            case Right(recipe) =>
              Ok(
                Map(
                  "message" -> "Recipe details by id".asJson,
                  "recipe" -> Seq(RecipeResponse.fromModel(recipe)).asJson,
                ),
              )
          }
        } yield resp

      case req @ POST -> Root / "recipes" =>
        req.attemptAs[RecipeSpec].value flatMap {
          _ match
            case Left(_err) =>
              Ok(
                Map(
                  "message" -> "Recipe creation failed!",
                  "required" -> "title, making_time, serves, ingredients, cost",
                ),
              )
            case Right(spec) =>
              for {
                created <- J.create(spec)
                recipe = spec.asModel(created)
                resp <- Ok(
                  Map(
                    "message" -> "Recipe successfully created!".asJson,
                    "recipe" -> Seq(recipe).asJson,
                  ),
                )
              } yield resp
        }

      case req @ PATCH -> Root / "recipes" / IntVar(id) =>
        for {
          spec <- req.as[RecipeSpec]
          updated <- J.update(id, spec)
          recipe = spec.asModel(id)
          resp <-
            if updated
            then
              Ok(
                Map(
                  "message" -> "Recipe successfully updated!".asJson,
                  "recipe" -> Seq(recipe).asJson,
                ),
              )
            else
              Ok(
                (
                  "message" -> "Recipe not found!"
                ),
              )
        } yield resp

      case DELETE -> Root / "recipes" / IntVar(id) =>
        J.delete(id) flatMap { deleted =>
          if deleted
          then
            Ok(
              Map(
                "message" -> "Recipe successfully removed!",
              ),
            )
          else
            Ok(
              Map(
                "message" -> "No recipe found",
              ),
            )
        }
    }
