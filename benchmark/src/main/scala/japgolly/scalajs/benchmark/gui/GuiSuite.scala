package japgolly.scalajs.benchmark.gui

import japgolly.scalajs.benchmark._
import japgolly.scalajs.react.ReactElement
import monocle.Lens

/**
  * A suite of benchmarks with additional info required to slap a GUI on top and present it to the user.
  *
  * If you don't need a GUI, then a plain [[Suite]] is all you need.
  */
final class GuiSuite[P](val suite: Suite[P], val params: GuiParams[P], val desc: Option[ReactElement]) {
  @inline def name = suite.name

  def describe(e: ReactElement): GuiSuite[P] =
    new GuiSuite(suite, params, Some(e))
}

object GuiSuite {
  def suite[P]: Lens[GuiSuite[P], Suite[P]] =
    Lens((_: GuiSuite[P]).suite)(s => g => new GuiSuite(s, g.params, g.desc))

  def apply(suite: Suite[Unit]): GuiSuite[Unit] =
    new GuiSuite(suite, GuiParams.none, None)

  def apply[P](suite: Suite[P], params: GuiParams[P]): GuiSuite[P] =
    new GuiSuite(suite, params, None)
}
