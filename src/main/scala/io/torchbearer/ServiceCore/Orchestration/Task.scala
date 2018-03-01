package io.torchbearer.ServiceCore.Orchestration

import io.torchbearer.ServiceCore.AWSServices.SFN
import com.amazonaws.services.stepfunctions.model
import com.amazonaws.services.stepfunctions.model.{SendTaskFailureRequest, SendTaskSuccessRequest}
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

/**
  * Created by fredricvollmer on 4/13/17.
  */
abstract class Task(epId:Int, hitId: Int, taskToken: String) {

  def run()

  def sendSuccess(output: (String, Any)*): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    // Append hitId and epId to output if needed
    var outputMap = output.toMap
    outputMap += ("epId" -> epId)
    outputMap += ("hitId" -> hitId)

    // Convert output map to json
    val outputString = write(outputMap)

    val sfn = SFN.getClient
    val req = new SendTaskSuccessRequest().withOutput(outputString).withTaskToken(taskToken)
    sfn.sendTaskSuccess(req)
  }

  def sendFailure(error: String, message: String): Unit = {
    val sfn = SFN.getClient
    val req = new SendTaskFailureRequest().withError(error).withCause(message).withTaskToken(taskToken)
    sfn.sendTaskFailure(req)
  }
}

object Task {
  def sendSuccess(taskToken: String, output: (String, Any)*): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    // Convert output map to json
    val outputString = write(output.toMap)

    val sfn = SFN.getClient
    val req = new SendTaskSuccessRequest().withOutput(outputString).withTaskToken(taskToken)
    sfn.sendTaskSuccess(req)
  }

  def sendFailure(taskToken: String, error: String, message: String = ""): Unit = {
    val sfn = SFN.getClient
    val req = new SendTaskFailureRequest().withError(error).withCause(message).withTaskToken(taskToken)
    sfn.sendTaskFailure(req)
  }
}
