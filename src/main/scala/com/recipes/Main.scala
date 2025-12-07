package com.recipes

import cats.effect.IOApp

object Main extends IOApp.Simple:
  val run = GiveryChallengeServer.run
