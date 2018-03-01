package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.simplesystemsmanagement.model.{GetParametersRequest, GetParametersResult}
import com.amazonaws.services.simplesystemsmanagement.{AWSSimpleSystemsManagement, AWSSimpleSystemsManagementClientBuilder}
import io.torchbearer.ServiceCore.Constants
import io.torchbearer.ServiceCore.Utils.getInstanceId

/**
  * Created by fredricvollmer on 11/10/16.
  */
object KeyStore {

  lazy private val client = createClient

  private def createClient: AWSSimpleSystemsManagement = {
    val credentials = Credentials.getCredentials
    val clientBuilder = AWSSimpleSystemsManagementClientBuilder.standard.withRegion(Constants.AWS_REGION)

    if (credentials.isDefined) {
      val provider = new AWSStaticCredentialsProvider(credentials.get)
      clientBuilder.withCredentials(provider).build()
    }
    else {
      clientBuilder.build()
    }
  }

  def getClient: AWSSimpleSystemsManagement = client

  def getKey(key: String): String = {
    val client = getClient
    val req = new GetParametersRequest().withNames(key).withWithDecryption(true)
    val results = client.getParameters(req)
    results.getParameters.get(0).getValue
  }
}
