package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.mturk.{AmazonMTurk, AmazonMTurkClientBuilder}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.mturk.model._
import io.torchbearer.ServiceCore.Constants

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by fredricvollmer on 11/10/16.
  */
object MechTurk {

  lazy private val client = createClient
  private val registeredNotificationsForHitTypes = mutable.Set[String]()

  private def createClient: AmazonMTurk = {
    val endpointUrl = if (sys.env.getOrElse("TURK_SANDBOX", "false").toBoolean)
      "https://mturk-requester-sandbox.us-east-1.amazonaws.com"
    else
      "https://mturk-requester.us-east-1.amazonaws.com"

    val endpointConfiguration = new EndpointConfiguration(endpointUrl, Constants.AWS_MTURK_REGION.getName)

    val credentials = Credentials.getCredentials
    val clientBuilder = AmazonMTurkClientBuilder
      .standard
      .withEndpointConfiguration(endpointConfiguration)

    if (credentials.isDefined) {
      val provider = new AWSStaticCredentialsProvider(credentials.get)
      clientBuilder.withCredentials(provider).build()
    }
    else {
      clientBuilder.build()
    }
  }

  def getClient: AmazonMTurk = client

  // Class extension of AmazonMTurk
  implicit class ExtendedAmazonMTurk(self: AmazonMTurk) {

    def getAllAssignmentsForHIT(hitId: String): List[Assignment] = {
      val req = new ListAssignmentsForHITRequest().withHITId(hitId)
      client.listAssignmentsForHIT(req).getAssignments.toList
    }

    def sendNotificationsToQueue(hitTypeId: String, queueURL: String): Unit = {
      // Only do this if we haven't already setup notifications for this hit type
      if (registeredNotificationsForHitTypes.contains(hitTypeId)) {
        return
      }

      registeredNotificationsForHitTypes += hitTypeId

      val spec = new NotificationSpecification()
        .withDestination(queueURL)
        .withTransport(NotificationTransport.SQS)
        .withVersion("2006-05-05")
        .withEventTypes(EventType.HITReviewable)

      val req = new UpdateNotificationSettingsRequest()
        .withActive(true)
        .withHITTypeId(hitTypeId)
        .withNotification(spec)

      client.updateNotificationSettings(req)
    }
  }
}
