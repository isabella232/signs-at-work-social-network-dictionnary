package com.orange.spring.demo.biz.view.controller;

/*
 * #%L
 * Spring demo
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import com.orange.spring.demo.biz.domain.Community;
import com.orange.spring.demo.biz.domain.Request;
import com.orange.spring.demo.biz.domain.User;
import com.orange.spring.demo.biz.persistence.model.RequestDB;
import com.orange.spring.demo.biz.persistence.model.UserDB;
import com.orange.spring.demo.biz.persistence.repository.RequestRepository;
import com.orange.spring.demo.biz.persistence.repository.UserRepository;
import com.orange.spring.demo.biz.persistence.service.CommunityService;
import com.orange.spring.demo.biz.persistence.service.MessageByLocaleService;
import com.orange.spring.demo.biz.persistence.service.RequestService;
import com.orange.spring.demo.biz.persistence.service.UserService;
import com.orange.spring.demo.biz.security.AppSecurityAdmin;
import com.orange.spring.demo.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

  @Autowired
  private UserService userService;
  @Autowired
  private CommunityService communityService;
  @Autowired
  private RequestService requestService;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @RequestMapping("/")
  public String index(Principal principal, Model model) {
    setAuthenticated(principal, model);
    model.addAttribute("title", messageByLocaleService.getMessage("welcome"));
    return "index";
  }

  @Secured("ROLE_USER")
  @RequestMapping("/users")
  public String users(Model model) {

    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("users"));
    model.addAttribute("users", UserView.from(userService.all()));
    return "users";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/communities")
  public String communities(Model model) {

    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    model.addAttribute("communities", CommunityView.from(communityService.all()));
    return "communities";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{id}")
  public String userDetails(@PathVariable long id, Model model) {
    User user = userService.withId(id);

    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("user_details"));

    UserProfileView userProfileView = new UserProfileView(user, communityService);
    model.addAttribute("userProfileView", userProfileView);

    model.addAttribute("requestView", new RequestView());
    model.addAttribute("favoriteView", new FavoriteView());
    model.addAttribute("signView", new SignView());

    return "user";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{userId}/add/communities", method = RequestMethod.POST)
  /**
   * We retrieve all form parameters directly from the raw request since in this case
   * we can not rely on a json object deserialization.
   * Indeed, POST form parameters look like this:
   *  - userCommunitiesIds -> "12"
   *  - userCommunitiesIds -> "34"
   *  - ...
   *  which in this case means that the user belongs to communities with id 12 & 34
   *
   *  Then we resend the user details page
   */
  public String changeUserCommunities(
          HttpServletRequest req, @PathVariable long userId, Model model) {

    List<Long> communitiesIds =
            transformCommunitiesIdsToLong(req.getParameterMap().get("userCommunitiesIds"));

    userService.changeUserCommunities(userId, communitiesIds);

    return userDetails(userId, model);
  }


  /** The form POST provides Ids as String, we convert it back to Long */
  private List<Long> transformCommunitiesIdsToLong(String[] userCommunitiesIds) {
    if (userCommunitiesIds == null) {
      return new ArrayList<>();
    }
    return Arrays.asList(userCommunitiesIds).stream()
            .map(communityIdString -> Long.parseLong(communityIdString))
            .collect(Collectors.toList());
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{userId}/add/request", method = RequestMethod.POST)
  public String createUserRequest(
          HttpServletRequest req, @PathVariable long userId, Model model) {

    String requestName = req.getParameter("requestName");
    if (requestService.withName(requestName).list().isEmpty()) {
      userService.createUserRequest(userId, requestName);

    }

    return userDetails(userId, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{userId}/add/favorite", method = RequestMethod.POST)
  public String createUserFavorite(
          HttpServletRequest req, @PathVariable long userId, Model model) {

    String favoriteName = req.getParameter("favoriteName");
    userService.createUserFavorite(userId, favoriteName);


    return userDetails(userId, model);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/user/{userId}/add/sign", method = RequestMethod.POST)
  public String createUserSignVideo(
          HttpServletRequest req, @PathVariable long userId, Model model) {

    String signName = req.getParameter("name");
    String signUrl = req.getParameter("url");
    userService.createUserSignVideo(userId, signName, signUrl);

    return userDetails(userId, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/community/{id}")
  public String community(@PathVariable long id, Model model) {

    Community community = communityService.withId(id);
    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("community_details"));
    model.addAttribute("community", community);
    return "community";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping("/admin")
  public String admin(Model model) {
    setAuthenticated(true, model);
    model.addAttribute("title", messageByLocaleService.getMessage("admin_page"));
    // for thymeleaf form management
    model.addAttribute("user", new UserCreationView());
    model.addAttribute("community", new CommunityView());
    return "admin";
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/admin/user/create", method = RequestMethod.POST)
  public String user(@ModelAttribute UserCreationView userCreationView, Model model) {
    User user = userService.create(userCreationView.toUser(), userCreationView.getPassword());
    return userDetails(user.id, model);
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = "/admin/community/create", method = RequestMethod.POST)
  public String community(@ModelAttribute CommunityView communityView, Model model) {
    Community community = communityService.create(communityView.toCommunity());
    return  community(community.id, model);

  }


  private void setAuthenticated(Principal principal, Model model) {
    boolean authenticated = principal != null && principal.getName() != null;
    setAuthenticated(authenticated, model);
    model.addAttribute("authenticatedUsername",
            authenticated ? principal.getName() : "Please sign in");
    model.addAttribute("isAdmin", authenticated && isAdmin(principal));
    if ((authenticated) && !(isAdmin(principal))) {
      model.addAttribute("user", userService.withUserName(principal.getName()));
    }
  }

  private void setAuthenticated(boolean isAuthenticated, Model model) {
    model.addAttribute("isAuthenticated", isAuthenticated);
  }

  private boolean isAdmin(Principal principal) {
    return AppSecurityAdmin.isAdmin(principal.getName());
  }
}
