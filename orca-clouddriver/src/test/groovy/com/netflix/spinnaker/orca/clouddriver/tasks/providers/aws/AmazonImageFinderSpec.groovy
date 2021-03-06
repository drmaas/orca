/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.netflix.spinnaker.orca.clouddriver.tasks.providers.aws

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.orca.clouddriver.OortService
import com.netflix.spinnaker.orca.pipeline.model.Pipeline
import com.netflix.spinnaker.orca.pipeline.model.PipelineStage
import spock.lang.Specification
import spock.lang.Subject

import java.util.stream.Collectors

class AmazonImageFinderSpec extends Specification {
  def objectMapper = new ObjectMapper()
  def oortService = Mock(OortService)

  @Subject
  def amazonImageFinder = new AmazonImageFinder(objectMapper: objectMapper, oortService: oortService)

  def "should prepend tag keys with 'tag:'"() {
    expect:
    amazonImageFinder.prefixTags([engine: "spinnaker"]) == ["tag:engine": "spinnaker"]
  }

  def "should match most recently created image per region"() {
    given:
    def stage = new PipelineStage(new Pipeline(), "", [
      regions: ["us-west-1", "us-west-2"]
    ])

    when:
    def imageDetails = amazonImageFinder.byTags(stage, "mypackage", ["engine": "spinnaker"])

    then:
    1 * oortService.findImage("aws", "mypackage", null, null, ["tag:engine": "spinnaker"]) >> {
      [
        [
          imageName: "image-0",
          attributes: [creationDate: bCD("2015")],
          amis: [
            "us-west-1": ["ami-0"],
            "us-west-2": ["ami-1"]
          ]
        ],
        [
          imageName: "image-2",
          attributes: [creationDate: bCD("2016")],
          amis: [
            "us-west-1": ["ami-2"]
          ]
        ],
        [
          imageName: "image-3",
          attributes: [creationDate: bCD("2016")],
          amis: [
            "us-west-2": ["ami-3"]
          ]
        ]
      ]
    }
    0 * _

    imageDetails.size() == 2
    imageDetails.find { it.region == "us-west-1" }.imageId == "ami-2"
    imageDetails.find { it.region == "us-west-1" }.imageName == "image-2"
    imageDetails.find { it.region == "us-west-2" }.imageId == "ami-3"
    imageDetails.find { it.region == "us-west-2" }.imageName == "image-3"
  }

  def "should sort images by 'creationDate'"() {
    given:
    def images = creationDates.collect {
      new AmazonImageFinder.AmazonImage(attributes: [
        creationDate: it
      ])
    }

    expect:
    images.stream().sorted().collect(Collectors.toList())*.attributes*.creationDate == expectedOrder

    where:
    creationDates                                              || expectedOrder
    [bCD("2016"), bCD("2014"), bCD("2012"), bCD("2015")]       || [bCD("2016"), bCD("2015"), bCD("2014"), bCD("2012")]
    [bCD("2016"), bCD("2014"), null, bCD("2012"), bCD("2015")] || [bCD("2016"), bCD("2015"), bCD("2014"), bCD("2012"), null]
  }

  String bCD(String year) {
    return "${year}-07-28T20:07:21.000Z"
  }
}
