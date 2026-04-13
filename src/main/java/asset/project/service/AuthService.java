package asset.project.service;

import asset.project.dto.request.LoginReq;
import asset.project.dto.response.LoginRes;

public interface AuthService {
    LoginRes login(LoginReq req);
    LoginRes refreshToken(String refreshToken);
    void logout(String token);
}