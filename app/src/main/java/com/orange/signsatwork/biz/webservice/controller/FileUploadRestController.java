package com.orange.signsatwork.biz.webservice.controller;

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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.orange.signsatwork.DalymotionToken;
import com.orange.signsatwork.SpringRestClient;
import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.nativeinterface.NativeInterface;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.storage.StorageService;
import com.orange.signsatwork.biz.view.model.RequestCreationView;
import com.orange.signsatwork.biz.view.model.SignCreationView;
import com.orange.signsatwork.biz.webservice.model.RequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.JCodecException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Types that carry this annotation are treated as controllers where @RequestMapping
 * methods assume @ResponseBody semantics by default, ie return json body.
 */
@Slf4j
@RestController
/** Rest controller: returns a json body */
public class FileUploadRestController {

  @Autowired
  private StorageService storageService;

  @Autowired
  Services services;
  @Autowired
  DalymotionToken dalymotionToken;
  @Autowired
  private SpringRestClient springRestClient;
  @Autowired
  MessageByLocaleService messageByLocaleService;
  @Autowired
  private Environment environment;


  String VIDEO_THUMBNAIL_FIELDS = "thumbnail_url,thumbnail_60_url,thumbnail_120_url,thumbnail_180_url,thumbnail_240_url,thumbnail_360_url,thumbnail_480_url,thumbnail_720_url,";
  String VIDEO_EMBED_FIELD = "embed_url";
  String VIDEO_STATUS = ",status";

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadRecordedVideoFile(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), principal, response);
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromRequest(@RequestBody VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String uploadRecordedVideoFileFromSign(@RequestBody VideoFile videoFile, @PathVariable long signId, @PathVariable long videoId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForNewVideo(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFile(videoFile, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), principal, response);
  }

  private String handleRecordedVideoFile(VideoFile videoFile, OptionalLong requestId,OptionalLong signId, OptionalLong videoId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
    String videoUrl = null;
    String file = environment.getProperty("app.file") + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      String  lexical = videoFile.contents.substring(videoFile.contents.indexOf(",") + 1);
      byte[] videoByte = DatatypeConverter.parseBase64Binary(lexical);

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);
      /*cmd = String.format("mencoder %s -ovc x264 -o %s", file, fileOutput);*/

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;
      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()){
        body.add("title",services.sign().withId(signId.getAsLong()).name);
      }else{
        body.add("title", videoFile.signNameRecording);
      }
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));



      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        if (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")) {
          pictureUri = "/img/no-such-asset.jpg";
        } else {
          pictureUri = videoDailyMotion.thumbnail_360_url;
        }
        log.warn("handleFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }


      if (!videoDailyMotion.embed_url.isEmpty()) {
        videoUrl = videoDailyMotion.embed_url;
        log.warn("handleFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }
      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
          /*sign = services.sign().withId(signId.getAsLong());*/
          video = services.video().withId(videoId.getAsLong());
          dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          }
          catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
          sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), videoUrl, pictureUri);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), videoUrl, pictureUri);
      } else {
         sign = services.sign().create(user.id, videoFile.signNameRecording, videoUrl, pictureUri);
        log.info("handleFileUpload : username = {} / sign name = {} / video url = {}", user.username, videoFile.signNameRecording, videoUrl);
          }


      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      //return Long.toString(sign.id);
      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }



  public static long parseSize(String text) {
    double d = Double.parseDouble(text.replaceAll("[GMK]B$", ""));
    long l = Math.round(d * 1024 * 1024 * 1024L);
    switch (text.charAt(Math.max(0, text.length() - 2))) {
      default:  l /= 1024;
      case 'K': l /= 1024;
      case 'M': l /= 1024;
      case 'G': return l;
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD, method = RequestMethod.POST)
  public String uploadSelectedVideoFile(@RequestParam("file") MultipartFile file, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST, method = RequestMethod.POST)
  public String createSignFromUploadondailymotion(@RequestParam("file") MultipartFile file,@PathVariable long requestId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.of(requestId), OptionalLong.empty(), OptionalLong.empty(), signCreationView, principal, response);

  }
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionFromSign(@RequestParam("file") MultipartFile file,@PathVariable long signId, @PathVariable long videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.of(videoId), signCreationView, principal, response);

  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NEW_VIDEO, method = RequestMethod.POST)
  public String createSignFromUploadondailymotionForNewVideo(@RequestParam("file") MultipartFile file,@PathVariable long signId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUpload(file, OptionalLong.empty(), OptionalLong.of(signId), OptionalLong.empty(), signCreationView, principal, response);

  }

  private String handleSelectedVideoFileUpload(@RequestParam("file") MultipartFile file, OptionalLong requestId, OptionalLong signId, OptionalLong videoId, @ModelAttribute SignCreationView signCreationView, Principal principal, HttpServletResponse response) throws InterruptedException {

    try {
      String dailymotionId;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());
      storageService.store(file);
      File inputFile = storageService.load(file.getOriginalFilename()).toFile();

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (signId.isPresent()) {
        body.add("title", services.sign().withId(signId.getAsLong()).name);
      } else {
        body.add("title", signCreationView.getSignName());
      }
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 100) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));

      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        if (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")) {
          pictureUri = "/img/no-such-asset.jpg";
        } else {
          pictureUri = videoDailyMotion.thumbnail_360_url;
        }
        log.warn("handleSelectedVideoFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        signCreationView.setVideoUrl(videoDailyMotion.embed_url);
        log.warn("handleSelectedVideoFileUpload : embed_url = {}", videoDailyMotion.embed_url);
      }

      Sign sign;
      Video video;
      if (signId.isPresent() && (videoId.isPresent())) {
        /*sign = services.sign().withId(signId.getAsLong());*/
        video = services.video().withId(videoId.getAsLong());
        dailymotionId = video.url.substring(video.url.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        }
        catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
        sign = services.sign().replace(signId.getAsLong(), videoId.getAsLong(), signCreationView.getVideoUrl(), pictureUri);
      } else if (signId.isPresent() && !(videoId.isPresent())) {
        sign = services.sign().addNewVideo(user.id, signId.getAsLong(), signCreationView.getVideoUrl(), pictureUri);
      } else {
        sign = services.sign().create(user.id, signCreationView.getSignName(), signCreationView.getVideoUrl(), pictureUri);
      }

      log.info("handleSelectedVideoFileUpload : username = {} / sign name = {} / video url = {}", user.username, signCreationView.getSignName(), signCreationView.getVideoUrl());

      if (requestId.isPresent()) {
        services.request().changeSignRequest(requestId.getAsLong(), sign.id);
      }

      response.setStatus(HttpServletResponse.SC_OK);

      return "/sec/sign/" + Long.toString(sign.id) + "/" + Long.toString(sign.lastVideoId) + "/detail";
    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForJobDescription(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForName(@RequestParam("file") MultipartFile file, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForProfil(file, principal, "Name", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_NAME, method = RequestMethod.PUT)
  public String deleteVideoFileForName(Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
      String dailymotionId;
      User user = services.user().withUserName(principal.getName());
      if (user.nameVideo != null) {
        dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
        try {
          DeleteVideoOnDailyMotion(dailymotionId);
        }
        catch (Exception errorDailymotionDeleteVideo) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
        }
        services.user().changeNameVideoUrl(user, null, null);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return "/sec/my_profil";
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_DELETE_VIDEO_FILE_FOR_JOB, method = RequestMethod.PUT)
  public String deleteVideoFileForJob(Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    String dailymotionId;
    User user = services.user().withUserName(principal.getName());
    if (user.jobDescriptionVideo != null) {
      dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
      try {
        DeleteVideoOnDailyMotion(dailymotionId);
      }
      catch (Exception errorDailymotionDeleteVideo) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
      }
      services.user().changeDescriptionVideoUrl(user, null, null);
    }
    response.setStatus(HttpServletResponse.SC_OK);
    return "/sec/my_profil";
  }

  private String handleSelectedVideoFileUploadForProfil(@RequestParam("file") MultipartFile file, Principal principal, String inputType, HttpServletResponse response) throws InterruptedException {
    {
      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        if (inputType.equals("JobDescription")) {
          body.add("title", messageByLocaleService.getMessage("user.job_description"));
        } else {
          body.add("title", messageByLocaleService.getMessage("user.name_LSF"));
        }
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);

        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
          log.info("status "+videoDailyMotion.status);
        }
        while (!videoDailyMotion.status.equals("published"));


        String pictureUri = null;
        if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
          if (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")) {
            pictureUri = "/img/no-such-asset.jpg";
          } else {
            pictureUri = videoDailyMotion.thumbnail_360_url;
          }
          log.warn("handleSelectedVideoFileUpload : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
        }

        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (inputType.equals("JobDescription")) {
            if (user.jobDescriptionVideo != null) {
              dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
            services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url, pictureUri);
          } else {
            if (user.nameVideo != null) {
              dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              }
            }
            services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url, pictureUri);
          }

          log.warn("handleSelectedVideoFileUploadForProfil : embed_url = {}", videoDailyMotion.embed_url);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        if (inputType.equals("JobDescription")) {
          return "/sec/your-job-description";
        } else {
          return "/sec/who-are-you";
        }
      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_JOB_DESCRIPTION, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForJobDescription(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "JobDescription", response);
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_NAME, method = RequestMethod.POST)
  public String uploadRecordedVideoFileForName(@RequestBody VideoFile videoFile, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForProfil(videoFile, principal, "Name", response);
  }

  private String handleRecordedVideoFileForProfil(VideoFile videoFile, Principal principal, String inputType, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    String videoUrl = null;
    String file = environment.getProperty("app.file") + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      if (inputType.equals("JobDescription")) {
        body.add("title", messageByLocaleService.getMessage("user.job_description"));
      } else {
        body.add("title", messageByLocaleService.getMessage("user.name_LSF"));
      }

      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);

      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));

      String pictureUri = null;
      if (!videoDailyMotion.thumbnail_360_url.isEmpty()) {
        if (videoDailyMotion.thumbnail_360_url.contains("no-such-asset")) {
          pictureUri = "/img/no-such-asset.jpg";
        } else {
          pictureUri = videoDailyMotion.thumbnail_360_url;
        }
        log.warn("handleRecordedVideoFileForProfil : thumbnail_360_url = {}", videoDailyMotion.thumbnail_360_url);
      }

      if (!videoDailyMotion.embed_url.isEmpty()) {
        if (inputType.equals("JobDescription")) {
          if (user.jobDescriptionVideo != null) {
            dailymotionId = user.jobDescriptionVideo.substring(user.jobDescriptionVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.user().changeDescriptionVideoUrl(user, videoDailyMotion.embed_url, pictureUri);
        } else {
          if (user.nameVideo != null) {
            dailymotionId = user.nameVideo.substring(user.nameVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.user().changeNameVideoUrl(user, videoDailyMotion.embed_url, pictureUri);
        }

        log.warn("handleRecordedVideoFileForProfil : embed_url = {}", videoDailyMotion.embed_url);
      }

      response.setStatus(HttpServletResponse.SC_OK);
      if (inputType.equals("JobDescription")) {
        return "/sec/your-job-description";
      } else {
        return "/sec/who-are-you";
      }

    }
    catch(Exception errorDailymotionUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  private void DeleteVideoOnDailyMotion(String dailymotionId) {

    AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
    if (authTokenInfo.isExpired()) {
      dalymotionToken.retrieveToken();
      authTokenInfo = dalymotionToken.getAuthTokenInfo();
    }

    final String uri = environment.getProperty("app.dailymotion_url") + "/video/"+dailymotionId;
    RestTemplate restTemplate = springRestClient.buildRestTemplate();

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + authTokenInfo.getAccess_token());

    HttpEntity<?> request = new HttpEntity<Object>(headers);

    restTemplate.exchange(uri, HttpMethod.DELETE, request, String.class );

    return;
  }


  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION, method = RequestMethod.POST)
  public RequestResponse uploadSelectedVideoFileForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest req) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForRequestDescription(file, requestId, requestCreationView, principal, response, req);
  }

  private RequestResponse handleSelectedVideoFileUploadForRequestDescription(@RequestParam("file") MultipartFile file, @PathVariable long requestId, @ModelAttribute RequestCreationView requestCreationView, Principal principal, HttpServletResponse response, HttpServletRequest req) throws InterruptedException {
    {
      Request request = null;
      RequestResponse requestResponse = new RequestResponse();
      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{requestCreationView.getRequestName()}));
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
          log.info("status "+videoDailyMotion.status);
        }
        while (!videoDailyMotion.status.equals("published"));

        List<String> emails;
        String title, bodyMail;
        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (requestId != 0) {
            request = services.request().withId(requestId);
            if (request.requestVideoDescription != null) {
              dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
              try {
                DeleteVideoOnDailyMotion(dailymotionId);
              }
              catch (Exception errorDailymotionDeleteVideo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
                return  requestResponse;
              }
            }
            services.request().changeRequestVideoDescription(requestId, videoDailyMotion.embed_url);

          } else {
            if (services.sign().withName(requestCreationView.getRequestName()).list().isEmpty()) {
              if (services.request().withName(requestCreationView.getRequestName()).list().isEmpty()) {
                request = services.request().create(user.id, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription(), videoDailyMotion.embed_url);
                log.info("createRequest: username = {} / request name = {}", user.username, requestCreationView.getRequestName(), requestCreationView.getRequestTextDescription());
                emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
                title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
                bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id});

                Request finalRequest = request;
                Runnable task = () -> {
                  log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                  services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl(req) + "/sec/other-request-detail/" + finalRequest.id, req.getLocale() );
                };

                new Thread(task).start();
              } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                requestResponse.errorType = 1;
                requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
                return requestResponse;
              }
            } else {
              response.setStatus(HttpServletResponse.SC_CONFLICT);
              requestResponse.errorType = 2;
              requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
              requestResponse.signId = services.sign().withName(requestCreationView.getRequestName()).list().get(0).id;
              return requestResponse;
            }
            log.warn("handleSelectedVideoFileUploadForRequestDescription : embed_url = {}", videoDailyMotion.embed_url);
          }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        requestResponse.requestId = request.id;
        return requestResponse;

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requestResponse.errorType = 3;
        requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
        return requestResponse;
      }
    }
  }

  private String getAppUrl(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_REQUEST_DESCRIPTION , method = RequestMethod.POST)
  public RequestResponse uploadRecordedVideoFileForRequestDescription(@RequestBody VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest req) {
    return handleRecordedVideoFileForRequestDescription(videoFile, requestId, principal, response, req);
  }

  private RequestResponse handleRecordedVideoFileForRequestDescription(VideoFile videoFile, @PathVariable long requestId, Principal principal, HttpServletResponse response, HttpServletRequest req) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);
    RequestResponse requestResponse = new RequestResponse();
    String videoUrl = null;
    String file = environment.getProperty("app.file") + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorFileSize");
      return requestResponse;
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorUploadFile");
      return requestResponse;
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorEncondingFile");
      return requestResponse;
    }

    try {
      String dailymotionId;
      Request request = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("request.title_description_LSF", new Object[]{videoFile.requestNameRecording}));
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));

      List<String> emails;
      String title, bodyMail;

      if (!videoDailyMotion.embed_url.isEmpty()) {
        if (requestId != 0) {
          request = services.request().withId(requestId);
          if (request.requestVideoDescription != null) {
            dailymotionId = request.requestVideoDescription.substring(request.requestVideoDescription.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
              return  requestResponse;
            }
          }
          services.request().changeRequestVideoDescription(requestId, videoDailyMotion.embed_url);

        } else {
          if (services.sign().withName(videoFile.requestNameRecording).list().isEmpty()) {
            if (services.request().withName(videoFile.requestNameRecording).list().isEmpty()) {
              request = services.request().create(user.id, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording, videoDailyMotion.embed_url);
              log.info("createRequest: username = {} / request name = {}", user.username, videoFile.requestNameRecording, videoFile.requestTextDescriptionRecording);
              emails = services.user().findEmailForUserHaveSameCommunityAndCouldCreateSign(user.id);
              title = messageByLocaleService.getMessage("request_created_by_user_title", new Object[]{user.name()});
              bodyMail = messageByLocaleService.getMessage("request_created_by_user_body", new Object[]{user.name(), request.name, getAppUrl(req) + "/sec/other-request-detail/" + request.id});

              Request finalRequest = request;
              Runnable task = () -> {
                log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
                services.emailService().sendRequestMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalRequest.name, getAppUrl(req) + "/sec/other-request-detail/" + finalRequest.id, req.getLocale());
              };

              new Thread(task).start();
            } else {
              response.setStatus(HttpServletResponse.SC_CONFLICT);
              requestResponse.errorType = 1;
              requestResponse.errorMessage = messageByLocaleService.getMessage("request.already_exists");
              return requestResponse;
            }
          } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            requestResponse.errorType = 2;
            requestResponse.errorMessage = messageByLocaleService.getMessage("sign.already_exists");
            requestResponse.signId = services.sign().withName(videoFile.requestNameRecording).list().get(0).id;
            return requestResponse;
          }
          log.warn("handleRecordedVideoFileForRequestDescription : embed_url = {}", videoDailyMotion.embed_url);
        }
      }

      response.setStatus(HttpServletResponse.SC_OK);
      requestResponse.requestId = request.id;
      return requestResponse;

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      requestResponse.errorType = 3;
      requestResponse.errorMessage = messageByLocaleService.getMessage("errorDailymotionUploadFile");
      return requestResponse;
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION , method = RequestMethod.POST)
  public String uploadRecordedVideoFileForSignDefinition(@RequestBody VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    return handleRecordedVideoFileForSignDefinition(videoFile, signId, principal, response);
  }

  private String handleRecordedVideoFileForSignDefinition(VideoFile videoFile, @PathVariable long signId, Principal principal, HttpServletResponse response) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String videoUrl = null;
    String file = environment.getProperty("app.file") + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;
      Sign sign = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
      sign = services.sign().withId(signId);

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{sign.name}));
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));

      if (!videoDailyMotion.embed_url.isEmpty()) {

        if (sign.videoDefinition != null) {
          dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          }
          catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        }
        services.sign().changeSignVideoDefinition(signId, videoDailyMotion.embed_url);

      }

      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(sign.id);

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_SIGN_DEFINITION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForSignDefinition(file, signId, principal, response);
  }

  private String handleSelectedVideoFileUploadForSignDefinition(@RequestParam("file") MultipartFile file, @PathVariable long signId, Principal principal, HttpServletResponse response) throws InterruptedException {
    {
      Sign sign = null;
      sign = services.sign().withId(signId);

      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();



        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", messageByLocaleService.getMessage("sign.title_description_LSF", new Object[]{sign.name}));
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
          log.info("status "+videoDailyMotion.status);
        }
        while (!videoDailyMotion.status.equals("published"));

        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (sign.videoDefinition != null) {
            dailymotionId = sign.videoDefinition.substring(sign.videoDefinition.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.sign().changeSignVideoDefinition(signId, videoDailyMotion.embed_url);

        }

        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(sign.id);

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

  /****/
  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FOR_COMMUNITY_DESCRIPTION , method = RequestMethod.POST)
  public String uploadRecordedVideoFileForCommunityDescription(@RequestBody VideoFile videoFile, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) {
    return handleRecordedVideoFileForCommunityDescription(videoFile, communityId, principal, response, request);
  }

  private String handleRecordedVideoFileForCommunityDescription(VideoFile videoFile, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) {
    log.info("VideoFile "+videoFile);
    log.info("VideoFile name"+videoFile.name);

    String videoUrl = null;
    String file = environment.getProperty("app.file") + videoFile.name;
    String fileOutput = file.replace(".webm", ".mp4");

    log.info("taille fichier "+videoFile.contents.length());
    log.info("taille max "+parseSize(environment.getProperty("spring.servlet.multipart.max-request-size")));

    if (videoFile.contents.length() > parseSize(environment.getProperty("spring.servlet.multipart.max-request-size"))) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorFileSize");
    }


    try {
      //This will decode the String which is encoded by using Base64 class
      byte[] videoByte = DatatypeConverter.parseBase64Binary(videoFile.contents.substring(videoFile.contents.indexOf(",") + 1));

      new FileOutputStream(file).write(videoByte);
    }
    catch(Exception errorUploadFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorUploadFile");
    }

    try {
      String cmd;

      cmd = String.format("mencoder %s -vf scale=640:-1 -ovc x264 -o %s", file, fileOutput);

      String cmdFilterLog = "/tmp/mencoder.log";
      NativeInterface.launch(cmd, null, cmdFilterLog);
    }
    catch(Exception errorEncondingFile)
    {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorEncondingFile");
    }

    try {
      String dailymotionId;
      Community community = null;
      String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
      community = services.community().withId(communityId);

      AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
      if (authTokenInfo.isExpired()) {
        dalymotionToken.retrieveToken();
        authTokenInfo = dalymotionToken.getAuthTokenInfo();
      }

      User user = services.user().withUserName(principal.getName());

      UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


      File fileMp4 = new File(fileOutput);
      Resource resource = new FileSystemResource(fileMp4.getAbsolutePath());
      MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
      parts.add("file", resource);

      RestTemplate restTemplate = springRestClient.buildRestTemplate();
      MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
      restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

      ResponseEntity<FileUploadDailymotion> responseDailymmotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
        HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
      FileUploadDailymotion fileUploadDailyMotion = responseDailymmotion.getBody();


      MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
      body.add("url", fileUploadDailyMotion.url);
      body.add("title", messageByLocaleService.getMessage("community.title_description_LSF", new Object[]{community.name}));
      body.add("channel", "tech");
      body.add("published", true);
      body.add("private", true);


      RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
      HttpHeaders headers1 = new HttpHeaders();
      headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
      headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

      HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
      String videosUrl = REST_SERVICE_URI + "/videos";
      ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
        HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
      VideoDailyMotion videoDailyMotion = response1.getBody();


      String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
      int i=0;
      do {
        videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
        Thread.sleep(2 * 1000);
        if (i > 30) {
          break;
        }
        i++;
        log.info("status "+videoDailyMotion.status);
      }
      while (!videoDailyMotion.status.equals("published"));

      if (!videoDailyMotion.embed_url.isEmpty()) {

        if (community.descriptionVideo != null) {
          dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
          try {
            DeleteVideoOnDailyMotion(dailymotionId);
          }
          catch (Exception errorDailymotionDeleteVideo) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
          }
        }
        services.community().changeDescriptionVideo(communityId, videoDailyMotion.embed_url);
        List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
        if (emails.size() != 0) {
          Community finalCommunity = community;
          Runnable task = () -> {
            String title, bodyMail;
            final String urlDescriptionCommunity = getAppUrl(request) + "/sec/community/" + finalCommunity.id + "/description";
            title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
            bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
            log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
            services.emailService().sendCommunityAddDescriptionMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, request.getLocale());
          };

          new Thread(task).start();
        }

      }

      response.setStatus(HttpServletResponse.SC_OK);
      return Long.toString(community.id);

    } catch (Exception errorDailymotionUploadFile) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return messageByLocaleService.getMessage("errorDailymotionUploadFile");
    }
  }

  @Secured("ROLE_USER")
  @RequestMapping(value = RestApi.WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FOR_COMMUNITY_DESCRIPTION, method = RequestMethod.POST)
  public String uploadSelectedVideoFileForCommunityDescription(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) throws IOException, JCodecException, InterruptedException {
    return handleSelectedVideoFileUploadForCommunityDescription(file, communityId, principal, response, request);
  }

  private String handleSelectedVideoFileUploadForCommunityDescription(@RequestParam("file") MultipartFile file, @PathVariable long communityId, Principal principal, HttpServletResponse response, HttpServletRequest request) throws InterruptedException {
    {
      Community community = null;
      community = services.community().withId(communityId);

      try {
        String dailymotionId;
        String REST_SERVICE_URI = environment.getProperty("app.dailymotion_url");
        AuthTokenInfo authTokenInfo = dalymotionToken.getAuthTokenInfo();
        if (authTokenInfo.isExpired()) {
          dalymotionToken.retrieveToken();
          authTokenInfo = dalymotionToken.getAuthTokenInfo();
        }

        User user = services.user().withUserName(principal.getName());
        storageService.store(file);
        File inputFile = storageService.load(file.getOriginalFilename()).toFile();

        UrlFileUploadDailymotion urlfileUploadDailymotion = services.sign().getUrlFileUpload();


        Resource resource = new FileSystemResource(inputFile.getAbsolutePath());
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", resource);

        RestTemplate restTemplate = springRestClient.buildRestTemplate();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        ResponseEntity<FileUploadDailymotion> responseDailyMotion = restTemplate.exchange(urlfileUploadDailymotion.upload_url,
          HttpMethod.POST, requestEntity, FileUploadDailymotion.class);
        FileUploadDailymotion fileUploadDailyMotion = responseDailyMotion.getBody();


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("url", fileUploadDailyMotion.url);
        body.add("title", messageByLocaleService.getMessage("community.title_description_LSF", new Object[]{community.name}));
        body.add("channel", "tech");
        body.add("published", true);
        body.add("private", true);


        RestTemplate restTemplate1 = springRestClient.buildRestTemplate();
        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers1.set("Authorization", "Bearer " + authTokenInfo.getAccess_token());
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> requestEntity1 = new HttpEntity<MultiValueMap<String, Object>>(body, headers1);
        String videosUrl = REST_SERVICE_URI + "/videos";
        ResponseEntity<VideoDailyMotion> response1 = restTemplate1.exchange(videosUrl,
          HttpMethod.POST, requestEntity1, VideoDailyMotion.class);
        VideoDailyMotion videoDailyMotion = response1.getBody();


        String url = REST_SERVICE_URI + "/video/" + videoDailyMotion.id + "?thumbnail_ratio=square&ssl_assets=true&fields=" + VIDEO_THUMBNAIL_FIELDS + VIDEO_EMBED_FIELD + VIDEO_STATUS;
        int i=0;
        do {
          videoDailyMotion = services.sign().getVideoDailyMotionDetails(videoDailyMotion.id, url);
          Thread.sleep(2 * 1000);
          if (i > 30) {
            break;
          }
          i++;
          log.info("status "+videoDailyMotion.status);
        }
        while (!videoDailyMotion.status.equals("published"));;

        if (!videoDailyMotion.embed_url.isEmpty()) {
          if (community.descriptionVideo != null) {
            dailymotionId = community.descriptionVideo.substring(community.descriptionVideo.lastIndexOf('/') + 1);
            try {
              DeleteVideoOnDailyMotion(dailymotionId);
            }
            catch (Exception errorDailymotionDeleteVideo) {
              response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
              return messageByLocaleService.getMessage("errorDailymotionDeleteVideo");
            }
          }
          services.community().changeDescriptionVideo(communityId, videoDailyMotion.embed_url);
          List<String> emails = community.users.stream().filter(u-> u.email != null).map(u -> u.email).collect(Collectors.toList());
          if (emails.size() != 0) {
            Community finalCommunity = community;
            Runnable task = () -> {
              String title, bodyMail;
              final String urlDescriptionCommunity = getAppUrl(request) + "/sec/community/" + finalCommunity.id + "/description";
              title = messageByLocaleService.getMessage("community_description_changed_by_user_title");
              bodyMail = messageByLocaleService.getMessage("community_description_changed_by_user_body", new Object[]{user.name(), finalCommunity.name, urlDescriptionCommunity});
              log.info("send mail email = {} / title = {} / body = {}", emails.toString(), title, bodyMail);
              services.emailService().sendCommunityAddDescriptionMessage(emails.toArray(new String[emails.size()]), title, user.name(), finalCommunity.name, urlDescriptionCommunity, request.getLocale());
            };

            new Thread(task).start();
          }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return Long.toString(community.id);

      } catch (Exception errorDailymotionUploadFile) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return messageByLocaleService.getMessage("errorDailymotionUploadFile");
      }
    }
  }

}
