/*
 * Copyright (C) 2017 Pluralsight, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hydra.spark.sources.kafka

import hydra.spark.api.InvalidDslException
import hydra.spark.util.KafkaUtils
import kafka.api.OffsetRequest
import kafka.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition

import scala.util.Try

/**
  * Created by alexsilva on 12/12/16.
  */
object Offsets {

  type TPO = Map[TopicPartition, (Long, Long)]

  /**
    * Indicates the last offset consumed by a consumer group.
    */
  val LastTimeString = "last"

  val LastSeen = -3L

  def stringToNumber(value: Option[Any], defaultValue: Long): Long = {
    value.map(x => x match {
      case OffsetRequest.SmallestTimeString => OffsetRequest.EarliestTime
      case OffsetRequest.LargestTimeString => OffsetRequest.LatestTime
      case LastTimeString => LastSeen
      case time => Try(time.toString.toLong)
        .recover { case t: Throwable => throw InvalidDslException(s"$time is not a valid offset.") }.get
    }).getOrElse(defaultValue)
  }

  def offsetRange(topic: String, start: Long, stop: Long, params: Map[String, String]): TPO = {
    import hydra.spark.util.Collections._
    val cfg = new ConsumerConfig(params)
    offsetRange(topic, start, stop, cfg)
  }

  def offsetRange(topic: String, start: Long, stop: Long, cfg: ConsumerConfig): TPO = {
    if (start == LastSeen) //-3 is a special case
      KafkaUtils.lastGroupOffsets(topic, cfg, stop)
    else
      KafkaUtils.offsetRange(topic, start, stop, cfg)
  }
}
