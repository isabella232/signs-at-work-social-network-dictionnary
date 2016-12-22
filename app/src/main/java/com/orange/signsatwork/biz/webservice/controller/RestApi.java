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

public class RestApi {
  public static final String WS_ROOT = "/ws/";
  public static final String WS_OPEN = WS_ROOT + "open/";
  public static final String WS_SEC = WS_ROOT + "sec/";
  public static final String WS_ADMIN = WS_ROOT + "admin/";

  public static final String WS_SEC_GET_USERS = WS_SEC + "users";
  public static final String WS_ADMIN_USER_CREATE = WS_ADMIN + "user/create";

  public static final String WS_OPEN_SIGN = WS_OPEN + "sign/";
  public static final String WS_SEC_SIGN_CREATE = WS_SEC + "sign/create";

  public static final String WS_SEC_REQUEST_CREATE = WS_SEC + "request/create";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD = WS_SEC + "uploadRecordedVideoFile";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_REQUEST = WS_SEC + "uploadRecordedVideoFile/{requestId}";

  public static final String WS_SEC_RECORDED_VIDEO_FILE_UPLOAD_FROM_SIGN = WS_SEC + "uploadRecordedVideoFileFromSign/{signId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD = WS_SEC + "uploadSelectedVideoFile";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_REQUEST = WS_SEC + "uploadSelectedVideoFile/{requestId}";

  public static final String WS_SEC_SELECTED_VIDEO_FILE_UPLOAD_FROM_SIGN = WS_SEC + "uploadSelectedVideoFileFromSign/{signId}";
}
