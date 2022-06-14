/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.e2e;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.skywalking.e2e.profile.creation.ProfileTaskCreationRequest;
import org.apache.skywalking.e2e.profile.creation.ProfileTaskCreationResult;
import org.apache.skywalking.e2e.profile.creation.ProfileTaskCreationResultWrapper;
import org.apache.skywalking.e2e.profile.query.ProfileAnalyzation;
import org.apache.skywalking.e2e.profile.query.ProfileTaskQuery;
import org.apache.skywalking.e2e.profile.query.ProfileTasks;
import org.apache.skywalking.e2e.profile.query.ProfiledSegment;
import org.apache.skywalking.e2e.profile.query.Traces;
import org.apache.skywalking.e2e.trace.Trace;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("UnstableApiUsage")
public class ProfileClient extends SimpleQueryClient {
    public ProfileClient(String host, int port) {
        super(host, port);
    }

    public ProfileTaskCreationResult createProfileTask(final ProfileTaskCreationRequest creationRequest) throws Exception {
        final URL queryFileUrl = Resources.getResource("profileTaskCreation.gql");
        final String queryString =
            Resources.readLines(queryFileUrl, StandardCharsets.UTF_8)
                     .stream()
                     .filter(it -> !it.startsWith("#"))
                     .collect(Collectors.joining())
                     .replace("{serviceId}", String.valueOf(creationRequest.getServiceId()))
                     .replace("{endpointName}", creationRequest.getEndpointName())
                     .replace("{duration}", String.valueOf(creationRequest.getDuration()))
                     .replace("{startTime}", String.valueOf(creationRequest.getStartTime()))
                     .replace("{minDurationThreshold}", String.valueOf(creationRequest.getMinDurationThreshold()))
                     .replace("{dumpPeriod}", String.valueOf(creationRequest.getDumpPeriod()))
                     .replace("{maxSamplingCount}", String.valueOf(creationRequest.getMaxSamplingCount()));
        final ResponseEntity<GQLResponse<ProfileTaskCreationResultWrapper>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
            new ParameterizedTypeReference<GQLResponse<ProfileTaskCreationResultWrapper>>() {
            }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData().getCreationResult();
    }

    public ProfileTasks getProfileTaskList(final ProfileTaskQuery query) throws IOException {
        final URL queryFileUrl = Resources.getResource("profileTaskList.gql");
        final String queryString = Resources.readLines(queryFileUrl, StandardCharsets.UTF_8)
                                            .stream()
                                            .filter(it -> !it.startsWith("#"))
                                            .collect(Collectors.joining())
                                            .replace("{serviceId}", String.valueOf(query.serviceId()))
                                            .replace("{endpointName}", query.endpointName());
        final ResponseEntity<GQLResponse<ProfileTasks>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
            new ParameterizedTypeReference<GQLResponse<ProfileTasks>>() {
            }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData();
    }

    public List<Trace> getProfiledTraces(final String taskId) throws Exception {
        final URL queryFileUrl = Resources.getResource("profileTaskSegmentList.gql");
        final String queryString = Resources.readLines(queryFileUrl, StandardCharsets.UTF_8)
                                            .stream()
                                            .filter(it -> !it.startsWith("#"))
                                            .collect(Collectors.joining())
                                            .replace("{taskID}", taskId);
        final ResponseEntity<GQLResponse<Traces>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
            new ParameterizedTypeReference<GQLResponse<Traces>>() {
            }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData().getTraces();
    }

    public ProfiledSegment.ProfiledSegmentData getProfiledSegment(final String segmentId) throws IOException {
        final URL queryFileUrl = Resources.getResource("profiledSegment.gql");
        final String queryString = Resources.readLines(queryFileUrl, StandardCharsets.UTF_8)
                                            .stream()
                                            .filter(it -> !it.startsWith("#"))
                                            .collect(Collectors.joining())
                                            .replace("{segmentId}", segmentId);
        final ResponseEntity<GQLResponse<ProfiledSegment>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
            new ParameterizedTypeReference<GQLResponse<ProfiledSegment>>() {
            }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData().getSegment();
    }

    public ProfileAnalyzation getProfileAnalyzation(final String segmentId, long start, long end) throws IOException {
        final URL queryFileUrl = Resources.getResource("profileAnalyzation.gql");
        final String queryString = Resources.readLines(queryFileUrl, StandardCharsets.UTF_8)
                                            .stream()
                                            .filter(it -> !it.startsWith("#"))
                                            .collect(Collectors.joining())
                                            .replace("{segmentId}", segmentId)
                                            .replace("{start}", String.valueOf(start))
                                            .replace("{end}", String.valueOf(end));
        final ResponseEntity<GQLResponse<ProfileAnalyzation>> responseEntity = restTemplate.exchange(
            new RequestEntity<>(queryString, HttpMethod.POST, URI.create(endpointUrl)),
            new ParameterizedTypeReference<GQLResponse<ProfileAnalyzation>>() {
            }
        );

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Response status != 200, actual: " + responseEntity.getStatusCode());
        }

        return Objects.requireNonNull(responseEntity.getBody()).getData();
    }
}
