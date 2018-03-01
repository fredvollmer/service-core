package io.torchbearer.ServiceCore.tyoes

/**
  * Created by fredricvollmer on 2/20/17.
  */

/*
This case class is used to represent a landmark description, of the following JSON form:

{
  noun: String,
  adjectives: {
    adj: String
  },
  parts: [
    {
      noun: String,
      adjectives: {
        adj: String
      }
    }
  ]
}

 */

case class Description(
                        noun: String,
                        adjectives: Map[String, String],
                        parts: List[Description]
                      ) {

  /**
    * Retrieve the name of this landmark
    * @return String
    */
  def getNoun = noun

  /**
    * Retrieve a list of all adjectives describing the principal object, ordered by priority
    * @return
    */
  def getOrderedAdjectives: List[String] = {
    // TODO: Order these in a meaningful way
    adjectives.values.toList
  }

  /**
    * Retrieve a mapping of sub-noun name onto list of adjectives describing that sub-noun.
    * @return
    */
  def getParts: Map[String, List[String]] = {
    parts.map(p => p.noun -> p.getOrderedAdjectives).toMap
  }

  def getRealization: String = {
    "Coming soon!"
  }

}
