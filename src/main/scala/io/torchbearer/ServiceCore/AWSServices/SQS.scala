package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sqs.AmazonSQSClient
import io.torchbearer.ServiceCore.Constants
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

/**
  * Created by fredricvollmer on 11/10/16.
  */
object SQS {

  lazy private val client = createClient

  private def createClient: AmazonSQSClient = {
    val q = Credentials.getCredentials
    val sqsClient = Credentials.getCredentials map (c => new AmazonSQSClient(c)) getOrElse new AmazonSQSClient
    val region = Region.getRegion(Constants.AWS_REGION)
    sqsClient.setRegion(region)

    sqsClient
  }

  def getClient: AmazonSQSClient = client
}
