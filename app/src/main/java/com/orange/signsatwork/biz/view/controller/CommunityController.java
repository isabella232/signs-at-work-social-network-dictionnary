package com.orange.signsatwork.biz.view.controller;

/*
 * #%L
 * Signs at work
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

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.view.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CommunityController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Value("${app.name}")
  String appName;

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/communities")
  public String communities(Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    List<Object[]> queryCommunities = services.community().forCommunitiesUser(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .sorted((c1, c2) -> c1.name.compareTo(c2.name))
      .collect(Collectors.toList());
    model.addAttribute("title", messageByLocaleService.getMessage("communities"));
    model.addAttribute("communities", communitiesViewData);
    model.addAttribute("appName", appName);

    return "communities";
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/community/{communityId}")
  public String community(@PathVariable long communityId, Model model, Principal principal)  {
    User user = services.user().withUserName(principal.getName());


    Community community = services.community().withId(communityId);
    if (community == null) {
      return "redirect:/sec/communities";
    }
    model.addAttribute("title", messageByLocaleService.getMessage("community.title", new Object[]{community.name}));
    model.addAttribute("backUrl", "/sec/communities");
    model.addAttribute("community", community);
    Boolean iBelowToCommunity = community.users.stream().anyMatch( u-> u.id == user.id);
    model.addAttribute("iBelowToCommunity", iBelowToCommunity);
    model.addAttribute("userId", user.id);
    model.addAttribute("appName", appName);

    return "community";
  }

    @Secured("ROLE_USER")
    @RequestMapping(value = "/sec/community/create", method = RequestMethod.POST)
    public String createCommunity(@ModelAttribute CommunityView communityView, Model model, Principal principal) {
      User user = services.user().withUserName(principal.getName());
      Community community = services.community().create(user.id, communityView.toCommunity());
      return community(community.id, model, principal);
    }


  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/community/search")
  public String searchCommunity(@ModelAttribute CommunityCreationView communityCreationView, @RequestParam("id") Long favoriteId) {
    if (favoriteId == null) {
      favoriteId = 0L;
    }
    String name = communityCreationView.getName();
    return "redirect:/sec/communities-suggest?name="+ URLEncoder.encode(name)+"&id="+favoriteId;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/communities-suggest")
  public String showCommunitiesSuggest(Model model, @RequestParam("name") String name, @RequestParam("id") Long favoriteId, Principal principal) {
    User user = services.user().withUserName(principal.getName());
    String decodeName = URLDecoder.decode(name);
    model.addAttribute("title", messageByLocaleService.getMessage("favorite.create_community"));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    List<Object[]> queryCommunities = services.community().searchBis(decodeName.toUpperCase());

    List<CommunityViewData> communityViewData = queryCommunities.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .collect(Collectors.toList());

    model.addAttribute("communityName", decodeName);
    model.addAttribute("isCommunityAlreadyExist", false);
    List<CommunityViewData> communitiesWithSameName = new ArrayList<>();
    for (CommunityViewData community:communityViewData) {
      if (community.name.equalsIgnoreCase(decodeName) ) {
        model.addAttribute("isCommunityAlreadyExist", true);
        model.addAttribute("communityMatche", community);
      } else {
        communitiesWithSameName.add(community);
      }
    }

    List<Object[]> queryCommunitiesForFavorite = services.community().allForFavorite(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunitiesForFavorite.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .filter(c -> communitiesWithSameName.stream().map(co -> co.id).collect(Collectors.toList()).contains(c.id))
      .sorted((c1, c2) -> c1.name.compareTo(c2.name))
      .collect(Collectors.toList());

    model.addAttribute("communitiesWithSameName", communitiesViewData);

    CommunityCreationView communityCreationView = new CommunityCreationView();
    communityCreationView.setName(decodeName);
    model.addAttribute("communityCreationView", communityCreationView);

    model.addAttribute("favoriteId", favoriteId);
    model.addAttribute("appName", appName);

    return "communities-suggest";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/community/{communityId}/manage-community")
  public String manageCommunity(@PathVariable long communityId, Model model, Principal principal)  {
    Boolean isCommunityBelowToMe = true;
    String name = null;
    User user = services.user().withUserName(principal.getName());

    Community community = services.community().withId(communityId);
    if (community == null) {
      return("redirect:/");
    }

    model.addAttribute("title", community.name);

    if (community.user.id != user.id) {
      name = community.user.name();
      isCommunityBelowToMe = false;
    }

    model.addAttribute("userName", name);
    model.addAttribute("isCommunityBelowToMe", isCommunityBelowToMe);
    model.addAttribute("community", community);
    model.addAttribute("user", user);
    model.addAttribute("appName", appName);

    return "manage-community";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/community/{communityId}/modify")
  public String modifyCommunity(@PathVariable long communityId, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    Community community = services.community().withId(communityId);


    model.addAttribute("title", messageByLocaleService.getMessage("favorite.title_create_community", new Object[]{community.name}));
    model.addAttribute("community", community);
    CommunityProfileView communityProfileView = new CommunityProfileView(community, services.user());
    model.addAttribute("communityProfileView", communityProfileView);
    List<User> usersWithoutMeAndWithoutAdmin = communityProfileView.getAllUsers().stream().filter(u -> u.id != user.id).filter(u-> u.id != 1).collect(Collectors.toList());
    model.addAttribute("users", usersWithoutMeAndWithoutAdmin);
    model.addAttribute("appName", appName);
    model.addAttribute("backUrl", "/sec/community/"+communityId);
    return "modify-community";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/descriptionCommunity/{communityId}")
  public String descriptionCommunity(@PathVariable long communityId, HttpServletRequest request, Principal principal, Model model)  {
    Community community = services.community().withId(communityId);
    String userAgent = request.getHeader("User-Agent");

    model.addAttribute("isIOSDevice", isIOSDevice(userAgent));
    model.addAttribute("title", messageByLocaleService.getMessage("community.description_title", new Object[]{community.name}));

   model.addAttribute("community", community);
    model.addAttribute("appName", appName);

    return "community-description";
  }

  private boolean isIOSDevice(String userAgent) {
    boolean isIOSDevice = false;
    String osType = "Unknown";
    String osVersion = "Unknown";
    String deviceType = "Unknown";

    if (userAgent.indexOf("Mac OS") >= 0) {
      osType = "Mac";
      osVersion = userAgent.substring(userAgent.indexOf("Mac OS ") + 7, userAgent.indexOf(")"));

      if (userAgent.indexOf("iPhone") >= 0) {
        deviceType = "iPhone";
        isIOSDevice = true;
      } else if (userAgent.indexOf("iPad") >= 0) {
        deviceType = "iPad";
        isIOSDevice = true;
      }
    }
    return isIOSDevice;
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = "/sec/community/create")
  public String createCommunity(@RequestParam("name") String name, Principal principal, Model model) {
    User user = services.user().withUserName(principal.getName());
    String decodeName = URLDecoder.decode(name);
    model.addAttribute("backUrl", "/sec/communities");
    model.addAttribute("communityName", decodeName);
    model.addAttribute("communityProfileView", new CommunityProfileView());
    Users users = services.user().allForCreateCommunity();
    List<User> usersWithoutMeAndWithoutAdmin = users.stream().filter(u -> u.id != user.id).filter(u-> u.id != 1).collect(Collectors.toList());
    model.addAttribute("users", usersWithoutMeAndWithoutAdmin);
    model.addAttribute("appName", appName);

    return "create-community";
  }
}
