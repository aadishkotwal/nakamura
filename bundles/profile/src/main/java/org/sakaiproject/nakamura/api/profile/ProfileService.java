/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.profile;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 */
public interface ProfileService {



  /**
   * Gets the compact profile information from JCR and expands external resources
   * efficiently.
   *
   * @param authorizable
   *          The profile of this authorizable will be written out.
   * @param session
   *          A JCR Session that can be used to access the necessary nodes.
   *
   * @return A Map that represents the profile, or null if no profile was found.
   * @throws AccessDeniedException 
   */
  ValueMap getProfileMap(
      Authorizable authorizable,
      Session session) throws RepositoryException, StorageClientException, AccessDeniedException;

  ValueMap getProfileMap(Content profileContent, Session session) throws RepositoryException;

  ValueMap getProfileMap(
      org.apache.jackrabbit.api.security.user.Authorizable authorizable, Session session) throws RepositoryException;

  /**
   * Gets the compact profile information from JCR and expands external resources
   * efficiently.
   *
   * @param profileNode
   *          The node that represents the top level profile node.
   *
   * @return A Map that represents the profile.
   */
  ValueMap getCompactProfileMap(
      Authorizable authorizable,
      Session session) throws RepositoryException, StorageClientException,
      AccessDeniedException;


  ValueMap getCompactProfileMap(org.apache.jackrabbit.api.security.user.Authorizable au,
      Session session) throws RepositoryException;

  /**
   * Update the profile using a json tree to replace the existing tree.
   * @param session the current session
   * @param profilePath the path to the profile
   * @param json the json representing the new profile
   * @throws StorageClientException
   * @throws AccessDeniedException
   * @throws JSONException
   */
  void update(org.sakaiproject.nakamura.api.lite.Session session, String profilePath, JSONObject json) throws StorageClientException, AccessDeniedException, JSONException;


}
