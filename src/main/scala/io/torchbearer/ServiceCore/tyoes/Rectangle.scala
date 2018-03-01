package io.torchbearer.ServiceCore.tyoes

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.jgrapht._
import org.jgrapht.graph.{DefaultDirectedGraph, _}

/**
  * Created by fredricvollmer on 11/15/16.
  */
case class Rectangle (var x1:Int, var x2:Int, var y1:Int, var y2:Int) {
  def isOverlapping(r: Rectangle): Boolean = {
    if (this.x1 > r.x2
      || this.x2 < r.x1
      || this.y1 < r.y2
      || this.y2 > r.y1) {

      return false
    }
    true
  }
}

object Rectangle {
  implicit val formats = DefaultFormats

  def apply(jsonString: String): Rectangle = {
    val parsedJson = parse(jsonString)
    parsedJson.extract[Rectangle]
  }

  def apply(pointMap: Map[String, String]): Rectangle = {
    new Rectangle(pointMap.getOrElse("x1", "0").toInt,
      pointMap.getOrElse("x2", "0").toInt,
      pointMap.getOrElse("y1", "0").toInt,
      pointMap.getOrElse("y2", "0").toInt)
  }

  def createRectangleGraph(rects: List[Rectangle]): UndirectedGraph[Rectangle, DefaultEdge] = {
    val G = new SimpleGraph[Rectangle, DefaultEdge](classOf[DefaultEdge])
    rects.foreach(r => G.addVertex(r))

    rects.combinations(2).foreach(p => {
      if (p.head.isOverlapping(p.last)) {
        G.addEdge(p.head, p.last)
      }
    })

    G
  }
}
