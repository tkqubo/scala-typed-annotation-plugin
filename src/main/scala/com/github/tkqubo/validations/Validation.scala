package com.github.tkqubo.validations

sealed abstract class Validation[+E, +A] {

  def isSuccess: Boolean = this match {
    case Success(_) => true
    case Failure(_) => false
  }

  def isFailure: Boolean = !isSuccess
}

final case class Success[A](a: A) extends Validation[Nothing, A]
final case class Failure[E](es: Seq[E]) extends Validation[E, Nothing]

object Failure {
  def apply[E](e: E): Failure[E] = Failure(Seq(e))
}
