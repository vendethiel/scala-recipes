package com.recipes

import com.recipes.model.Recipe
import com.recipes.spec.RecipeSpec
import com.recipes.repository.RecipeRepository
import com.recipes.repository.RecipeNotFoundError
import cats.effect.Concurrent
import cats.syntax.all.*

trait Recipes[F[_]]:
  def list: F[List[Recipe]]
  def get(id: Int): F[Either[RecipeNotFoundError.type, Recipe]]
  def create(spec: RecipeSpec): F[Int]
  def update(id: Int, spec: RecipeSpec): F[Boolean]
  def delete(id: Int): F[Boolean]

object Recipes:
  def apply[F[_]](using ev: Recipes[F]): Recipes[F] = ev

  def impl[F[_]: Concurrent](repository: RecipeRepository[F]): Recipes[F] = new Recipes:
    def list: F[List[Recipe]] =
      repository.list.compile.toList

    def get(id: Int): F[Either[RecipeNotFoundError.type, Recipe]] =
      repository.get(id).value

    def create(spec: RecipeSpec): F[Int] =
      repository.create(spec)

    def update(id: Int, spec: RecipeSpec): F[Boolean] =
      repository.update(id, spec).map(_ == 1) // 1 updated row

    def delete(id: Int): F[Boolean] =
      repository.delete(id).map(_ == 1) // 1 deleted row

final case class RecipeError(e: Throwable) extends RuntimeException
