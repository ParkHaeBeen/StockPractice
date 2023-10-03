package com.zerobase.stock.service;

import com.zerobase.stock.exception.Impl.AlreadyExistUserException;
import com.zerobase.stock.model.Auth;
import com.zerobase.stock.model.MemberEntity;
import com.zerobase.stock.persisit.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.rmi.AlreadyBoundException;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberEntity member = this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user-> " + username));
        return member;
    }

    public MemberEntity register(Auth.SignUp member){
        boolean exist = this.memberRepository.existsByUsername(member.getUsername());
        if(exist){
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        MemberEntity result = memberRepository.save(member.toEntity());
        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member){
        System.out.println(member.getUsername());
        MemberEntity user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID입니다"));

        if(!passwordEncoder.matches(member.getPassword(),user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        return user;
    }
}
