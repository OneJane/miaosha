package com.onejane.miaosha.service;

import com.onejane.miaosha.dao.MiaoshaUserDao;
import com.onejane.miaosha.domain.MiaoshaUser;
import com.onejane.miaosha.exception.GlobalException;
import com.onejane.miaosha.redis.MiaoshaUserKey;
import com.onejane.miaosha.redis.RedisService;
import com.onejane.miaosha.result.CodeMsg;
import com.onejane.miaosha.util.MD5Util;
import com.onejane.miaosha.util.UUIDUtil;
import com.onejane.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Service
public class MiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private MiaoshaUserDao miaoshaUserDao;

    @Autowired
    private RedisService redisService;

    /**
     * 对象缓存
     * @param id
     * @return
     */
    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
        if(user != null) {
            return user;
        }
        //取数据库
        user = miaoshaUserDao.getById(id);
        if(user != null) {
            redisService.set(MiaoshaUserKey.getById, ""+id, user);
        }
        return user;
    }

    public boolean updatePassword(String token, long id, String formPass) {
        //取user
        MiaoshaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.FormPassToDBPass(formPass, user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }

    public String login(HttpServletResponse response, LoginVo loginVO) {

        if(loginVO == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String fromPass = loginVO.getPassword();
        String mobile = loginVO.getMobile();

        MiaoshaUser user = getById(Long.parseLong(mobile));

        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.FormPassToDBPass(fromPass, saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWRD_ERROR);
        }

        String token = addCookie(response, user, null);

        return token;
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        // 延长有效期
        if(user != null){
            addCookie(response, user, token);
        }

        return user;
    }


    private String addCookie(HttpServletResponse response, MiaoshaUser user, String token){
        //生成token
        if(token == null) {
            token = UUIDUtil.uuid();
        }

        redisService.set( MiaoshaUserKey.token, token, user);

        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.TOKEN_EXPIRE);
        cookie.setPath("/");
        response.addCookie(cookie);
        return token;
    }
}
