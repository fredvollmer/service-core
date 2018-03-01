package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.stepfunctions.model.{GetActivityTaskRequest, GetActivityTaskResult}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.torchbearer.ServiceCore.Constants
import io.torchbearer.ServiceCore.Utils.getInstanceId

/**
  * Created by fredricvollmer on 11/10/16.
  */
object S3 {

  lazy private val client = createClient

  private def createClient: AmazonS3 = {
    val credentials = Credentials.getCredentials
    val clientBuilder = AmazonS3ClientBuilder.standard.withRegion(Constants.AWS_REGION)

    if (credentials.isDefined) {
      val provider = new AWSStaticCredentialsProvider(credentials.get)
      clientBuilder.withCredentials(provider).build()
    }
    else {
      clientBuilder.build()
    }
  }

  def getClient: AmazonS3 = client
}
