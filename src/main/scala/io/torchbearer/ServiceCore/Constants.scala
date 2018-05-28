package io.torchbearer.ServiceCore

import com.amazonaws.regions.Regions

/**
  * Created by fredricvollmer on 10/30/16.
  */
object Constants {
  val AWS_REGION = Regions.US_WEST_2
  val AWS_MTURK_REGION = Regions.US_EAST_1

  val S3_SV_IMAGE_BUCKET = "torchbearer-sv-images"
  val S3_CROPPED_IMAGES_BUCKET = "torchbearer-cropped-images"
  val S3_SALIENCY_MAP_BUCKET = "torchbearer-saliency-maps"

  val REDIS_HOST = "pub-redis-16209.us-west-2-1.1.ec2.garantiadata.com"
  val REDIS_PORT = 16209

  val HIT_STATUS_PROCESSING = "PROCESSING"
  val HIT_STATUS_UNKNOWN = "UNKNOWN"
  val HIT_STATUS_COMPLETE = "COMPLETE"

  val IMAGE_DISTANCE_AT = 0 / 5280.0
  val IMAGE_DISTANCE_JUST_BEFORE = 50 / 5280.0
  val IMAGE_DISTANCE_BEFORE = 100 / 5280.0

  val ActivityARNs = Map(
    "STREETVIEW_IMAGE_LOAD" -> "arn:aws:states:us-west-2:814009652816:activity:STREETVIEW_LOAD_IMAGE",
    "CV_GET_SALIENCY_MASK" -> "arn:aws:states:us-west-2:814009652816:activity:CV_GET_SALIENCY_MASK",
    "TURK_SALIENCY" -> "arn:aws:states:us-west-2:814009652816:activity:TURK_SALIENCY",
    "TURK_DESCRIPTION" -> "arn:aws:states:us-west-2:814009652816:activity:TURK_DESCRIPTION",
    "CV_DESCRIPTION" -> "arn:aws:states:us-west-2:814009652816:activity:CV_DESCRIPTION",
    "DB_DESCRIPTION" -> "arn:aws:states:us-west-2:814009652816:activity:DB_DESCRIPTION",
    "LANDMARK_DECIDERER" -> "arn:aws:states:us-west-2:814009652816:activity:LANDMARK_DECIDERER"
  )

  val PIPELINES_CV_CV = "CV-CV"
  val PIPELINES_CV_HUMAN = "CV-HUMAN"
  val PIPELINES_HUMAN_CV = "HUMAN-CV"
  val PIPELINES_HUMAN_HUMAN = "HUMAN-HUMAN"
  val PIPELINES_RAW = "RAW"

  val STATE_MACHINES_ARN_CV_CV = "arn:aws:states:us-west-2:814009652816:stateMachine:cv_cv"
  val STATE_MACHINES_ARN_CV_HUMAN = "arn:aws:states:us-west-2:814009652816:stateMachine:cv_human"
  val STATE_MACHINES_ARN_HUMAN_CV = "arn:aws:states:us-west-2:814009652816:stateMachine:human_cv"
  val STATE_MACHINES_ARN_HUMAN_HUMAN = "arn:aws:states:us-west-2:814009652816:stateMachine:human_human"

  val EXECUTION_POINT_TYPE_MANEUVER = "maneuver"
  val EXECUTION_POINT_TYPE_DESTINATION_LEFT = "destination_left"
  val EXECUTION_POINT_TYPE_DESTINATION_RIGHT = "destination_right"

  val POSITION_BEFORE = "before"
  val POSITION_JUST_BEFORE = "just_before"
  val POSITION_AT = "at"

  val SV_IMAGE_DIMS = (640, 640)
}
