package com.recipes

import cats.effect.IO
import com.recipes.model.Recipe
import com.recipes.spec.RecipeSpec
import com.recipes.repository.RecipeRepository
import com.recipes.repository.RecipeNotFoundError

trait Recipes:
  def list: IO[List[Recipe]]
  def get(id: Int): IO[Either[RecipeNotFoundError.type, Recipe]]
  def create(spec: RecipeSpec): IO[Int]
  def update(id: Int, spec: RecipeSpec): IO[Boolean]
  def delete(id: Int): IO[Boolean]

object Recipes:
  def apply(using ev: Recipes): Recipes = ev

  def impl(repository: RecipeRepository): Recipes = new Recipes:
    def list: IO[List[Recipe]] =
      repository.list.compile.toList

    def get(id: Int): IO[Either[RecipeNotFoundError.type, Recipe]] =
      repository.get(id).value

    def create(spec: RecipeSpec): IO[Int] =
      repository.create(spec)

    def update(id: Int, spec: RecipeSpec): IO[Boolean] =
      repository.update(id, spec).map(_ == 1) // 1 updated row

    def delete(id: Int): IO[Boolean] =
      repository.delete(id).map(_ == 1) // 1 deleted row

final case class RecipeError(e: Throwable) extends RuntimeException
