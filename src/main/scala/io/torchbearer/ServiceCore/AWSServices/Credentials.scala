package io.torchbearer.ServiceCore.AWSServices

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider

/**
  * Created by fredricvollmer on 11/10/16.
  */
object Credentials {
  // Load AWS credentials
  private var credentials: Option[AWSCredentials] = None

  // Only load credentials if we're not in production env
  if (sys.env.getOrElse("ENVIRONMENT", "production") != "production" ) {
    try {
      credentials = Some(new ProfileCredentialsProvider("torchbearer").getCredentials)
    } catch {
      case _: Throwable => throw new AmazonClientException(
        "Cannot load the credentials from the credential profiles file. " +
          "Please make sure that your credentials file is at the correct " +
          "location (~/.aws/credentials), and is in valid format.")
    }
  }

  def getCredentials = credentials

}
