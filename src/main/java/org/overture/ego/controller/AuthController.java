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

package org.overture.ego.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.overture.ego.model.entity.User;
import org.overture.ego.service.UserService;
import org.overture.ego.provider.google.GoogleTokenService;
import org.overture.ego.provider.facebook.FacebookTokenService;
import org.overture.ego.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth")
public class AuthController {

  @Autowired
  TokenService tokenService;
  @Autowired
  UserService userService;
  @Autowired
  GoogleTokenService googleTokenService;
  @Autowired
  SimpleDateFormat formatter;
  @Autowired
  FacebookTokenService facebookTokenService;

  @RequestMapping(method = RequestMethod.GET, value = "/google/token")
  @ResponseStatus(value = HttpStatus.OK)
  @SneakyThrows
  public @ResponseBody
  String exchangeGoogleTokenForAuth(
      @RequestHeader(value = "id_token", required = true) final String idToken) {
    if (!googleTokenService.validToken(idToken))
      throw new Exception("Invalid user token:" + idToken);
    val authInfo = googleTokenService.decode(idToken);
    return generateUserToken(authInfo);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/facebook/token")
  @ResponseStatus(value = HttpStatus.OK)
  @SneakyThrows
  public @ResponseBody
  String exchangeFacebookTokenForAuth(
          @RequestHeader(value = "id_token", required = true) final String idToken) {
    if (!facebookTokenService.validToken(idToken))
      throw new Exception("Invalid user token:" + idToken);
    val authInfo = facebookTokenService.getAuthInfo(idToken);
    if(authInfo.isPresent()) {
      return generateUserToken(authInfo.get());
    } else {
      throw new Exception("Unable to generate auth token for this user");
    }

  }

  @RequestMapping(method = RequestMethod.GET, value = "/token/verify")
  @ResponseStatus(value = HttpStatus.OK)
  @SneakyThrows
  public @ResponseBody
  boolean verifyJWToken(
      @RequestHeader(value = "token", required = true) final String token) {
    if (token == null || token.isEmpty()) return false;
    return tokenService.validateToken(token);
  }

  private String generateUserToken(Map authInfo){
    val userName = authInfo.get("email").toString();
    User user = userService.getByName(userName);
    if (user == null) {
      user = createNewUser(userName,authInfo);
      userService.create(user);
    }
    return tokenService.generateToken(user);
  }

  private User createNewUser(String userName, Map authInfo) {
    String role = "USER";
    String status = "Pending";


    User u = new User();
            u.setName(userName);
            u.setEmail(userName);
            u.setFirstName(authInfo.containsKey("given_name") ? authInfo.get("given_name").toString() : "");
            u.setLastName(authInfo.containsKey("family_name") ? authInfo.get("family_name").toString() : "");
            u.setStatus(status);
            u.setCreatedAt(formatter.format(new Date()));
            u.setLastLogin(null);
            u.setRole(role);
    return u;
  }
}
