/*
 * Copyright (c) 2017. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.overture.ego.repository.queryspecification;

import lombok.val;
import org.overture.ego.common.Utils;
import org.overture.ego.model.entity.Application;
import org.overture.ego.model.entity.Group;
import org.overture.ego.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;

public class UserSpecification {

  public static Specification<User> containsText(String text) {
    val finalText = Utils.prepareForQuery(text);
    return (root, query, builder) -> builder.or(
            builder.like(builder.lower(root.get("name")), finalText),
            builder.like(builder.lower(root.get("email")), finalText),
            builder.like(builder.lower(root.get("firstName")), finalText),
            builder.like(builder.lower(root.get("lastName")), finalText)

    );
  }

  public static Specification<User> inGroup(Integer groupId) {
    val finalGroupId = groupId;
    return (root, query, builder) ->
    {
      Join<User, Group> groupJoin = root.join("groups");
      return builder.equal(groupJoin.<Integer> get("id"), groupId);
    };

  }

  public static Specification<User> ofApplication(Integer appId) {
    val finalAppId = appId;
    return (root, query, builder) ->
    {
      Join<User, Application> applicationJoin = root.join("applications");
      return builder.equal(applicationJoin.<Integer> get("id"), finalAppId);
    };

  }
}
