package com.recipes

import cats.effect.IO
import cats.effect.Resource
import com.comcast.ip4s.*
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import fs2.io.net.Network
import com.recipes.repository.RecipeRepository
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.http4s.server.middleware.Logger

object GiveryChallengeServer:

  private def db: Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.fromHikariConfig({
      val config = new HikariConfig()
      config.setDriverClassName("org.sqlite.JDBC")
      config.setJdbcUrl("jdbc:sqlite:db.db")
      config
    })

  def errorHandler(prefix: String)(t: Throwable, msg: => String): IO[Unit] =
    IO.println(s"[$prefix]: $msg") >>
      IO.println(t) >>
      IO(t.printStackTrace())

  def run: IO[Nothing] = {
    for {
      transactor <- db
      repository = RecipeRepository(transactor)
      recipeAlg = Recipes.impl(repository)

      httpApp = (
        GiveryChallengeRoutes.recipeRoutes(recipeAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(
        ErrorHandling.Recover.total(
          ErrorAction.log(
            httpApp,
            messageFailureLogAction = errorHandler("message failure: "),
            serviceErrorLogAction = errorHandler("service error: "),
          ),
        ),
      )

      _ <-
        EmberServerBuilder
          .default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8182")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
