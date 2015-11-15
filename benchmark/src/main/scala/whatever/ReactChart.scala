package whatever

import japgolly.scalajs.react._, vdom.prefix_<^._
import org.scalajs.dom.html.Canvas
import whatever.chartjs._

import scala.scalajs.js

object ReactChart {
  def newObj[T <: js.Object]: T =
    js.Object().asInstanceOf[T]

  case class ScalaBarData(
    labels: Vector[String],
    datasets: Vector[ScalaDataset]) {

    def toJs: BarData = {
      val d = newObj[BarData]
      d.labels = js.Array(labels: _*)
      d.datasets = js.Array(datasets.map(_.toJs): _*)
      d
    }
  }

  case class ScalaDataset(
    label: String,
    data: Vector[Chart.Value],
    fillColor: js.UndefOr[String] = js.undefined,
    strokeColor: js.UndefOr[String] = js.undefined,
    highlightFill: js.UndefOr[String] = js.undefined,
    highlightStroke: js.UndefOr[String] = js.undefined) {

    def toJs: Dataset = {
      val d = newObj[Dataset]
      d.label = label
      d.data = js.Array(data: _*)
      fillColor.foreach(d.fillColor = _)
      strokeColor.foreach(d.strokeColor = _)
      highlightFill.foreach(d.highlightFill = _)
      highlightStroke.foreach(d.highlightStroke = _)
      d
    }
  }

  case class Props(style: TagMod, data: ScalaBarData, options: Chart.BarOptions = newObj)

  type State = Option[BarChart]

  //val chartRef = Ref[]("c")

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props) =
      <.canvas(p.style)

    private def newChart(p: Props): CallbackTo[BarChart] =
      CallbackTo {
        val canvas = $.getDOMNode().domCast[Canvas]
        val c = new Chart(canvas.getContext("2d")).Bar(p.data.toJs, p.options)
js.Dynamic.global.ccc = c //////////////////////////////////
        c
      }

    def mount: Callback =
      $.props >>= newChart >>= (c => $ setState Some(c))

    def update(np: Props): Callback =
      for {
        c  <- $.state.asCBO[BarChart]
        op <- $.props
      } yield {

//        println(np.data)

        def xs = c.scale.xLabels.length

        while (xs > np.data.labels.length)
          c.removeData()

        for (i <- xs until np.data.labels.length) {
          val values = new js.Array[Chart.Value]()
          np.data.datasets.take(xs).foreach(values push _.data(i))
          c.addData(values, np.data.labels(i))
        }

        for {
          (d, i) <- np.data.datasets.iterator.zipWithIndex
          (v, j) <- d.data.iterator.zipWithIndex
        } {
          c.datasets.lift(i).flatMap(_.bars.toOption).flatMap(_.lift(j)) match {
            case Some(e) => e.value = v
            case None    =>
          }
        }

        c.scale.xLabels = np.data.labels.toJsArray
        c.scale.calculateXLabelRotation()
        c.update()
      }

    def unmount: Callback =
      for {
        c <- $.state.asCBO[BarChart]
        _ <- Callback(c.destroy())
        _ <- $.setState(None)
      } yield ()
  }

  val Comp = ReactComponentB[Props]("")
    .initialState[State](None)
    .renderBackend[Backend]
    .domType[Canvas]
    .componentDidMount(_.backend.mount)
    .componentWillUnmount(_.backend.unmount)
    .componentWillReceiveProps(x => x.$.backend.update(x.nextProps))
    .build

}
