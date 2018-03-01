package io.torchbearer.ServiceCore.Redis

import com.redis._
import io.torchbearer.ServiceCore.Constants

/**
  * Created by fredricvollmer on 1/29/17.
  */
object RedisClient {
  private val r = new RedisClient(Constants.REDIS_HOST, Constants.REDIS_PORT)
  r.auth("7fchyj156")

  def getClient = r
}
