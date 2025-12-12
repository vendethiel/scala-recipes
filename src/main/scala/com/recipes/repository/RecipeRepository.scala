package com.recipes.repository

import cats.data.OptionT
import cats.data.EitherT
import doobie.Transactor
import fs2.Stream
import doobie.*
import doobie.implicits.*
import com.recipes.model.Recipe
import com.recipes.spec.RecipeSpec
import cats.effect.Concurrent

class RecipeRepository[F[_]: Concurrent](transactor: Transactor[F]) {
  def list: Stream[F, Recipe] =
    sql"SELECT rowid, title, making_time, serves, ingredients, cost, created_at, updated_at FROM recipe"
      .query[Recipe]
      .stream
      .transact(transactor)

  def get(id: Int): EitherT[F, RecipeNotFoundError.type, Recipe] =
    OptionT(
      sql"SELECT rowid, title, making_time, serves, ingredients, cost, created_at, updated_at FROM recipe WHERE rowid = $id"
        .query[Recipe]
        .option
        .transact(transactor),
    ).toRight(RecipeNotFoundError)

  def create(spec: RecipeSpec): F[Int] =
    sql"INSERT INTO recipe (title, making_time, serves, ingredients, cost, created_at, updated_at) values (${spec.title}, ${spec.makingTime}, ${spec.serves}, ${spec.ingredients}, ${spec.cost}, datetime(), datetime())".update
      .withUniqueGeneratedKeys[Int]("rowid")
      .transact(transactor)

  def update(id: Int, spec: RecipeSpec): F[Int] =
    sql"UPDATE recipe SET title = ${spec.title}, making_time = ${spec.makingTime}, serves = ${spec.serves}, ingredients = ${spec.ingredients}, cost = ${spec.cost}, updated_at = datetime() where rowid = $id".update.run
      .transact(transactor)

  def delete(id: Int): F[Int] =
    sql"DELETE FROM recipe WHERE rowid = $id".update.run.transact(transactor)
}

case object RecipeNotFoundError
