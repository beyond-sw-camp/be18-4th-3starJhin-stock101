package com.monstersinc.stock101.auth.model.service;

import com.monstersinc.stock101.auth.jwt.JwtUtil;
import com.monstersinc.stock101.auth.model.mapper.AuthMapper;
import com.monstersinc.stock101.user.model.vo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {


    private final AuthMapper authMapper;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try{
            Long userId = Long.parseLong(username);
            User user = authMapper.selectUserByUserId(userId);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with ID: " + username);
            }
            return user;
        }
        catch (NumberFormatException e){
            throw new UsernameNotFoundException("올바른 형식이 아닙니다.");
        }

    }
}
