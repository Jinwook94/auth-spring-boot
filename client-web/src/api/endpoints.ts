import { Endpoint } from "./types";

export const API_ENDPOINTS = {
  auth: {
    googleLogin: "/api/v1/auth/google/login" as Endpoint,
    kakaoLogin: "/api/v1/auth/kakao/login" as Endpoint,
    naverLogin: "/api/v1/auth/naver/login" as Endpoint,
    logout: "/api/v1/auth/logout" as Endpoint,
  },

  users: {
    me: "/api/v1/users/me" as Endpoint,
  },
};
