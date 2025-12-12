package com.recipes

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
import org.http4s.server.middleware.Logger as LoggerMiddleware
import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory

object RecipesServer:

  private def db[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    HikariTransactor.fromHikariConfig({
      val config = new HikariConfig()
      config.setDriverClassName("org.sqlite.JDBC")
      config.setJdbcUrl("jdbc:sqlite:db.db")
      config
    })

  def errorHandler[F[_]: LoggerFactory](prefix: String) =
    val logger = LoggerFactory[F].getLogger
    def log(t: Throwable, msg: => String): F[Unit] =
      logger.error(Map.empty, t)(s"[$prefix]: $msg")
    log

  def run[F[_]: Async: Network: LoggerFactory]: F[Nothing] = {
    for {
      transactor <- db[F]
      repository = RecipeRepository[F](transactor)
      recipeAlg = Recipes.impl[F](repository)

      httpApp = (
        RecipesRoutes.recipeRoutes[F](recipeAlg)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = LoggerMiddleware.httpApp(true, true)(
        ErrorHandling.Recover.total(
          ErrorAction.log(
            httpApp,
            messageFailureLogAction = errorHandler[F]("message failure: "),
            serviceErrorLogAction = errorHandler[F]("service error: "),
          ),
        ),
      )

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8182")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
