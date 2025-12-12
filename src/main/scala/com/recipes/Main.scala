package com.recipes

import cats.effect.IOApp
import org.typelevel.log4cats.LoggerFactory
import cats.effect.IO
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp.Simple:
  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

  val run = RecipesServer.run
