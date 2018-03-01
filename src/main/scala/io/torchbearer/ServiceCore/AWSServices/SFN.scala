package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.stepfunctions.model.{GetActivityTaskRequest, GetActivityTaskResult, StartExecutionRequest}
import com.amazonaws.services.stepfunctions.{AWSStepFunctions, AWSStepFunctionsClientBuilder}
import io.torchbearer.ServiceCore.Constants
import io.torchbearer.ServiceCore.Utils.getInstanceId
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

/**
  * Created by fredricvollmer on 11/10/16.
  */
object SFN {

  lazy private val client = createClient

  private def createClient: AWSStepFunctions = {
    val credentials = Credentials.getCredentials

    // Requests for getting an activity can last up to 60 seconds (long poll)
    // We don't want to throw a timeout exception until after that time
    val clientConfig = new ClientConfiguration()
      .withRequestTimeout(65 * 1000)
      .withSocketTimeout(65 * 1000)
    val clientBuilder = AWSStepFunctionsClientBuilder.standard
      .withRegion(Constants.AWS_REGION)
      .withClientConfiguration(clientConfig)

    if (credentials.isDefined) {
      val provider = new AWSStaticCredentialsProvider(credentials.get)
      clientBuilder.withCredentials(provider).build()
    }
    else {
      clientBuilder.build()
    }
  }

  def getClient: AWSStepFunctions = client

  def getTaskForActivityArn(arn: String): GetActivityTaskResult = {
    val client = getClient
    val req = new GetActivityTaskRequest().withActivityArn(arn).withWorkerName(getInstanceId)
    val t = client.getActivityTask(req)
    t
  }

  def startExecution(stateMachineARN: String, input: (String, Any)*): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val inputString = write(input.toMap)
    val epId = input.find(t => t._1 == "epId").map(t => t._2) getOrElse ""
    val hitId = input.find(t => t._1 == "hitId").map(t => t._2) getOrElse ""
    val guid = java.util.UUID.randomUUID.toString
    val name = s"ep-$epId-hit-$hitId-$guid"
    val req = new StartExecutionRequest().withInput(inputString).withName(name).withStateMachineArn(stateMachineARN)
    client.startExecution(req)
  }

  def getStateMachineArnForPipeline(pipeline: String): String = {
    pipeline match {
      case s if s matches s"(?i)${Constants.PIPELINES_CV_CV}" => Constants.STATE_MACHINES_ARN_CV_CV
      case s if s matches s"(?i)${Constants.PIPELINES_CV_HUMAN}" => Constants.STATE_MACHINES_ARN_CV_HUMAN
      case s if s matches s"(?i)${Constants.PIPELINES_HUMAN_CV}" => Constants.STATE_MACHINES_ARN_HUMAN_CV
      case s if s matches s"(?i)${Constants.PIPELINES_HUMAN_HUMAN}" => Constants.STATE_MACHINES_ARN_HUMAN_HUMAN
    }
  }
}
